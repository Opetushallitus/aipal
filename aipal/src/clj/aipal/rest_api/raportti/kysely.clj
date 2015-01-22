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
  (:require [aipal.compojure-util :as cu]
            [korma.db :as db]
            [oph.common.util.http-util :refer [json-response parse-iso-date]]
            [oph.korma.korma :refer [joda-date->sql-date]]
            [aipal.rest-api.kyselykerta :refer [paivita-arvot]]
            [aipal.toimiala.raportti.kysely :refer [muodosta-raportti]]))

(defn muodosta-raportti-parametreilla
  [kyselyid parametrit]
  (let [kyselyid (Integer/parseInt kyselyid)
        parametrit (paivita-arvot parametrit [:vertailujakso_alkupvm :vertailujakso_loppupvm] parse-iso-date)
        parametrit (paivita-arvot parametrit [:vertailujakso_alkupvm :vertailujakso_loppupvm] joda-date->sql-date)
        parametrit (paivita-arvot parametrit [:tutkinnot] seq)] ; tyhjä lista -> nil
    (muodosta-raportti kyselyid parametrit)))

(defn reitit [asetukset]
  (cu/defapi :kysely-raportti kyselyid :post "/:kyselyid" [kyselyid & parametrit]
    (db/transaction
      (let [vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)
            raportti (muodosta-raportti-parametreilla kyselyid parametrit)]
        (json-response
          (if (>= (:vastaajien-lkm raportti) vaaditut-vastaajat)
            raportti
            (assoc (dissoc raportti :raportti) :virhe "ei-riittavasti-vastaajia")))))))
