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

(ns aipal.integraatio.koodistopalvelu
 (:require [clj-time.core :as time]
           [aipal.arkisto.tutkinto :as tutkinto-arkisto]
           [aipal.arkisto.koulutusala :as koulutusala-arkisto]
           [aipal.arkisto.opintoala :as opintoala-arkisto]
           [clojure.set :refer [intersection difference rename-keys]]
           [oph.common.util.util :refer :all]
           [clojure.tools.logging :as log]
           [korma.db :as db]))

;; Tässä nimiavaruudessa viitataan "koodi"-sanalla koodistopalvelun palauttamaan tietorakenteeseen.
;; Jos koodi on muutettu Aipalin käyttämään muotoon, siihen viitataan ko. käsitteen nimellä, esim. "osatutkinto".

(defn koodi->kasite
  "Muuttaa koodistopalvelun koodin ohjelmassa käytettyyn muotoon.
Koodin arvo laitetaan arvokentta-avaimen alle."
  [koodi arvokentta]
  (when koodi
    (let [metadata_fi (first (filter #(= "FI" (:kieli %)) (:metadata koodi)))
          metadata_sv (first (filter #(= "SV" (:kieli %)) (:metadata koodi)))]
      {:nimi_fi (:nimi metadata_fi)
       :nimi_sv (:nimi metadata_sv)
       :voimassa_alkupvm (some-> (:voimassaAlkuPvm koodi) parse-ymd)
       :voimassa_loppupvm (some-> (:voimassaLoppuPvm koodi) parse-ymd)
       :koodiUri (:koodiUri koodi)
       arvokentta (:koodiArvo koodi)})))

(defn koodi->tutkinto [koodi]
  (koodi->kasite koodi :tutkintotunnus))

(defn koodi->koulutusala [koodi]
  (koodi->kasite koodi :koulutusalatunnus))

(defn koodi->opintoala [koodi]
  (koodi->kasite koodi :opintoalatunnus))

; https://virkailija.opintopolku.fi/koodisto-service/rest/json/koulutusalaoph2002/koodi?koodistoVersio=1
(defn ^:private hae-koodit
  "Hakee kaikki koodit annetusta koodistosta ja asettaa koodin koodiarvon avaimeksi arvokentta"
  ([asetukset koodisto] (get-json-from-url (str (:url asetukset) koodisto "/koodi")))
  ([asetukset koodisto versio] (get-json-from-url (str (:url asetukset) koodisto "/koodi?koodistoVersio=" versio))))

(defn kuuluu-koodistoon
  "Filtteröi koodilistasta annetun koodiston koodit"
  [koodisto]
  (fn [koodi]
    (= koodisto (get-in koodi [:koodisto :koodistoUri]))))

(defn ^:private opintoala-koodi?
  [koodi]
  ((kuuluu-koodistoon "opintoalaoph2002") koodi))

(defn ^:private koulutusala-koodi?
  [koodi]
  ((kuuluu-koodistoon "koulutusalaoph2002") koodi))

(defn ^:private tyyppikoodi?
  [koodi]
  ((kuuluu-koodistoon "tutkintotyyppi") koodi))

(defn ^:private tutkintotyyppikoodi?
  [koodi]
  ((kuuluu-koodistoon "koulutustyyppi") koodi))

(defn ^:private tutkintotyyppikoodi->tutkintotyyppi
  [koodi]
  (when koodi
    (case (:koodiArvo koodi)
      "12" "erikoisammattitutkinto"
      "11" "ammattitutkinto"
      "13" "perustutkinto"
      "4" "perustutkinto"
      "1" "perustutkinto"
      nil)))

(defn koodiston-uusin-versio
  [asetukset koodisto]
  (loop [versio nil]
     (when-let [json (get-json-from-url (str (:url asetukset)
                                             koodisto
                                             (when versio (str "?koodistoVersio=" versio))))]
       (if (= "HYVAKSYTTY" (:tila json))
         (:versio json)
         (recur (dec (:versio json)))))))

(defn ^:private lisaa-opintoalaan-koulutusala
  [asetukset opintoala]
  (let [ylakoodit (get-json-from-url (str (:url asetukset) "relaatio/sisaltyy-ylakoodit/" (:koodiUri opintoala)))
        koulutusala (some-value koulutusala-koodi? ylakoodit)]
    (assoc opintoala :koulutusala (:koodiArvo koulutusala))))

(defn ^:private hae-alakoodit
  [asetukset koodi] (get-json-from-url (str (:url asetukset) "relaatio/sisaltyy-alakoodit/" (:koodiUri koodi))))

(defn lisaa-alakoodien-data
  [asetukset tutkinto]
  (let [alakoodit (hae-alakoodit asetukset tutkinto)
        opintoala (some-value opintoala-koodi? alakoodit)
        tyyppi (some-value tyyppikoodi? alakoodit)
        tutkintotyyppi (some-value tutkintotyyppikoodi? alakoodit)]
    (assoc tutkinto
           :opintoala (:koodiArvo opintoala)
           :tyyppi (:koodiArvo tyyppi)
           :tutkintotyyppi (tutkintotyyppikoodi->tutkintotyyppi tutkintotyyppi))))

(defn hae-koodisto
  [asetukset koodisto versio]
  (koodi->kasite (get-json-from-url (str (:url asetukset) koodisto "?koodistoVersio=" versio)) :koodisto))

(defn hae-tutkinnot
  [asetukset]
  (let [koodistoversio (koodiston-uusin-versio asetukset "koulutus")]
    (map koodi->tutkinto (hae-koodit asetukset "koulutus" koodistoversio))))

(defn hae-koulutusalat
  [asetukset]
  (->> (hae-koodit asetukset "koulutusalaoph2002")
    (map koodi->koulutusala)
    (map #(dissoc % :kuvaus_fi :kuvaus_sv))))

(defn hae-opintoalat
  [asetukset]
  (->> (hae-koodit asetukset "opintoalaoph2002")
    (map koodi->opintoala)
    (map (partial lisaa-opintoalaan-koulutusala asetukset))
    (map #(dissoc % :kuvaus_fi :kuvaus_sv))))

(defn muutokset
  [uusi vanha]
  (into {}
        (for [[avain [uusi-arvo vanha-arvo :as diff]] (diff-maps uusi vanha)
              :when diff]
          [avain (cond
                   (or (nil? vanha-arvo) (nil? uusi-arvo)) diff
                   (map? uusi-arvo) (diff-maps uusi-arvo vanha-arvo)
                   :else diff)])))

(defn hae-tutkinto-muutokset
  [asetukset]
  (let [tutkinto-kentat [:nimi_fi :nimi_sv :voimassa_alkupvm :voimassa_loppupvm :tutkintotunnus :opintoala :tutkintotyyppi]
        vanhat (into {} (for [tutkinto (tutkinto-arkisto/hae-kaikki)]
                          [(:tutkintotunnus tutkinto) (select-keys tutkinto tutkinto-kentat)]))
        uudet (->> (hae-tutkinnot asetukset)
                (map (partial lisaa-alakoodien-data asetukset))
                (filter (comp #{"02" "03"} :tyyppi))
                (map #(select-keys % tutkinto-kentat))
                (map-by :tutkintotunnus))]
    (muutokset uudet vanhat)))

(defn hae-koulutusala-muutokset
  [asetukset]
  (let [koulutusala-kentat [:nimi_fi :nimi_sv :koulutusalatunnus]
        vanhat (into {} (for [koulutusala (koulutusala-arkisto/hae-kaikki)]
                          [(:koulutusalatunnus koulutusala) (select-keys koulutusala koulutusala-kentat)]))
        uudet (map-by :koulutusalatunnus
                      (map #(select-keys % koulutusala-kentat) (hae-koulutusalat asetukset)))]
    (muutokset uudet vanhat)))

(defn hae-opintoala-muutokset
  [asetukset]
  (let [opintoala-kentat [:nimi_fi :nimi_sv :opintoalatunnus :koulutusala]
        vanhat (into {} (for [opintoala (opintoala-arkisto/hae-kaikki)]
                          [(:opintoalatunnus opintoala) (select-keys opintoala opintoala-kentat)]))
        uudet (map-by :opintoalatunnus
                      (map #(select-keys % opintoala-kentat) (hae-opintoalat asetukset)))]
    (muutokset uudet vanhat)))

(defn uusi
  "Jos muutos on uuden tiedon lisääminen, palauttaa uudet tiedot, muuten nil"
  [[tutkintotunnus muutos]]
  (when (vector? muutos)
    (assoc (first muutos) :tutkintotunnus tutkintotunnus)))

(defn uudet-arvot [muutos]
  (into {}
        (for [[k v] muutos
             :when v]
         [k (first v)])))

(defn muuttunut
  "Jos muutos on tietojen muuttuminen, palauttaa muuttuneet tiedot, muuten nil"
  [[tutkintotunnus muutos]]
  (when (and (map? muutos)
             (not-every? nil? (vals muutos)))
    (merge {:tutkintotunnus tutkintotunnus}
          (uudet-arvot muutos))))

(defn ^:integration-api tallenna-uudet-koulutusalat! [koulutusalat]
  (doseq [ala koulutusalat]
    (log/info "Lisätään koulutusala " (:koulutusalatunnus ala))
    (koulutusala-arkisto/lisaa! ala)))

(defn ^:integration-api tallenna-muuttuneet-koulutusalat! [koulutusalat]
  (doseq [ala koulutusalat
          :let [tunnus (:koulutusalatunnus ala)
                ala (dissoc ala :koulutusalatunnus)]]
    (log/info "Päivitetään koulutusala " tunnus ", muutokset: " ala)
    (koulutusala-arkisto/paivita! tunnus ala)))

(defn ^:integration-api tallenna-koulutusalat! [koulutusalat]
  (let [uudet (for [[alakoodi ala] koulutusalat
                    :when (and (vector? ala) (first ala))]
                (first ala))
        muuttuneet (for [[alakoodi ala] koulutusalat
                         :when (map? ala)]
                     (assoc (uudet-arvot ala) :koulutusalatunnus alakoodi))]
    (tallenna-uudet-koulutusalat! uudet)
    (tallenna-muuttuneet-koulutusalat! muuttuneet)))

(defn ^:integration-api tallenna-uudet-opintoalat! [opintoalat]
  (doseq [ala opintoalat]
    (log/info "Lisätään opintoala " (:opintoalatunnus ala))
    (opintoala-arkisto/lisaa! ala)))

(defn ^:integration-api tallenna-muuttuneet-opintoalat! [opintoalat]
  (doseq [ala opintoalat
          :let [tunnus (:opintoalatunnus ala)
                ala (dissoc ala :opintoalatunnus)]]
    (log/info "Päivitetään opintoala " tunnus ", muutokset: " ala)
    (opintoala-arkisto/paivita! tunnus ala)))

(defn ^:integration-api tallenna-opintoalat! [opintoalat]
  (let [uudet (for [[alakoodi ala] opintoalat
                    :when (and (vector? ala) (first ala))]
                (first ala))
        muuttuneet (for [[alakoodi ala] opintoalat
                         :when (map? ala)]
                     (assoc (uudet-arvot ala) :opintoalatunnus alakoodi))]
    (tallenna-uudet-opintoalat! (filter :koulutusala uudet))
    (tallenna-muuttuneet-opintoalat! muuttuneet)))

(defn ^:integration-api tallenna-uudet-tutkinnot! [tutkinnot]
  (doseq [tutkinto tutkinnot]
    (log/info "Lisätään tutkinto " (:tutkintotunnus tutkinto))
    (tutkinto-arkisto/lisaa! tutkinto)))

(defn ^:integration-api tallenna-muuttuneet-tutkinnot! [tutkinnot]
  (doseq [tutkinto tutkinnot
          :let [tunnus (:tutkintotunnus tutkinto)
                tutkinto (dissoc tutkinto :tutkintotunnus)]]
    (log/info "Päivitetään tutkinto " tunnus ", muutokset: " tutkinto)
    (tutkinto-arkisto/paivita! tunnus tutkinto)))

(defn ^:integration-api tallenna-tutkinnot! [tutkinnot]
  (let [uudet (for [[tunnus tutkinto] tutkinnot
                    :when (and (vector? tutkinto) (first tutkinto))]
                (first tutkinto))
        muuttuneet (for [[tunnus tutkinto] tutkinnot
                         :when (map? tutkinto)]
                     (assoc (uudet-arvot tutkinto) :tutkintotunnus tunnus))]
    (tallenna-uudet-tutkinnot! (filter :opintoala uudet))
    (tallenna-muuttuneet-tutkinnot! muuttuneet)))

(defn ^:integration-api paivita-tutkinnot! [asetukset]
  (try
    (db/transaction
      (log/info "Aloitetaan tutkintojen päivitys koodistopalvelusta")
      (tallenna-koulutusalat! (hae-koulutusala-muutokset asetukset))
      (tallenna-opintoalat! (hae-opintoala-muutokset asetukset))
      (tallenna-tutkinnot! (hae-tutkinto-muutokset asetukset))
      (log/info "Tutkintojen päivitys koodistopalvelusta valmis"))
    (catch org.postgresql.util.PSQLException e
      (log/error e "Tutkintojen päivitys koodistopalvelusta epäonnistui"))))

