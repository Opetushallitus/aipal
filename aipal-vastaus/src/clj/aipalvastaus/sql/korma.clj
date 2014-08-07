(ns aipalvastaus.sql.korma
  (:import java.sql.Date
           java.sql.Timestamp
           org.joda.time.DateTime
           org.joda.time.LocalDate)
  (:require korma.db
            [korma.core :as sql]
            [oph.korma.korma-auth :as korma-auth]
            [clj-time.coerce :as time-coerce]
            [clj-time.core :as time]))

(defn korma-asetukset
  "Muuttaa asetustiedoston db-avaimen arvon Korman odottamaan muotoon."
  [db-asetukset]
  (clojure.set/rename-keys db-asetukset {:name :db}))

(defn bonecp-datasource
  "BoneCP based connection pool"
  [db-asetukset]
  (let [korma-postgres (korma.db/postgres (korma-asetukset db-asetukset))
        bonecp-ds  (doto (com.jolbox.bonecp.BoneCPDataSource.)
                     (.setJdbcUrl (str "jdbc:" (:subprotocol korma-postgres) ":" (:subname korma-postgres)))
                     (.setUsername (:user korma-postgres))
                     (.setPassword (:password korma-postgres))
                     (.setConnectionTestStatement "select 42")
                     (.setConnectionTimeoutInMs 2000)
                     (.setDefaultAutoCommit false)
                     (.setMaxConnectionsPerPartition 10)
                     (.setMinConnectionsPerPartition 5)
                     (.setPartitionCount 1)
                     (.setConnectionHook korma-auth/customizer-impl-bonecp)
                     )]
    bonecp-ds))

(defn luo-db [db-asetukset]
  (korma.db/default-connection
    (korma.db/create-db {:make-pool? false
                         :delimiters ""
                         :datasource (bonecp-datasource db-asetukset)})))

(defn convert-instances-of [c f m]
  (clojure.walk/postwalk #(if (instance? c %) (f %) %) m))

(defn joda-datetime->sql-timestamp [m]
  (convert-instances-of org.joda.time.DateTime
                        time-coerce/to-sql-time
                        m))

(defn sql-timestamp->joda-datetime [m]
  (convert-instances-of java.sql.Timestamp
                        time-coerce/from-sql-time
                        m))

(defn ^:private to-local-date-default-tz
  [date]
  (let [dt (time-coerce/to-date-time date)]
    (time-coerce/to-local-date (time/to-time-zone dt (time/default-time-zone)))))

(defn sql-date->joda-date [m]
  (convert-instances-of java.sql.Date
                        to-local-date-default-tz
                        m))

(defn joda-date->sql-date [m]
  (convert-instances-of org.joda.time.LocalDate
                        time-coerce/to-sql-date
                        m))
(defmacro defentity
  "Wrapperi Korman defentitylle, lisää yleiset prepare/transform-funktiot."
  [ent & body]
  `(sql/defentity ~ent
                  (sql/prepare joda-date->sql-date)
                  (sql/prepare joda-datetime->sql-timestamp)
                  (sql/transform sql-date->joda-date)
                  (sql/transform sql-timestamp->joda-datetime)
                  ~@body))

(defn entity-alias [entity alias]
  (assoc entity :name alias
                :alias alias))

;; Korma ei salli useampaa kuin yhtä linkkiä samojen entityjen välillä.
;; Tämä makro tekee kopion entitystä uudelle nimelle.
(defmacro defalias [alias entity]
  `(def ~alias (entity-alias ~entity ~(name alias))))


(def validationquery "select count(*) from kayttajarooli")

(defn validate-connection!
  []
  (first
    (sql/select validationquery)))

