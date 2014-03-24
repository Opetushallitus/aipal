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

(ns aipal-e2e.tietokanta.yhteys
  (:require [aipal-e2e.arkisto.sql.korma]
            [aipal-e2e.tietokanta.data :as data]))

(def ^:private jarjestelmakayttaja-oid "JARJESTELMA")
(def ^:private testikayttaja-uid "AIPAL-E2E")
(def ^:private testikayttaja-oid "OID.AIPAL-E2E")

(def ^:private tietokantaasetukset
  {:host (or (System/getenv "AIPAL_DB_HOST") "127.0.0.1")
   :port (or (System/getenv "AIPAL_DB_PORT") "3456")
   :name (or (System/getenv "AIPAL_DB_NAME") "aipal")
   :user (or (System/getenv "AIPAL_DB_USER") "aipal_user")
   :password (or (System/getenv "AIPAL_DB_PASSWORD") "aipal")
   :maximum-pool-size "15"
   :minimum-pool-size "3"})

(defn ^:private alusta-korma!
  []
  (aipal-e2e.arkisto.sql.korma/luo-db tietokantaasetukset))

(defn ^:private aseta-kayttaja!
  [oid]
  (aipal-e2e.arkisto.sql.korma/aseta-kayttaja oid))

(defn ^:private luo-testikayttaja!
  []
  (aseta-kayttaja! jarjestelmakayttaja-oid)
  (data/luo-testikayttaja! testikayttaja-oid testikayttaja-uid))

(defn ^:private aseta-testikayttaja!
  []
  (aseta-kayttaja! testikayttaja-oid))

(defn ^:private poista-testikayttaja!
  []
  (data/poista-testikayttaja! testikayttaja-oid))

(defn muodosta-yhteys
  [f]
  (let [db (alusta-korma!)]
    (luo-testikayttaja!)
    (aseta-testikayttaja!)
    (try
      (f)
      (finally
        (data/tyhjenna-testidata! testikayttaja-oid)
        (poista-testikayttaja!)
        (-> db :pool :datasource .close)))))
