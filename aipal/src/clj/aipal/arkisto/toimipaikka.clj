(ns aipal.arkisto.toimipaikka
  (:require [korma.core :as sql])
  (:use [aipal.integraatio.sql.korma]))

(defn lisaa!
  [tiedot]
  (sql/insert toimipaikka
    (sql/values tiedot)))

(defn paivita!
  [toimipaikkakoodi tiedot]
  (sql/update toimipaikka
    (sql/set-fields tiedot)
    (sql/where {:toimipaikkakoodi toimipaikkakoodi})))

(defn hae-kaikki
  []
  (->
    (sql/select* toimipaikka)
    (sql/order :toimipaikkakoodi)
    sql/exec))
