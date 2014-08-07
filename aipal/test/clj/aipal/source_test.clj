(ns aipal.source-test
  "Tarkistuksia lähdekoodille."
  (:require [clojure.test :refer [deftest testing is]])
  (:use oph.source-test))

(deftest clj-debug-test
  (is (empty? (vastaavat-rivit "src/clj"
                               #".*\.clj"
                               [#"println"]
                               ;; palvelin.clj tulostaa käynnistyksen
                               ;; poikkeukset login lisäksi stderr:iin.
                               :ohita ["src/clj/aipal/palvelin.clj"]))))
