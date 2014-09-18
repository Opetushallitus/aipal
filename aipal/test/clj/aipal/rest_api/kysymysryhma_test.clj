(ns aipal.rest-api.kysymysryhma_test
  (:require [aipal.rest-api.kysymysryhma :as api])
  (:use clojure.test))

(deftest kysymyksiin-lisataan-jarjestys
  (testing "kysymysten järjestys lisää järjestyksen kysymyksiin"
    (is (= (api/jarjesta-kysymykset [{:kysymys "a"} {:kysymys "b"}])
           [{:kysymys "a" :jarjestys 0} {:kysymys "b" :jarjestys 1}]))))
