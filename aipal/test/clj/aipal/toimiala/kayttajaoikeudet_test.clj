(ns aipal.toimiala.kayttajaoikeudet-test
  (:require [clojure.test :refer :all]
            [aipal.toimiala.kayttajaoikeudet :refer :all]))

(deftest sisaltaa-jonkin-rooleista?-sisaltaa
  (is (sisaltaa-jonkin-rooleista? #{"foo" "bar" "baz"}
                                  [{:rooli "jee"} {:rooli "bar"} {:rooli "joo"}])))

(deftest sisaltaa-jonkin-rooleista?-ei-sisalla
  (is (not (sisaltaa-jonkin-rooleista? #{"foo" "bar" "baz"}
                                       [{:rooli "jee"} {:rooli "asdf"} {:rooli "joo"}]))))
