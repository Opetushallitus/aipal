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
  (:require [compojure.core :as c]
            [korma.db :as db]
            [schema.core :as schema]
            [oph.common.util.http-util :refer [json-response-nocache]]
            [aipalvastaus.sql.vastaus :as arkisto]
            [aipalvastaus.sql.kyselykerta :as kysely-arkisto]
            [aipalvastaus.sql.vastaaja :as vastaaja]
            [aipalvastaus.toimiala.skeema :refer [KayttajanVastaus]]
            [oph.common.util.util :refer [map-by]]))

(defn kylla-jatkovastaus-validi?
  [vastaus kysymys]
  (or (and (= (:jatkokysymysid kysymys) (:jatkokysymysid vastaus))
           (:jatkovastaus_kylla vastaus)
           (:kylla_teksti_fi kysymys))
      (and (not (:jatkokysymysid vastaus))
           (not (:jatkovastaus_kylla vastaus))
           (not (:kylla_teksti_fi kysymys)))))

(defn ei-jatkovastaus-validi?
  [vastaus kysymys]
  (or (and (= (:jatkokysymysid kysymys) (:jatkokysymysid vastaus))
           (:jatkovastaus_ei vastaus)
           (:ei_teksti_fi kysymys))
      (and (not (:jatkokysymysid vastaus))
           (not (:jatkovastaus_ei vastaus))
           (not (:ei_teksti_fi kysymys)))))

(defn jatkovastaus-validi?
  [vastaus kysymys]
  (if (:jatkokysymysid kysymys)
    (or (kylla-jatkovastaus-validi? vastaus kysymys)
        (ei-jatkovastaus-validi? vastaus kysymys)
        (and (not (:pakollinen kysymys))
             (not (:jatkokysymysid vastaus))
             (not (:jatkovastaus_kylla vastaus))
             (not (:jatkovastaus_ei vastaus))))
    (not (:jatkokysymysid vastaus))))

(defn validoi-vastaukset
  [vastaukset kysymykset]
  (when (every? true? (let [kysymysid->kysymys (map-by :kysymysid kysymykset)]
                        (for [vastaus vastaukset
                              :let [kysymys (kysymysid->kysymys (:kysymysid vastaus))]]
                          (when (and kysymys
                                     (jatkovastaus-validi? vastaus kysymys))
                            true))))
    vastaukset))

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
                  jatkovastaus (tallenna-jatkovastaus! vastaus)]]
      (doseq [arvo vastaus-arvot]
        (arkisto/tallenna! {:kysymysid (:kysymysid vastaus)
                            :vastaajaid vastaajaid
                            :jatkovastausid (:jatkovastausid jatkovastaus)
                            :numerovalinta (when (#{"monivalinta" "asteikko"} vastaustyyppi) arvo)
                            :vapaateksti (when (= "vapaateksti" vastaustyyppi) arvo)
                            :vaihtoehto (when (= "kylla_ei_valinta" vastaustyyppi) arvo)})))
    "OK"))

(defn validoi-ja-tallenna-vastaukset
  [vastaajaid vastaukset kysymykset]
  (when (some-> vastaukset
          (validoi-vastaukset kysymykset)
          (tallenna-vastaukset! vastaajaid kysymykset))
    (vastaaja/paivata-vastaaja! vastaajaid)
    "OK"))

(c/defroutes reitit
  (c/POST "/:vastaajatunnus" [vastaajatunnus vastaukset]
    (db/transaction
      (schema/validate [KayttajanVastaus] vastaukset)
      (if-let [vastaajaid (:vastaajaid (vastaaja/luo-vastaaja! vastaajatunnus))]
        (json-response-nocache
          (validoi-ja-tallenna-vastaukset vastaajaid vastaukset (kysely-arkisto/hae-kysymykset vastaajatunnus)))
        {:status 403}))))
