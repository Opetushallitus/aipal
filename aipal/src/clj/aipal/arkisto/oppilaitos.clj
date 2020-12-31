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

(ns aipal.arkisto.oppilaitos
  (:require [korma.core :as sql]
            [oph.korma.common :refer [select-unique]]
            [clojure.tools.logging :as log]
            [aipal.integraatio.sql.korma :as taulut]))

(defn ^:integration-api lisaa!
  [tiedot]
  (sql/insert taulut/oppilaitos
    (sql/values tiedot)))

(defn ^:integration-api paivita!
  [oppilaitoskoodi tiedot]
  (sql/update taulut/oppilaitos
    (sql/set-fields tiedot)
    (sql/where {:oppilaitoskoodi oppilaitoskoodi})))

(defn ^:integration-api aseta-kaikki-vanhentuneiksi!
  []
  (sql/update taulut/oppilaitos
    (sql/set-fields {:voimassa false})))

(defn hae-kaikki
  []
  (->
    (sql/select* taulut/oppilaitos)
    (sql/order :oppilaitoskoodi)
    sql/exec))

;;avopfi
(defn hae
  [oppilaitosid]
  (select-unique taulut/oppilaitos
    (sql/fields :oppilaitoskoodi :koulutustoimija)
    (sql/where {:oppilaitoskoodi oppilaitosid
                :voimassa true})))
;;end avopfi

(defn hae-koulutustoimijan-oppilaitokset
  [koulutustoimija]
  (->
    (sql/select* taulut/oppilaitos)
    (sql/fields :oppilaitoskoodi :koulutustoimija :nimi_fi :nimi_sv :nimi_en :oppilaitostyyppi)
    (sql/where {:koulutustoimija koulutustoimija
                :voimassa true})
    sql/exec))

(defn ^:integration-api laske-voimassaolo! []
  (sql/update taulut/oppilaitos
    (sql/set-fields {:voimassa false})
    (sql/where {:lakkautuspaiva [< (sql/raw "current_date")]})))