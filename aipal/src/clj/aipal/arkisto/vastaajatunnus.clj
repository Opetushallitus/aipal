;; Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
;;
;; This program is free software:  Licensed under the EUPL, Version 1.1 or - as
;; soon as they will be approved by the European Commission - subsequent versions
;; of the EUPL (the "Licence");
;;
;; You may not use this work except in compliance with the Licence.
;; You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; European Union Public Licence for more details.

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

(defn hae-kyselykerralla
  "Hae kyselykerran vastaajatunnukset"
  [kyselykertaid]
  (->
    (sql/select* vastaajatunnus)
    (sql/fields :kyselykertaid :lukittu :rahoitusmuotoid :tunnus :tutkintotunnus :vastaajatunnusid :vastaajien_lkm :voimassa_alkupvm :voimassa_loppupvm)
    (sql/where (= :kyselykertaid kyselykertaid))
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

(defn lisaa!
  "Lisää uuden vastaajatunnuksen tietokantaan"
  [kyselykertaid rahoitusmuotoid tutkintotunnus voimassa_alkupvm voimassa_loppupvm]
  (let [t (luo-tunnus 6)]
    (sql/insert vastaajatunnus
      (sql/values {:kyselykertaid kyselykertaid
                   :rahoitusmuotoid rahoitusmuotoid
                   :tutkintotunnus tutkintotunnus
                   :voimassa_alkupvm voimassa_alkupvm
                   :voimassa_loppupvm voimassa_loppupvm
                   :lukittu false
                   :vastaajien_lkm 1
                   :tunnus t}))))


