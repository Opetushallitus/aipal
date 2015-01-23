(ns oph.common.util.util-test
  (:require [clojure.test :refer :all]
            [oph.common.util.util :refer :all]))

(deftest some-value-with-not-found-test
  (is (nil? (some-value-with :name "John"
                             [{:name "Alice", :age 25},
                              {:name "Bob", :age 43}]))))

(deftest some-value-with-found-test
  (is (= (some-value-with :name "John"
                          [{:name "Alice", :age 25},
                           {:name "John", :age 31},
                           {:name "Bob", :age 43},
                           {:name "John", :age 70}])
         {:name "John", :age 31})))
