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

(deftest ^:integraatio hae-kysymysryhmat-valtakunnalliset
  (is (= #{3341886 1 3341887 4 3341885 3341884 3341888 3342148 3342146 3342149 3342147 3 2} (set (map :kysymysryhmaid (hae-kysymysryhmat nil))))))

;; tarkastetaan ettei haku duplikoi organisaatiolla olevia valtakunnallisia ryhmiä
;(deftest ^:integraatio hae-kysymysryhmat-valtakunnalliset-oph
;  (let [oph (test-data/lisaa-koulutustoimija!  {:ytunnus "1111111-1"})]
;    (sql/insert kysymysryhma (sql/values [{:nimi_fi "a"
;                                           :tila "julkaistu"
;                                           :valtakunnallinen true
;                                           :koulutustoimija (:ytunnus oph)}]))
;    (is (= ["a"] (map :nimi_fi (hae-kysymysryhmat (:ytunnus oph)))))))

(deftest ^:integraatio hae-taustakysymysryhmat-test
  (is (= #{1 3341885 3342146}  (set (map :kysymysryhmaid (hae-taustakysymysryhmat))))))

(deftest ^:integraatio hae-ntm-kysymysryhmat
  (let [opetushallitus (test-data/lisaa-koulutustoimija! {:ytunnus "1111111-1"})
        koulutustoimija (test-data/lisaa-koulutustoimija!)
        {:keys [kysymysryhmaid]} (test-data/lisaa-kysymysryhma! {:ntm_kysymykset true
                                                                 :taustakysymykset false
                                                                 :tila "julkaistu"
                                                                 :valtakunnallinen true}
                                                                opetushallitus)
        sisaltaa-kysymysryhman (fn [kysymysryhmat kysymysryhmaid]
                                 (contains? (set (map :kysymysryhmaid kysymysryhmat))
                                            kysymysryhmaid))]
    (testing
      "pääkäyttäjä näkee NTM-kysymysryhmän"
      (with-redefs [aipal.infra.kayttaja/yllapitaja? (constantly true)]
       (is (sisaltaa-kysymysryhman (hae-kysymysryhmat (:ytunnus opetushallitus))
                                   kysymysryhmaid))))
    (testing
      "NTM-vastuukäyttäjä näkee NTM-kysymysryhmän"
      (with-redefs [aipal.infra.kayttaja/yllapitaja? (constantly false)
                    aipal.infra.kayttaja/ntm-vastuukayttaja? (constantly true)]
        (is (sisaltaa-kysymysryhman (hae-kysymysryhmat (:ytunnus koulutustoimija))
                                    kysymysryhmaid))))
    (testing
      "tavallinen käyttäjä ei näe NTM-kysymysryhmää"
      (with-redefs [aipal.infra.kayttaja/yllapitaja? (constantly false)
                    aipal.infra.kayttaja/ntm-vastuukayttaja? (constantly false)]
        (is (not (sisaltaa-kysymysryhman (hae-kysymysryhmat (:ytunnus koulutustoimija))
                                         kysymysryhmaid)))))))

(deftest ^:integraatio hae-ntm-taustakysymysryhmat
  (let [opetushallitus (test-data/lisaa-koulutustoimija! {:ytunnus "1111111-1"})
        {:keys [kysymysryhmaid]} (test-data/lisaa-kysymysryhma! {:ntm_kysymykset true
                                                                 :taustakysymykset true
                                                                 :tila "julkaistu"
                                                                 :valtakunnallinen true}
                                                                opetushallitus)
        sisaltaa-kysymysryhman (fn [kysymysryhmat kysymysryhmaid]
                                 (contains? (set (map :kysymysryhmaid kysymysryhmat))
                                            kysymysryhmaid))]
    (testing
      "pääkäyttäjä näkee NTM-taustakysymysryhmän"
      (with-redefs [aipal.infra.kayttaja/yllapitaja? (constantly true)]
       (is (sisaltaa-kysymysryhman (hae-taustakysymysryhmat)
                                   kysymysryhmaid))))
    (testing
      "NTM-vastuukäyttäjä näkee NTM-taustakysymysryhmän"
      (with-redefs [aipal.infra.kayttaja/yllapitaja? (constantly false)
                    aipal.infra.kayttaja/ntm-vastuukayttaja? (constantly true)]
        (is (sisaltaa-kysymysryhman (hae-taustakysymysryhmat)
                                    kysymysryhmaid))))
    (testing
      "tavallinen käyttäjä ei näe NTM-taustakysymysryhmää"
      (with-redefs [aipal.infra.kayttaja/yllapitaja? (constantly false)
                    aipal.infra.kayttaja/ntm-vastuukayttaja? (constantly false)]
        (is (not (sisaltaa-kysymysryhman (hae-taustakysymysryhmat)
                                         kysymysryhmaid)))))))
