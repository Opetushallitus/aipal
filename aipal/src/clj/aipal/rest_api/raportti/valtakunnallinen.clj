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

(ns aipal.rest-api.raportti.valtakunnallinen
  (:require [compojure.core :as c]
            [aipal.compojure-util :as cu]
            [korma.db :as db]
            [clj-time.core :as t]
            [oph.common.util.http-util :refer [json-response parse-iso-date csv-download-response]]
            [oph.common.util.util :refer [paivita-arvot muunna-avainsanoiksi]]
            [aipal.toimiala.raportti.yhdistaminen :as yhdistaminen]
            [aipal.toimiala.raportti.valtakunnallinen :as raportti]
            [aipal.toimiala.raportti.raportointi :refer [ei-riittavasti-vastaajia muodosta-csv muodosta-tyhja-csv vertailuraportti-vertailujakso]]
            [aipal.arkisto.tutkinto :as tutkinto-arkisto]
            [aipal.arkisto.opintoala :as opintoala-arkisto]))

; Valtakunnallinen vertailuraportti on ilman koulutustoimijoita, ylemmälle tutkintohierarkian tasolle
(defn kehitysraportti-vertailuraportti-parametrit [parametrit]
  (let [parametrit (assoc parametrit :koulutustoimijat [])]
    (case (:tutkintorakennetaso parametrit)
      "tutkinto" (assoc parametrit :tutkintorakennetaso "opintoala"
                                   :opintoalat [(:opintoala (tutkinto-arkisto/hae (first (:tutkinnot parametrit))))])
      "opintoala" (assoc parametrit :tutkintorakennetaso "koulutusala"
                                    :koulutusalat [(:koulutusala (opintoala-arkisto/hae (first (:opintoalat parametrit))))])
      "koulutusala" parametrit)))

(defn lisaa-vertailuraportille-otsikko [raportti]
  (merge raportti {:nimi_fi "Valtakunnallinen"
                   :nimi_sv "Valtakunnallinen (sv)"}))

(defn kehitysraportti-vertailuraportti [parametrit]
  (let [vertailujakso_alkupvm (:vertailujakso_alkupvm parametrit)
        vertailujakso_loppupvm (:vertailujakso_loppupvm parametrit)
        parametrit (merge parametrit (vertailuraportti-vertailujakso vertailujakso_alkupvm vertailujakso_loppupvm))]
    (-> (raportti/muodosta (kehitysraportti-vertailuraportti-parametrit parametrit))
      lisaa-vertailuraportille-otsikko)))

(defn koulutustoimija-vertailuraportti [parametrit]
  (-> (raportti/muodosta (merge
                          parametrit
                          {:koulutustoimijat []
                           :tyyppi "vertailu"}
                          (vertailuraportti-vertailujakso (:vertailujakso_alkupvm parametrit) (:vertailujakso_loppupvm parametrit))))
    lisaa-vertailuraportille-otsikko))

(defn luo-raportit [parametrit]
  (case (:tyyppi parametrit)
    "vertailu" (case (:tutkintorakennetaso parametrit)
                 "tutkinto" (for [tutkinto (:tutkinnot parametrit)] (raportti/muodosta (assoc parametrit :tutkinnot [tutkinto])))
                 "opintoala" (for [opintoala (:opintoalat parametrit)] (raportti/muodosta (assoc parametrit :opintoalat [opintoala])))
                 "koulutusala" (for [koulutusala (:koulutusalat parametrit)] (raportti/muodosta (assoc parametrit :koulutusalat [koulutusala]))))
    "kehitys" (concat [(raportti/muodosta parametrit)] [(kehitysraportti-vertailuraportti parametrit)])
    "koulutustoimijat" (concat
                         (for [koulutustoimija (:koulutustoimijat parametrit)] (raportti/muodosta (assoc parametrit :koulutustoimijat [koulutustoimija])))
                         [(koulutustoimija-vertailuraportti parametrit)])))

(defn reitit [asetukset]
  (cu/defapi :valtakunnallinen-raportti nil :post "/" [& parametrit]
    (db/transaction
      (json-response
        (let [kaikki-raportit (for [raportti (luo-raportit parametrit)]
                                 (ei-riittavasti-vastaajia raportti asetukset))
              naytettavat (filter (comp nil? :virhe) kaikki-raportit)
              virheelliset (filter :virhe kaikki-raportit)]
          (merge (when (seq naytettavat)
                   (yhdistaminen/yhdista-raportit naytettavat))
                 {:raportoitavia (count naytettavat)
                  :virheelliset virheelliset}))))))

(defn csv-reitit [asetukset]
  (cu/defapi :valtakunnallinen-raportti nil :get "/:kieli/csv" [kieli & parametrit]
    (db/transaction
      (let [vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)
            parametrit (muunna-avainsanoiksi (cheshire.core/parse-string (:raportti parametrit)))]
        (csv-download-response
          (apply str
                 (for [raportti (luo-raportit parametrit)]
                   (if (>= (:vastaajien_lukumaara raportti) vaaditut-vastaajat)
                     (muodosta-csv raportti kieli)
                     (muodosta-tyhja-csv raportti kieli))))
          (str (:tyyppi parametrit) "raportti.csv"))))))
