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

(ns aipal.arkisto.kayttaja
  (:require [korma.core :as sql]
            [korma.db :as db]
            [clojure.tools.logging :as log]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.toimiala.kayttajaroolit :refer [kayttajaroolit]]
            [oph.common.util.util :refer [sisaltaako-kentat?]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vakiot
             :refer [jarjestelma-oid integraatio-uid integraatio-oid
                     konversio-oid vastaaja-oid]]))


(defn hae
  "Hakee käyttäjätunnuksen perusteella."
  [oid]
  (first (sql/select taulut/kayttaja (sql/where {:oid oid}))))

(defn hae-voimassaoleva [uid]
  (first (sql/select taulut/kayttaja (sql/where {:uid uid, :voimassa true}))))

(defn olemassa? [k]
  (boolean (hae (:oid k))))

(defn ^:integration-api paivita!
  "Päivittää käyttäjätaulun uusilla käyttäjillä kt."
  [kt]
  {:pre [(= (:uid *kayttaja*) integraatio-uid)]}
  (db/transaction
    ;; Merkitään nykyiset käyttäjät ei-voimassaoleviksi
    (log/debug "Merkitään olemassaolevat käyttäjät ei-voimassaoleviksi")
    (sql/update taulut/kayttaja
      (sql/set-fields {:voimassa false})
      (sql/where {:luotu_kayttaja [= (:oid *kayttaja*)]}))
    (doseq [k kt]
      (log/debug "Päivitetään käyttäjä" (pr-str k))
      (if (olemassa? k)
        ;; Päivitetään olemassaoleva käyttäjä merkiten voimassaolevaksi
        (do
          (log/debug "Käyttäjä on jo olemassa, päivitetään tiedot")
          (sql/update taulut/kayttaja
          (sql/set-fields (assoc k :voimassa true))
          (sql/where {:oid [= (:oid k)]})))
        ;; Lisätään uusi käyttäjä
        (do
          (log/debug "Luodaan uusi käyttäjä")
          (sql/insert taulut/kayttaja (sql/values k)))))))

(defn hae-impersonoitava-termilla
  "Hakee impersonoitavia käyttäjiä termillä"
  [termi]
  (for [kayttaja (sql/select taulut/kayttaja
                   (sql/fields :oid :uid :etunimi :sukunimi)
                   (sql/where (and
                                (sql/sqlfn "not exists"
                                  (sql/subselect taulut/rooli_organisaatio
                                    (sql/fields :rooli_organisaatio_id)
                                    (sql/where {:rooli (:paakayttaja kayttajaroolit)
                                                :kayttaja :kayttaja.oid})))
                                {:oid [not-in [jarjestelma-oid konversio-oid integraatio-oid vastaaja-oid]]})))
        :when (sisaltaako-kentat? kayttaja [:etunimi :sukunimi] termi)]
    {:nimi (str (:etunimi kayttaja) " " (:sukunimi kayttaja) " (" (:uid kayttaja) ")")
     :oid (:oid kayttaja)}))

(defn olemassa? [k]
  (boolean (hae (:oid k))))

(defn ^:integration-api paivita-kayttaja!
  "Päivittää tai lisää käyttäjän"
  [k]
  (log/debug "Päivitetään käyttäjä" (pr-str k))
  (if (olemassa? k)
    ;; Päivitetään olemassaoleva käyttäjä merkiten voimassaolevaksi
    (do
      (log/debug "Käyttäjä on jo olemassa, päivitetään tiedot")
      (sql/update taulut/kayttaja
                  (sql/set-fields k)
                  (sql/where {:oid [= (:oid k)]})))
    ;; Lisätään uusi käyttäjä
    (do
      (log/debug "Luodaan uusi käyttäjä")
      (sql/insert taulut/kayttaja (sql/values k)))))
