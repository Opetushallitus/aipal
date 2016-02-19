;; Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.arkisto.vastaajatunnus
  (:require [clojure.string :as st]
            [clojure.set :refer [rename-keys]]
            [oph.korma.common :refer [select-unique-or-nil]]
            [korma.core :as sql]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.auditlog :as auditlog]))

(def sallitut-merkit "ACEFHJKLMNPRTWXY347")

(def vastaajatunnus-select
  (-> (sql/select* taulut/vastaajatunnus)
    (sql/join :left taulut/tutkinto (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus))
    (sql/join :left taulut/koulutustoimija (= :koulutustoimija.ytunnus :vastaajatunnus.valmistavan_koulutuksen_jarjestaja))
    (sql/join :left taulut/oppilaitos (= :oppilaitos.oppilaitoskoodi :vastaajatunnus.valmistavan_koulutuksen_oppilaitos))
    (sql/join :kyselykerta (= :kyselykerta.kyselykertaid :vastaajatunnus.kyselykertaid))
    (sql/join :kysely (= :kysely.kyselyid :kyselykerta.kyselyid))
    (sql/fields :kyselykertaid :lukittu :rahoitusmuotoid :tunnus :tutkintotunnus :vastaajatunnusid :vastaajien_lkm :kaytettavissa
                :tutkinto.nimi_fi :tutkinto.nimi_sv
                :koulutustoimija.ytunnus [:koulutustoimija.nimi_fi :koulutustoimija_nimi_fi] [:koulutustoimija.nimi_sv :koulutustoimija_nimi_sv]
                :oppilaitos.oppilaitoskoodi [:oppilaitos.nimi_fi :oppilaitos_nimi_fi] [:oppilaitos.nimi_sv :oppilaitos_nimi_sv]
                [(sql/raw "COALESCE(COALESCE(vastaajatunnus.voimassa_loppupvm, kyselykerta.voimassa_loppupvm, kysely.voimassa_loppupvm) + 30 > CURRENT_DATE, TRUE)") :muokattavissa]
                :voimassa_alkupvm :voimassa_loppupvm)
    (sql/fields [(sql/subselect taulut/vastaaja
                   (sql/aggregate (count :*) :count)
                   (sql/where {:vastannut true
                               :vastaajatunnusid :vastaajatunnus.vastaajatunnusid})) :vastausten_lkm])
    (sql/order :luotuaika :DESC)
    (sql/order :vastaajatunnusid :DESC)))

(defn ^:private erota-tutkinto
  [vastaajatunnus]
  (let [tutkinto (select-keys vastaajatunnus [:nimi_fi :nimi_sv :tutkintotunnus])]
    (some-> vastaajatunnus
      (dissoc :nimi_fi :nimi_sv :tutkintotunnus)
      (assoc :tutkinto tutkinto))))

(defn ^:private erota-koulutustoimija
  [vastaajatunnus]
  (let [koulutustoimija (rename-keys
                          (select-keys vastaajatunnus [:ytunnus :koulutustoimija_nimi_fi :koulutustoimija_nimi_sv])
                          {:koulutustoimija_nimi_fi :nimi_fi
                           :koulutustoimija_nimi_sv :nimi_sv})]
    (some-> vastaajatunnus
      (dissoc :ytunnus :koulutustoimija_nimi_fi :koulutustoimija_nimi_sv)
      (assoc :valmistavan_koulutuksen_jarjestaja koulutustoimija))))

(defn ^:private erota-oppilaitos
  [vastaajatunnus]
  (let [oppilaitos (rename-keys
                     (select-keys vastaajatunnus [:oppilaitoskoodi :oppilaitos_nimi_fi :oppilaitos_nimi_sv])
                     {:oppilaitos_nimi_fi :nimi_fi
                      :oppilaitos_nimi_sv :nimi_sv})]
    (some-> vastaajatunnus
      (dissoc :oppilaitoskoodi :oppilaitos_nimi_fi :oppilaitos_nimi_sv)
      (assoc :valmistavan_koulutuksen_oppilaitos oppilaitos))))

(defn hae-kyselykerralla
  "Hae kyselykerran vastaajatunnukset"
  [kyselykertaid]
  (-> vastaajatunnus-select
    (sql/where (= :kyselykertaid kyselykertaid))
    sql/exec
    (->>
      (map erota-tutkinto)
      (map erota-koulutustoimija)
      (map erota-oppilaitos))))

(defn hae-viimeisin-tutkinto
  "Hakee vastaajatunnuksiin tallennetuista tutkinnoista viimeisimmän koulutustoimijalle kuuluvan"
  [kyselykertaid koulutustoimija]
  (first
    (sql/select taulut/vastaajatunnus
      (sql/join :inner taulut/tutkinto (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus))
      (sql/fields :tutkinto.tutkintotunnus :tutkinto.nimi_fi :tutkinto.nimi_sv)
      (sql/where (and (= :vastaajatunnus.kyselykertaid kyselykertaid)
                      [(sql/sqlfn :exists (sql/subselect :koulutustoimija_ja_tutkinto
                                            (sql/where {:koulutustoimija_ja_tutkinto.tutkinto :tutkinto.tutkintotunnus
                                                        :koulutustoimija_ja_tutkinto.koulutustoimija koulutustoimija})))]))
      (sql/order :vastaajatunnus.luotuaika :desc))))

(defn hae [kyselykertaid vastaajatunnusid]
  (-> vastaajatunnus-select
    (sql/where {:kyselykertaid kyselykertaid
                :vastaajatunnusid vastaajatunnusid})
    sql/exec
    first
    erota-tutkinto
    erota-koulutustoimija
    erota-oppilaitos))

(defn luo-satunnainen-tunnus
  [pituus]
  {:post [(and
            (string? %)
            (= pituus (.length %)))]}
  (apply str (take pituus (repeatedly #(rand-nth sallitut-merkit)))))

(defn luo-tunnuksia
  "Luo keskenään uniikkeja satunnaisia määritellyn pituisia tunnuksia."
  [pituus]
  (distinct
    (repeatedly 10000 #(luo-satunnainen-tunnus pituus))))

(defn vastaajatunnus-olemassa?
  [vastaajatunnus]
  (select-unique-or-nil taulut/vastaajatunnus
    (sql/fields :tunnus)
    (sql/where {(sql/sqlfn :upper :tunnus) (clojure.string/upper-case vastaajatunnus)})))

(defn ^:private tallenna-vastaajatunnus! [vastaajatunnus]
  (let [vastaajatunnus (-> (sql/insert taulut/vastaajatunnus
                             (sql/values vastaajatunnus)))
        vastaajatunnus (hae (:kyselykertaid vastaajatunnus) (:vastaajatunnusid vastaajatunnus))]
    (auditlog/vastaajatunnus-luonti! (:tunnus vastaajatunnus) (:kyselykertaid vastaajatunnus))
    vastaajatunnus))

(defn lisaa! [vastaajatunnus]
  {:pre [(pos? (:vastaajien_lkm vastaajatunnus))]}
  (auditlog/vastaajatunnus-luonti! (:kyselykertaid vastaajatunnus))
  (let [tunnusten-lkm (if (:henkilokohtainen vastaajatunnus) (:vastaajien_lkm vastaajatunnus) 1)
        vastaajien-lkm (if (:henkilokohtainen vastaajatunnus) 1 (:vastaajien_lkm vastaajatunnus))
        tutkintotunnus (get-in vastaajatunnus [:tutkinto :tutkintotunnus])
        valmistavan-koulutuksen-jarjestaja (get-in vastaajatunnus [:koulutuksen_jarjestaja :ytunnus])
        valmistavan-koulutuksen-oppilaitos (get-in vastaajatunnus [:koulutuksen_jarjestaja_oppilaitos :oppilaitoskoodi])
        vastaajatunnus (-> vastaajatunnus
                         (dissoc :henkilokohtainen :tutkinto :koulutuksen_jarjestaja :koulutuksen_jarjestaja_oppilaitos)
                         (assoc :vastaajien_lkm vastaajien-lkm
                                :tutkintotunnus tutkintotunnus
                                :valmistavan_koulutuksen_jarjestaja valmistavan-koulutuksen-jarjestaja
                                :valmistavan_koulutuksen_oppilaitos valmistavan-koulutuksen-oppilaitos))]
    (doall
      (for [tunnus (->> (luo-tunnuksia 6)
                     (remove vastaajatunnus-olemassa?)
                     (take tunnusten-lkm))]
        (tallenna-vastaajatunnus! (assoc vastaajatunnus :tunnus tunnus))))))

(defn aseta-lukittu! [kyselykertaid vastaajatunnusid lukitse]
  (auditlog/vastaajatunnus-muokkaus! vastaajatunnusid kyselykertaid lukitse)
  (sql/update taulut/vastaajatunnus
    (sql/set-fields {:lukittu lukitse})
    (sql/where {:kyselykertaid kyselykertaid
                :vastaajatunnusid vastaajatunnusid}))
  ;; haetaan vastaajatunnus, jotta saadaan kaytettavissa arvo
  (hae kyselykertaid vastaajatunnusid))

(defn poista! [kyselykertaid vastaajatunnusid]
  (auditlog/vastaajatunnus-poisto! vastaajatunnusid kyselykertaid)
  (sql/delete taulut/vastaajatunnus
    (sql/where {:kyselykertaid kyselykertaid
                :vastaajatunnusid vastaajatunnusid})))

(defn laske-vastaajat [vastaajatunnusid]
  (->
    (sql/select taulut/vastaaja
      (sql/aggregate (count :*) :cnt)
      (sql/where {:vastaajatunnusid vastaajatunnusid}))
    first
    :cnt))

(defn muokkaa-lukumaaraa
  [kyselykertaid vastaajatunnusid lukumaara]
  (sql/update :vastaajatunnus
    (sql/set-fields {:vastaajien_lkm lukumaara})
    (sql/where {:kyselykertaid kyselykertaid
                :vastaajatunnusid vastaajatunnusid}))
  ;; haetaan vastaajatunnus, jotta saadaan palautettua muokattu tunnus
  (hae kyselykertaid vastaajatunnusid))
