(ns aipal.source-test
  "Tarkistuksia lähdekoodille."
  (:require [clojure.test :refer [deftest testing is]]
            [oph.source-util :refer :all]
            [oph.source-test :refer :all]))

(deftest clj-debug-test
  (is (empty? (vastaavat-rivit "src/clj"
                               #".*\.clj"
                               [#"println"]
                               ;; palvelin.clj tulostaa käynnistyksen
                               ;; poikkeukset login lisäksi stderr:iin.
                               :ohita ["src/clj/aipal/palvelin.clj"]))))

;; $window.location.hash:n asetus jumittaa/kaataa IE9:n. Tämän sijasta pitää
;; käyttää $location.pathia:
;; http://stackoverflow.com/a/18138174/13340
(deftest window-location-hash-test
  (is (empty? (vastaavat-rivit "frontend/src/js"
                               #".*\.js"
                               [#"location\.hash[^(]"]))))
