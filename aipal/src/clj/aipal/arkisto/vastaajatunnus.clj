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

(defn hae-kyselykerralla
  "Hae kyselykerran vastaajatunnukset"
  [kyselykertaid]
  (->
    (sql/select* vastaajatunnus)
    (sql/fields :kyselykertaid :lukittu :rahoitusmuotoid :tunnus :tutkintotunnus :vastaajatunnusid :vastaajien_lkm :voimassa_alkupvm :voimassa_loppupvm)
    (sql/fields [(sql/subselect vastaaja
                   (sql/aggregate (count :*) :count)
                   (sql/where {:vastannut true
                               :vastaajatunnusid :vastaajatunnus.vastaajatunnusid})) :vastausten_lkm])
    (sql/where (= :kyselykertaid kyselykertaid))
    (sql/order :muutettuaika :DESC)
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
  [kyselykertaid kentat]
  (sql/insert vastaajatunnus
    (sql/values (merge kentat {:kyselykertaid kyselykertaid
                               :tunnus (luo-tunnus 13)}))))

(defn lisaa-vastaajatunnuksia
  [kyselykertaid henkilokohtainen kentat]
  (if henkilokohtainen
    (doall
      (for [x (range (:vastaajien_lkm kentat))]
        (lisaa! kyselykertaid (assoc kentat :vastaajien_lkm 1))))
    [(lisaa! kyselykertaid kentat)]))


