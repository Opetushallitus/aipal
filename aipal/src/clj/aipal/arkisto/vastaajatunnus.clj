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
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.auditlog :as auditlog]))

(def sallitut-merkit "ACEFHJKLMNPRTWXY347")

(def kyselykerta-select
  (-> (sql/select* taulut/vastaajatunnus)
    (sql/join :left taulut/tutkinto (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus))
    (sql/fields :kyselykertaid :lukittu :rahoitusmuotoid :tunnus :tutkintotunnus :vastaajatunnusid :vastaajien_lkm :kaytettavissa
                :tutkinto.nimi_fi :tutkinto.nimi_sv)
    (sql/fields [(sql/subselect taulut/vastaaja
                   (sql/aggregate (count :*) :count)
                   (sql/where {:vastannut true
                               :vastaajatunnusid :vastaajatunnus.vastaajatunnusid})) :vastausten_lkm])
    (sql/order :luotuaika :DESC)))

(defn ^:private erota-tutkinto
  [vastaajatunnus]
  (let [tutkinto (select-keys vastaajatunnus [:nimi_fi :nimi_sv :tutkintotunnus])]
    (some-> vastaajatunnus
      (dissoc :nimi_fi :nimi_sv :tutkintotunnus)
      (assoc :tutkinto tutkinto))))

(defn hae-kyselykerralla
  "Hae kyselykerran vastaajatunnukset"
  [kyselykertaid]
  (-> kyselykerta-select
    (sql/where (= :kyselykertaid kyselykertaid))
    sql/exec
    (->> (map erota-tutkinto))))

(defn hae [id]
  (-> kyselykerta-select
    (sql/where {:vastaajatunnusid id})
    sql/exec
    first
    erota-tutkinto))

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

(defn tallenna-vastaajatunnus! [vastaajatunnus]
  (let [vastaajatunnus (-> (sql/insert taulut/vastaajatunnus
                             (sql/values vastaajatunnus))
                         :vastaajatunnusid
                         hae)]
    (auditlog/vastaajatunnus-luonti! vastaajatunnus (:kyselykertaid vastaajatunnus))
    vastaajatunnus))
        

(defn lisaa! [vastaajatunnus]
  {:pre [(pos? (:vastaajien_lkm vastaajatunnus))]}
  (let [tunnusten-lkm (if (:henkilokohtainen vastaajatunnus) (:vastaajien_lkm vastaajatunnus) 1)
        vastaajien-lkm (if (:henkilokohtainen vastaajatunnus) 1 (:vastaajien_lkm vastaajatunnus))
        tutkintotunnus (get-in vastaajatunnus [:tutkinto :tutkintotunnus])
        valmistavan-koulutuksen-jarjestaja (get-in vastaajatunnus [:koulutuksen_jarjestaja :ytunnus])
        vastaajatunnus (-> vastaajatunnus
                         (dissoc :henkilokohtainen :tutkinto :koulutuksen_jarjestaja)
                         (assoc :vastaajien_lkm vastaajien-lkm
                                :tutkintotunnus tutkintotunnus
                                :valmistavan_koulutuksen_jarjestaja valmistavan-koulutuksen-jarjestaja))]
    (doall
      (take tunnusten-lkm
        (for [tunnus (luo-tunnuksia 6)]
          (when-not (vastaajatunnus-olemassa? tunnus)
            (tallenna-vastaajatunnus! (assoc vastaajatunnus :tunnus tunnus))))))))

(defn aseta-lukittu! [kyselykertaid vastaajatunnusid lukitse]
  (auditlog/vastaajatunnus-muokkaus! vastaajatunnusid kyselykertaid lukitse)
  (sql/update taulut/vastaajatunnus
    (sql/set-fields {:lukittu lukitse})
    (sql/where {:kyselykertaid kyselykertaid
                :vastaajatunnusid vastaajatunnusid}))
  ;; haetaan vastaajatunnus, jotta saadaan kaytettavissa arvo
  (hae vastaajatunnusid))
