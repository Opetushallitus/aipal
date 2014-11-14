(ns aipalvastaus.sql.korma
  (:require korma.db
            [korma.core :as sql]
            [aipalvastaus.sql.korma-auth :as korma-auth]))

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

(defn vastaajatunnus-where
  "Käytetään vastaajatunnuksella tehtäviin hakuihin. Vertailee tunnusta merkistökoosta riippumatta."
  [query tunnus]
  (sql/where query {(sql/sqlfn :upper :vastaajatunnus.tunnus) (clojure.string/upper-case tunnus)}))
