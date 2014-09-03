(ns aipal.arkisto.oppilaitos
  (:require [korma.core :as sql])
  (:use [aipal.integraatio.sql.korma]))

(defn lisaa!
  [tiedot]
  (sql/insert oppilaitos
    (sql/values tiedot)))

(defn paivita!
  [oppilaitoskoodi tiedot]
  (sql/update oppilaitos
    (sql/set-fields tiedot)
    (sql/where {:oppilaitoskoodi oppilaitoskoodi})))

(defn hae-kaikki
  []
  (->
    (sql/select* oppilaitos)
    (sql/order :oppilaitoskoodi)
    sql/exec))
