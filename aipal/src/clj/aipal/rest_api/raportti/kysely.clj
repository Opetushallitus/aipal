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
            [oph.common.util.http-util :refer [json-response csv-download-response]]
            [oph.common.util.util :refer [muunna-avainsanoiksi]]
            [aipal.compojure-util :as cu]
            [aipal.rest-api.i18n :as i18n]
            [aipal.toimiala.raportti.yhdistaminen :as yhdistaminen]
            [aipal.toimiala.raportti.kysely :refer [muodosta-raportti muodosta-valtakunnallinen-vertailuraportti muodosta-yhteenveto]]
            [aipal.toimiala.raportti.raportointi :refer [ei-riittavasti-vastaajia muodosta-csv muodosta-tyhja-csv]]
            [aipal.toimiala.raportti.kyselyraportointi :refer [paivita-parametrit]]))

(defn ^:private muodosta-kyselyn-raportti-parametreilla
  [kyselyid parametrit]
  (let [parametrit (paivita-parametrit (assoc parametrit :tutkinnot []))
        raportti (muodosta-raportti kyselyid parametrit)]
    (assoc raportti :parametrit parametrit)))

(defn ^:private muodosta-kyselyn-tutkintojen-raportit-parametreilla
  [kyselyid parametrit]
  (let [parametrit (paivita-parametrit parametrit)]
    (for [tutkinto (:tutkinnot parametrit)]
      (-> (muodosta-raportti kyselyid (assoc parametrit :tutkinnot [tutkinto]))
        (assoc :parametrit parametrit)))))

(defn ^:private hae-kysymysryhmista [haettava-kysymysryhmaid kysymysryhmat]
  (first
    (filter
      (fn [{:keys [kysymysryhmaid]}]
        (= kysymysryhmaid
           haettava-kysymysryhmaid))
      kysymysryhmat)))

(defn ^:private valitse-valtakunnalliseen-kyselyn-kysymysryhmat
  [valtakunnallinen-raportti {kyselyn-kysymysryhmat :raportti}]
  (let [valtakunnalliset-kysymysryhmat (:raportti valtakunnallinen-raportti)]
    (assoc valtakunnallinen-raportti
           :raportti
           (map (fn [{:keys [kysymysryhmaid]}]
                  (hae-kysymysryhmista kysymysryhmaid valtakunnalliset-kysymysryhmat))
                kyselyn-kysymysryhmat))))

(defn ^:private lisaa-raporttiin-nimi [valtakunnallinen-raportti]
  (let [tekstit-fi (i18n/hae-tekstit "fi")
        tekstit-sv (i18n/hae-tekstit "sv")]
    (-> valtakunnallinen-raportti
      (assoc :nimi_fi (get-in tekstit-fi [:yleiset :valtakunnallinen]))
      (assoc :nimi_sv (get-in tekstit-sv [:yleiset :valtakunnallinen])))))

(defn ^:private muodosta-raportit-parametreilla [kyselyid parametrit]
  (if (empty? (:tutkinnot parametrit))
    (let [raportti (muodosta-kyselyn-raportti-parametreilla kyselyid parametrit)
          valtakunnallinen-raportti (some-> (muodosta-valtakunnallinen-vertailuraportti kyselyid parametrit)
                                      (lisaa-raporttiin-nimi)
                                      (valitse-valtakunnalliseen-kyselyn-kysymysryhmat raportti))]
      [raportti valtakunnallinen-raportti])
    (muodosta-kyselyn-tutkintojen-raportit-parametreilla kyselyid parametrit)))

(defn muodosta-kyselyraportti [kyselyid parametrit asetukset]
  (let [raportit (muodosta-raportit-parametreilla kyselyid parametrit)
        kaikki-raportit (for [raportti raportit
                              :when raportti]
                          (ei-riittavasti-vastaajia raportti asetukset))
        naytettavat (filter (comp nil? :virhe) kaikki-raportit)
        virheelliset (filter :virhe kaikki-raportit)
        yhteenveto (muodosta-yhteenveto kyselyid (paivita-parametrit parametrit))]
    (merge (when (seq naytettavat)
             (yhdistaminen/yhdista-raportit naytettavat))
           {:raportoitavia (count naytettavat)
            :yhteenveto yhteenveto
            :virheelliset virheelliset})))

(defn reitit [asetukset]
  (cu/defapi :kysely-raportti kyselyid :post "/:kyselyid" [kyselyid & parametrit]
    (db/transaction
      (json-response
        (muodosta-kyselyraportti (Integer/parseInt kyselyid) parametrit asetukset)))))

(defn csv-reitit [asetukset]
  (cu/defapi :kysely-raportti kyselyid :get "/:kyselyid/csv" [kyselyid & parametrit]
    (db/transaction
      (let [vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)
            parametrit (muunna-avainsanoiksi (cheshire.core/parse-string (:raportti parametrit)))
            raportti (muodosta-kyselyn-raportti-parametreilla (Integer/parseInt kyselyid) parametrit)]
        (if (>= (:vastaajien_lukumaara raportti) vaaditut-vastaajat)
          (csv-download-response (muodosta-csv raportti (:kieli parametrit)) "kysely.csv")
          (csv-download-response (muodosta-tyhja-csv raportti (:kieli parametrit)) "kysely_ei_vastaajia.csv"))))))
