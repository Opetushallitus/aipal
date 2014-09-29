(ns aipal.arkisto.vastaajatunnus-sql-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as time]
            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [aipal.arkisto.vastaajatunnus :refer :all]))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio kyselykerralla-haku
  (testing "hae-kyselykerralla palauttaa vain annetun kyselykerran vastaajatunnukset."
    (let [k1 (:kyselykertaid (lisaa-kyselykerta!))
          k2 (:kyselykertaid (lisaa-kyselykerta!))]
      (lisaa! k1 {:vastaajien_lkm 1})
      (lisaa! k2 {:vastaajien_lkm 2})
      (is (= (map :vastaajien_lkm (hae-kyselykerralla k1))
             [1])))))

(deftest ^:integraatio henkilokohtaiset-vastaajatunnukset
  (testing "henkilökohtaisten vastaajatunnusten lisääminen lisää vastaajien_lkm kpl vastaajatunnuksia"
    (let [k (:kyselykertaid (lisaa-kyselykerta!))]
      (lisaa-vastaajatunnuksia k true {:vastaajien_lkm 3})
      (is (= (map :vastaajien_lkm (hae-kyselykerralla k))
             [1 1 1])))))
