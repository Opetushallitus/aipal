(ns aipal.arkisto.koulutustoimija
  (:require [korma.core :as sql])
  (:use [aipal.integraatio.sql.korma]))

(defn lisaa! 
  [tiedot]
  (sql/insert koulutustoimija
    (sql/values tiedot)))

(defn hae-kaikki 
  []
  (->
    (sql/select* koulutustoimija)
    (sql/order :ytunnus :DESC)
    sql/exec))
