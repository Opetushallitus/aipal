(ns arvo.db.core
  (:require [hugsql.core :as hugsql]
            [mount.core :as mount]
            [conman.core :as conman]
            [aipal.asetukset :refer [asetukset]]
            [clojure.java.jdbc :as jdbc]
            [cheshire.core :refer [parse-string generate-string]]
            [clj-time.coerce :as c]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import (org.postgresql.jdbc4 Jdbc4Array)
           (org.postgresql.util PGobject)
           (java.sql Timestamp Date PreparedStatement)
           (clojure.lang IPersistentVector IPersistentMap)))

(require 'clj-time.jdbc)

(defn pool-spec []
  (let [db-conf (:db @asetukset)]
    {:jdbc-url (str "jdbc:postgresql://" (:host db-conf)
                    "/"(:name db-conf)"?user="(:user db-conf)"&password=" (:password db-conf))}))

(mount/defstate ^:dynamic *db*
           :start (conman/connect! (pool-spec))
           :stop (conman/disconnect! *db*))

(def query-resources
  (do
    (map #(str "sql/" %) (.list (io/file "resources/sql")))))

;a version of bind-connection that loads all sql resources
(defmacro bind-connection [conn]
  (let [filenames query-resources
        _ (log/info "Loaded sql resources:" filenames)
        options?  (map? (first filenames))
        options   (if options? (first filenames) {})
        filenames (if options? (rest filenames) filenames)]
    `(let [{snips# :snips fns# :fns :as queries#} (conman.core/load-queries '~filenames ~options)]
       (doseq [[id# {fn# :fn {doc# :doc} :meta}] snips#]
         (intern *ns* (with-meta (symbol (name id#)) {:doc doc#}) fn#))
       (doseq [[id# {fn# :fn {doc# :doc} :meta}] fns#]
         (intern *ns* (with-meta (symbol (name id#)) {:doc doc#})
                 (fn
                   ([] (fn# ~conn {}))
                   ([params#] (fn# ~conn params#))
                   ([conn# params#] (fn# conn# params#))
                   ([conn# params# opts# & command-opts#]
                    (apply fn# conn# params# opts# command-opts#)))))
       queries#)))


(bind-connection *db*)

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Date
  (result-set-read-column [v _ _] (c/from-sql-date v))
  ;
  ;java.sql.Timestamp
  ;(result-set-read-column [v _ _] (c/from-sql-time v))

  Jdbc4Array
  (result-set-read-column [v _ _] (vec (.getArray v)))

  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (let [type (.getType pgobj)
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
    (let [conn (.getConnection stmt)
          meta (.getParameterMetaData stmt)
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
