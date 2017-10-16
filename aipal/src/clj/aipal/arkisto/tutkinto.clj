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
            [oph.korma.common :refer [select-unique-or-nil select-unique]]
            [aipal.integraatio.sql.korma :as taulut]
            [oph.common.util.util :refer [pvm-mennyt-tai-tanaan? pvm-tuleva-tai-tanaan?]]
            [clojure.tools.logging :as log]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

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
  (select-unique-or-nil taulut/tutkinto
    (sql/where {:tutkintotunnus tutkintotunnus})))

;;avop.fi
(defn hae-kentat
  [tutkintotunnus]
  (select-unique taulut/tutkinto
    (sql/fields :tutkintotunnus)
    (sql/where {:tutkintotunnus tutkintotunnus})))
;;end avop.fi

(defn hae-koulutustoimijan-tutkinnot
  [y-tunnus]
  (sql/select taulut/koulutustoimija_ja_tutkinto
    (sql/join taulut/tutkinto
              (= :tutkinto.tutkintotunnus :koulutustoimija_ja_tutkinto.tutkinto))
    (sql/where {:koulutustoimija y-tunnus})
    (sql/fields :tutkinto.tutkintotunnus :tutkinto.nimi_fi :tutkinto.nimi_sv :tutkinto.nimi_en :tutkinto.voimassa_alkupvm :tutkinto.voimassa_loppupvm :tutkinto.siirtymaajan_loppupvm [:koulutustoimija_ja_tutkinto.voimassa_alkupvm :sopimus_alkupvm] [:koulutustoimija_ja_tutkinto.voimassa_loppupvm :sopimus_loppupvm])))

(defn tutkinto-voimassa? [tutkinto]
  (let [{alkupvm :voimassa_alkupvm
         loppupvm :voimassa_loppupvm
         siirtymaajan-loppupvm :siirtymaajan_loppupvm} tutkinto]
    (and (or (nil? alkupvm)
             (pvm-mennyt-tai-tanaan? alkupvm))
         (or (nil? loppupvm)
             (pvm-tuleva-tai-tanaan? loppupvm)
             (and siirtymaajan-loppupvm
                  (pvm-tuleva-tai-tanaan? siirtymaajan-loppupvm))))))

(defn tutkinnot-hierarkiaksi
  [tutkinnot]
  (let [opintoalatMap (group-by #(select-keys % [:opintoalatunnus :opintoala_nimi_fi :opintoala_nimi_sv :opintoala_nimi_en :koulutusalatunnus :koulutusala_nimi_fi :koulutusala_nimi_sv :koulutusala_nimi_en]) tutkinnot)
        opintoalat (for [[opintoala tutkinnot] opintoalatMap]
                     (assoc opintoala :tutkinnot (sort-by :tutkintotunnus (map #(select-keys % [:tutkintotunnus :nimi_fi :nimi_sv :nimi_en]) tutkinnot))))
        koulutusalatMap (group-by #(select-keys % [:koulutusalatunnus :koulutusala_nimi_fi :koulutusala_nimi_sv :koulutusala_nimi_en]) opintoalat)
        koulutusalat (for [[koulutusala opintoalat] koulutusalatMap]
                       (assoc koulutusala :opintoalat (sort-by :opintoalatunnus (map #(select-keys % [:opintoalatunnus :opintoala_nimi_fi :opintoala_nimi_sv :opintoala_nimi_en :tutkinnot]) opintoalat))))]
    (sort-by :koulutusalatunnus koulutusalat)))

(defn hae-tutkinnot []
  (let [tutkintotyypit (mapcat vals (sql/select taulut/oppilaitostyyppi_tutkintotyyppi
                                      (sql/where {:oppilaitostyyppi (:oppilaitostyypit *kayttaja*)})
                                      (sql/fields :tutkintotyyppi)))]
    (sql/select taulut/tutkinto
      (sql/where {:tutkinto.tutkintotyyppi [in tutkintotyypit]})
      (sql/join :inner taulut/opintoala (= :opintoala.opintoalatunnus :tutkinto.opintoala))
      (sql/join :inner taulut/koulutusala (= :koulutusala.koulutusalatunnus :opintoala.koulutusala))
      (sql/fields :tutkinto.tutkintotunnus :tutkinto.nimi_fi :tutkinto.nimi_sv :tutkinto.nimi_en
                  :tutkinto.voimassa_alkupvm :tutkinto.voimassa_loppupvm :tutkinto.siirtymaajan_loppupvm
                  :opintoala.opintoalatunnus [:opintoala.nimi_fi :opintoala_nimi_fi] [:opintoala.nimi_sv :opintoala_nimi_sv] [:opintoala.nimi_en :opintoala_nimi_en]
                                             :koulutusala.koulutusalatunnus [:koulutusala.nimi_fi :koulutusala_nimi_fi] [:koulutusala.nimi_sv :koulutusala_nimi_sv] [:koulutusala.nimi_en :koulutusala_nimi_en]))))

(defn hae-voimassaolevat-tutkinnot-listana []
  (->>
    (hae-tutkinnot)
    (filter tutkinto-voimassa?)
    (map #(select-keys % [:tutkintotunnus :nimi_fi :nimi_sv :nimi_en]))))

(defn hae-voimassaolevat-tutkinnot []
  (tutkinnot-hierarkiaksi
    (filter tutkinto-voimassa? (hae-tutkinnot))))

(defn hae-vanhentuneet-tutkinnot []
  (tutkinnot-hierarkiaksi
    (remove tutkinto-voimassa? (hae-tutkinnot))))
