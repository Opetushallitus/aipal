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
            [aipalvastaus.sql.vastaus :as vastaus]
            [aipalvastaus.sql.kyselykerta :as kysely]
            [aipalvastaus.sql.vastaaja :as vastaaja]
            [aipalvastaus.toimiala.skeema :refer [KayttajanVastaus]]
            [oph.common.util.util :refer [map-by]]))

(defn validoi-vastaukset
  [vastaukset kysymykset]
  (when (every? true? (let [kysymysid->kysymys (map-by :kysymysid kysymykset)]
                        (for [vastaus vastaukset]
                          (when (kysymysid->kysymys (:kysymysid vastaus)) true))))
    vastaukset))

(defn muodosta-tallennettavat-vastaukset
  [vastaukset vastaajaid kysymykset]
  (flatten (let [kysymysid->kysymys (map-by :kysymysid kysymykset)]
             (for [vastaus vastaukset
                   :let [vastauksen-kysymys (kysymysid->kysymys (:kysymysid vastaus))
                         vastaustyyppi (:vastaustyyppi vastauksen-kysymys)
                         vastaus-arvot (:vastaus vastaus)]]
               (for [arvo vastaus-arvot]
                 {:kysymysid (:kysymysid vastaus)
                  :vastaajaid vastaajaid
                  :vastaustyyppi (:vastaustyyppi vastauksen-kysymys)
                  :numerovalinta (when (#{"monivalinta" "asteikko"} vastaustyyppi) arvo)
                  :vapaateksti (when (= "vapaateksti" vastaustyyppi) arvo)
                  :vaihtoehto (when (= "kylla_ei_valinta" vastaustyyppi) arvo)})))))

(defn tallenna-vastaukset!
  [vastaukset]
  (doall (map vastaus/tallenna! vastaukset)))

(defn validoi-ja-tallenna-vastaukset
  [vastaajaid vastaukset kysymykset]
  (when (some-> vastaukset
          (validoi-vastaukset kysymykset)
          (muodosta-tallennettavat-vastaukset vastaajaid kysymykset)
          tallenna-vastaukset!)
    (vastaaja/paivata-vastaaja! vastaajaid)
    "OK"))

(c/defroutes reitit
  (c/POST "/:vastaustunnus" [vastaustunnus vastaukset]
    (db/transaction
      (schema/validate [KayttajanVastaus] vastaukset)
      (let [vastaajaid (:vastaajaid (vastaaja/luo-vastaaja! vastaustunnus))]
        (json-response-nocache
          (validoi-ja-tallenna-vastaukset vastaajaid vastaukset (kysely/hae-kysymykset vastaustunnus)))))))
