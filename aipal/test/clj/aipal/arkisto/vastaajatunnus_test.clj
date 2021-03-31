(ns aipal.arkisto.vastaajatunnus-test
    (:require [arvo.db.vastaajatunnus :refer :all])
    (:use clojure.test))

(deftest tunnukset-ovat-keskenaan-yksilollisia
  (testing "Tarkistetaan että funktio osaa luoda vain keskenään uniikkien tunnusten joukon kun mahdolliset merkit loppuvat."
    (let [merkkien-lkm (count sallitut-merkit)
          haetut-tunnukset (take (+ 100 merkkien-lkm) (luo-tunnuksia 1))]
      (is (distinct? haetut-tunnukset)))))
