(ns aipal.arkisto.kysymysryhma-sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [clj-time.core :as time]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            [aipal.integraatio.sql.korma :refer [jatkokysymys kysymys kysymysryhma]]
            [aipal.arkisto.kysymysryhma :refer :all]
            [aipal.sql.test-data-util :as test-data]))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio hae-kysymysryhman-kysymykset
  (let [kysymysryhmaid (-> (test-data/lisaa-kysymysryhma!)
                         :kysymysryhmaid)
        kysymysid (-> (test-data/lisaa-kysymys! {:kysymysryhmaid kysymysryhmaid})
                    :kysymysid)]
    (is (= [kysymysid]
           (->> (hae kysymysryhmaid)
             :kysymykset
             (map :kysymysid))))))

(deftest ^:integraatio hae-kysymysryhman-kysymysten-jatkokysymykset
  (let [kysymysryhmaid (-> (test-data/lisaa-kysymysryhma!)
                         :kysymysryhmaid)
        jatkokysymysid (-> (test-data/lisaa-jatkokysymys! {:kylla_teksti_fi "Jatkokysymys"})
                         :jatkokysymysid)]
    (test-data/lisaa-kysymys! {:kysymysryhmaid kysymysryhmaid
                               :jatkokysymysid jatkokysymysid})
    (is (= [{:kylla_teksti_fi "Jatkokysymys"}]
           (->> (hae kysymysryhmaid)
             :kysymykset
             (map :jatkokysymys)
             (map #(select-keys % [:kylla_teksti_fi])))))))

(deftest ^:integraatio hae-kysymysryhman-monivalintakysymysten-vaihtoehdot
  (let [kysymysryhmaid (-> (test-data/lisaa-kysymysryhma!) :kysymysryhmaid)
        kysymysid (-> (test-data/lisaa-kysymys!
                        {:kysymysryhmaid kysymysryhmaid
                         :vastaustyyppi "monivalinta"})
                    :kysymysid)]
    (test-data/lisaa-monivalintavaihtoehto! {:kysymysid kysymysid
                                             :teksti_fi "Vaihtoehto 1"
                                             :jarjestys 1})
    (test-data/lisaa-monivalintavaihtoehto! {:kysymysid kysymysid
                                             :teksti_fi "Vaihtoehto 2"
                                             :jarjestys 2})
    (is (= ["Vaihtoehto 1" "Vaihtoehto 2"]
           (->> (hae kysymysryhmaid)
             :kysymykset
             first
             :monivalintavaihtoehdot
             (map :teksti_fi))))))

;; hae-kysymysryhmat palauttaa kaikki kysymysryhmät riippumatta voimassaolosta
(deftest ^:integraatio hae-kysymysryhmat-voimassaolo
  (let [koulutustoimija (test-data/lisaa-koulutustoimija!)]
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
  (let [oma-koulutustoimija (test-data/lisaa-koulutustoimija! {:ytunnus "1111111-1"})
        muu-koulutustoimija (test-data/lisaa-koulutustoimija! {:ytunnus "2222222-2"})]
    (sql/insert kysymysryhma (sql/values [{:nimi_fi "a"
                                           :koulutustoimija (:ytunnus oma-koulutustoimija)}
                                          {:nimi_fi "b"
                                           :koulutustoimija (:ytunnus muu-koulutustoimija)}
                                          {:nimi_fi "c"
                                           :koulutustoimija (:ytunnus oma-koulutustoimija)}]))
    (is (= #{"a" "c"} (set (map :nimi_fi (hae-kysymysryhmat (:ytunnus oma-koulutustoimija))))))))

(deftest ^:integraatio hae-kysymysryhmat-valtakunnalliset
  (let [koulutustoimija (test-data/lisaa-koulutustoimija!)]
    (sql/insert kysymysryhma (sql/values [{:nimi_fi "a"
                                           :valtakunnallinen true}
                                          {:nimi_fi "b"
                                           :valtakunnallinen true}]))
    (is (= #{"a" "b"} (set (map :nimi_fi (hae-kysymysryhmat (:ytunnus koulutustoimija))))))))

;; tarkastetaan ettei haku duplikoi organisaatiolla olevia valtakunnallisia ryhmiä
(deftest ^:integraatio hae-kysymysryhmat-valtakunnalliset-oph
  (let [oph (test-data/lisaa-koulutustoimija!  {:ytunnus "1111111-1"})]
    (sql/insert kysymysryhma (sql/values [{:nimi_fi "a"
                                           :valtakunnallinen true
                                           :koulutustoimija (:ytunnus oph)}]))
    (is (= ["a"] (map :nimi_fi (hae-kysymysryhmat (:ytunnus oph)))))))
