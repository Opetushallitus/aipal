(ns aipal.toimiala.raportti.kyselykerta-test
  (:require [clojure.test :refer [are deftest is testing]]
            [aipal.toimiala.raportti.kyselykerta :refer :all]))

(deftest jaottele-vaihtoehdot-test
 (testing
   "jaottele vaihtoehdot:"
   (are [kuvaus vastaukset odotettu-tulos]
        (is (= (jaottele-vaihtoehdot vastaukset) odotettu-tulos) kuvaus)
        "ei vastauksia" [] {:kylla 0 :ei 0}
        "kyllä-vastaus" [{:vaihtoehto "kylla"}] {:kylla 1 :ei 0}
        "ei-vastaus" [{:vaihtoehto "ei"}] {:kylla 0 :ei 1}
        "molempia valintoja" [{:vaihtoehto "kylla"} {:vaihtoehto "ei"}] {:kylla 1 :ei 1}
        "joku muu vastaus" [{:vaihtoehto "jokumuu"}] {:jokumuu 1 :kylla 0 :ei 0})))


