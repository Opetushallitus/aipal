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
  (:import [java.sql SQLException])
  (:require [clojure.string :as st]
            [clojure.java.jdbc :as jdbc]
            [clojure.core.match :refer [match]]
            [oph.common.util.util :refer [select-and-rename-keys]]
            [oph.korma.common :refer [select-unique-or-nil]]
            [korma.core :as sql]
            [clojure.tools.logging :as log]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
            [aipal.infra.kayttaja.vakiot :refer [integraatio-uid]]
            [aipal.auditlog :as auditlog]
            [arvo.db.core :as vastaajatunnus]))

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
                :tutkinto.nimi_fi :tutkinto.nimi_sv :tutkinto.nimi_en
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
    (sql/where {(sql/sqlfn :upper :tunnus) (clojure.string/upper-case vastaajatunnus)})))

(def ^:private common-and-legacy-props [:kyselykertaid
                                        :tunnus
                                        :voimassa_alkupvm
                                        :voimassa_loppupvm
                                        :vastaajien_lkm
                                        :valmistavan_koulutuksen_jarjestaja
                                        :valmistavan_koulutuksen_oppilaitos
                                        :rahoitusmuotoid
                                        :tutkintotunnus
                                        ;duplicate data to old table for vipunen and reports, at least for now
                                        :valmistavan_koulutuksen_toimipaikka
                                        :kunta
                                        :koulutusmuoto
                                        :suorituskieli
                                        :koulutusmuoto])

(defn ^:private tallenna-vastaajatunnus! [vastaajatunnus]
    (log/info (format "Storing: %s" vastaajatunnus)) 
    (let [tallennettava-tunnus (select-keys vastaajatunnus common-and-legacy-props)
          vastaajatunnus (-> (sql/insert taulut/vastaajatunnus
                               (sql/values tallennettava-tunnus)))
          vastaajatunnus (hae (:kyselykertaid vastaajatunnus) (:vastaajatunnusid vastaajatunnus))]
      (auditlog/vastaajatunnus-luonti! (:tunnus vastaajatunnus) (:kyselykertaid vastaajatunnus))
      vastaajatunnus))

(defn ^:private tallenna-tiedot! [entry]
  (-> (sql/insert taulut/vastaajatunnus_tiedot
                  (sql/values entry))))

(defn palaute-fields [vastaajatunnus]
  {:kieli (:suorituskieli vastaajatunnus)
   :tutkinto (get-in vastaajatunnus [:tutkinto :tutkintotunnus])
   :koulutusmuoto (:koulutusmuoto vastaajatunnus)
   :toimipaikka (get-in vastaajatunnus [:koulutuksen_toimipaikka :toimipaikkakoodi])
   :kunta (get-in vastaajatunnus [:koulutuksen_toimipaikka :kunta])})

; These should be read through kyselytyyppi eventually
(defn vastaajatunnus-fields [vastaajatunnus kyselytyyppi]
  (match [kyselytyyppi]
         [1] (palaute-fields vastaajatunnus)
         [2] (select-keys vastaajatunnus [:haun_numero :henkilonumero])))

(defn find-id [kentta kyselytyyppi-kentat]
  (:id (first (filter #(= kentta (:kentta_id %))kyselytyyppi-kentat))))

(defn tieto-entries [vastaajatunnus-kentat kyselytyyppi-kentat vastaajatunnus-id]
  (map #(into {} [[:vastaajatunnus_id vastaajatunnus-id]
                  [:kentta (find-id (name (first %)) kyselytyyppi-kentat)]
                  [:arvo (second %)]]) (seq vastaajatunnus-kentat)))

(defn tallenna-vastaajatunnus-tiedot! [tunnus vastaajatunnus kyselytyyppi]
  (let [kyselytyyppi-kentat (vastaajatunnus/kyselytyypin_kentat {:kyselytyyppi kyselytyyppi})
        vastaajatunnus-kentat (vastaajatunnus-fields vastaajatunnus kyselytyyppi)
        entries (->> (tieto-entries vastaajatunnus-kentat kyselytyyppi-kentat tunnus)
                    (filter :kentta)
                    (filter :arvo))]
    (run! #(tallenna-tiedot! %) entries)))


(defn get-vastaajatunnukset [tunnusten-lkm]
  (->> (luo-tunnuksia 6)
       (remove vastaajatunnus-olemassa?)
       (take tunnusten-lkm)))

(defn lisaa! [vastaajatunnus]
  {:pre [(pos? (:vastaajien_lkm vastaajatunnus))]}
  (auditlog/vastaajatunnus-luonti! (:kyselykertaid vastaajatunnus))
  (let [kyselytyyppi (:tyyppi (vastaajatunnus/kyselykerran-tyyppi {:kyselykertaid (:kyselykertaid vastaajatunnus)}))
        tunnusten-lkm (if (:henkilokohtainen vastaajatunnus) (:vastaajien_lkm vastaajatunnus) 1)
        vastaajien-lkm (if (:henkilokohtainen vastaajatunnus) 1 (:vastaajien_lkm vastaajatunnus))
        valmistavan-koulutuksen-jarjestaja (get-in vastaajatunnus [:koulutuksen_jarjestaja :ytunnus])
        tutkintotunnus (get-in vastaajatunnus [:tutkinto :tutkintotunnus])
        valmistavan-koulutuksen-oppilaitos (get-in vastaajatunnus [:koulutuksen_jarjestaja_oppilaitos :oppilaitoskoodi])
        valmistavan-koulutuksen-toimipaikka (get-in vastaajatunnus [:koulutuksen_toimipaikka :toimipaikkakoodi])
        kunta (get-in vastaajatunnus [:koulutuksen_toimipaikka :kunta])
        vastaajatunnus (-> (if (:rahoitusmuotoid vastaajatunnus)
                             vastaajatunnus
                             (assoc vastaajatunnus :rahoitusmuotoid 5)))
        vastaajatunnus (-> vastaajatunnus
                         (assoc :vastaajien_lkm vastaajien-lkm
                                :kunta kunta
                                :tutkintotunnus tutkintotunnus
                                :valmistavan_koulutuksen_jarjestaja valmistavan-koulutuksen-jarjestaja
                                :valmistavan_koulutuksen_oppilaitos valmistavan-koulutuksen-oppilaitos
                                :valmistavan_koulutuksen_toimipaikka valmistavan-koulutuksen-toimipaikka))]
    (doall
      (for [tunnus (get-vastaajatunnukset tunnusten-lkm)]
        (let [tallennettu-tunnus (tallenna-vastaajatunnus! (assoc vastaajatunnus :tunnus tunnus))]
          (tallenna-vastaajatunnus-tiedot! (:vastaajatunnusid tallennettu-tunnus) vastaajatunnus kyselytyyppi)
          tallennettu-tunnus)))))


;;AVOP.FI FIXME: binding manually to INTEGRAATIO user (check if that is right)
(defn lisaa-avopfi! [vastaajatunnus]
  ;;FIXME korjaa integraatio-testin, mutta rikkoo muuten: (auditlog/vastaajatunnus-luonti! (:kyselykertaid vastaajatunnus))
  (doall
    (for [tunnus (->> (luo-tunnuksia 6)
                   (remove vastaajatunnus-olemassa?)
                   (take 1))]

      (with-kayttaja integraatio-uid nil nil
                     (let [kyselytyyppi (:tyyppi (vastaajatunnus/kyselykerran-tyyppi {:kyselykertaid (:kyselykertaid vastaajatunnus)}))
                           tallennettu-tunnus (tallenna-vastaajatunnus! (assoc vastaajatunnus :tunnus tunnus))
                           kunta (:kunta vastaajatunnus)
                           tutkintotunnus (:tutkintotunnus vastaajatunnus)]
                         (tallenna-vastaajatunnus-tiedot! (:vastaajatunnusid tallennettu-tunnus)
                                                          (assoc vastaajatunnus
                                                            :toimipaikka {:kunta kunta}
                                                            :tutkinto {:tutkintotunnus tutkintotunnus}) kyselytyyppi)
                         tallennettu-tunnus)))))
;;END AVOP.FI

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
    (sql/set-fields {:vastaajien_lkm lukumaara})
    (sql/where {:kyselykertaid kyselykertaid
                :vastaajatunnusid vastaajatunnusid}))
  ;; haetaan vastaajatunnus, jotta saadaan palautettua muokattu tunnus
  (hae kyselykertaid vastaajatunnusid))
