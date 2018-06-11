(ns arvo.db.migrations
  (:require [migratus.core :as migratus]
            [aipal.asetukset :refer [asetukset]]
            [clojure.tools.logging :as log]))

(defn parse-ids [args]
  (map #(Long/parseLong %) (rest args)))

(defn migrate [args]
  (log/info "Ajetaan kantamigraatiot" args)
  (let [db-conf (:db @asetukset)
        config {:store :database
                :db {:connection-uri (str "jdbc:postgresql://" (:host db-conf)
                                          "/"(:name db-conf)"?user="(:migration-user db-conf)"&password=" (:migration-password db-conf))}}]
    (case (first args)
      "migrate"
      (if (> (count args) 1)
        (apply migratus/up config (parse-ids args))
        (migratus/migrate config))
      "rollback"
      (if (> (count args) 1)
        (apply migratus/down config (parse-ids args))
        (migratus/rollback config)))))
