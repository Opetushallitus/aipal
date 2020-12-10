(ns aipal.arkisto.kysymysryhma-sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [clj-time.core :as time]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            [aipal.integraatio.sql.korma :refer [kysymys kysymysryhma]]
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

(defn ei-valtakunnallisia [kysymysryhmaseq]
  (filter #(= false (:valtakunnallinen %)) kysymysryhmaseq))

;; hae-kysymysryhmat palauttaa kaikki kysymysryhmät riippumatta voimassaolosta
(deftest ^:integraatio hae-kysymysryhmat-voimassaolo
  (let [koulutustoimija (test-data/lisaa-koulutustoimija!)]
    (sql/insert kysymysryhma (sql/values [{:nimi_fi "a"
                                           :tila "julkaistu"
                                           :voimassa_alkupvm (time/local-date 1900 1 1)
                                           :voimassa_loppupvm (time/local-date 2000 1 1)
                                           :koulutustoimija (:ytunnus koulutustoimija)}
                                          {:nimi_fi "b"
                                           :tila "julkaistu"
                                           :voimassa_alkupvm (time/local-date 2000 1 1)
                                           :voimassa_loppupvm (time/local-date 2100 1 1)
                                           :koulutustoimija (:ytunnus koulutustoimija)}
                                          {:nimi_fi "c"
                                           :tila "julkaistu"
                                           :voimassa_alkupvm (time/local-date 2100 1 1)
                                           :voimassa_loppupvm (time/local-date 2200 1 1)
                                           :koulutustoimija (:ytunnus koulutustoimija)}]))
    
    (is (= #{"a" "b" "c"} (set (map :nimi_fi (ei-valtakunnallisia (hae-kysymysryhmat (:ytunnus koulutustoimija)))))))
    (is (= #{"b"} (set (map :nimi_fi (ei-valtakunnallisia (hae-kysymysryhmat (:ytunnus koulutustoimija) true))))))))

(deftest ^:integraatio hae-kysymysryhmat-oma-organisaatio
  (let [oma-koulutustoimija (test-data/lisaa-koulutustoimija! {:ytunnus "1111111-1"})
        muu-koulutustoimija (test-data/lisaa-koulutustoimija! {:ytunnus "2222222-2"})]
    (sql/insert kysymysryhma (sql/values [{:nimi_fi "a"
                                           :koulutustoimija (:ytunnus oma-koulutustoimija)}
                                          {:nimi_fi "b"
                                           :koulutustoimija (:ytunnus muu-koulutustoimija)}
                                          {:nimi_fi "c"
                                           :koulutustoimija (:ytunnus oma-koulutustoimija)}]))
    (is (= #{"a" "c"} (set (map :nimi_fi (ei-valtakunnallisia (hae-kysymysryhmat (:ytunnus oma-koulutustoimija)))))))))
