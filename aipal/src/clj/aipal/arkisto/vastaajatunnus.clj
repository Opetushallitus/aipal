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

(def sallitut-merkit "ACEFHJKLMNPRTWXY347")

(def kyselykerta-select
  (-> (sql/select* taulut/vastaajatunnus)
    (sql/fields :kyselykertaid :lukittu :rahoitusmuotoid :tunnus :tutkintotunnus :vastaajatunnusid :vastaajien_lkm :kaytettavissa)
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

(defn luo-satunnainen-tunnus
  [pituus]
  {:post [(and
            (string? %)
            (= pituus (.length %)))]}
  (apply str (take pituus (repeatedly #(rand-nth sallitut-merkit)))))

(defn luo-tunnuksia
  "Luo keskenään uniikkeja satunnaisia määritellyn pituisia tunnuksia."
  [pituus]
  (distinct
    (repeatedly 10000 #(luo-satunnainen-tunnus pituus))))

(defn vastaajatunnus-olemassa?
  [vastaajatunnus]
  (first
    (sql/select taulut/vastaajatunnus
      (sql/fields :tunnus)
      (sql/where {(sql/sqlfn :upper :tunnus) (clojure.string/upper-case vastaajatunnus)}))))

(defn tallenna-vastaajatunnus!
  [vastaajatunnus]
  (-> (sql/insert taulut/vastaajatunnus
        (sql/values vastaajatunnus))
    :vastaajatunnusid
    hae))

(defn lisaa! [vastaajatunnus]
  {:pre [(pos? (:vastaajien_lkm vastaajatunnus))]}
  (let [tunnusten-lkm (if (:henkilokohtainen vastaajatunnus) (:vastaajien_lkm vastaajatunnus) 1)
        vastaajien-lkm (if (:henkilokohtainen vastaajatunnus) 1 (:vastaajien_lkm vastaajatunnus))
        vastaajatunnus (-> vastaajatunnus
                         (dissoc :henkilokohtainen)
                         (assoc :vastaajien_lkm vastaajien-lkm))]
    (doall
      (take tunnusten-lkm
        (for [tunnus (luo-tunnuksia 6)]
          (when-not (vastaajatunnus-olemassa? tunnus)
            (tallenna-vastaajatunnus! (assoc vastaajatunnus :tunnus tunnus))))))))

(defn lukitse! [kyselykertaid vastaajatunnusid lukitse]
  (sql/update taulut/vastaajatunnus
    (sql/set-fields {:lukittu lukitse})
    (sql/where {:kyselykertaid kyselykertaid
                :vastaajatunnusid vastaajatunnusid})))
