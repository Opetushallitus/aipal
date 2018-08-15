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
            [korma.db :as kdb]
            [aipal.arkisto.kayttaja :as kayttaja-arkisto]
            [clojure.tools.logging :as log]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vakiot :refer [integraatio-uid]]
            [aipal.integraatio.sql.korma :as taulut]
            [arvo.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [oph.korma.common :refer [select-unique-or-nil update-unique]]
            [clojure.set :as set]))

(defn hae-roolit [oid]
  (db/hae-voimassaolevat-roolit {:kayttajaOid oid}))

(defn hae-oikeudet
  ([oid]
   (kdb/transaction
     (let [kayttaja (kayttaja-arkisto/hae oid)
           roolit (hae-roolit oid)]
       (assoc kayttaja :roolit roolit))))
  ([]
   (hae-oikeudet (:oid *kayttaja*))))

(defn paivita-roolit! [tx k]
  (let [vanhat-roolit (->> (db/hae-roolit tx {:kayttaja (:oid k)})
                          (map #(select-keys % [:rooli :organisaatio]))
                          (into #{}))
        poistuneet-roolit (set/difference
                              vanhat-roolit
                              (into #{} (map #(select-keys % [:rooli :organisaatio]) (:roolit k))))]
    (doseq [r poistuneet-roolit]
      (db/aseta-roolin-tila! tx (merge r {:kayttaja (:oid k) :voimassa false})))
    (doseq [r (:roolit k)]
      (if (contains? vanhat-roolit (select-keys r [:rooli :organisaatio]))
        (db/aseta-roolin-tila! tx (merge r {:kayttaja (:oid k) :voimassa true}))
        (db/lisaa-rooli! tx (assoc r :kayttaja (:oid k)))))
    (db/hae-voimassaolevat-roolit tx {:kayttajaOid (:oid k)})))


(defn paivita-kayttaja! [k]
  (jdbc/with-db-transaction [tx *db*]
    (let [olemassa? (db/hae-kayttaja {:kayttajaOid (:oid k)})]
      (if olemassa?
        (db/paivita-kayttaja! tx {:kayttajaOid (:oid k) :etunimi (:etunimi k) :sukunimi (:sukunimi k)})
        (db/lisaa-kayttaja! tx (assoc k :kayttajaOid (:oid k)))))
    (assoc k :roolit (paivita-roolit! tx k))))
