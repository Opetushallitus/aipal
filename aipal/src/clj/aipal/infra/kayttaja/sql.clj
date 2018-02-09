(ns aipal.infra.kayttaja.sql
  (:require [korma.core :as sql]
            [korma.db :as db]
            [clojure.tools.logging :as log]))

(defn with-sql-kayttaja* [oid f]
  (db/transaction
    (try
      (f)
      (catch java.sql.SQLException e
        (log/error "Virhe tietokantafunktiossa" (.getNextException e))
        (throw e))
      (catch Throwable t
        (log/error "Virhe tietokantafunktiossa" t)
        (throw t)))))

(defmacro with-sql-kayttaja [oid & body]
  `(with-sql-kayttaja* ~oid (fn [] ~@body)))
