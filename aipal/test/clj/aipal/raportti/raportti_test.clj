(ns aipal.raportti.raportti-test
  (:require [clojure.test :refer :all]
    [peridot.core :as peridot]
    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.toimiala.raportti.kysely :refer :all :as kysely-raportti]))

(use-fixtures :each tietokanta-fixture)

;                :kysymysryhmaid (:kysymysryhmaid ryhma)
;                 :jarjestys (:jarjestys ryhma)})))
                 
(deftest ^:integraatio muodosta-raportteja
  (testing "bla bla bla"
    (let [kysely1 (kysely-raportti/muodosta-raportti 1 nil)
          kysely2 (kysely-raportti/muodosta-raportti 2 nil)]
    (is (= 5 (:vastaajien_lukumaara kysely2)))
    (is (= 5 (:vastaajien_maksimimaara kysely2)))
    (is (= 5 (:vastaajien_lukumaara kysely1)))
    (is (= 5 (:vastaajien_maksimimaara kysely1))))))
    
