(ns aipal.db.core
  (:require [hugsql.core :as hugsql]
            [aipal.asetukset :refer [asetukset]]))

(defn db []
  (let [db-conf (:db @asetukset)]
    {:classname "org.postgresql.Driver"
     :subprotocol "postgresql"
     :subname (str "//" (:host db-conf)":"(:port db-conf)"/"(:name db-conf))
     :user (-> :user db-conf)
     :password (:password db-conf)
     }
    ))
