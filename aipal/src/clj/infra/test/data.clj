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

(ns infra.test.data
  (:require [korma.core :as sql]
            [korma.db :as db]
            [oph.korma.common :refer [select-unique-or-nil]]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
            [aipal.infra.kayttaja.vakiot :refer [jarjestelma-uid]]))

(def taulut
  "Taulut vierasavainriippuvuuksien mukaisessa järjestyksessä, ensin taulu josta viitataan myöhemmin nimettyyn."
  ["monivalintavaihtoehto"
   "kysymys"
   "kysely_kysymysryhma"
   "kysely_kysymys"
   "kysymysryhma_kyselypohja"
   "kysymysryhma"
   "jatkokysymys"
   "kyselypohja"
   "vastaus"
   "vastaaja"
   "vastaajatunnus"
   "kyselykerta"
   "kysely"
   "tutkinto"
   "opintoala"
   "koulutusala"
   "koulutustoimija"
   "kayttaja"])

(defn ^:test-api luo-dummy-rivit!
  "Luo jokaiseen tauluun yhden rivin, johon voidaan viitata testeissä, kun
  tarvitaan vierasavainviittaus, jonka kohdedatalla ei ole merkitystä testin
  kannalta." []
  (sql/insert taulut/koulutustoimija
    (sql/values
      {:ytunnus "0000000-0"
       :nimi_fi "Dummy"}))
  (sql/insert taulut/kysely
    (sql/values
      {:kyselyid -1000
       :nimi_fi "Dummy"
       :koulutustoimija "0000000-0"}))
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "Dummy", :kyselyid -1000, :voimassa_alkupvm (sql/raw "now()"),
                 :kyselykertaid -1000})))

(defn ^:test-api tyhjenna-testidata!
  [oid]
  (doseq [taulu taulut]
    (sql/exec-raw (str "delete from " taulu " where luotu_kayttaja = '" oid "'"))))

(defn ^:test-api luo-testikayttaja!
  ([testikayttaja-oid testikayttaja-uid roolitunnus]
   (with-kayttaja jarjestelma-uid nil nil
     (when-not (select-unique-or-nil taulut/kayttaja
                 (sql/where {:oid testikayttaja-oid}))
       (db/transaction
         (sql/insert taulut/kayttaja
           (sql/values
             {:voimassa true
              :sukunimi "Leiningen"
              :etunimi "Testi"
              :uid testikayttaja-uid
              :oid testikayttaja-oid}))
         (sql/insert taulut/rooli_organisaatio
           (sql/values
             {:voimassa true
              :kayttaja testikayttaja-oid
              :rooli roolitunnus}))))))
  ([testikayttaja-oid testikayttaja-uid]
   (luo-testikayttaja! testikayttaja-oid testikayttaja-uid "YLLAPITAJA")))

(defn ^:test-api poista-testikayttaja!
  [testikayttaja-oid]
  (sql/exec-raw (str "delete from rooli_organisaatio where kayttaja = '" testikayttaja-oid "'"))
  (sql/exec-raw (str "delete from kayttaja where oid = '" testikayttaja-oid "'")))
