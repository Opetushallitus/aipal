(ns aipal.rest-api.kysymysryhma-lisays-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.arkisto.kysymysryhma :as arkisto]
            [aipal.infra.kayttaja]
            [aipal.rest-api.kysymysryhma :refer [lisaa-kysymysryhma!]]
            [aipal.sql.test-data-util :as test-data]))

(defn arkisto-stub-fixture [f]
  (with-redefs [arkisto/hae (fn [kysymysryhmaid] {})
                arkisto/poista-kysymys! (fn [kysymysid])
                arkisto/poista-kysymyksen-monivalintavaihtoehdot! (fn [kysymysid])
                arkisto/lisaa-jatkokysymys! (fn [jatkokysymys] {})
                arkisto/lisaa-kysymys! (fn [kysymys] {})
                arkisto/lisaa-monivalintavaihtoehto! (fn [vaihtoehto] {})
                arkisto/paivita! (fn [kysymysryhma] kysymysryhma)]
    (f)))

(use-fixtures :each arkisto-stub-fixture)

(deftest lisaa-ntm-kysymysryhma-test
  (testing "ntm-kysymysryhman lisäys"
    (let [kysymysryhma (merge test-data/default-kysymysryhma {:ntm_kysymykset true})
          lisatty-kysymysryhma (atom nil)]
      (with-redefs [arkisto/lisaa-kysymysryhma! (partial reset! lisatty-kysymysryhma)]
        (testing "on sallittu pääkäyttäjälle"
          (with-redefs [aipal.infra.kayttaja/yllapitaja? (constantly true)]
            (lisaa-kysymysryhma! kysymysryhma [])
            (is (true? (:ntm_kysymykset @lisatty-kysymysryhma)))))

        (testing "ei ole sallittu muille käyttäjille"
          (with-redefs [aipal.infra.kayttaja/yllapitaja? (constantly false)]
            (lisaa-kysymysryhma! kysymysryhma [])
            (is (false? (:ntm_kysymykset @lisatty-kysymysryhma)))))))))
