(ns aipal.arkisto.vastaajatunnus 
  (:require [korma.core :as sql]
    [clojure.string :as st])
  (:use [aipal.integraatio.sql.korma]))

(defn hae-kaikki
  "Hae kaikki vastaajatunnukset"
  []
  (->
    (sql/select* vastaajatunnus)
    (sql/fields :kyselykertaid :lukittu :rahoitusmuotoid :tunnus :tutkintotunnus :vastaajatunnusid :vastaajien_lkm :voimassa_alkupvm :voimassa_loppupvm)
    (sql/order :kyselykertaid :DESC)
    sql/exec))

(defn luo-tunnus 
  "Luo yksilöllisen tunnuksen. Ei erikoismerkkejä. Tunnus toimii samalla URL-osoitteen parametrina."
  ([pituus]
  {:post [(and 
            (string? %)
            (= pituus (.length %)))]}
  (apply str (take pituus (repeatedly #(rand-nth "01234567890abcdefghjlkmnopqrstuwvxyzABCDEFGHJLMNOPQRSTUWVXYZ")))))
  ([pituus luodut-tunnukset]
    (first (drop-while #(contains? luodut-tunnukset %)
      (repeatedly #(luo-tunnus pituus))))))
  


