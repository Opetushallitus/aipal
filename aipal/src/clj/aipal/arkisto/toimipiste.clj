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

(ns aipal.arkisto.toimipiste
  (:require [korma.core :as sql]
            [oph.korma.common :refer [select-unique-or-nil]]
            [aipal.integraatio.sql.korma :as taulut]))

(defn ^:integration-api lisaa!
  [tiedot]
  (sql/insert taulut/toimipiste
    (sql/values tiedot)))

(defn ^:integration-api paivita!
  [oid tiedot]
  (sql/update taulut/toimipiste
    (sql/set-fields tiedot)
    (sql/where {:oid oid})))

(defn ^:integration-api aseta-kaikki-vanhentuneiksi!
  []
  (sql/update taulut/toimipiste
    (sql/set-fields {:voimassa false})))

(defn hae-kaikki
  []
  (->
    (sql/select* taulut/toimipiste)
    (sql/order :toimipistekoodi)
    sql/exec))

(defn ^:integration-api laske-voimassaolo! []
  (sql/update taulut/toimipiste
    (sql/set-fields {:voimassa false})
    (sql/where {:lakkautuspaiva [< (sql/raw "current_date")]})))

(defn hae-oppilaitoksen-voimassaolevat-toimipisteet [oppilaitos]
  (sql/select taulut/toimipiste
    (sql/where {:oppilaitos oppilaitos
                :voimassa true})))
