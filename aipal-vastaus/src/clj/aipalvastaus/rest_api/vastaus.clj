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
  (:require [compojure.api.core :refer [defroutes POST GET]]
            [korma.db :as db]
            [schema.core :as schema]
            [clojure.tools.logging :as log]
            [oph.common.util.http-util :refer [response-nocache]]
            [aipalvastaus.sql.vastaus :as arkisto]
            [aipalvastaus.sql.kyselykerta :as kysely-arkisto]
            [aipalvastaus.sql.vastaaja :as vastaaja]
            [aipalvastaus.toimiala.skeema :refer [KayttajanVastaus]]
            [oph.common.util.util :refer [map-by]]))

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
             (or (nil? (#{"arvosana" "likert_asteikko" "arvosana4_ja_eos" "asteikko5_1"} (:vastaustyyppi kysymys))) (numerovalintavastaus-validi? vastaus kysymys)))))))

(defn ^:private pakollisille-kysymyksille-loytyy-vastaukset?
  [vastaukset kysymykset]
  (every?
    seq
    (for [kysymys kysymykset
          :when (:pakollinen kysymys)]
      (filter #(= (:kysymysid %) (:kysymysid kysymys)) vastaukset))))

(defn validoi-vastaukset
  [vastaukset kysymykset kyselytyyppi]
  (if (and (vastausvalinnat-valideja? vastaukset kysymykset)
           (or (= kyselytyyppi "itsearviointi") (pakollisille-kysymyksille-loytyy-vastaukset? vastaukset kysymykset)))
    vastaukset
    (log/error "Vastausten validointi epäonnistui. Ei voida tallentaa vastauksia.")))

(defn tallenna-jatkovastaus!
  [vastaus]
  (when (:jatkokysymysid vastaus)
    (arkisto/tallenna-jatkovastaus! {:jatkokysymysid (:jatkokysymysid vastaus)
                                     :kylla_asteikko (:jatkovastaus_kylla vastaus)
                                     :ei_vastausteksti (:jatkovastaus_ei vastaus)})))

(defn tallenna-vastaukset!
  [vastaukset vastaajaid kysymykset kyselytyyppi]
  (let [kysymysid->kysymys (map-by :kysymysid kysymykset)]
    (when (= "itsearviointi" kyselytyyppi) (arkisto/poista! vastaajaid))
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
                          :numerovalinta (when (and (not en-osaa-sanoa) (#{"monivalinta" "arvosana" "arvosana6" "arvosana7" "nps" "asteikko" "likert_asteikko" "asteikko5_1" "arvosana4_ja_eos" "arvosana6_ja_eos"} vastaustyyppi)) arvo)
                          :vapaateksti (when (= "vapaateksti" vastaustyyppi) arvo)
                          :vaihtoehto (when (and (not en-osaa-sanoa) (= "kylla_ei_valinta" vastaustyyppi)) arvo)}))
    (log/info (str "Vastaukset (" vastaajaid  ") tallennettu onnistuneesti."))
    true))

(defn validoi-ja-tallenna-vastaukset
  [vastaajaid vastaukset kysymykset tunnus]
  (let [kyselytyyppi (:tyyppi (kysely-arkisto/hae-kyselyn-tiedot tunnus))]
    (when (some-> vastaukset
            (validoi-vastaukset kysymykset kyselytyyppi)
            (tallenna-vastaukset! vastaajaid kysymykset kyselytyyppi))
      true)))

(defn hae-vastaus [vastaus]
  (let [vals (-> vastaus
               (select-keys [:numerovalinta :vaihtoehto :vapaateksti :en_osaa_sanoa])
               vals)
        value (first (filter some? vals))]
    {:kysymysid (:kysymysid vastaus) :vastaus value :kysymysryhmaid (:kysymysryhmaid vastaus) :en_osaa_sanoa (:en_osaa_sanoa vastaus)}))


(defroutes reitit
  (POST "/:vastaajatunnus" []
    :path-params [vastaajatunnus :- schema/Str]
    :body-params [vastaukset :- [KayttajanVastaus]]
    (db/transaction
      (let [vastaajaid (:vastaajaid (vastaaja/luo-tai-hae-vastaaja! vastaajatunnus))]
        (if (and vastaajaid
                 (validoi-ja-tallenna-vastaukset vastaajaid vastaukset (kysely-arkisto/hae-kysymykset vastaajatunnus) vastaajatunnus))
          (response-nocache "OK")
          (do
            (log/error (str "Vastausten (" vastaajatunnus ") tallentaminen epäonnistui."))
            (db/rollback)
            {:status 403})))))
  (GET "/:vastaajatunnus" []
    :path-params [vastaajatunnus :- schema/Str]
    (map hae-vastaus (kysely-arkisto/hae-vastaukset vastaajatunnus))))
