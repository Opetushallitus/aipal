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
  (:require [aipal.arkisto.tutkinto :as tutkinto-arkisto]
            [aipal.arkisto.tutkintotyyppi :as tutkintotyyppi-arkisto]
            [aipal.arkisto.koulutusala :as koulutusala-arkisto]
            [aipal.arkisto.opintoala :as opintoala-arkisto]
            [clojure.set :refer [intersection difference rename-keys]]
            [oph.common.util.util :refer :all]
            [clojure.tools.logging :as log]
            korma.db
            [arvo.db.core :refer [*db*] :as db]
            [clojure.set :as set]
            [clj-time.coerce :as c]))

;  "koulutusalaoph2002"
(def ^:private koulutusala-koodisto "isced2011koulutusalataso1")

; "opintoalaoph2002"
(def ^:private opintoala-koodisto "isced2011koulutusalataso2")

;; Tässä nimiavaruudessa viitataan "koodi"-sanalla koodistopalvelun palauttamaan tietorakenteeseen.
;; Jos koodi on muutettu Aipalin käyttämään muotoon, siihen viitataan ko. käsitteen nimellä, esim. "osatutkinto".

(defn koodi->kasite
  "Muuttaa koodistopalvelun koodin ohjelmassa käytettyyn muotoon.
Koodin arvo laitetaan arvokentta-avaimen alle."
  [koodi arvokentta]
  (when koodi
    (let [metadata_fi (first (filter #(= "FI" (:kieli %)) (:metadata koodi)))
          metadata_sv (first (filter #(= "SV" (:kieli %)) (:metadata koodi)))
          metadata_en (first (filter #(= "EN" (:kieli %)) (:metadata koodi)))]
      {:nimi_fi (:nimi metadata_fi)
       :nimi_sv (:nimi metadata_sv)
       :nimi_en (:nimi metadata_en)
       :voimassa_alkupvm (some-> (:voimassaAlkuPvm koodi) parse-ymd)
       :voimassa_loppupvm (some-> (:voimassaLoppuPvm koodi) parse-ymd)
       :koodiUri (:koodiUri koodi)
       arvokentta (:koodiArvo koodi)})))

(defn koodi->tutkintotyyppi [koodi]
  (koodi->kasite koodi :tutkintotyyppi))

(defn koodi->tutkinto [koodi]
  (koodi->kasite koodi :tutkintotunnus))

(defn koodi->koulutusala [koodi]
  (koodi->kasite koodi :koulutusalatunnus))

(defn koodi->opintoala [koodi]
  (koodi->kasite koodi :opintoalatunnus))

(defn koodi->koodi [koodi]
  (koodi->kasite koodi :koodi_arvo))

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
  ((kuuluu-koodistoon opintoala-koodisto) koodi))

(defn ^:private koulutusala-koodi?
  [koodi]
  ((kuuluu-koodistoon koulutusala-koodisto) koodi))

(defn ^:private tyyppikoodi?
  [koodi]
  ((kuuluu-koodistoon "tutkintotyyppi") koodi))

(defn uusin-hyvaksytty-koodisto
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
        tyyppi (some-value tyyppikoodi? alakoodit)]
    (assoc tutkinto
           :opintoala (:koodiArvo opintoala)
           :tutkintotyyppi (:koodiArvo tyyppi))))

(defn hae-tutkintotyypit [asetukset]
  (map koodi->tutkintotyyppi (hae-koodit asetukset "tutkintotyyppi")))

(defn hae-tutkinnot [asetukset]
  (map koodi->tutkinto (hae-koodit asetukset "koulutus")))

(defn hae-koulutusalat
  [asetukset]
  (->> (hae-koodit asetukset koulutusala-koodisto)
    (map koodi->koulutusala)
    (map #(dissoc % :kuvaus_fi :kuvaus_sv :kuvaus_en))))

(defn hae-opintoalat
  [asetukset]
  (->> (hae-koodit asetukset opintoala-koodisto)
    (map koodi->opintoala)
    (map (partial lisaa-opintoalaan-koulutusala asetukset))
    (map #(dissoc % :kuvaus_fi :kuvaus_sv :kuvaus_en))))

; TODO pitääkö hakea kannasta?
(defn hae-kunnat [asetukset]
  (->> (hae-koodit asetukset "kunta")
       (map #(koodi->kasite % :kuntakoodi))))

(defn in? [coll elem]
  (some #(= elem %) coll))

(defn hae-muuttuneet
  "Lajittelee koodit lisättäviin ja päivitettäviin. Ei tutki löytyykö ennestään samoilla tiedoilla."
  [uudet vanhat tunniste]
  (let [muuttuneet (set/difference (into #{} uudet) (into #{} vanhat))
        vanhat-idt (map tunniste vanhat)
        lisattavat (remove #(in? vanhat-idt (tunniste %)) muuttuneet)]
    {:lisattavat    lisattavat
     :paivitettavat (set/difference muuttuneet lisattavat)}))

(defn hae-tutkintotyyppi-muutokset [asetukset]
  (let [tutkintotyypi_kentat [:tutkintotyyppi :nimi_fi :nimi_sv :nimi_en]
        vanhat (->> (tutkintotyyppi-arkisto/hae-kaikki)
                    (map #(select-keys % tutkintotyypi_kentat)))
        uudet (map #(select-keys % tutkintotyypi_kentat) (hae-tutkintotyypit asetukset))]
    (hae-muuttuneet uudet vanhat :tutkintotyyppi)))

(defn hae-tutkinto-muutokset [asetukset]
  (let [tutkinto-kentat [:nimi_fi :nimi_sv :nimi_en :voimassa_alkupvm :voimassa_loppupvm :tutkintotunnus :opintoala :tutkintotyyppi]
        vanhat (->> (tutkinto-arkisto/hae-kaikki)
                    (map #(select-keys % tutkinto-kentat)))
        uudet (->> (hae-tutkinnot asetukset)
                   (map (partial lisaa-alakoodien-data asetukset))
                   (map #(select-keys % tutkinto-kentat))
                   (map #(update % :voimassa_alkupvm c/to-date))
                   (map #(update % :voimassa_loppupvm c/to-date)))]
    (hae-muuttuneet uudet vanhat :tutkintotunnus)))

(defn hae-koulutusala-muutokset [asetukset]
  (let [koulutusala-kentat [:nimi_fi :nimi_sv :nimi_en :koulutusalatunnus]
        vanhat (->> (koulutusala-arkisto/hae-kaikki)
                    (map #(select-keys % koulutusala-kentat)))
        uudet (map #(select-keys % koulutusala-kentat) (hae-koulutusalat asetukset))]
    (hae-muuttuneet uudet vanhat :koulutusalatunnus)))

(defn hae-opintoala-muutokset [asetukset]
  (let [opintoala-kentat [:nimi_fi :nimi_sv :nimi_en :opintoalatunnus :koulutusala]
        vanhat (->> (opintoala-arkisto/hae-kaikki)
                    (map #(select-keys % opintoala-kentat)))
        uudet (map #(select-keys % opintoala-kentat) (hae-opintoalat asetukset))]
    (hae-muuttuneet uudet vanhat :opintoalatunnus)))

(defn ^:integration-api tallenna-uudet-koulutusalat! [koulutusalat]
  (doseq [ala koulutusalat]
    (log/info "Lisätään koulutusala " (:koulutusalatunnus ala))
    (koulutusala-arkisto/lisaa! ala)))

(defn ^:integration-api tallenna-muuttuneet-koulutusalat! [koulutusalat]
  (doseq [ala koulutusalat]
    (log/info "Päivitetään koulutusala " (:koulutusalatunnus ala))
    (koulutusala-arkisto/paivita! ala)))

(defn ^:integration-api tallenna-koulutusalat! [koulutusalat]
    (tallenna-uudet-koulutusalat! (:lisattavat koulutusalat))
    (tallenna-muuttuneet-koulutusalat! (:paivitettavat koulutusalat)))

(defn ^:integration-api tallenna-uudet-opintoalat! [opintoalat]
  (doseq [ala opintoalat]
    (log/info "Lisätään opintoala " (:opintoalatunnus ala))
    (opintoala-arkisto/lisaa! ala)))

(defn ^:integration-api tallenna-muuttuneet-opintoalat! [opintoalat]
  (doseq [ala opintoalat]
    (log/info "Päivitetään opintoala " (:opintoalatunnus ala))
    (opintoala-arkisto/paivita! ala)))

(defn ^:integration-api tallenna-opintoalat! [opintoalat]
  (tallenna-uudet-opintoalat! (filter :koulutusala (:lisattavat opintoalat)))
  (tallenna-muuttuneet-opintoalat! (filter :koulutusala (:paivitettavat opintoalat))))

(defn ^:integration-api tallenna-uudet-tutkinnot! [tutkinnot]
  (doseq [tutkinto tutkinnot]
    (log/info "Lisätään tutkinto " (:tutkintotunnus tutkinto))
    (tutkinto-arkisto/lisaa! tutkinto)))

(defn ^:integration-api tallenna-muuttuneet-tutkinnot! [tutkinnot]
  (doseq [tutkinto tutkinnot]
    (log/info "Päivitetään tutkinto " (:tutkintotunnus tutkinto))
    (tutkinto-arkisto/paivita! tutkinto)))

(defn logita-puutteelliset-tutkinnot [tutkinnot]
  (log/info "Uudet tutkinnot ilman opintoalaa " (map :tutkintotunnus (filter #(nil? (:opintoala %))(:lisattavat tutkinnot))))
  (log/info "Muuttuneet tutkinnot ilman opintoalaa " (map :tutkintotunnus (filter #(nil? (:opintoala %))(:paivitettavat tutkinnot)))))

(defn ^:integration-api tallenna-tutkinnot! [tutkinnot]
  (logita-puutteelliset-tutkinnot tutkinnot)
  (tallenna-uudet-tutkinnot! (:lisattavat tutkinnot))
  (tallenna-muuttuneet-tutkinnot! (:paivitettavat tutkinnot)))

(defn ^:integration-api tallenna-uudet-tutkintotyypit! [tutkintotyypit]
  (doseq [tutkintotyyppi tutkintotyypit]
    (log/info "Lisätään tutkintotyyppi" (:tutkintotyyppi tutkintotyyppi))
    (tutkintotyyppi-arkisto/lisaa! tutkintotyyppi)))

(defn ^:integration-api tallenna-muuttuneet-tutkintotyypit! [tutkintotyypit]
  (doseq [tutkintotyyppi tutkintotyypit]
    (log/info "Päivitetään tutkintotyyppi" (:tutkintotyyppi tutkintotyyppi))
    (tutkintotyyppi-arkisto/paivita! tutkintotyyppi)))

(defn ^:integration-api tallenna-tutkintotyypit! [tutkintotyypit]
    (tallenna-uudet-tutkintotyypit! (filter :tutkintotyyppi (:lisattavat tutkintotyypit)))
    (tallenna-muuttuneet-tutkintotyypit! (filter :tutkintotyyppi (:paivitettavat tutkintotyypit))))

(defn ^:integration-api tallenna-uudet-koodisto! [uudet-koodit koodisto-uri]
  (doseq [koodi uudet-koodit]
;    (log/info "lisätään koodia" (assoc koodi :koodisto_uri koodisto-uri))
    (db/lisaa-koodiston-koodi! (assoc koodi :koodisto_uri koodisto-uri)))
  (log/info (count uudet-koodit) "uutta tallennettiin"))

(defn ^:integration-api tallenna-muuttuneet-koodisto! [muuttuneet-koodit koodisto-uri]
  (doseq [koodi muuttuneet-koodit]
;    (log/info "lisätään koodia" (assoc koodi :koodisto_uri koodisto-uri))
    (db/paivita-koodiston-koodi! (assoc koodi :koodisto_uri koodisto-uri)))
  (log/info (count muuttuneet-koodit) "muutosta tallenttiin"))

(defn ^:integration-api tallenna-koodisto! [koodistomuutokset koodisto-uri]
  (tallenna-uudet-koodisto! (filter :koodi_arvo (:lisattavat koodistomuutokset)) koodisto-uri)
  (tallenna-muuttuneet-koodisto! (filter :koodi_arvo (:paivitettavat koodistomuutokset)) koodisto-uri))

(defn hae-koodisto-muutokset [koodisto-uri asetukset]
  (let [vanhat (db/hae-koodiston-koodit {:koodistouri koodisto-uri})
        uudet (map koodi->koodi (hae-koodit asetukset koodisto-uri))
        muuttuneet (hae-muuttuneet uudet vanhat :koodi_arvo)]
    (log/info (count vanhat) "vanhaa" (count uudet) "uutta")
    (log/info "lisattavia" (count (:lisattavat muuttuneet)) "paivitettavia" (count (:paivitettavat muuttuneet)))
    muuttuneet))

(defn paivita-koodisto! [asetukset koodisto-uri]
  (log/info "Päivitetään koodisto" koodisto-uri)
  (-> koodisto-uri
      (hae-koodisto-muutokset asetukset)
      (tallenna-koodisto! koodisto-uri)))

(defn ^:integration-api paivita-koodistot! [asetukset]
  (try
    (korma.db/transaction
      (log/info "Aloitetaan tutkintojen päivitys koodistopalvelusta")
      (tallenna-tutkintotyypit! (hae-tutkintotyyppi-muutokset asetukset))
      (tallenna-koulutusalat! (hae-koulutusala-muutokset asetukset))
      (tallenna-opintoalat! (hae-opintoala-muutokset asetukset))
      (tallenna-tutkinnot! (hae-tutkinto-muutokset asetukset))
      (log/info "Tutkintojen päivitys koodistopalvelusta valmis"))
    (korma.db/transaction
     (log/info "Päivitetään muut koodistot")
     (paivita-koodisto! asetukset "maatjavaltiot2")
     (paivita-koodisto! asetukset "kunta")
     (log/info "Muiden koodistojen päivitys valmis"))
    (catch org.postgresql.util.PSQLException e
      (log/error e "Tutkintojen päivitys koodistopalvelusta epäonnistui"))))

