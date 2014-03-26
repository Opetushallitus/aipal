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

(ns aipal-e2e.tietokanta.data
  (:require [korma.core :as sql]
            [korma.db :as db]
            [aipal-e2e.arkisto.sql.korma :refer :all]))

(def ^:private yllapitajarooli "YLLAPITAJA")

(def ^:private taulut ["kysely"])

(defn tyhjenna-testidata!
  [oid]
  (doseq [taulu taulut]
    (sql/exec-raw (str "delete from " taulu " where luotu_kayttaja = '" oid "'"))))

(defn luo-testikayttaja!
  ([testikayttaja-oid testikayttaja-uid roolitunnus]
  (when-not (first (sql/select kayttaja
                               (sql/where {:oid testikayttaja-oid})))
    (db/transaction
      (sql/insert kayttaja
                  (sql/values
                    {:uid testikayttaja-uid
                     :oid testikayttaja-oid
                     :rooli roolitunnus
                     :etunimi "E2E"
                     :sukunimi "AIPAL"
                     :voimassa true})))))
  ([testikayttaja-oid testikayttaja-uid]
    (luo-testikayttaja! testikayttaja-oid testikayttaja-uid yllapitajarooli)))

(defn poista-testikayttaja!
  [testikayttaja-oid]
  (sql/exec-raw (str "delete from kayttaja where oid = '" testikayttaja-oid "'")))
