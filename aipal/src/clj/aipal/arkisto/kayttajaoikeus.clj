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

(ns aipal.arkisto.kayttajaoikeus
  (:require [korma.core :as sql]
            [korma.db :as db]
            [aipal.arkisto.kayttaja :as kayttaja-arkisto]
            [clojure.tools.logging :as log]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vakiot :refer [integraatio-uid]]
            [aipal.integraatio.sql.korma :as taulut]
            [oph.korma.common :refer [select-unique-or-nil update-unique]]))

(defn hae-roolit [oid]
  (sql/select taulut/rooli_organisaatio
    (sql/join taulut/koulutustoimija
              (= :rooli_organisaatio.organisaatio :koulutustoimija.ytunnus))
    (sql/fields :rooli :organisaatio :rooli_organisaatio_id
                [:koulutustoimija.nimi_fi :koulutustoimija_fi]
                [:koulutustoimija.nimi_sv :koulutustoimija_sv]
                [:koulutustoimija.nimi_en :koulutustoimija_en])
    (sql/where {:kayttaja oid
                :voimassa true})))

(defn hae-oikeudet
  ([oid]
    (db/transaction
      (let [kayttaja (kayttaja-arkisto/hae oid)
            roolit (hae-roolit oid)]
        (assoc kayttaja :roolit roolit))))
  ([]
    (hae-oikeudet (:oid *kayttaja*))))

(defn hae-rooli [rooli kayttaja organisaatio]
  (select-unique-or-nil taulut/rooli_organisaatio
    (sql/where {:rooli rooli
                :kayttaja kayttaja
                :organisaatio organisaatio})))

(defn olemassa? [k]
  (hae-rooli (:rooli k) (:kayttaja k) (:organisaatio k)))

(defn ^:integration-api tyhjaa-kayttooikeudet!
  "Merkitään olemassaolevat käyttäjät ja roolit ei-voimassaoleviksi"
  []
  (log/debug "Merkitään olemassaolevat käyttäjät ei-voimassaoleviksi")
  (sql/update taulut/kayttaja
    (sql/set-fields {:voimassa false})
    (sql/where {:luotu_kayttaja [= (:oid *kayttaja*)]}))
  (log/debug "Merkitään olemassaolevien käyttäjien roolit ei-voimassaoleviksi")
  (sql/update taulut/rooli_organisaatio
    (sql/set-fields {:voimassa false})
    (sql/where {:luotu_kayttaja [= (:oid *kayttaja*)]})))

(defn ^:integration-api paivita-rooli!
  "Päivittää tai lisää käyttäjän roolin"
  [r]
  (log/debug "Päivitetään rooli" (pr-str r))
  (if (olemassa? r)
    (do
      (log/debug "Rooli on jo olemassa, päivitetään tiedot")
      (update-unique
        taulut/rooli_organisaatio
        (sql/set-fields r)
        (sql/where {:kayttaja (:kayttaja r)
                    :rooli (:rooli r)
                    :organisaatio (:organisaatio r)})))
    (do
      (log/debug "Luodaan uusi rooli")
      (sql/insert taulut/rooli_organisaatio (sql/values r)))))

(defn ^:integration-api paivita-kaikki!
  "Päivittää käyttäjätaulun uusilla käyttäjillä kt."
  [kt]
  {:pre [(= (:uid *kayttaja*) integraatio-uid)]}
  (db/transaction
    ;; Merkitään nykyiset käyttäjät ei-voimassaoleviksi
    (tyhjaa-kayttooikeudet!)
    (doseq [k kt]
      (let [rooli (clojure.set/rename-keys (select-keys k [:rooli :oid :organisaatio :voimassa])
                                           {:oid :kayttaja})
            kayttaja (dissoc k :rooli :organisaatio)]
        (kayttaja-arkisto/paivita-kayttaja! kayttaja)
        (paivita-rooli! rooli)))))

(defn ^:integration-api paivita-kayttaja!
  "Päivittää yhden käyttäjän käyttäjätauluun"
  [k]
  {:pre [(= (:uid *kayttaja*) integraatio-uid)]}
  (db/transaction
    (let [kayttaja (dissoc k :roolit)]
      (kayttaja-arkisto/paivita-kayttaja! kayttaja)
      (doseq [r (:roolit k)
              :let [rooli (assoc r :kayttaja (:oid kayttaja))]]
        (paivita-rooli! rooli)))))
