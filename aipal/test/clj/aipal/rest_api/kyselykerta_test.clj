(ns aipal.rest-api.kyselykerta-test
  (:require [aipal.rest-api.rest-util :refer [rest-kutsu body-json]]
            [aipal.sql.test-data-util :refer :all]
            [aipal.sql.test-util :refer :all])
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)