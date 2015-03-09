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

(ns aipal.rest-api.raportti.kyselykerta
  (:require [cheshire.core :as cheshire]
            [korma.db :as db]

            [oph.common.util.http-util :refer [csv-download-response json-response]]
            [oph.common.util.util :refer [muunna-avainsanoiksi]]

            [aipal.compojure-util :as cu]
            [aipal.rest-api.raportti.yhdistaminen :as yhdistaminen]
            [aipal.toimiala.raportti.kyselykerta :refer [muodosta-raportti]]
            [aipal.toimiala.raportti.raportointi :as raportointi]
            [aipal.toimiala.raportti.kyselyraportointi :refer [paivita-parametrit]]))

(defn muodosta-raportti-parametreilla
  [kyselykertaid parametrit]
  (let [parametrit (paivita-parametrit parametrit)
        raportti (muodosta-raportti (Integer/parseInt kyselykertaid) parametrit)]
    (assoc raportti :parametrit parametrit)))

(defn muodosta-kyselykertaraportti
  [kyselykertaid parametrit asetukset]
  (let [raportti (raportointi/ei-riittavasti-vastaajia
                   (muodosta-raportti-parametreilla kyselykertaid parametrit)
                   asetukset)
        nimi (-> raportti :yhteenveto :kyselykerta)
        naytettavat (filter (comp nil? :virhe) [raportti])
        virheelliset (filter :virhe [raportti])]
    (merge (when (seq naytettavat)
             (yhdistaminen/yhdista-raportit naytettavat))
           {:nimi nimi
            :nimet [{:nimi_fi nimi
                     :nimi_sv nimi}]
            :raportoitavia (count naytettavat)
            :virheelliset virheelliset})))

(defn reitit [asetukset]
  (cu/defapi :kyselykerta-raportti kyselykertaid :post "/:kyselykertaid" [kyselykertaid & parametrit]
    (db/transaction
      (json-response
        (muodosta-kyselykertaraportti kyselykertaid parametrit asetukset)))))

(defn csv-reitit [asetukset]
  (cu/defapi :kyselykerta-raportti kyselykertaid :get "/:kyselykertaid/csv" [kyselykertaid & parametrit]
    (db/transaction
      (let [vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)
            parametrit (muunna-avainsanoiksi (cheshire.core/parse-string (:raportti parametrit)))
            raportti (muodosta-raportti-parametreilla kyselykertaid parametrit)]
        (if (>= (:vastaajien_lukumaara raportti) vaaditut-vastaajat)
          (csv-download-response (raportointi/muodosta-csv raportti (:kieli parametrit)) "kyselykerta.csv")
          (csv-download-response (raportointi/muodosta-tyhja-csv raportti (:kieli parametrit)) "kyselykerta_ei_vastaajia.csv"))))))
