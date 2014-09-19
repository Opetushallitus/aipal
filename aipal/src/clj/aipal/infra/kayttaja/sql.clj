(ns aipal.infra.kayttaja.sql
  (:require [korma.core :as sql]
            [korma.db :as db]))

(defn with-sql-kayttaja* [oid f]
  (db/transaction
    (try
      (sql/exec-raw (str "set aipal.kayttaja = '" oid "';"))
      (f)
      (finally
        (sql/exec-raw (str "reset aipal.kayttaja;"))))))

(defmacro with-sql-kayttaja [oid & body]
  `(with-sql-kayttaja* ~oid (fn [] ~@body)))
