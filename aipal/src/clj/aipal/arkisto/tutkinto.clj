;; Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.arkisto.tutkinto
  (:require [korma.core :as sql]
            [aipal.integraatio.sql.korma :as taulut]))

(defn ^:integration-api lisaa!
  [tiedot]
  (sql/insert taulut/tutkinto
    (sql/values tiedot)))

(defn ^:integration-api paivita!
  [tutkintotunnus tiedot]
  (sql/update taulut/tutkinto
    (sql/set-fields tiedot)
    (sql/where {:tutkintotunnus tutkintotunnus})))

(defn hae-kaikki
  []
  (sql/select taulut/tutkinto
    (sql/order :tutkintotunnus)))

(defn hae
  [tutkintotunnus]
  (first (sql/select taulut/tutkinto
           (sql/where {:tutkintotunnus tutkintotunnus}))))

(defn hae-koulutustoimijan-tutkinnot
  [y-tunnus]
  (sql/select taulut/koulutustoimija-ja-tutkinto
    (sql/join taulut/tutkinto
              (= :tutkinto.tutkintotunnus :koulutustoimija_ja_tutkinto.tutkinto))
    (sql/where {:koulutustoimija y-tunnus})
    (sql/fields :tutkinto.tutkintotunnus :tutkinto.nimi_fi :tutkinto.nimi_sv)))

(defn hae-tutkinnot
  []
  (sql/select taulut/tutkinto
    (sql/join :inner taulut/opintoala (= :opintoala.opintoalatunnus :tutkinto.opintoala))
    (sql/join :inner taulut/koulutusala (= :koulutusala.koulutusalatunnus :opintoala.koulutusala))
    (sql/fields :tutkinto.tutkintotunnus :tutkinto.nimi_fi :tutkinto.nimi_sv
                :opintoala.opintoalatunnus [:opintoala.nimi_fi :opintoala_nimi_fi] [:opintoala.nimi_sv :opintoala_nimi_sv]
                :koulutusala.koulutusalatunnus [:koulutusala.nimi_fi :koulutusala_nimi_fi] [:koulutusala.nimi_sv :koulutusala_nimi_sv])))
