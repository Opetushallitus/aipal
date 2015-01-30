;; Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.rest-api.raportti.kysely
  (:require [cheshire.core :as cheshire]
            [korma.db :as db]
            [oph.common.util.http-util :refer [json-response parse-iso-date csv-download-response]]
            [oph.common.util.util :refer [paivita-arvot poista-tyhjat]]
            [oph.korma.korma :refer [joda-date->sql-date]]
            [aipal.compojure-util :as cu]
            [aipal.toimiala.raportti.kysely :refer [muodosta-raportti muodosta-valtakunnallinen-vertailuraportti]]
            [aipal.toimiala.raportti.raportointi :refer [ei-riittavasti-vastaajia muodosta-csv muodosta-tyhja-csv]]))

(defn muodosta-raportti-parametreilla
  [kyselyid parametrit]
  (let [kyselyid (Integer/parseInt kyselyid)
        parametrit (paivita-arvot parametrit [:vertailujakso_alkupvm :vertailujakso_loppupvm] parse-iso-date)
        parametrit (paivita-arvot parametrit [:vertailujakso_alkupvm :vertailujakso_loppupvm] joda-date->sql-date)
        parametrit (poista-tyhjat parametrit)
        raportti (muodosta-raportti kyselyid parametrit)]
    (assoc raportti :parametrit parametrit)))

(defn reitit [asetukset]
  (cu/defapi :kysely-raportti kyselyid :post "/:kyselyid" [kyselyid & parametrit]
    (db/transaction
      (let [valtakunnallinen-raportti (muodosta-valtakunnallinen-vertailuraportti (Integer/parseInt kyselyid) parametrit)
            raportti (muodosta-raportti-parametreilla kyselyid parametrit)]
        (json-response
          (for [raportti [valtakunnallinen-raportti raportti]
                :when raportti]
            (ei-riittavasti-vastaajia raportti asetukset)))))))

(defn csv-reitit [asetukset]
  (cu/defapi :kysely-raportti kyselyid :get "/:kyselyid/:kieli/csv" [kyselyid kieli & parametrit]
    (db/transaction
      (let [vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)
            parametrit (paivita-arvot parametrit [:tutkinnot] #(remove clojure.string/blank? (clojure.string/split % #",")))
            raportti (muodosta-raportti-parametreilla kyselyid parametrit)]
        (if (>= (:vastaajien-lkm raportti) vaaditut-vastaajat)
          (csv-download-response (muodosta-csv raportti kieli) "kysely.csv")
          (csv-download-response (muodosta-tyhja-csv raportti kieli) "kysely_ei_vastaajia.csv"))))))
