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

(ns aipalvastaus.sql.korma-auth
  "SQL Kormalle oma kantayhteyksien hallinta. Sitoo kantayhteyteen vastaajakäyttäjän. BoneCP pool."
  (:require [clojure.tools.logging :as log]))

(def vastaajakayttaja "VASTAAJA")

(defn exec-sql
  "execute sql and close statement."
  [c sql]
  (with-open [stm (.createStatement c)]
    (.execute stm sql)))

(defn auth-onCheckOut
  [c]
  (log/debug "auth user " vastaajakayttaja)
  (try
    (catch IllegalArgumentException e
      (.printStackTrace e))
    (catch Exception e
      (log/error e "Odottamaton poikkeus")))
  (log/debug "con ok" (.hashCode c)))

(defn auth-onCheckIn
  [c]
  (log/debug "connection release ")
  (try
    (catch Exception e
      (log/error e "Odottamaton poikkeus")))
  (log/debug "con release ok" (.hashCode c)))

(defonce customizer-impl-bonecp
  (proxy [com.jolbox.bonecp.hooks.AbstractConnectionHook] []))
