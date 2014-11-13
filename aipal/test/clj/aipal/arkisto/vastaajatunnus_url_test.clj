(ns aipal.arkisto.vastaajatunnus-url-test
    (:require [aipal.arkisto.vastaajatunnus :refer :all])
    (:use clojure.test))

(deftest tunnukset-ovat-keskenaan-yksilollisia
  []
  (testing "Tarkistetaan että funktio osaa luoda vain keskenään uniikkien tunnusten joukon kun mahdolliset merkit loppuvat."
    (let [merkkien-lkm (count sallitut-url-merkit)
          haetut-tunnukset (take (+ 1 merkkien-lkm) (luo-tunnuksia 1))]
      (is (distinct? haetut-tunnukset))
      (is (= merkkien-lkm (count haetut-tunnukset))))))

(deftest luonti-lopettaa-aina
  []
  (testing  "Tunnuksien luonti ei jää ikuiseen silmukkaan jos kaikki mahdolliset tunnukset on jo luotu."
    (let [merkkien-lkm (count sallitut-url-merkit)]
      (is (= (count (take (+ 1 merkkien-lkm) (luo-tunnuksia 1)))
             merkkien-lkm)))))
