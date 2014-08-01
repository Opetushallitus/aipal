(ns aipal.integraatio.sql.korma
  (:require 
    [korma.core :as sql]
    [aitu.integraatio.sql.korma  :refer [defentity]]))

(defentity kyselykerta
  (sql/pk :kyselykertaid))
(defentity kysely
  (sql/pk :kyselyid)
  (sql/has-many kyselykerta {:fk :kyselyid}))
