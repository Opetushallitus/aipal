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
  (:require [compojure.api.core :refer [GET POST defroutes]]
            [schema.core :as s]
            [arvo.db.core :refer [*db*] :as db]
            aipal.compojure-util
            [aipal.rest-api.i18n :as i18n]
            [aipal.rest-api.raportti.yhteinen :as yhteinen]
            [aipal.toimiala.raportti.kysely :refer [muodosta-raportti muodosta-valtakunnallinen-vertailuraportti muodosta-yhteenveto]]
            [aipal.toimiala.raportti.kyselyraportointi :refer [paivita-parametrit]]
            [aipal.toimiala.raportti.raportointi :refer [ei-riittavasti-vastaajia muodosta-csv muodosta-tyhja-csv]]
            [aipal.toimiala.raportti.taustakysymykset :as taustakysymykset]
            [aipal.toimiala.raportti.yhdistaminen :as yhdistaminen]
            [arvo.toimiala.raportti.csv :refer [kysely-csv kysely-csv-vastauksittain vastaajatunnus-csv kohteet-csv vastanneet-csv]]
            [oph.common.util.http-util :refer [csv-download-response response-or-404]]))

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
        tekstit-sv (i18n/hae-tekstit "sv")
        tekstit-en (i18n/hae-tekstit "en")]
    (-> valtakunnallinen-raportti
      (assoc :nimi_fi (get-in tekstit-fi [:yleiset :valtakunnallinen]))
      (assoc :nimi_sv (get-in tekstit-sv [:yleiset :valtakunnallinen]))
      (assoc :nimi_en (get-in tekstit-en [:yleiset :valtakunnallinen])))))

(defn ^:private muodosta-raportit-parametreilla [kyselyid parametrit]
  (if (empty? (:tutkinnot parametrit))
    (let [raportti (muodosta-kyselyn-raportti-parametreilla kyselyid parametrit)
          valtakunnallinen-raportti (some-> (muodosta-valtakunnallinen-vertailuraportti kyselyid parametrit)    ;; Suurin osa ajasta kuluu täällä
                                      (lisaa-raporttiin-nimi)
                                      (valitse-valtakunnalliseen-kyselyn-kysymysryhmat raportti))]
      [raportti valtakunnallinen-raportti])
    (muodosta-kyselyn-tutkintojen-raportit-parametreilla kyselyid parametrit)))

(defn muodosta-kyselyraportti [kyselyid parametrit asetukset]
  (let [kysely-tyyppi (:tyyppi (db/hae-kysely-tyyppi {:kyselyid kyselyid}))
        raportit (muodosta-raportit-parametreilla kyselyid parametrit)
        kaikki-raportit (for [raportti raportit
                              :when raportti]
                          (ei-riittavasti-vastaajia raportti asetukset kysely-tyyppi))
        naytettavat (filter (comp nil? :virhe) kaikki-raportit)
        virheelliset (filter :virhe kaikki-raportit)
        yhteenveto (muodosta-yhteenveto kyselyid (paivita-parametrit parametrit))]
    (merge (when (seq naytettavat)
             (yhdistaminen/yhdista-raportit naytettavat true))
           (when (seq naytettavat)
             {:yhteenveto yhteenveto})
           {:raportoitavia (count naytettavat)
            :virheelliset virheelliset})))

(defn reitit [asetukset]
  (POST "/:kyselyid" []
    :path-params [kyselyid :- s/Int]
    :body [parametrit s/Any]
    :kayttooikeus [:katselu kyselyid]
    (response-or-404 (muodosta-kyselyraportti kyselyid (yhteinen/korjaa-numero-avaimet parametrit) asetukset))))

(defn get-csv-name
  ([csv-data modifier]
   (str (:nimi csv-data) "-" (:date csv-data) "-" (:koulutustoimija csv-data) (when modifier (str "-" modifier))".csv"))
  ([csv-data]
   (get-csv-name csv-data nil)))

(defroutes csv
  (GET "/kysely/:kyselyid" []
       :path-params [kyselyid :- s/Int]
       :query-params [{lang :- s/Str "fi"}]
       :kayttooikeus [:kysely {:kyselyid kyselyid}]
       (let [csv-data (kysely-csv kyselyid (keyword lang))]
         (csv-download-response (apply str (:csv csv-data)) (get-csv-name csv-data))))

  (GET "/kysely/vastauksittain/:kyselyid" []
       :path-params [kyselyid :- s/Int]
       :query-params [{lang :- s/Str "fi"}]
       :kayttooikeus [:kysely {:kyselyid kyselyid}]
       (let [csv-data (kysely-csv-vastauksittain kyselyid (keyword lang))]
         (csv-download-response (apply str (:csv csv-data)) (get-csv-name csv-data))))
  (GET "/kysely/kohteet/:kyselyid" []
       :path-params [kyselyid :- s/Int]
       :query-params [{lang :- s/Str "fi"}]
       :kayttooikeus [:kysely {:kyselyid kyselyid}]
       (let [csv-data (kohteet-csv kyselyid (keyword lang))]
         (csv-download-response csv-data (str "Kohteet -" kyselyid ".csv"))))
  (GET "/kysely/vastaajat/:kyselyid" []
       :path-params [kyselyid :- s/Int]
       :query-params [{lang :- s/Str "fi"}]
       :kayttooikeus [:kysely {:kyselyid kyselyid}]
       (let [csv-data (vastanneet-csv kyselyid (keyword lang))]
         (csv-download-response csv-data (str "Vastaajat -"kyselyid ".csv"))))

  (GET "/vastaajatunnus/:kyselykertaid" []
       :path-params [kyselykertaid :- s/Int]
       :query-params [{lang :- s/Str "fi"}]
       :kayttooikeus :katselu
       (let [csv-data (vastaajatunnus-csv kyselykertaid (keyword lang))]
         (csv-download-response csv-data (str "vastaajatunnukset -" kyselykertaid ".csv")))))

(defn csv-reitit [asetukset]
  (yhteinen/wrap-muunna-raportti-json-param
    (GET "/:kyselyid/csv" []
      :path-params [kyselyid :- s/Int]
      :query-params [parametrit]
      :kayttooikeus [:katselu {:kyselyid kyselyid}]
      (let [vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)
            raportit (muodosta-raportit-parametreilla kyselyid parametrit)
            kieli (:kieli parametrit)]
        (csv-download-response
          (apply str
                 (for [raportti raportit]
                   (if (>= (:vastaajien_lukumaara raportti) vaaditut-vastaajat)
                     (muodosta-csv raportti kieli)
                     (muodosta-tyhja-csv raportti kieli))))
          "kysely.csv")))))
