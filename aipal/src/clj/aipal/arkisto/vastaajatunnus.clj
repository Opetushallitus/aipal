(ns aipal.arkisto.vastaajatunnus 
  (:require [korma.core :as sql])
  (:use [aipal.integraatio.sql.korma]))

(defn hae-kaikki
  "Hae kaikki vastaajatunnukset"
  []
  (->
    (sql/select* vastaajatunnus)
    (sql/fields :kyselykertaid :lukittu :rahoitusmuotoid :tunnus :tutkintotunnus :vastaajatunnusid :vastaajien_lkm :voimassa_alkupvm :voimassa_loppupvm)
    (sql/order :kyselykertaid :DESC)
    sql/exec))



