(ns arvo.db.core
  (:require [hugsql.core :as hugsql]
            [mount.core :as mount]
            [conman.core :as conman]
            [aipal.asetukset :refer [asetukset]]
            [clojure.java.jdbc :as jdbc]
            [cheshire.core :refer [parse-string generate-string]]
            [clj-time.coerce :as c])
  (:import (org.postgresql.jdbc4 Jdbc4Array)
           (org.postgresql.util PGobject)
           (java.sql Timestamp Date PreparedStatement)
           (clojure.lang IPersistentVector IPersistentMap)))

;(require 'clj-time.jdbc)

(defn pool-spec []
  (let [db-conf (:db @asetukset)]
    {:jdbc-url (str "jdbc:postgresql://" (:host db-conf)
                    "/"(:name db-conf)"?user="(:user db-conf)"&password=" (:password db-conf))}))

(mount/defstate ^:dynamic *db*
           :start (conman/connect! (pool-spec))
           :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/vastaajatunnus.sql" "sql/vastaus.sql" "sql/uraseuranta.sql" "sql/kysymysryhma.sql" "sql/tutkinto.sql"
                             "sql/kyselykerta.sql" "sql/koodisto.sql" "sql/kayttaja.sql" "sql/vipunen.sql" "sql/kysely.sql")

;(defn to-date [sql-date]
;  (-> sql-date (.getTime) (java.util.Date.)))

(extend-protocol jdbc/IResultSetReadColumn
  ;java.sql.Date
  ;(result-set-read-column [v _ _] (c/from-sql-date v))
  ;
  ;java.sql.Timestamp
  ;(result-set-read-column [v _ _] (c/from-sql-time v))

  Jdbc4Array
  (result-set-read-column [v _ _] (vec (.getArray v)))

  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (parse-string value true)
        "jsonb" (parse-string value true)
        "citext" (str value)
        value))))

(extend-type java.util.Date
  jdbc/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt ^long idx]
    (.setTimestamp stmt idx (Timestamp. (.getTime v)))))

(extend-type org.joda.time.DateTime
  jdbc/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (c/to-sql-time v))))

(extend-type org.joda.time.LocalDate
  jdbc/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (c/to-sql-time v))))

(extend-type clojure.lang.IPersistentVector
  jdbc/ISQLParameter
  (set-parameter [v ^java.sql.PreparedStatement stmt ^long idx]
    (let [conn      (.getConnection stmt)
          meta      (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta idx)]
      (if-let [elem-type (when (= (first type-name) \_) (apply str (rest type-name)))]
        (.setObject stmt idx (.createArrayOf conn elem-type (to-array v)))
        (.setObject stmt idx v)))))

(defn to-pg-json [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(extend-protocol jdbc/ISQLValue
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value)))
