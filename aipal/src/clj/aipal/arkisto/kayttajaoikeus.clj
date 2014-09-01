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
            [oph.korma.korma-auth :refer [*current-user-oid*
                                          *current-user-uid*
                                          integraatiokayttaja]])
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
    (let [userid oph.korma.korma-auth/*current-user-uid*]
      (assert (realized? oph.korma.korma-auth/*current-user-oid*) (str "Ongelma sisäänkirjautumisessa. Käyttäjätunnuksella " userid " ei ole käyttöoikeuksia. (uid -> oid epäonnistui)."))
      (hae-oikeudet @oph.korma.korma-auth/*current-user-oid*))))

(defn hae-kyselylla
  [kyselyid kayttaja-oid]
  (sql/exec-raw [(str "select  ytunnus, oid, organisaatio, rooli, kayttaja, voimassa, kyselyid, oppilaitos, toimipaikka from kysely_omistaja_view "
                   "where kyselyid = ? and kayttaja=?") [kyselyid kayttaja-oid]]
             :results))

(defn hae-rooli [rooli kayttaja organisaatio]
  (sql/select rooli-organisaatio
    (sql/where {:rooli rooli
                :kayttaja kayttaja
                :organisaatio organisaatio})))

(defn olemassa? [k]
  (boolean (hae-rooli (:rooli k) (:oid k) (:organisaatio k))))

(defn ^:integration-api tyhjaa-kayttooikeudet!
  "Merkitään olemassaolevat käyttäjät ja roolit ei-voimassaoleviksi"
  []
  (log/debug "Merkitään olemassaolevat käyttäjät ei-voimassaoleviksi")
  (sql/update kayttaja
    (sql/set-fields {:voimassa false})
    (sql/where {:luotu_kayttaja [= @*current-user-oid*]}))
  (log/debug "Merkitään olemassaolevien käyttäjien roolit ei-voimassaoleviksi")
  (sql/update rooli-organisaatio
    (sql/set-fields {:voimassa false})
    (sql/where {:luotu_kayttaja [= @*current-user-oid*]})))

(defn ^:integration-api paivita-rooli!
  "Päivittää tai lisää käyttäjän roolin"
  [k]
  (log/debug "Päivitetään rooli" (pr-str k))
  (if (olemassa? k)
    ;; Päivitetään olemassaoleva rooli merkiten voimassaolevaksi
    (do
      (log/debug "Rooli on jo olemassa, päivitetään tiedot")
      (sql/update rooli-organisaatio
                  (sql/set-fields (assoc k :voimassa true))
                  (sql/where {:kayttaja [= (:kayttaja k)]})))
    ;; Lisätään uusi käyttäjä
    (do
      (log/debug "Luodaan uusi rooli")
      (sql/insert rooli-organisaatio (sql/values k)))))

(defn ^:integration-api paivita-kaikki!
  "Päivittää käyttäjätaulun uusilla käyttäjillä kt."
  [kt]
  {:pre [(= *current-user-uid* integraatiokayttaja)]}
  (db/transaction
    ;; Merkitään nykyiset käyttäjät ei-voimassaoleviksi
    (tyhjaa-kayttooikeudet!)
    (doseq [[_ k] (group-by :oid kt)
            :let [ei-roolia (dissoc (first k) :rooli)]]
      (kayttaja-arkisto/paivita-kayttaja! ei-roolia))
    (doseq [k kt
            :let [vain-rooli (clojure.set/rename-keys (select-keys k [:rooli :oid]) {:oid :kayttaja})]]
      (paivita-rooli! vain-rooli))))
