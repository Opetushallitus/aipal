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
  (:require [clojure.string :as str]
            [conman.core :as conman]
            [clojure.set :refer [rename-keys]]
            [clojure.core.match :refer [match]]
            [oph.common.util.util :refer [select-and-rename-keys]]
            [oph.korma.common :refer [select-unique-or-nil]]
            [korma.core :as sql]
            [clojure.tools.logging :as log]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vakiot :refer [integraatio-uid]]
            [aipal.auditlog :as auditlog]
            [arvo.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clj-time.coerce :refer [to-sql-date]]))

(def sallitut-merkit "ACEFHJKLMNPRTWXY347")

(def vastaajatunnus-select
  (-> (sql/select* taulut/vastaajatunnus)
    (sql/join :left taulut/tutkinto (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus))
    (sql/join :left taulut/koulutustoimija (= :koulutustoimija.ytunnus :vastaajatunnus.valmistavan_koulutuksen_jarjestaja))
    (sql/join :left taulut/oppilaitos (= :oppilaitos.oppilaitoskoodi :vastaajatunnus.valmistavan_koulutuksen_oppilaitos))
    (sql/join :kyselykerta (= :kyselykerta.kyselykertaid :vastaajatunnus.kyselykertaid))
    (sql/join :kysely (= :kysely.kyselyid :kyselykerta.kyselyid))
    (sql/join :left taulut/toimipaikka (= :toimipaikka.toimipaikkakoodi :vastaajatunnus.valmistavan_koulutuksen_toimipaikka))
    (sql/fields :kyselykertaid :lukittu :rahoitusmuotoid :tunnus :tutkintotunnus :vastaajatunnusid :vastaajien_lkm :kaytettavissa :suorituskieli
                :tutkinto.nimi_fi :tutkinto.nimi_sv :tutkinto.nimi_en :luotuaika
                :koulutustoimija.ytunnus [:koulutustoimija.nimi_fi :koulutustoimija_nimi_fi] [:koulutustoimija.nimi_sv :koulutustoimija_nimi_sv] [:koulutustoimija.nimi_en :koulutustoimija_nimi_en]
                :oppilaitos.oppilaitoskoodi [:oppilaitos.nimi_fi :oppilaitos_nimi_fi] [:oppilaitos.nimi_sv :oppilaitos_nimi_sv] [:oppilaitos.nimi_en :oppilaitos_nimi_en]
                [(sql/raw "COALESCE(COALESCE(vastaajatunnus.voimassa_loppupvm, kyselykerta.voimassa_loppupvm, kysely.voimassa_loppupvm) + 30 > CURRENT_DATE, TRUE)") :muokattavissa]
                :toimipaikka.toimipaikkakoodi [:toimipaikka.nimi_fi :toimipaikka_nimi_fi] [:toimipaikka.nimi_sv :toimipaikka_nimi_sv] [:toimipaikka.nimi_en :toimipaikka_nimi_en]
                :kunta
                :koulutusmuoto
                :voimassa_alkupvm :voimassa_loppupvm)
    (sql/fields [(sql/subselect taulut/vastaaja
                   (sql/aggregate (count :*) :count)
                   (sql/where {:vastannut true
                               :vastaajatunnusid :vastaajatunnus.vastaajatunnusid})) :vastausten_lkm])
    (sql/order :luotuaika :DESC)
    (sql/order :vastaajatunnusid :DESC)))

(defn ^:private erota-tutkinto
  [vastaajatunnus]
  (let [tutkinto (select-keys vastaajatunnus [:nimi_fi :nimi_sv :nimi_en :tutkintotunnus])]
    (some-> vastaajatunnus
      (dissoc :nimi_fi :nimi_sv :nimi_en :tutkintotunnus)
      (assoc :tutkinto tutkinto))))

(defn ^:private erota-koulutustoimija
  [vastaajatunnus]
  (let [koulutustoimija (select-and-rename-keys vastaajatunnus [:ytunnus [:koulutustoimija_nimi_fi :nimi_fi] [:koulutustoimija_nimi_sv :nimi_sv] [:koulutustoimija_nimi_sv :nimi_sv]])]
    (some-> vastaajatunnus
      (dissoc :ytunnus :koulutustoimija_nimi_fi :koulutustoimija_nimi_sv :koulutustoimija_nimi_en)
      (assoc :valmistavan_koulutuksen_jarjestaja koulutustoimija))))

(defn ^:private erota-oppilaitos
  [vastaajatunnus]
  (let [oppilaitos (select-and-rename-keys vastaajatunnus [:oppilaitoskoodi [:oppilaitos_nimi_fi :nimi_fi] [:oppilaitos_nimi_sv :nimi_sv] [:oppilaitos_nimi_en :nimi_en]])]
    (some-> vastaajatunnus
      (dissoc :oppilaitoskoodi :oppilaitos_nimi_fi :oppilaitos_nimi_sv :oppilaitos_nimi_en)
      (assoc :valmistavan_koulutuksen_oppilaitos oppilaitos))))

(defn ^:private erota-toimipaikka
  [vastaajatunnus]
  (let [toimipaikka (select-and-rename-keys vastaajatunnus [:toimipaikkakoodi [:toimipaikka_nimi_fi :nimi_fi] [:toimipaikka_nimi_sv :nimi_sv] [:toimipaikka_nimi_en :nimi_en]])]
    (some-> vastaajatunnus
            (dissoc :toimipaikkakoodi :toimipaikka_nimi_fi :toimipaikka_nimi_sv :toimipaikka_nimi_en)
            (assoc :valmistavan_koulutuksen_toimipaikka toimipaikka))))


(defn hae-kyselykerralla
  "Hae kyselykerran vastaajatunnukset"
  [kyselykertaid]
  (-> vastaajatunnus-select
    (sql/where (= :kyselykertaid kyselykertaid))
    sql/exec
    (->>
      (map erota-tutkinto)
      (map erota-koulutustoimija)
      (map erota-oppilaitos)
      (map erota-toimipaikka))))


(defn hae-viimeisin-tutkinto
  "Hakee vastaajatunnuksiin tallennetuista tutkinnoista viimeisimmän koulutustoimijalle kuuluvan"
  [kyselykertaid koulutustoimija]
  (first
    (sql/select taulut/vastaajatunnus
      (sql/join :inner taulut/tutkinto (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus))
      (sql/fields :tutkinto.tutkintotunnus :tutkinto.nimi_fi :tutkinto.nimi_sv :tutkinto.nimi_en)
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
    erota-oppilaitos
    erota-toimipaikka))

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
    (sql/where {(sql/sqlfn :upper :tunnus) (str/upper-case vastaajatunnus)})))

(def common-props [:kyselykertaid
                   :tunnus
                   :voimassa_alkupvm
                   :voimassa_loppupvm
                   :vastaajien_lkm
                   :valmistavan_koulutuksen_jarjestaja
                   :valmistavan_koulutuksen_oppilaitos])


(def legacy-props [:toimipaikka :kunta :koulutusmuoto :kieli :tutkinto])

(defn vastaajatunnus-base-data [vastaajatunnus tunnus]
  (-> vastaajatunnus
      (assoc :tunnus tunnus)
      (select-keys (concat common-props legacy-props))))

(def ^:private common-and-legacy-props (vec (concat common-props legacy-props)))

(defn ^:private tallenna-vastaajatunnus! [vastaajatunnus]
    (let [tallennettava-tunnus (select-keys vastaajatunnus common-and-legacy-props)
          vastaajatunnus (-> (sql/insert taulut/vastaajatunnus
                               (sql/values (merge tallennettava-tunnus
                                                  {:luotu_kayttaja (:oid *kayttaja*)
                                                   :muutettu_kayttaja (:oid *kayttaja*)}))))
          vastaajatunnus (hae (:kyselykertaid vastaajatunnus) (:vastaajatunnusid vastaajatunnus))]
      (auditlog/vastaajatunnus-luonti! (:vastaajatunnusid vastaajatunnus) (:tunnus vastaajatunnus) (:kyselykertaid vastaajatunnus))
      vastaajatunnus))

(defn find-id [kentta kyselytyyppi-kentat]
  (log/info "Find id: " kentta "FROM" kyselytyyppi-kentat)
  (:id (first (filter #(= kentta (:kentta_id %)) kyselytyyppi-kentat))))


(defn get-vastaajatunnukset [tunnusten-lkm]
  (->> (luo-tunnuksia 6)
       (remove vastaajatunnus-olemassa?)
       (take tunnusten-lkm)))

(defn format-taustatiedot [taustatieto-kentat vastaajatunnus]
  (-> vastaajatunnus
    (select-keys taustatieto-kentat)))



(defn lisaa! [vastaajatunnus]
  {:pre [(pos? (:vastaajien_lkm vastaajatunnus))]}
  (let [kyselytyyppi (:tyyppi (db/kyselykerran-tyyppi vastaajatunnus))
        kyselytyypin_kentat (map (comp keyword :kentta_id) (db/kyselytyypin_kentat {:kyselytyyppi kyselytyyppi}))]
    (doall
      (for [tunnus (get-vastaajatunnukset (:tunnusten-lkm vastaajatunnus))]
        (let [base-data (vastaajatunnus-base-data vastaajatunnus tunnus)
              taustatiedot (format-taustatiedot kyselytyypin_kentat vastaajatunnus)
              tallennettava-tunnus (-> base-data
                                       (assoc :taustatiedot taustatiedot)
                                       (update :voimassa_alkupvm to-sql-date))]
          ;(tallenna-vastaajatunnus-tiedot! (:vastaajatunnusid tallennettu-tunnus) vastaajatunnus kyselytyyppi)
          (db/lisaa-vastaajatunnus! (assoc tallennettava-tunnus :kayttaja (:oid *kayttaja*)))
          tallennettava-tunnus)))))

;;AVOP.FI FIXME: binding manually to INTEGRAATIO user (check if that is right)
(defn lisaa-avopfi! [vastaajatunnus]
  (with-kayttaja integraatio-uid nil nil
     (lisaa! vastaajatunnus)))
;;END AVOP.FI

(defn lisaa-massana! [vastaajatunnukset]
  (jdbc/with-db-transaction [tx *db*]
    (doseq [tunnus vastaajatunnukset]
      (db/lisaa-vastaajatunnus! tx (assoc tunnus :kayttaja (:oid *kayttaja*))))
    vastaajatunnukset))

(defn liita-taustatiedot! [taustatieto-seq]
  (jdbc/with-db-transaction [tx *db*]
    (doseq [taustatiedot taustatieto-seq]
      (let [vastaajatunnus (:vastaajatunnus taustatiedot)
            tutkintotunnus (:tutkinto_koulutuskoodi taustatiedot)
            oppilaitos (:oppilaitoskoodi taustatiedot)]
        (db/paivita-taustatiedot! {:vastaajatunnus vastaajatunnus
                                   :tutkintotunnus tutkintotunnus
                                   :oppilaitos oppilaitos
                                   :taustatiedot (dissoc taustatiedot
                                                         :vastaajatunnus
                                                         :tutkinto_koulutuskoodi
                                                         :oppilaitoskoodi)}))))
  (count taustatieto-seq))

(defn aseta-lukittu! [kyselykertaid vastaajatunnusid lukitse]
  (auditlog/vastaajatunnus-muokkaus! vastaajatunnusid kyselykertaid lukitse)
  (sql/update taulut/vastaajatunnus
    (sql/set-fields {:lukittu lukitse :muutettu_kayttaja (:oid *kayttaja*)})
    (sql/where {:kyselykertaid kyselykertaid
                :vastaajatunnusid vastaajatunnusid}))
  ;; haetaan vastaajatunnus, jotta saadaan kaytettavissa arvo
  (hae kyselykertaid vastaajatunnusid))

(defn poista! [kyselykertaid vastaajatunnusid]
  (auditlog/vastaajatunnus-poisto! vastaajatunnusid kyselykertaid)
  (sql/delete taulut/vastaajatunnus_tiedot
              (sql/where {:vastaajatunnus_id vastaajatunnusid}))
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
    (sql/set-fields {:vastaajien_lkm lukumaara :muutettu_kayttaja (:oid *kayttaja*)})
    (sql/where {:kyselykertaid kyselykertaid
                :vastaajatunnusid vastaajatunnusid}))
  ;; haetaan vastaajatunnus, jotta saadaan palautettua muokattu tunnus
  (hae kyselykertaid vastaajatunnusid))
