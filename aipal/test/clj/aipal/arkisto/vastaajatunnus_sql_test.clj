(ns aipal.arkisto.vastaajatunnus-sql-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as time]
            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [aipal.arkisto.vastaajatunnus :refer :all]))

(use-fixtures :each tietokanta-fixture)

;; Lisäys testataan implisiittisesti hakujen kautta

(deftest ^:integraatio kyselykerralla-haku
  (testing "hae-kyselykerralla palauttaa vain annetun kyselykerran vastaajatunnukset."
    (let [k1 (:kyselykertaid (lisaa-kyselykerta!))
          k2 (:kyselykertaid (lisaa-kyselykerta!))]
      (lisaa! k1 {:vastaajien_lkm 1})
      (lisaa! k2 {:vastaajien_lkm 2})
      (is (= (map :vastaajien_lkm (hae-kyselykerralla k1))
             [1])))))
