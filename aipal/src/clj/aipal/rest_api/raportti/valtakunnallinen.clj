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
  (:require [clj-time.core :as t]
            [compojure.api.core :refer [GET POST]]
            [schema.core :as s]
            aipal.compojure-util
            [aipal.rest-api.raportti.yhteinen :as yhteinen]
            [aipal.toimiala.raportti.yhdistaminen :as yhdistaminen]
            [aipal.toimiala.raportti.valtakunnallinen :as raportti]
            [aipal.toimiala.raportti.raportointi :refer [ei-riittavasti-vastaajia muodosta-csv muodosta-tyhja-csv valtakunnallinen-raportti-vertailujakso]]
            [aipal.arkisto.tutkinto :as tutkinto-arkisto]
            [aipal.arkisto.opintoala :as opintoala-arkisto]
            [oph.common.util.http-util :refer [csv-download-response parse-iso-date response-or-404]]
            [oph.common.util.util :refer [paivita-arvot]]))

(defn ^:private kehitysraportti-valtakunnallinen-raportti-tutkintotason-parametrit [parametrit]
  (if (seq (:koulutustoimijat parametrit))
    (case (:tutkintorakennetaso parametrit)
      "tutkinto"    {:tutkintorakennetaso "tutkinto"
                     :tutkinnot (:tutkinnot parametrit)}
      "opintoala"   {:tutkintorakennetaso "opintoala"
                     :opintoalat (:opintoalat parametrit)}
      "koulutusala" {:tutkintorakennetaso "koulutusala"
                     :koulutusalat (:koulutusalat parametrit)})
    (case (:tutkintorakennetaso parametrit)
      "tutkinto"    {:tutkintorakennetaso "opintoala"
                     :opintoalat [(:opintoala (tutkinto-arkisto/hae (first (:tutkinnot parametrit))))]}
      "opintoala"   {:tutkintorakennetaso "koulutusala"
                     :koulutusalat [(:koulutusala (opintoala-arkisto/hae (first (:opintoalat parametrit))))]}
      "koulutusala" {:tutkintorakennetaso "koulutusala"
                     :koulutusalat []})))

(defn ^:private muodosta-koulutusalavertailun-parametrit []
  {:tutkintorakennetaso "koulutusala"
   :koulutusalat []})

(defn ^:private muodosta-opintoalavertailun-parametrit [koulutusalat]
  (if (and (seq koulutusalat) (apply = koulutusalat))
    {:tutkintorakennetaso "koulutusala"
     :koulutusalat [(first koulutusalat)]} ; TODO: First?
    (muodosta-koulutusalavertailun-parametrit)))

(defn ^:private muodosta-tutkintovertailun-parametrit [opintoalat koulutusalat]
  (if (and (seq opintoalat) (apply = opintoalat))
    {:tutkintorakennetaso "opintoala"
     :opintoalat [(first opintoalat)]} ; TODO: First?
    (muodosta-opintoalavertailun-parametrit koulutusalat)))

(defn ^:private muodosta-koulutustoimijakohtaisen-vertailutiedon-parametrit [tutkinnot opintoalat koulutusalat]
  (if (and (seq tutkinnot) (apply = tutkinnot))
    {:tutkintorakennetaso "tutkinto"
     :tutkinnot [(first tutkinnot)]} ; TODO: First? Entä ne muut tutkinnot?
    (muodosta-tutkintovertailun-parametrit opintoalat koulutusalat)))

(defn ^:private tutkintojen-vertailutiedon-parametrit [parametrit]
  (let [opintoalat   (map (comp :opintoala tutkinto-arkisto/hae) (:tutkinnot parametrit))
        koulutusalat (map (comp :koulutusala opintoala-arkisto/hae) opintoalat)]
    (muodosta-tutkintovertailun-parametrit opintoalat koulutusalat)))

(defn ^:private opintoalojen-vertailutiedon-parametrit [parametrit]
  (let [koulutusalat (map (comp :koulutusala opintoala-arkisto/hae) (:opintoalat parametrit))]
    (muodosta-opintoalavertailun-parametrit koulutusalat)))

(defn ^:private koulutusalojen-vertailutiedon-parametrit [parametrit]
  (muodosta-koulutusalavertailun-parametrit))

(defn ^:private koulutustoimijakohtaisen-vertailutiedon-parametrit [parametrit]
  (case (:tutkintorakennetaso parametrit)
    "tutkinto"    (let [tutkinnot    (:tutkinnot parametrit)
                        opintoalat   (map (comp :opintoala tutkinto-arkisto/hae) (:tutkinnot parametrit))
                        koulutusalat (map (comp :koulutusala opintoala-arkisto/hae) opintoalat)]
                    (muodosta-koulutustoimijakohtaisen-vertailutiedon-parametrit tutkinnot opintoalat koulutusalat))
    "opintoala"   (let [opintoalat   (:opintoalat parametrit)
                        koulutusalat (map (comp :koulutusala opintoala-arkisto/hae) opintoalat)]
                    (muodosta-tutkintovertailun-parametrit opintoalat koulutusalat))
    "koulutusala" (muodosta-opintoalavertailun-parametrit (:koulutusalat parametrit))))

(defn ^:private vertailuraportti-valtakunnallinen-raportti-tutkintotason-parametrit [parametrit]
  (if (seq (:koulutustoimijat parametrit))
    (koulutustoimijakohtaisen-vertailutiedon-parametrit parametrit)
    (case (:tutkintorakennetaso parametrit)
      "tutkinto"    (tutkintojen-vertailutiedon-parametrit parametrit)
      "opintoala"   (opintoalojen-vertailutiedon-parametrit parametrit)
      "koulutusala" (koulutusalojen-vertailutiedon-parametrit parametrit))))

(defn ^:private muodosta-valtakunnallinen-raportti [parametrit tutkintotason-parametrit]
  (let [vertailujakso_alkupvm (:vertailujakso_alkupvm parametrit)
        vertailujakso_loppupvm (:vertailujakso_loppupvm parametrit)
        parametrit (merge (select-keys parametrit [:taustakysymysryhmaid])
                          {:koulutustoimijat []
                           :tyyppi "valtakunnallinen"}
                          (valtakunnallinen-raportti-vertailujakso vertailujakso_alkupvm vertailujakso_loppupvm)
                          tutkintotason-parametrit)]
    (raportti/muodosta parametrit)))

(defn ^:private vertailuraportti-valtakunnallinen-raportti [parametrit]
  (muodosta-valtakunnallinen-raportti
   parametrit
   (vertailuraportti-valtakunnallinen-raportti-tutkintotason-parametrit parametrit)))

(defn ^:private kehitysraportti-valtakunnallinen-raportti [parametrit]
  (muodosta-valtakunnallinen-raportti
   parametrit
   (kehitysraportti-valtakunnallinen-raportti-tutkintotason-parametrit parametrit)))

(defn ^:private koulutustoimija-valtakunnallinen-raportti [parametrit]
  (muodosta-valtakunnallinen-raportti
   parametrit
   (koulutustoimijakohtaisen-vertailutiedon-parametrit parametrit)))

(defn ^:private luo-tutkintotyyppi-raportit [parametrit]
  [[(raportti/muodosta parametrit)]
   [(muodosta-valtakunnallinen-raportti parametrit nil)]])

(defn ^:private luo-vertailuraportit [parametrit]
  (case (:tutkintorakennetaso parametrit)
    "tutkinto"    (if-let [tutkinnot (seq (:tutkinnot parametrit))]
                    [(for [tutkinto tutkinnot]
                       (raportti/muodosta (assoc parametrit :tutkinnot [tutkinto])))
                     [(vertailuraportti-valtakunnallinen-raportti parametrit)]]
                    (luo-tutkintotyyppi-raportit parametrit))
    "opintoala"   (if-let [opintoalat (seq (:opintoalat parametrit))]
                    [(for [opintoala opintoalat]
                       (raportti/muodosta (assoc parametrit :opintoalat [opintoala])))
                     [(vertailuraportti-valtakunnallinen-raportti parametrit)]]
                    (luo-tutkintotyyppi-raportit parametrit))
    "koulutusala" (if-let [koulutusalat (seq (:koulutusalat parametrit))]
                    [(for [koulutusala koulutusalat]
                       (raportti/muodosta (assoc parametrit :koulutusalat [koulutusala])))
                     [(vertailuraportti-valtakunnallinen-raportti parametrit)]]
                    (luo-tutkintotyyppi-raportit parametrit))))

(defn luo-raportit [parametrit]
  (apply concat
         (case (:tyyppi parametrit)
           "vertailu"         (luo-vertailuraportit parametrit)
           "kehitys"          [[(raportti/muodosta parametrit)]
                               [(kehitysraportti-valtakunnallinen-raportti parametrit)]]
           "koulutustoimijat" [(for [koulutustoimija (:koulutustoimijat parametrit)]
                                 (raportti/muodosta (assoc parametrit :koulutustoimijat [koulutustoimija])))
                               [(koulutustoimija-valtakunnallinen-raportti parametrit)]])))

(defn muodosta-raportit [parametrit asetukset]
  (let [kaikki-raportit (for [raportti (luo-raportit (yhteinen/korjaa-numero-avaimet parametrit))]
                          (ei-riittavasti-vastaajia raportti asetukset ""))
        naytettavat (filter (comp nil? :virhe) kaikki-raportit)
        virheelliset (filter :virhe kaikki-raportit)]
    (merge (when (seq naytettavat)
             (yhdistaminen/yhdista-raportit naytettavat true))
      {:raportoitavia (count naytettavat)
       :virheelliset virheelliset})))

(defn muodosta-ketjutettu [parametrit asetukset]
  {:raportit
   (for [tutkinto (:tutkinnot parametrit)]
     (-> parametrit
       (assoc :tutkinnot [tutkinto])
       (assoc :tyyppi "kehitys")
       (muodosta-raportit asetukset)))})
;      (muodosta-raportit (assoc parametrit :tutkinnot (vector tutkinto)) asetukset)))


(defn reitit [asetukset]
  (POST "/" [& parametrit]
    :body [parametrit s/Any]
    :kayttooikeus :yllapitaja
    (response-or-404
      (if (= "kehitys-ketjutettu" (:tyyppi parametrit))
        (muodosta-ketjutettu parametrit asetukset)
        (muodosta-raportit parametrit asetukset)))))

(defn csv-reitit [asetukset]
  (yhteinen/wrap-muunna-raportti-json-param
    (GET "/:kieli/csv" [kieli parametrit]
      :path-params [kieli]
      :query-params [parametrit]
      :kayttooikeus :yllapitaja
      (let [vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)]
        (csv-download-response
          (apply str
                 (for [raportti (luo-raportit parametrit)]
                   (if (>= (:vastaajien_lukumaara raportti) vaaditut-vastaajat)
                     (muodosta-csv raportti kieli)
                     (muodosta-tyhja-csv raportti kieli))))
          (str (:tyyppi parametrit) "raportti.csv"))))))
