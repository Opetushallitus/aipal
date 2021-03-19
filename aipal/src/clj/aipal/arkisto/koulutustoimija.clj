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
            [aipal.integraatio.sql.korma :as taulut]
            [clojure.tools.logging :as log]
            [arvo.db.core :refer [*db*] :as db]))

(def oph-koulutustoimija {:ytunnus "0920632-0"})

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

;;avopfi
(defn hae-kentat
  [y-tunnus]
  (first (sql/select taulut/koulutustoimija
                     (sql/fields :ytunnus)
                     (sql/where {:ytunnus y-tunnus}))))

;;end avopfi
(defn hae-kaikki
  []
  (->
    (sql/select* taulut/koulutustoimija)
    (sql/fields :ytunnus :nimi_fi :nimi_sv :nimi_en)
    (sql/order :ytunnus :DESC)
    sql/exec))

(defn hae-koulutusluvalliset []
  (db/hae-koulutustoimijat-joilla-koulutuslupa))

(defn hae-nimella [termi]
  (db/hae-koulutustoimija-nimella {:termi termi}))

(defn hae-kaikki-organisaatiopalvelulle
  "Hakee kaikista koulutustoimijoista organisaatiopalveluintegraation tarvitsemat tiedot"
  []
  (sql/select taulut/koulutustoimija
              (sql/fields :ytunnus :nimi_fi :nimi_sv :nimi_en :osoite :postinumero :postitoimipaikka
                          :puhelin :www_osoite :sahkoposti :oid :voimassa :lakkautuspaiva)))

(defn hae-kaikki-joissa-oid
  []
  (sql/select taulut/koulutustoimija
              (sql/fields :oid :ytunnus)
              (sql/where (not= :oid nil))))

(defn ^:integration-api lisaa-koulutustoimijalle-tutkinto!
  [y-tunnus tutkintotunnus alkupvm loppupvm]
  (sql/insert taulut/koulutustoimija_ja_tutkinto
              (sql/values {:koulutustoimija   y-tunnus
                           :tutkinto          tutkintotunnus
                           :voimassa_alkupvm  alkupvm
                           :voimassa_loppupvm loppupvm})))

(defn ^:integration-api aseta-kaikki-vanhentuneiksi!
  []
  (sql/update taulut/koulutustoimija
              (sql/set-fields {:voimassa false})))

(defn ^:integration-api laske-voimassaolo! []
  (sql/update taulut/koulutustoimija
              (sql/set-fields {:voimassa false})
              (sql/where {:lakkautuspaiva [< (sql/raw "current_date")]})))
