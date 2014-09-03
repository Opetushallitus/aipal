(ns aipal.arkisto.kysymysryhma-sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [clj-time.core :as time]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            [aipal.integraatio.sql.korma :refer [kysymysryhma]]
            [aipal.arkisto.kysymysryhma :refer :all]))

(use-fixtures :each tietokanta-fixture)

;; hae-kysymysryhmat palauttaa kaikki kysymysryhmät riippumatta voimassaolosta
(deftest ^:integraatio hae-kysymysryhmat-voimassaolo
  (sql/insert kysymysryhma (sql/values [{:nimi_fi "a"
                                         :voimassa_alkupvm (time/local-date 1900 1 1)
                                         :voimassa_loppupvm (time/local-date 2000 1 1)
                                         :valtakunnallinen true}
                                        {:nimi_fi "b"
                                         :voimassa_alkupvm (time/local-date 2000 1 1)
                                         :voimassa_loppupvm (time/local-date 2100 1 1)
                                         :valtakunnallinen true}
                                        {:nimi_fi "c"
                                         :voimassa_alkupvm (time/local-date 2100 1 1)
                                         :voimassa_loppupvm (time/local-date 2200 1 1)
                                         :valtakunnallinen true}]))
  (is (= #{"a" "b" "c"} (set (map :nimi_fi (hae-kysymysryhmat))))))
