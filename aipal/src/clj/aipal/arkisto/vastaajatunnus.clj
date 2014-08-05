(ns aipal.arkisto.vastaajatunnus 
  (:require [korma.core :as sql]
    [clojure.string :as st])
  (:use [aipal.integraatio.sql.korma]))

(def sallitut-url-merkit 
  "Merkit, joista vastaajatunnus muodostetaan. Ei erikoismerkkejä, koska näistä tulee samalla URL-osoite vastaajan selainta varten."
  "01234567890abcdefghjlkmnopqrstuwvxyzABCDEFGHJLMNOPQRSTUWVXYZ")


(defn hae-kaikki
  "Hae kaikki vastaajatunnukset"
  []
  (->
    (sql/select* vastaajatunnus)
    (sql/fields :kyselykertaid :lukittu :rahoitusmuotoid :tunnus :tutkintotunnus :vastaajatunnusid :vastaajien_lkm :voimassa_alkupvm :voimassa_loppupvm)
    (sql/order :kyselykertaid :DESC)
    sql/exec))

(defn luo-tunnus 
  "Luo yksilöllisen tunnuksen. "
  ([pituus]
  {:post [(and 
            (string? %)
            (= pituus (.length %)))]}
  (apply str (take pituus (repeatedly #(rand-nth sallitut-url-merkit)))))
  ([pituus luodut-tunnukset]
    (first (drop-while #(contains? luodut-tunnukset %)
      (take 10000 (repeatedly #(luo-tunnus pituus)))))))


