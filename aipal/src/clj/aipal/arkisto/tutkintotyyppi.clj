;; Copyright (c) 2016 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.arkisto.tutkintotyyppi
  (:require [korma.core :as sql]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(defn hae-kaikki []
  (->
    (sql/select* taulut/tutkintotyyppi)
    (sql/fields :tutkintotyyppi :nimi_fi, :nimi_sv, :nimi_en)
    (sql/order :tutkintotyyppi :ASC)
    sql/exec))

(defn hae-kayttajalle []
  (let [tutkintotyypit (mapcat vals (sql/select taulut/oppilaitostyyppi_tutkintotyyppi
                                                (sql/where {:oppilaitostyyppi (:oppilaitostyypit *kayttaja*)})
                                                (sql/fields :tutkintotyyppi)))]
    (->
      (sql/select* taulut/tutkintotyyppi)
      (sql/fields :tutkintotyyppi :nimi_fi, :nimi_sv, :nimi_en)
      (sql/where {:tutkintotyyppi [in tutkintotyypit]})
      (sql/order :tutkintotyyppi :ASC)
      sql/exec)))

(defn ^:integration-api lisaa!
  [tutkintotyyppi]
  (sql/insert taulut/tutkintotyyppi
    (sql/values tutkintotyyppi)))

(defn ^:integration-api paivita!
  [tutkintotyyppi tiedot]
  (sql/update taulut/tutkintotyyppi
    (sql/set-fields tiedot)
    (sql/where {:tutkintotyyppi tutkintotyyppi})))