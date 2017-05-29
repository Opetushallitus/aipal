(ns aipal.db.vastaajatunnus
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "aipal/db/sql/vastaajatunnus.sql")
(hugsql/def-sqlvec-fns "aipal/db/sql/vastaajatunnus.sql")
