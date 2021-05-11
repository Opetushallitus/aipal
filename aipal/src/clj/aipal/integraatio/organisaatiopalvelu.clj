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

(ns aipal.integraatio.organisaatiopalvelu
  (:require [aipal.arkisto.koulutustoimija :as koulutustoimija-arkisto]
            [aipal.arkisto.oppilaitos :as oppilaitos-arkisto]
            [aipal.arkisto.toimipiste :as toimipiste-arkisto]
            [aipal.arkisto.organisaatiopalvelu :as organisaatiopalvelu-arkisto]
            [clj-time.core :as time]
            [clj-time.format :as f]
            [cheshire.core :as json]
            [clj-time.coerce :as time-coerce]
            [oph.common.util.util :refer [get-json-from-url post-json-from-url map-by some-value muutos]]
            [clojure.tools.logging :as log]
            [korma.db :as db]))

(defn halutut-kentat [koodi]
  (select-keys koodi [:nimi :oppilaitosTyyppiUri :postiosoite :yhteystiedot :virastoTunnus :ytunnus :oppilaitosKoodi :toimipistekoodi :oid :tyypit :parentOid :lakkautusPvm :kotipaikkaUri]))


(defn oppilaitostyyppi [oppilaitostyyppi_uri]
  (if oppilaitostyyppi_uri
    (let [matcher (re-matcher #"\d+" oppilaitostyyppi_uri)]
      (re-find matcher))))

(defn lisaa-oppilaitostyyppi [koodi]
  (assoc koodi :oppilaitostyyppi (oppilaitostyyppi (:oppilaitosTyyppiUri koodi))))

; Käytetyt tyypit: KOULUTUSTOIMIJA OPPILAITOS TOIMIPISTE https://github.com/Opetushallitus/organisaatio/blob/84d7f0822a76c802c9de30b1d1fb08b161164115/organisaatio-api/src/main/java/fi/vm/sade/organisaatio/api/model/types/OrganisaatioTyyppi.java
(defn hae-oidit-tyypilla [url tyyppi]
  (get-json-from-url url {:query-params {"searchTerms" (str "type=" tyyppi)}}))

(def organisaatiopalvelu-formatter (f/formatter "yyyy-MM-dd hh:mm"))

; Haetaan minuutin tarkkuudella niin voidaan tarvittaessa hakea useasti päivässä
(defn hae-muuttuneet [url viimeisin-paivitys]
;  Substracting one minute to be sure no data is omitted
  (let [org-aikaleima (f/unparse organisaatiopalvelu-formatter (time/minus viimeisin-paivitys (time/minutes 1)))]
    (log/info "Haetaan muuttuneet organisaatiot organisaatiopalvelusta" url "aikaleimalla" org-aikaleima)
    (map (comp lisaa-oppilaitostyyppi halutut-kentat)
         (get-json-from-url (str url "v4/muutetut") {:query-params {"lastModifiedSince" org-aikaleima}}))))

(defn hae-era [oid-era url]
  (log/info "Haetaan erä " (count oid-era) "kpl")
  (let [body (json/generate-string oid-era)
        full-url (str url "v4/findbyoids")]
    (map (comp lisaa-oppilaitostyyppi halutut-kentat)
         (post-json-from-url full-url {:body body :content-type "application/json"}))))

;; Koodistopalvelun oppilaitostyyppikoodistosta
(def ^:private halutut-tyypit
  #{"oppilaitostyyppi_21" ;; Ammatilliset oppilaitokset
    "oppilaitostyyppi_22" ;; Ammatilliset erityisoppilaitokset
    "oppilaitostyyppi_23" ;; Ammatilliset erikoisoppilaitokset
    "oppilaitostyyppi_24" ;; Ammatilliset aikuiskoulutuskeskukset
    "oppilaitostyyppi_41" ;; Ammattikorkeakoulut
    "oppilaitostyyppi_42" ;; Yliopistot
    "oppilaitostyyppi_43" ;; Sotilaskorkeakoulut
    "oppilaitostyyppi_61" ;; Musiikkioppilaitokset
    "oppilaitostyyppi_62" ;; Liikunnan koulutuskeskukset
    "oppilaitostyyppi_63" ;; Kansanopistot
    "oppilaitostyyppi_65" ;; Opintokeskukset
    "oppilaitostyyppi_93" ;; Muut koulutuksen järjestäjät
    "oppilaitostyyppi_99" ;; Muut oppilaitokset
    "oppilaitostyyppi_xx"}) ;; Tyyppi ei tiedossa

(defn ^:private haluttu-tyyppi? [koodi]
  (when-let [tyyppi (:oppilaitosTyyppiUri koodi)]
    (contains? halutut-tyypit (subs tyyppi 0 19))))

(defn ^:private kunta [koodi]
  (if (:kotipaikkaUri koodi)
    (clojure.string/replace (:kotipaikkaUri koodi) #"kunta_", "")
    ""))

(defn ^:private nimi [koodi]
  ((some-fn :fi :sv :en) (:nimi koodi)))

(defn ^:private nimi-sv [koodi]
  ((some-fn :sv :fi :en) (:nimi koodi)))

(defn ^:private nimi-en [koodi]
  ((some-fn :en :fi :sv) (:nimi koodi)))

(defn ^:private postinumero [koodi]
  (when-let [postinumerokoodi (get-in koodi [:postiosoite :postinumeroUri])]
    (subs postinumerokoodi 6)))

(defn ^:private email [koodi]
  (some :email (:yhteystiedot koodi)))

(defn ^:private www-osoite [koodi]
  (some :www (:yhteystiedot koodi)))

(defn ^:private puhelin [koodi]
  (:numero (some-value #(= "puhelin" (:tyyppi %)) (:yhteystiedot koodi))))

(defn ^:private y-tunnus [koodi]
  (or (:ytunnus koodi) (:virastoTunnus koodi)))

(defn ^:private voimassa? [koodi]
  (if-let [lakkautus-pvm (time-coerce/to-local-date (:lakkautusPvm koodi))]
    (not (time/before? lakkautus-pvm (time/today)))
    true))

(defn ^:private koodi->koulutustoimija [koodi]
  {:nimi_fi (nimi koodi)
   :nimi_sv (nimi-sv koodi)
   :nimi_en (nimi-en koodi)
   :oid (:oid koodi)
   :sahkoposti (email koodi)
   :puhelin (puhelin koodi)
   :osoite (get-in koodi [:postiosoite :osoite])
   :postinumero (postinumero koodi)
   :postitoimipaikka (get-in koodi [:postiosoite :postitoimipaikka])
   :www_osoite (www-osoite koodi)
   :ytunnus (y-tunnus koodi)
   :lakkautuspaiva (time-coerce/to-local-date (:lakkautusPvm koodi))
   :voimassa (voimassa? koodi)})

(defn ^:private koodi->oppilaitos [koodi]
  {:nimi_fi (nimi koodi)
   :nimi_sv (nimi-sv koodi)
   :nimi_en (nimi-en koodi)
   :oid (:oid koodi)
   :sahkoposti (email koodi)
   :puhelin (puhelin koodi)
   :osoite (get-in koodi [:postiosoite :osoite])
   :postinumero (postinumero koodi)
   :postitoimipaikka (get-in koodi [:postiosoite :postitoimipaikka])
   :www_osoite (www-osoite koodi)
   :oppilaitoskoodi (:oppilaitosKoodi koodi)
   :lakkautuspaiva (time-coerce/to-local-date (:lakkautusPvm koodi))
   :voimassa (voimassa? koodi)
   :oppilaitostyyppi (:oppilaitostyyppi koodi)})

(defn ^:private koodi->toimipiste [koodi]
  {:kunta (kunta koodi)
   :nimi_fi (nimi koodi)
   :nimi_sv (nimi-sv koodi)
   :nimi_en (nimi-en koodi)
   :oid (:oid koodi)
   :sahkoposti (email koodi)
   :puhelin (puhelin koodi)
   :osoite (get-in koodi [:postiosoite :osoite])
   :postinumero (postinumero koodi)
   :postitoimipaikka (get-in koodi [:postiosoite :postitoimipaikka])
   :www_osoite (www-osoite koodi)
   :toimipistekoodi (:toimipistekoodi koodi)
   :lakkautuspaiva (time-coerce/to-local-date (:lakkautusPvm koodi))
   :voimassa (voimassa? koodi)})

(def ^:private yhteiset-kentat [:nimi_fi :nimi_sv :nimi_en :oid :sahkoposti :puhelin :osoite
                                :postinumero :postitoimipaikka :www_osoite :voimassa :lakkautuspaiva])

(defn ^:private koulutustoimijan-kentat [koulutustoimija]
  (when koulutustoimija
    (select-keys koulutustoimija (conj yhteiset-kentat :ytunnus))))

(defn ^:private oppilaitoksen-kentat [oppilaitos]
  (when oppilaitos
    (select-keys oppilaitos (conj yhteiset-kentat :oppilaitoskoodi :koulutustoimija :oppilaitostyyppi))))

(defn ^:private toimipaikan-kentat [toimipiste]
  (when toimipiste
    (select-keys toimipiste (conj yhteiset-kentat :toimipistekoodi :oppilaitos))))

(defn ^:private tyyppi [koodi]
  (cond
    (some #{"organisaatiotyyppi_01"} (:tyypit koodi)) :koulutustoimija
    (haluttu-tyyppi? koodi) :oppilaitos
    (:toimipistekoodi koodi) :toimipiste))

(defn generoi-oid->y-tunnus [koulutustoimijat-oid->ytunnus oppilaitoskoodit]
  (loop [oid->ytunnus koulutustoimijat-oid->ytunnus
         oppilaitoskoodit oppilaitoskoodit]
    (let [uudet (for [o oppilaitoskoodit
                      :when (contains? oid->ytunnus (:parentOid o))]
                  [(:oid o) (oid->ytunnus (:parentOid o))])]
      (if (seq uudet)
        (recur (into oid->ytunnus uudet) (remove #(contains? oid->ytunnus (:parentOid %)) oppilaitoskoodit))
        (do
          (doseq [oppilaitos oppilaitoskoodit]
            (log/warn "Oppilaitos ilman parenttia:" (:oppilaitosKoodi oppilaitos)))
          oid->ytunnus)))))

(defn ^:integration-api paivita-koulutustoimijat! [koodit]
  (let [koulutustoimijat (->> (koulutustoimija-arkisto/hae-kaikki-organisaatiopalvelulle)
                           (map-by :ytunnus))]
    (doseq [koodi (vals (map-by y-tunnus koodit)) ;; Poistetaan duplikaatit
            :let [uusi-kt (koodi->koulutustoimija koodi)
                  y-tunnus (:ytunnus uusi-kt)
                  vanha-kt (koulutustoimijan-kentat (get koulutustoimijat y-tunnus))]
            :when y-tunnus]
      (cond
        (nil? vanha-kt) (koulutustoimija-arkisto/lisaa! uusi-kt)
        (not= vanha-kt uusi-kt) (do
                                  (log/info "Muuttunut koulutustoimija: " (:ytunnus uusi-kt) (muutos vanha-kt uusi-kt) vanha-kt uusi-kt)
                                  (koulutustoimija-arkisto/paivita! y-tunnus uusi-kt)))))
  (koulutustoimija-arkisto/laske-voimassaolo!))

(defn ^:private ^:integration-api paivita-oppilaitokset! [koodit]
  (let [oid->ytunnus (generoi-oid->y-tunnus (into {} (for [k (koulutustoimija-arkisto/hae-kaikki-organisaatiopalvelulle)]
                                                       [(:oid k) (:ytunnus k)]))
                                            koodit)
        oppilaitokset (->> (oppilaitos-arkisto/hae-kaikki)
                        (map-by :oppilaitoskoodi))]
    (doseq [koodi (vals (map-by :oppilaitosKoodi koodit)) ;; Poistetaan duplikaatit
            ;; Poistetaan oppilaitokset joille ei löydy koulutustoimijaa
            ;; Oppilaitoksella on oltava koulutustoimija
            :when (oid->ytunnus (:parentOid koodi))
            :let [oppilaitoskoodi (:oppilaitosKoodi koodi)
                  koulutustoimija (oid->ytunnus (:parentOid koodi))
                  vanha-oppilaitos (oppilaitoksen-kentat (get oppilaitokset oppilaitoskoodi))
                  uusi-oppilaitos (assoc (koodi->oppilaitos koodi)
                                         :koulutustoimija koulutustoimija)]]
      (cond
        (nil? vanha-oppilaitos) (do
                                  (log/info "Uusi oppilaitos: " (:oppilaitoskoodi uusi-oppilaitos))
                                  (oppilaitos-arkisto/lisaa! uusi-oppilaitos))
        (not= vanha-oppilaitos uusi-oppilaitos) (do
                                                  (log/info "Muuttunut oppilaitos: " (:oppilaitoskoodi uusi-oppilaitos) (muutos vanha-oppilaitos uusi-oppilaitos))
                                                  (oppilaitos-arkisto/paivita! oppilaitoskoodi uusi-oppilaitos)))))
  (oppilaitos-arkisto/laske-voimassaolo!))

(defn ^:integration-api paivita-toimipisteet! [koodit]
  (let [oid->oppilaitostunnus (into {} (for [o (oppilaitos-arkisto/hae-kaikki)
                                             :when (:oid o)]
                                         [(:oid o) (:oppilaitoskoodi o)]))
        toimipaikat (map-by :oid (toimipiste-arkisto/hae-kaikki))]
    (doseq [koodi (vals (map-by :oid koodit)) ;; Poistetaan duplikaatit. Voi kuitenkin olla useita samalla toimipistekoodilla (vaikkei pitäisi)
            ;; Poistetaan toimipaikat jotka eivät ole suoraan oppilaitoksen alla
            ;; Toimipaikalla on oltava oppilaitos
            :when (oid->oppilaitostunnus (:parentOid koodi))
            :let [oid (:oid koodi)
                  oppilaitoskoodi (oid->oppilaitostunnus (:parentOid koodi))
                  vanha-toimipiste (toimipaikan-kentat (get toimipaikat oid))
                  uusi-toimipiste (assoc (koodi->toimipiste koodi)
                                          :oppilaitos oppilaitoskoodi)]]
      (cond
        (nil? vanha-toimipiste) (do
                                   (log/info "Uusi toimipiste: " (:oid uusi-toimipiste))
                                   (toimipiste-arkisto/lisaa! uusi-toimipiste))
        (not= vanha-toimipiste uusi-toimipiste) (do
                                                    (log/info "Muuttunut toimipiste: " (:toimipistekoodi uusi-toimipiste) (muutos vanha-toimipiste uusi-toimipiste))
                                                    (toimipiste-arkisto/paivita! oid uusi-toimipiste)))))
  (toimipiste-arkisto/laske-voimassaolo!))

(defn ^:integration-api ^:private paivita-haetut-organisaatiot! [koodit]
  (let [koodit-tyypeittain (group-by tyyppi koodit)
        koulutustoimijakoodit (:koulutustoimija koodit-tyypeittain)
        oppilaitoskoodit (:oppilaitos koodit-tyypeittain)
        toimipistekoodit (:toimipiste koodit-tyypeittain)]
    (log/info "Haettu muuttuneet organisaatiot," (count koodit) "kpl")
    (paivita-koulutustoimijat! koulutustoimijakoodit)
    (paivita-oppilaitokset! oppilaitoskoodit)
    (paivita-toimipisteet! toimipistekoodit)))

(defn paivita-erassa [oids paivitys-funktio url]
  (let [oid-erat (partition-all 200 oids)]
    (db/transaction
     (run! (comp paivitys-funktio #(hae-era % url)) oid-erat))))

(defn hae-ja-paivita-kaikki [url]
  (log/info "Haetaan kaikki koulutustoimijat, oppilaitokset ja toimipisteet organisaatiopalvelusta")
  (let [koulutustoimija-oidit (hae-oidit-tyypilla url "KOULUTUSTOIMIJA")
        oppilaitos-oidit (hae-oidit-tyypilla url "OPPILAITOS")
        toimipiste-oidit (hae-oidit-tyypilla url "TOIMIPISTE")]
    (paivita-erassa koulutustoimija-oidit paivita-koulutustoimijat! url)
    (paivita-erassa oppilaitos-oidit paivita-oppilaitokset! url)
    (paivita-erassa toimipiste-oidit paivita-toimipisteet! url)))

(defn ^:integration-api paivita-organisaatiot!
  [asetukset]
  (log/info "Aloitetaan organisaatioiden päivitys organisaatiopalvelusta")
  (db/transaction
    (let [viimeisin-paivitys (organisaatiopalvelu-arkisto/hae-viimeisin-paivitys)
          _ (when viimeisin-paivitys (log/info "Edellinen päivitys:" (str viimeisin-paivitys)))
          url (get asetukset "url")
          vanhat-koulutustoimijat (set (map :ytunnus (koulutustoimija-arkisto/hae-kaikki)))
          nyt (time/now)
          koodit (when viimeisin-paivitys (hae-muuttuneet url viimeisin-paivitys))]
      (when-not viimeisin-paivitys
        ;; Ajetaan käytännössä vain kerran. Konversiossa on tuotu organisaatioita, joita ei käytetä eikä haeta organisaatiopalvelusta
        ;; ja tällä tavalla saadaan ne merkittyä vanhentuneiksi.
        (koulutustoimija-arkisto/aseta-kaikki-vanhentuneiksi!)
        (oppilaitos-arkisto/aseta-kaikki-vanhentuneiksi!)
        (toimipiste-arkisto/aseta-kaikki-vanhentuneiksi!))
      (if viimeisin-paivitys
        (paivita-haetut-organisaatiot! koodit)
        (hae-ja-paivita-kaikki url))
      (organisaatiopalvelu-arkisto/tallenna-paivitys! nyt)
      (let [nykyiset-koulutustoimijat (set (map :ytunnus (koulutustoimija-arkisto/hae-kaikki)))]
        (doseq [kt (clojure.set/difference nykyiset-koulutustoimijat vanhat-koulutustoimijat)]
          (log/info "Uusi koulutustoimija" kt))))))
