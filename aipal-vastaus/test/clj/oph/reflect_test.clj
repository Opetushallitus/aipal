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

(ns oph.reflect-test
  (:require [clojure.test :refer :all]
            [oph.reflect :refer :all]))

(defn  test-fn-noargs "f" [] (constantly true))
(defn test-fn-args [x] (constantly true))
(defn test-fn-varargs
  ([x] (constantly true))
  ([] (constantly true)))

(deftest test-fns
  (is (= true (no-args? test-fn-noargs)))
  (is (= false (no-args? test-fn-args)))
  (is (= false (no-args? test-fn-varargs))))

(deftest test-let-bindings
  (let [f '#(println "rai")
        fa '#(println % "kreegah bundolo")]
    (is (= true (no-args? f)))
    (is (= false (no-args? fa)))))

(deftest test-noargs-vars
  (is (= true (no-args? #'test-fn-noargs)))
  (is (= false (no-args? #'test-fn-args)))
  (is (= false (no-args? #'test-fn-varargs))))
