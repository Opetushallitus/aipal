(ns aipal.infra.kayttaja.sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.sql.test-util :refer [exec-raw-fixture]]
            [aipal.infra.kayttaja.sql :refer :all]))

(use-fixtures :each exec-raw-fixture)

;; with-sql-kayttaja asettaa käyttäjän PostgreSQL-parametriin.
(deftest ^:integraatio with-sql-kayttaja-test
  (with-sql-kayttaja "foobar"
    (is (= (:current_setting (first (sql/exec-raw "select current_setting('aipal.kayttaja');"
                                                  :results)))
           "foobar"))))
