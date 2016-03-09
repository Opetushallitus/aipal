;; Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
;;
;; This program is free software:  Licensed under the EUPL, Version 1.1 or - as
;; soon as they will be approved by the European Commission - subsequent versions
;; of the EUPL (the "Licence");
;;
;; You may not use this work except in compliance with the Licence.
;; You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; European Union Public Licence for more details.

(ns aipalvastaus.rest-api.vastaus
  (:require [compojure.api.core :refer [defroutes POST]]
            [korma.db :as db]
            [schema.core :as schema]
            [clojure.tools.logging :as log]
            [oph.common.util.http-util :refer [response-nocache]]
            [aipalvastaus.sql.vastaus :as arkisto]
            [aipalvastaus.sql.kyselykerta :as kysely-arkisto]
            [aipalvastaus.sql.vastaaja :as vastaaja]
            [aipalvastaus.toimiala.skeema :refer [KayttajanVastaus]]
            [oph.common.util.util :refer [map-by]]))

(defn kylla-jatkovastaus-validi?
  [vastaus kysymys]
  (boolean
    (and (= (:jatkokysymysid kysymys) (:jatkokysymysid vastaus))
         (:jatkovastaus_kylla vastaus)
         (not (:jatkovastaus_ei vastaus))
         (:kylla_kysymys kysymys))))

(defn ei-jatkovastaus-validi?
  [vastaus kysymys]
  (boolean
    (and (= (:jatkokysymysid kysymys) (:jatkokysymysid vastaus))
         (:jatkovastaus_ei vastaus)
         (not (:jatkovastaus_kylla vastaus))
         (:ei_kysymys kysymys))))

(defn ei-jatkovastausta?
  [vastaus]
  (and (not (:jatkokysymysid vastaus))
       (not (:jatkovastaus_kylla vastaus))
       (not (:jatkovastaus_ei vastaus))))

(defn jatkovastaus-validi?
  [vastaus kysymys]
  (or (ei-jatkovastausta? vastaus)
      (and (contains? kysymys :jatkokysymysid)
           (or (kylla-jatkovastaus-validi? vastaus kysymys)
               (ei-jatkovastaus-validi? vastaus kysymys)))))

(defn monivalintavastaus-validi?
  [vastaus kysymys]
  (let [kysymyksen-monivalintavaihtoehtoidt (set (map :jarjestys (:monivalintavaihtoehdot kysymys)))]
    (every? true? (for [vastaus-arvo (:vastaus vastaus)]
                    (or (and (:eos_vastaus_sallittu kysymys) (= vastaus-arvo "EOS"))
                        (contains? kysymyksen-monivalintavaihtoehtoidt vastaus-arvo))))))

(defn numerovalintavastaus-validi?
  [vastaus kysymys]
  (every? true? (for [vastaus-arvo (:vastaus vastaus)]
                     (or (and (:eos_vastaus_sallittu kysymys) (= vastaus-arvo "EOS"))
                         (and (integer? vastaus-arvo) (<= 1 vastaus-arvo 5))))))

(defn kylla-ei-vastaus-validi?
  [vastaus kysymys]
  (or (and (:eos_vastaus_sallittu kysymys) (= (:vastaus vastaus) ["EOS"]))
      (every? #{"kylla" "ei"} (:vastaus vastaus))))

(defn ^:private vastausvalinnat-valideja?
  [vastaukset kysymykset]
  (every?
    identity
    (let [kysymysid->kysymys (map-by :kysymysid kysymykset)]
      (for [vastaus vastaukset
            :let [kysymys (kysymysid->kysymys (:kysymysid vastaus))]]
        (and kysymys
             (or (not= "kylla_ei_valinta" (:vastaustyyppi kysymys)) (kylla-ei-vastaus-validi? vastaus kysymys))
             (or (not= "monivalinta" (:vastaustyyppi kysymys)) (monivalintavastaus-validi? vastaus kysymys))
             (or (nil? (#{"arvosana" "asteikko" "likert_asteikko"} (:vastaustyyppi kysymys))) (numerovalintavastaus-validi? vastaus kysymys))
             (jatkovastaus-validi? vastaus kysymys))))))

(defn ^:private pakollisille-kysymyksille-loytyy-vastaukset?
  [vastaukset kysymykset]
  (every?
    seq
    (for [kysymys kysymykset
          :when (:pakollinen kysymys)]
      (filter #(= (:kysymysid %) (:kysymysid kysymys)) vastaukset))))

(defn validoi-vastaukset
  [vastaukset kysymykset]
  (if (and (vastausvalinnat-valideja? vastaukset kysymykset)
           (pakollisille-kysymyksille-loytyy-vastaukset? vastaukset kysymykset))
    vastaukset
    (log/error "Vastausten validointi epäonnistui. Ei voida tallentaa vastauksia.")))

(defn tallenna-jatkovastaus!
  [vastaus]
  (when (:jatkokysymysid vastaus)
    (arkisto/tallenna-jatkovastaus! {:jatkokysymysid (:jatkokysymysid vastaus)
                                     :kylla_asteikko (:jatkovastaus_kylla vastaus)
                                     :ei_vastausteksti (:jatkovastaus_ei vastaus)})))

(defn tallenna-vastaukset!
  [vastaukset vastaajaid kysymykset]
  (let [kysymysid->kysymys (map-by :kysymysid kysymykset)]
    (doseq [vastaus vastaukset
            :let [vastauksen-kysymys (kysymysid->kysymys (:kysymysid vastaus))
                  vastaustyyppi (:vastaustyyppi vastauksen-kysymys)
                  vastaus-arvot (:vastaus vastaus)
                  jatkovastaus (tallenna-jatkovastaus! vastaus)
                  en-osaa-sanoa (and (= vastaus-arvot ["EOS"]) (not= vastaustyyppi "vapaateksti"))]
            arvo vastaus-arvot]
      (arkisto/tallenna! {:kysymysid (:kysymysid vastaus)
                          :vastaajaid vastaajaid
                          :jatkovastausid (:jatkovastausid jatkovastaus)
                          :en_osaa_sanoa en-osaa-sanoa
                          :numerovalinta (when (and (not en-osaa-sanoa) (#{"monivalinta" "arvosana" "arvosana7" "asteikko" "likert_asteikko"} vastaustyyppi)) arvo)
                          :vapaateksti (when (= "vapaateksti" vastaustyyppi) arvo)
                          :vaihtoehto (when (and (not en-osaa-sanoa) (= "kylla_ei_valinta" vastaustyyppi)) arvo)}))
    (log/info (str "Vastaukset (" vastaajaid  ") tallennettu onnistuneesti."))
    true))

(defn validoi-ja-tallenna-vastaukset
  [vastaajaid vastaukset kysymykset]
  (when (some-> vastaukset
          (validoi-vastaukset kysymykset)
          (tallenna-vastaukset! vastaajaid kysymykset))
    (vastaaja/paivata-vastaaja! vastaajaid)
    true))

(defroutes reitit
  (POST "/:vastaajatunnus" []
    :path-params [vastaajatunnus :- schema/Str]
    :body-params [vastaukset :- [KayttajanVastaus]]
    (db/transaction
      (let [vastaajaid (:vastaajaid (vastaaja/luo-vastaaja! vastaajatunnus))]
        (if (and vastaajaid
                 (validoi-ja-tallenna-vastaukset vastaajaid vastaukset (kysely-arkisto/hae-kysymykset vastaajatunnus)))
          (response-nocache "OK")
          (do
            (log/error (str "Vastausten (" vastaajatunnus ") tallentaminen epäonnistui."))
            (db/rollback)
            {:status 403}))))))
