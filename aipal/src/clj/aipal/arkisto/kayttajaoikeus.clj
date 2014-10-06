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
            [aipal.infra.kayttaja.vakiot :refer [integraatio-uid]])
  (:use [aipal.integraatio.sql.korma]))

(defn hae-oikeudet
  ([oid]
    (db/transaction
      (let [kayttaja (kayttaja-arkisto/hae oid)
            roolit (sql/select rooli-organisaatio
                     (sql/where {:kayttaja oid
                                 :voimassa true})
                     (sql/fields :rooli :organisaatio))]
        (assoc kayttaja :roolit roolit))))
  ([]
    (hae-oikeudet (:oid *kayttaja*))))

(defn hae-kyselylla
  [kyselyid kayttaja-oid]
  (sql/exec-raw [(str "select ytunnus, oid, organisaatio, rooli, kayttaja, "
                      "voimassa, kyselyid, oppilaitos, toimipaikka "
                      "from kysely_omistaja_view "
                      "where kyselyid = ? and kayttaja=?")
                 [kyselyid kayttaja-oid]]
                :results))

(defn hae-kysymysryhmalla
  [kysymysryhmaid organisaatio]
  (first
    (sql/select :kysymysryhma_organisaatio_view
      (sql/fields :koulutustoimija :valtakunnallinen)
      (sql/where (and {:kysymysryhmaid kysymysryhmaid}
                      (or {:valtakunnallinen true}
                          {:koulutustoimija organisaatio}))))))

(defn hae-rooli [rooli kayttaja organisaatio]
  (sql/select rooli-organisaatio
    (sql/where {:rooli rooli
                :kayttaja kayttaja
                :organisaatio organisaatio})))

(defn hae-roolit [oid]
  (sql/select rooli-organisaatio
    (sql/fields :rooli :organisaatio)
    (sql/where {:kayttaja oid
                :voimassa true})))

(defn olemassa? [k]
  (boolean (hae-rooli (:rooli k) (:kayttaja k) (:organisaatio k))))

(defn ^:integration-api tyhjaa-kayttooikeudet!
  "Merkitään olemassaolevat käyttäjät ja roolit ei-voimassaoleviksi"
  []
  (log/debug "Merkitään olemassaolevat käyttäjät ei-voimassaoleviksi")
  (sql/update kayttaja
    (sql/set-fields {:voimassa false})
    (sql/where {:luotu_kayttaja [= (:oid *kayttaja*)]}))
  (log/debug "Merkitään olemassaolevien käyttäjien roolit ei-voimassaoleviksi")
  (sql/update rooli-organisaatio
    (sql/set-fields {:voimassa false})
    (sql/where {:luotu_kayttaja [= (:oid *kayttaja*)]})))

(defn ^:integration-api paivita-rooli!
  "Päivittää tai lisää käyttäjän roolin"
  [r]
  (log/debug "Päivitetään rooli" (pr-str r))
  (if (olemassa? r)
    (do
      (log/debug "Rooli on jo olemassa, päivitetään tiedot")
      (sql/update rooli-organisaatio
                  (sql/set-fields r)
                  (sql/where {:kayttaja [= (:kayttaja r)]})))
    (do
      (log/debug "Luodaan uusi rooli")
      (sql/insert rooli-organisaatio (sql/values r)))))

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
