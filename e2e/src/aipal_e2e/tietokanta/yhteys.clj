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
  (:require [korma.db :as db]
            [aipal-e2e.arkisto.sql.korma]
            [aipal-e2e.tietokanta.data :as data]))

(def testikayttaja-uid "AIPAL-E2E")
(def testikayttaja-oid "OID.AIPAL-E2E")

(def ^:private tietokantaasetukset
  {:host (or (System/getenv "AIPAL_DB_HOST") "127.0.0.1")
   :port (Integer/parseInt (or (System/getenv "AIPAL_DB_PORT") "3456"))
   :name (or (System/getenv "AIPAL_DB_NAME") "aipal")
   :user (or (System/getenv "AIPAL_DB_USER") "aipal_user")
   :password (or (System/getenv "AIPAL_DB_PASSWORD") "aipal")
   :maximum-pool-size 15
   :minimum-pool-size 3})

(defn  alusta-korma!
  []
  (aipal-e2e.arkisto.sql.korma/luo-db tietokantaasetukset))

(defn aseta-testikayttaja!
  []
  (aseta-kayttaja! testikayttaja-oid))

(defn  poista-testidata!
  []
  (data/tyhjenna-testidata! testikayttaja-oid))
