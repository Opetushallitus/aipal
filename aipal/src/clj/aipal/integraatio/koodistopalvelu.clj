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
 (:require [clj-time.format :as time]
           [aipal.arkisto.tutkinto :as tutkinto-arkisto]
           [aipal.arkisto.koulutusala :as koulutusala-arkisto]
           [aipal.arkisto.opintoala :as opintoala-arkisto]
           [clojure.set :refer [intersection difference rename-keys]]
           [oph.common.util.util :refer :all]))

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
       :koodiUri (:koodiUri koodi)
       arvokentta (:koodiArvo koodi)})))

(defn koodi->tutkinto [koodi]
  (koodi->kasite koodi :tutkintotunnus))

(defn koodi->koulutusala [koodi]
  (koodi->kasite koodi :koulutusalatunnus))

(defn koodi->opintoala [koodi]
  (koodi->kasite koodi :opintoalatunnus))

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

(defn ^:private tutkintotyyppi-koodi?
  [koodi]
  ((kuuluu-koodistoon "tutkintotyyppi") koodi))

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

(defn lisaa-opintoala-koulutusala-tyyppi
  [asetukset tutkinto]
  (let [alakoodit (hae-alakoodit asetukset tutkinto)
        opintoala (some-value opintoala-koodi? alakoodit)
        koulutusala (some-value koulutusala-koodi? alakoodit)
        tyyppi (some-value tutkintotyyppi-koodi? alakoodit)]
    (merge tutkinto
           {:opintoala (:koodiArvo opintoala)
            :koulutusala (:koodiArvo koulutusala)
            :tyyppi (:koodiArvo tyyppi)})))

(defn hae-koodisto
  [asetukset koodisto versio]
  (koodi->kasite (get-json-from-url (str (:url asetukset) koodisto "?koodistoVersio=" versio)) :koodisto))

(defn hae-tutkinnot
  [asetukset]
  (let [koodistoversio (koodiston-uusin-versio asetukset "koulutus")]
    (map koodi->tutkinto (hae-koodit asetukset "koulutus" koodistoversio))))

(defn hae-koulutusalat
  [asetukset]
  (let [koodistoversio (koodiston-uusin-versio asetukset "koulutusalaoph2002")]
    (->> (hae-koodit asetukset "koulutusalaoph2002" koodistoversio)
      (map koodi->koulutusala)
      (map #(dissoc % :kuvaus_fi :kuvaus_sv)))))

(defn hae-opintoalat
  [asetukset]
  (let [koodistoversio (koodiston-uusin-versio asetukset "opintoalaoph2002")]
    (->> (hae-koodit asetukset "opintoalaoph2002" koodistoversio)
      (map koodi->opintoala)
      (map (partial lisaa-opintoalaan-koulutusala asetukset))
      (map #(dissoc % :kuvaus_fi :kuvaus_sv)))))

(defn tutkintorakenne
  "Lukee koko tutkintorakenteen koodistosta. Suoritus kestää n. 8 minuuttia ja aiheuttaa
tuhansia http-pyyntöjä koodistopalveluun.

Palautuva tutkintorakenne on lista koulutusaloista, joista jokainen
sisältää listan siihen kuuluvista opintoaloista, joista jokainen
sisältää listan siihen kuuluvista tutkinnoista, joista jokainen
sisältää listat siihen kuuluvista osaamisaloista ja tutkinnonosista."
   [asetukset]
   (let [oa-tunnus->tutkinnot (group-by :opintoala
                                        (for [tutkinto (hae-tutkinnot asetukset)]
                                          (lisaa-opintoala-koulutusala-tyyppi asetukset tutkinto)))
         ;; Muodostetaan map opintoalatunnuksesta koulutusalatunnukseen.
         ;; Koulutusalatunnus saadaan mistä tahansa kyseisen opintoalan tutkinnosta.
         ;; Koodistopalvelussa ei ole relaatiota opintoala->koulutusala.
         oa-tunnus->ka-tunnus (into {} (for [[oa-tunnus tutkinnot] oa-tunnus->tutkinnot]
                                         {oa-tunnus (:koulutusala (first tutkinnot))}))
         ka-tunnus->opintoalat (group-by :koulutusala
                                         (for [opintoala (hae-opintoalat asetukset)
                                               :let [oa-tunnus (:opintoalatunnus opintoala)]]
                                           (assoc opintoala
                                                  :tutkinnot (oa-tunnus->tutkinnot oa-tunnus [])
                                                  :koulutusala (oa-tunnus->ka-tunnus oa-tunnus))))]
     (for [koulutusala (hae-koulutusalat asetukset)
           :let [ka-tunnus (:koulutusalatunnus koulutusala)]]
       (assoc koulutusala
              :opintoalat (ka-tunnus->opintoalat ka-tunnus [])))))

(defn muutokset
  [uusi vanha]
  (into {}
        (for [[avain [uusi-arvo vanha-arvo :as diff]] (diff-maps uusi vanha)
              :when diff]
          [avain (cond
                   (nil? uusi-arvo) diff
                   (nil? vanha-arvo) diff
                   (map? uusi-arvo) (diff-maps uusi-arvo vanha-arvo)
                   :else diff)])))

(defn hae-tutkinto-muutokset
  [asetukset]
  (let [vanhat (into {} (for [tutkinto (tutkinto-arkisto/hae-kaikki)]
                          [(:tutkintotunnus tutkinto) (select-keys tutkinto [:nimi_fi :nimi_sv :tutkintotunnus])]))
        uudet (->> (hae-tutkinnot asetukset)
                (map (partial lisaa-opintoala-koulutusala-tyyppi asetukset))
                (filter #(#{"02" "03"} (:tyyppi %)))
                (map #(dissoc % :koodiUri :tyyppi))
                (map-by :tutkintotunnus))]
    (muutokset uudet vanhat)))

(defn hae-koulutusala-muutokset
  [asetukset]
  (let [vanhat (into {} (for [koulutusala (koulutusala-arkisto/hae-kaikki)]
                          [(:koulutusalatunnus koulutusala) (select-keys koulutusala [:nimi_fi :nimi_sv :koulutusalatunnus])]))
        uudet (map-by :opintoalatunnus
                      (map #(dissoc % :koodiUri) (hae-koulutusalat asetukset)))]
    (muutokset uudet vanhat)))

(defn hae-opintoala-muutokset
  [asetukset]
  (let [vanhat (into {} (for [opintoala (opintoala-arkisto/hae-kaikki)]
                          [(:opintoalatunnus opintoala) (select-keys opintoala [:nimi_fi :nimi_sv :opintoalatunnus])]))
        uudet (map-by :opintoalatunnus
                      (map #(dissoc % :koodiUri) (hae-opintoalat asetukset)))]
    (muutokset uudet vanhat)))
