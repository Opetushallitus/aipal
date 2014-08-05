(ns aipal.arkisto.vastaajatunnus-url-test
    (:require [aipal.arkisto.vastaajatunnus :refer :all])
    (:use clojure.test))

(deftest tunnukset-ovat-yksilollisia
  []
  (testing "Tarkistetaan että funktio osaa tarkastaa oikein jo luotujen tunnusten joukon."
    ;  Z on ainoa mahdollinen joka ei ole jo luotujen tunnusten joukossa
    (let [viimeinen-sallittu (take-last 1 sallitut-url-merkit)
          rajoitettu-joukko (drop-last sallitut-url-merkit)]
      (is viimeinen-sallittu (luo-tunnus 1 (set (map str rajoitettu-joukko)))))))

(deftest luonti-lopettaa-aina
  []
  (testing  "Tunnuksen luonti ei jää ikuiseen silmukkaan jos kaikki mahdolliset tunnukset on jo luotu."
    (is (nil? (luo-tunnus 1 (set (map str sallitut-url-merkit)))))))

