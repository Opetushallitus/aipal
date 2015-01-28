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
            [aipal.toimiala.raportti.valtakunnallinen :as raportti]
            [aipal.toimiala.raportti.raportointi :refer [muodosta-csv muodosta-tyhja-csv]]
            [aipal.arkisto.tutkinto :as tutkinto-arkisto]
            [aipal.arkisto.opintoala :as opintoala-arkisto]))

(defn vertailuraportti-vertailujakso [vertailujakso_alkupvm vertailujakso_loppupvm]
  (let [alkupvm (parse-iso-date vertailujakso_alkupvm)
        loppupvm (or (parse-iso-date vertailujakso_loppupvm) (t/today))
        vertailupvm (t/minus loppupvm (t/years 1))]
    (if (and alkupvm (<= (.compareTo alkupvm vertailupvm) 0))
      {:vertailujakso_alkupvm vertailujakso_alkupvm
       :vertailujakso_loppupvm vertailujakso_loppupvm}
      {:vertailujakso_alkupvm (and alkupvm (.toString vertailupvm))
       :vertailujakso_loppupvm vertailujakso_loppupvm})))

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
    "kehitys" (concat [(kehitysraportti-vertailuraportti parametrit)] [(raportti/muodosta parametrit)])
    "koulutustoimijat" (concat
                         [(koulutustoimija-vertailuraportti parametrit)]
                         (for [koulutustoimija (:koulutustoimijat parametrit)] (raportti/muodosta (assoc parametrit :koulutustoimijat [koulutustoimija]))))))

(defn reitit [asetukset]
  (cu/defapi :valtakunnallinen-raportti nil :post "/" [& parametrit]
    (db/transaction
      (json-response
        (for [raportti (luo-raportit parametrit)
              :let [vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)]]
          (if (>= (:vastaajien-lkm raportti) vaaditut-vastaajat)
            raportti
            (assoc (dissoc raportti :raportti) :virhe "ei-riittavasti-vastaajia")))))))

(defn csv-reitit [asetukset]
  (cu/defapi :valtakunnallinen-raportti nil :get "/:kieli/csv" [kieli & parametrit]
    (db/transaction
      (let [vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)
            parametrit (muunna-avainsanoiksi (cheshire.core/parse-string (:raportti parametrit)))]
        (csv-download-response
          (apply str
                 (for [raportti (luo-raportit parametrit)]
                   (if (>= (:vastaajien-lkm raportti) vaaditut-vastaajat)
                     (muodosta-csv raportti kieli)
                     (muodosta-tyhja-csv raportti kieli))))
          (str (:tyyppi parametrit) "raportti.csv"))))))
