(ns aipal.arkisto.kysymysryhma-sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [clj-time.core :as time]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            [aipal.integraatio.sql.korma :refer [kysymysryhma]]
            [aipal.arkisto.kysymysryhma :refer :all]
            [aipal.sql.test-data-util :refer :all]))

(use-fixtures :each tietokanta-fixture)

;; hae-kysymysryhmat palauttaa kaikki kysymysryhmät riippumatta voimassaolosta
(deftest ^:integraatio hae-kysymysryhmat-voimassaolo
  (let [koulutustoimija (lisaa-koulutustoimija!)]
    (sql/insert kysymysryhma (sql/values [{:nimi_fi "a"
                                           :voimassa_alkupvm (time/local-date 1900 1 1)
                                           :voimassa_loppupvm (time/local-date 2000 1 1)
                                           :koulutustoimija (:ytunnus koulutustoimija)}
                                          {:nimi_fi "b"
                                           :voimassa_alkupvm (time/local-date 2000 1 1)
                                           :voimassa_loppupvm (time/local-date 2100 1 1)
                                           :koulutustoimija (:ytunnus koulutustoimija)}
                                          {:nimi_fi "c"
                                           :voimassa_alkupvm (time/local-date 2100 1 1)
                                           :voimassa_loppupvm (time/local-date 2200 1 1)
                                           :koulutustoimija (:ytunnus koulutustoimija)}]))
    (is (= #{"a" "b" "c"} (set (map :nimi_fi (hae-kysymysryhmat (:ytunnus koulutustoimija))))))
    (is (= #{"b"} (set (map :nimi_fi (hae-kysymysryhmat (:ytunnus koulutustoimija) true)))))))

(deftest ^:integraatio hae-kysymysryhmat-oma-organisaatio
  (let [oma-koulutustoimija (lisaa-koulutustoimija! {:ytunnus "1111111-1"})
        muu-koulutustoimija (lisaa-koulutustoimija! {:ytunnus "2222222-2"})]
    (sql/insert kysymysryhma (sql/values [{:nimi_fi "a"
                                           :koulutustoimija (:ytunnus oma-koulutustoimija)}
                                          {:nimi_fi "b"
                                           :koulutustoimija (:ytunnus muu-koulutustoimija)}
                                          {:nimi_fi "c"
                                           :koulutustoimija (:ytunnus oma-koulutustoimija)}]))
    (is (= #{"a" "c"} (set (map :nimi_fi (hae-kysymysryhmat (:ytunnus oma-koulutustoimija))))))))

(deftest ^:integraatio hae-kysymysryhmat-valtakunnalliset
  (let [koulutustoimija (lisaa-koulutustoimija!)]
    (sql/insert kysymysryhma (sql/values [{:nimi_fi "a"
                                           :valtakunnallinen true}
                                          {:nimi_fi "b"
                                           :valtakunnallinen true}]))
    (is (= #{"a" "b"} (set (map :nimi_fi (hae-kysymysryhmat (:ytunnus koulutustoimija))))))))

;; tarkastetaan ettei haku duplikoi organisaatiolla olevia valtakunnallisia ryhmiä
(deftest ^:integraatio hae-kysymysryhmat-valtakunnalliset-oph
  (let [oph (lisaa-koulutustoimija!  {:ytunnus "1111111-1"})]
    (sql/insert kysymysryhma (sql/values [{:nimi_fi "a"
                                           :valtakunnallinen true
                                           :koulutustoimija (:ytunnus oph)}]))
    (is (= ["a"] (map :nimi_fi (hae-kysymysryhmat (:ytunnus oph)))))))
