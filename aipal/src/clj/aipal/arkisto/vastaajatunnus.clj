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
  (:require [clojure.set :refer [rename-keys]]
            [clojure.core.match :refer [match]]
            [oph.common.util.util :refer [select-and-rename-keys]]
            [oph.korma.common :refer [select-unique-or-nil]]
            [clojure.tools.logging :as log]
            [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vakiot :refer [integraatio-uid]]
            [arvo.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clj-time.coerce :refer [to-sql-date]]))

(def errors {:ei-kyselykertaa {:error "ei-kyselykertaa" :msg "Ei kyselykertaa annetuille tiedoille"}})

(def sallitut-merkit "ACEFHJKLMNPRTWXY347")

(def jarjestelma-kayttajat #{"INTEGRAATIO" "JARJESTELMA"})

(defn ^:private erota-tutkinto
  [vastaajatunnus]
  (let [tutkinto (select-keys vastaajatunnus [:nimi_fi :nimi_sv :nimi_en :tutkintotunnus])]
    (some-> vastaajatunnus
      (dissoc :nimi_fi :nimi_sv :nimi_en :tutkintotunnus)
      (assoc :tutkinto tutkinto))))

(defn ^:private erota-oppilaitos
  [vastaajatunnus]
  (let [oppilaitos (select-and-rename-keys vastaajatunnus [:oppilaitoskoodi [:oppilaitos_nimi_fi :nimi_fi] [:oppilaitos_nimi_sv :nimi_sv] [:oppilaitos_nimi_en :nimi_en]])]
    (some-> vastaajatunnus
      (dissoc :oppilaitoskoodi :oppilaitos_nimi_fi :oppilaitos_nimi_sv :oppilaitos_nimi_en)
      (assoc :valmistavan_koulutuksen_oppilaitos oppilaitos))))

(defn ^:private erota-toimipiste
  [vastaajatunnus]
  (let [toimipiste (select-and-rename-keys vastaajatunnus [:toimipistekoodi [:toimipiste_nimi_fi :nimi_fi] [:toimipiste_nimi_sv :nimi_sv] [:toimipiste_nimi_en :nimi_en]])]
    (some-> vastaajatunnus
            (dissoc :toimipistekoodi :toimipiste_nimi_fi :toimipiste_nimi_sv :toimipiste_nimi_en)
            (assoc :toimipiste toimipiste))))

(defn yhdistä-taustatiedot [vastaajatunnus]
  (let [taustatiedot (:taustatieot vastaajatunnus)]
    (merge vastaajatunnus {:suorituskieli (:kieli taustatiedot)
                           :koulutusmuoto (:koulutusmuoto taustatiedot)})))

(defn format-vastaajatunnus [vastaajatunnus]
  (-> vastaajatunnus
      yhdistä-taustatiedot
      erota-tutkinto
      erota-oppilaitos
      erota-toimipiste))

(defn hae-kyselykerralla
  "Hae kyselykerran vastaajatunnukset"
  [kyselykertaid hae-kayttajalle]
  (map format-vastaajatunnus
    (db/hae-vastaajatunnus (merge
                             {:kyselykertaid kyselykertaid}
                             (when hae-kayttajalle {:oid (:aktiivinen-oid *kayttaja*)})))))

(defn hae-viimeisin-tutkinto
  "Hakee vastaajatunnuksiin tallennetuista tutkinnoista viimeisimmän koulutustoimijalle kuuluvan"
  [kyselykertaid koulutustoimija]
  (first
    (db/hae-viimeisin-tutkinto {:koulutustoimija koulutustoimija :kyselykertaid kyselykertaid})))

(defn hae [kyselykertaid vastaajatunnusid]
  (-> (db/hae-vastaajatunnus {:kyselykertaid kyselykertaid :vastaajatunnusid vastaajatunnusid})
    first
    format-vastaajatunnus))

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

(defn vastaajatunnus-olemassa? [vastaajatunnus]
  (db/vastaajatunnus-olemassa? {:vastaajatunnus vastaajatunnus}))

(def common-props [:kyselykertaid
                   :tunnus
                   :voimassa_alkupvm
                   :voimassa_loppupvm
                   :kohteiden_lkm
                   :metatiedot
                   :valmistavan_koulutuksen_oppilaitos])

(def legacy-props [:toimipiste :kunta :koulutusmuoto :kieli :tutkinto])

(defn vastaajatunnus-base-data [vastaajatunnus tunnus]
  (-> vastaajatunnus
      (assoc :tunnus tunnus)
      (assoc :metatiedot (:metatiedot vastaajatunnus))
      (select-keys (concat common-props legacy-props))))

(def ^:private common-and-legacy-props (vec (concat common-props legacy-props)))

(defn find-id [kentta kyselytyyppi-kentat]
  (:id (first (filter #(= kentta (:kentta_id %)) kyselytyyppi-kentat))))

(defn get-vastaajatunnukset [tunnusten-lkm]
  (->> (luo-tunnuksia 6)
       (remove vastaajatunnus-olemassa?)
       (take tunnusten-lkm)))

(defn format-taustatiedot [taustatieto-kentat vastaajatunnus]
  (-> vastaajatunnus
    (select-keys taustatieto-kentat)))

(defn lisaa! [vastaajatunnus]
  {:pre [(pos? (:kohteiden_lkm vastaajatunnus))]}
  (let [kyselytyyppi (:tyyppi (db/kyselykerran-tyyppi vastaajatunnus))
        kyselytyypin_kentat (map (comp keyword :kentta_id) (db/kyselytyypin-kentat {:kyselytyyppi kyselytyyppi}))]
    (doall
      (for [tunnus (get-vastaajatunnukset (:tunnusten-lkm vastaajatunnus))]
        (let [base-data (vastaajatunnus-base-data vastaajatunnus tunnus)
              taustatiedot (format-taustatiedot kyselytyypin_kentat vastaajatunnus)
              tallennettava-tunnus (-> base-data
                                       (assoc :taustatiedot taustatiedot)
                                       (update :voimassa_alkupvm to-sql-date)
                                       (update :voimassa_loppupvm to-sql-date)
                                       (assoc :kayttaja (:oid *kayttaja*)))
              vastaajatunnusid (:vastaajatunnusid (db/lisaa-vastaajatunnus! tallennettava-tunnus))]
          (hae (:kyselykertaid tallennettava-tunnus) vastaajatunnusid))))))

(defn lisaa-automaattitunnus! [vastaajatunnus]
  (log/info "Lisätään tunnus:" vastaajatunnus)
  (if (:kyselykertaid vastaajatunnus)
    (with-kayttaja integraatio-uid nil nil
      (-> (lisaa! vastaajatunnus)
          first
          (select-keys [:tunnus :voimassa_loppupvm])))
    {:error (:ei-kyselykertaa errors)}))

(defn lisaa-massana! [vastaajatunnukset]
  (jdbc/with-db-transaction [tx *db*]
    (doseq [tunnus vastaajatunnukset]
      (db/lisaa-vastaajatunnus! tx tunnus))
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
  (str (count taustatieto-seq)))

(defn aseta-lukittu! [kyselykertaid vastaajatunnusid lukitse]
  (db/lukitse-vastaajatunnus! {:vastaajatunnusid vastaajatunnusid :lukittu lukitse})
  (hae kyselykertaid vastaajatunnusid))

(defn poista! [vastaajatunnusid]
  (db/poista-vastaajatunnus! {:vastaajatunnusid vastaajatunnusid}))

(defn laske-vastaajat [vastaajatunnusid]
  (:count (db/vastaajien-lkm {:vastaajatunnusid vastaajatunnusid})))

(defn tunnus-poistettavissa? [kyselykertaid vastaajatunnusid]
  (let [tunnus (hae kyselykertaid vastaajatunnusid)]
    (not (contains? jarjestelma-kayttajat (:luotu_kayttaja tunnus)))))

(defn muokkaa-lukumaaraa!
  [kyselykertaid vastaajatunnusid lukumaara]
  (db/muokkaa-vastaajien-maaraa! {:vastaajatunnusid vastaajatunnusid :vastaajia lukumaara})
  (hae kyselykertaid vastaajatunnusid))

(defn lisaa-amispalaute-automatisointi! [tunnus]
  (db/lisaa-automatisointiin! {:koulutustoimija (:koulutustoimija tunnus)
                               :lahde "EHOKS"}))

(defn niputa-tunnukset [data]
  (jdbc/with-db-transaction [tx *db*]
    (do
      (db/lisaa-nippu! tx data)
      (db/liita-tunnukset-nippuun! tx data)
      data)))

(defn taydenna-nippu [nippu]
  (let [tutkinto (db/hae-tutkinto {:tutkintotunnus (get-in nippu [:taustatiedot :tutkinto])})]
    (merge nippu {:tutkinto tutkinto})))

(defn hae-niput [kyselykertaid]
  (map taydenna-nippu (db/hae-kyselykerran-niput {:kyselykertaid kyselykertaid})))

(defn poista-nippu [tunniste]
  (jdbc/with-db-transaction [tx *db*]
    (do (db/poista-tunnukset-nipusta! {:tunniste tunniste})
        (db/poista-nippu! {:tunniste tunniste}))))
