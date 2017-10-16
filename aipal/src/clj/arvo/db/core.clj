(ns arvo.db.core
  (:require [hugsql.core :as hugsql]
            [mount.core :as mount]
            [conman.core :as conman]
            [aipal.asetukset :refer [asetukset]]
            [clojure.java.jdbc :as jdbc]))

(defn pool-spec []
  (let [db-conf (:db @asetukset)]
    {:jdbc-url (str "jdbc:postgresql://" (:host db-conf)
                    "/"(:name db-conf)"?user="(:user db-conf)"&password=" (:password db-conf))}))

(mount/defstate ^:dynamic *db*
           :start (conman/connect! (pool-spec))
           :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/vastaajatunnus.sql" "sql/uraseuranta.sql" "sql/kysymysryhma.sql")
