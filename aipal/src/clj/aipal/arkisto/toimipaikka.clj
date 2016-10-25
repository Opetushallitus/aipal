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

(ns aipal.arkisto.toimipaikka
  (:require [korma.core :as sql]
            [oph.korma.common :refer [select-unique-or-nil]]
            [aipal.integraatio.sql.korma :as taulut]))

(defn ^:integration-api lisaa!
  [tiedot]
  (sql/insert taulut/toimipaikka
    (sql/values tiedot)))

(defn ^:integration-api paivita!
  [toimipaikkakoodi tiedot]
  (sql/update taulut/toimipaikka
    (sql/set-fields tiedot)
    (sql/where {:toimipaikkakoodi toimipaikkakoodi})))

(defn ^:integration-api aseta-kaikki-vanhentuneiksi!
  []
  (sql/update taulut/toimipaikka
    (sql/set-fields {:voimassa false})))

(defn hae-kaikki
  []
  (->
    (sql/select* taulut/toimipaikka)
    (sql/order :toimipaikkakoodi)
    sql/exec))

(defn ^:integration-api laske-voimassaolo! []
  (sql/update taulut/toimipaikka
    (sql/set-fields {:voimassa false})
    (sql/where {:lakkautuspaiva [< (sql/raw "current_date")]})))

(defn ^:integration-api hae-oppilaitoksen-toimipaikat [oppilaitos]
  (sql/select taulut/toimipaikka
              (sql/fields :toimipaikkakoodi :oppilaitos :nimi_fi :nimi_sv :nimi_en :kunta)
              (sql/where {:oppilaitos oppilaitos})))


(defn hae-oppilaitoksen-voimassaolevat-toimipaikat [oppilaitos]
  (sql/select taulut/toimipaikka
    (sql/where {:oppilaitos oppilaitos
                :voimassa true})))
