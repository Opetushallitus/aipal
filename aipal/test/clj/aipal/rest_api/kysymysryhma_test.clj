(ns aipal.rest-api.kysymysryhma_test
  (:require [aipal.rest-api.kysymysryhma :as api]
            [arvo.util :refer [add-index]])
  (:use clojure.test))

(deftest kysymyksiin-lisataan-jarjestys
  (testing "kysymysten järjestys lisää järjestyksen kysymyksiin"
    (is (= (add-index :jarjestys [{:kysymys "a"} {:kysymys "b"}])
           [{:kysymys "a" :jarjestys 0} {:kysymys "b" :jarjestys 1}]))))

(deftest kysymyksesta-valitaan-oikeat-kentat
  (testing "valitaan kaikki kysymykseen liittyvät kentät ja pudotetaan muut pois"
    (is (= (api/valitse-kysymyksen-kentat {:foo "bar"
                                           :pakollinen true
                                           :poistettava false
                                           :vastaustyyppi "tyyppi"
                                           :kysymys_fi "k1"
                                           :kysymys_sv "k2"
                                           :max_vastaus 10
                                           :monivalinta_max 20
                                           :jarjestys 1
                                           :jotain "muuta"})
           {:pakollinen true
            :poistettava false
            :vastaustyyppi "tyyppi"
            :kysymys_fi "k1"
            :kysymys_sv "k2"
            :max_vastaus 10
            :monivalinta_max 20
            :jarjestys 1}))))
