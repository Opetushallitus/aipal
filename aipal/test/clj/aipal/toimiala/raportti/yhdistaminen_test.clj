;; Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
;;
;; This program is free software:  Licensed under the EUPL, Version 1.1 or - as
;; soon as they will be approved by the European Commission - subsequent versions
;; of the EUPL (the "Licence");
;;
;; You may not use this work except in compliance with the Licence.
;; You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; European Union Public Licence for more details.

(ns aipal.toimiala.raportti.yhdistaminen_test
  (:require [clojure.test :refer [are deftest is testing]]
            [aipal.toimiala.raportti.yhdistaminen :refer :all]))

(deftest yhdistä-kentästä-test
  (is (= (yhdistä-kentästä :kenttä [{:kenttä "1"}
                                    {:kenttä "2"}
                                    {:kenttä "3"}])
         {:kenttä ["1"
                   "2"
                   "3"]})))

(deftest yhdistä-kaikki-kentät-test
  (is (= (yhdistä-kaikki-kentät [{:kenttä1 "1" :kenttä2 "a"}
                                 {:kenttä1 "2" :kenttä2 "b"}
                                 {:kenttä1 "3" :kenttä2 "c"}])
         {:kenttä1 ["1"
                    "2"
                    "3"]
          :kenttä2 ["a"
                    "b"
                    "c"]})))

(deftest yhdistä-vektorit-test
  (is (= (yhdistä-vektorit [["1.1" "1.2"]
                            ["2.1" "2.2"]
                            ["3.1" "3.2"]])
         [["1.1"
           "2.1"
           "3.1"]
          ["1.2"
           "2.2"
           "3.2"]])))

(deftest yhdistä-vektorit-jokin-nil-test
  (is (= (yhdistä-vektorit [["1.1" "1.2"]
                            ["2.1" "2.2"]
                            nil])
         [["1.1"
           "2.1"
           nil]
          ["1.2"
           "2.2"
           nil]])))

(deftest yhdistä-vektorit-kaikki-nil-test
  (is (= (yhdistä-vektorit [nil nil])
         nil)))

(deftest yhdistä-samat-test
  (testing "yhdistä samat arvot"
           (are [rakenteet odotettu-tulos]
               (= (yhdistä-samat rakenteet) odotettu-tulos)
               ["1" "1" "1"] "1"
               ["1" nil "1"] "1"
               [nil] nil
               [] nil
               nil nil))
  (testing "eri arvojen yhdistäminen epäonnistuu"
           (is (thrown? AssertionError (yhdistä-samat ["1" "1" "2"])))))

(deftest päivitä-polusta-test
  (are [polku funktio rakenne odotettu-tulos]
       (= (päivitä-polusta polku funktio rakenne) odotettu-tulos)
       [:kenttä] inc {:kenttä 1} {:kenttä 2}
       [:*] inc [1 2 3] [2 3 4]
       [:kenttä1 :kenttä2] inc {:kenttä1 {:kenttä2 1}} {:kenttä1 {:kenttä2 2}}
       [:kenttä1 :*] inc {:kenttä1 [1 2 3]} {:kenttä1 [2 3 4]}
       [:* :kenttä] inc [{:kenttä 1} {:kenttä 2}] [{:kenttä 2} {:kenttä 3}]
       [:*] inc (seq [1 2 3]) [2 3 4]))

(deftest päivitä-polusta-sallii-komposition-test
  (is (= (päivitä-polusta [:kenttä1 :kenttä2]
                          inc
                          {:kenttä1 {:kenttä2 1}})
         (päivitä-polusta [:kenttä1]
                          (fn [rakenne]
                            (päivitä-polusta [:kenttä2] inc rakenne))
                          {:kenttä1 {:kenttä2 1}}))))

(deftest päivitä-kentät-test
  (is (= (päivitä-kentät [:kenttä1 :kenttä2] inc {:kenttä1 1 :kenttä2 2 :kenttä3 3})
         {:kenttä1 2 :kenttä2 3 :kenttä3 3})))
