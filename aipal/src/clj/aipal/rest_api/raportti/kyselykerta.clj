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
  (:require [compojure.core :as c]
            [korma.db :as db]

            [oph.common.util.http-util :refer [json-response parse-iso-date]]
            [oph.common.util.util :refer [paivita-arvot poista-tyhjat]]
            [oph.korma.korma :refer [joda-date->sql-date]]

            [aipal.compojure-util :as cu]
            [aipal.toimiala.raportti.kyselykerta :refer [muodosta-raportti]]
            [aipal.toimiala.raportti.raportointi :as raportointi]))

(defn muodosta-raportti-parametreilla
  [kyselykertaid parametrit]
  (let [kyselykertaid (Integer/parseInt kyselykertaid)
        parametrit (paivita-arvot parametrit [:vertailujakso_alkupvm :vertailujakso_loppupvm] parse-iso-date)
        parametrit (paivita-arvot parametrit [:vertailujakso_alkupvm :vertailujakso_loppupvm] joda-date->sql-date)
        parametrit (paivita-arvot parametrit [:tutkinnot] seq) ; tyhjä lista -> nil
        raportti (muodosta-raportti kyselykertaid parametrit)]
    (assoc raportti :parametrit parametrit)))

(defn reitit [asetukset]
  (cu/defapi :kyselykerta-raportti kyselykertaid :post "/:kyselykertaid" [kyselykertaid & parametrit]
    (db/transaction
      (json-response
        (raportointi/ei-riittavasti-vastaajia (muodosta-raportti-parametreilla kyselykertaid parametrit) asetukset)))))
