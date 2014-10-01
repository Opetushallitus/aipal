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
  (:require [clojure.string :as st]
            [korma.core :as sql]
            [aipal.integraatio.sql.korma :as taulut]))

(def sallitut-url-merkit
  "Merkit, joista vastaajatunnus muodostetaan. Ei erikoismerkkejä, koska näistä tulee samalla URL-osoite vastaajan selainta varten."
  "01234567890abcdefghjlkmnopqrstuwvxyzABCDEFGHJLMNOPQRSTUWVXYZ")

(def kyselykerta-select
  (-> (sql/select* taulut/vastaajatunnus)
    (sql/fields :kyselykertaid :lukittu :rahoitusmuotoid :tunnus :tutkintotunnus :vastaajatunnusid :vastaajien_lkm :voimassa_alkupvm :voimassa_loppupvm
                [(sql/raw "((voimassa_alkupvm IS NULL OR voimassa_alkupvm < now()) AND (voimassa_loppupvm IS NULL OR voimassa_loppupvm >= now()))") :voimassa])
    (sql/fields [(sql/subselect taulut/vastaaja
                   (sql/aggregate (count :*) :count)
                   (sql/where {:vastannut true
                               :vastaajatunnusid :vastaajatunnus.vastaajatunnusid})) :vastausten_lkm])
    (sql/order :luotuaika :DESC)))

(defn hae-kyselykerralla
  "Hae kyselykerran vastaajatunnukset"
  [kyselykertaid]
  (-> kyselykerta-select
    (sql/where (= :kyselykertaid kyselykertaid))
    sql/exec))

(defn hae [id]
  (-> kyselykerta-select
    (sql/where {:vastaajatunnusid id})
    sql/exec
    first))

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

(defn lisaa! [vastaajatunnus]
  {:pre [(pos? (:vastaajien_lkm vastaajatunnus))]}
  (letfn [(lisaa-1! [vastaajatunnus]
            (-> (sql/insert taulut/vastaajatunnus
                  (sql/values (-> vastaajatunnus
                                (dissoc :henkilokohtainen)
                                (assoc :tunnus (luo-tunnus 13)))))
              :vastaajatunnusid
              hae))]
    (if (:henkilokohtainen vastaajatunnus)
      (doall (map lisaa-1! (repeat (:vastaajien_lkm vastaajatunnus)
                                   (assoc vastaajatunnus :vastaajien_lkm 1))))
      [(lisaa-1! vastaajatunnus)])))

(defn lukitse! [kyselykertaid vastaajatunnusid lukitse]
  (sql/update taulut/vastaajatunnus
    (sql/set-fields {:lukittu lukitse})
    (sql/where {:kyselykertaid kyselykertaid
                :vastaajatunnusid vastaajatunnusid})))
