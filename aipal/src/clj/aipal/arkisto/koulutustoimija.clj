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

(ns aipal.arkisto.koulutustoimija
  (:require [korma.core :as sql]
            [aipal.integraatio.sql.korma :as taulut])
  (:use [aipal.integraatio.sql.korma]))

(defn ^:integration-api lisaa!
  [tiedot]
  (sql/insert taulut/koulutustoimija
    (sql/values tiedot)))

(defn ^:integration-api paivita!
  [y-tunnus tiedot]
  (sql/update taulut/koulutustoimija
    (sql/set-fields tiedot)
    (sql/where {:ytunnus y-tunnus})))

(defn hae
  [y-tunnus]
  (first (sql/select taulut/koulutustoimija
           (sql/where {:ytunnus y-tunnus}))))

(defn hae-kaikki
  []
  (->
    (sql/select* taulut/koulutustoimija)
    (sql/fields :ytunnus :nimi_fi :nimi_sv)
    (sql/order :ytunnus :DESC)
    sql/exec))

(defn hae-kaikki-organisaatiopalvelulle
  "Hakee kaikista koulutustoimijoista organisaatiopalveluintegraation tarvitsemat tiedot"
  []
  (sql/select taulut/koulutustoimija
    (sql/fields :ytunnus :nimi_fi :nimi_sv :osoite :postinumero :postitoimipaikka
                :puhelin :www_osoite :sahkoposti :oid)))

(defn hae-kaikki-joissa-oid
  []
  (sql/select taulut/koulutustoimija
    (sql/fields :oid :ytunnus)
    (sql/where (not= :oid nil))))

(defn ^:integration-api lisaa-koulutustoimijalle-tutkinto!
  [y-tunnus tutkintotunnus]
  (sql/insert taulut/koulutustoimija_ja_tutkinto
    (sql/values {:koulutustoimija y-tunnus
                 :tutkinto tutkintotunnus})))

(defn ^:integration-api poista-kaikki-koulutustoimijoiden-tutkinnot!
  []
  (sql/delete taulut/koulutustoimija_ja_tutkinto))
