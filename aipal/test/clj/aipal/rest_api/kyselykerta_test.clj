(ns aipal.rest-api.kyselykerta-test
  (:require 
    [clj-time.core :as time]    
    
    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
    [aipal.rest-api.rest-util :refer [rest-kutsu json-find]]
    )
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio kyselykertojen-haku
  (testing "kyselykertojen hakurajapinta vastaa"
    (let [response (rest-kutsu "/api/kyselykerta" :get {})]
      (is (= (:status (:response response)) 200)))))

(deftest ^:integraatio kyselykertojen-haku
  (testing "kyselykertojen hakurajapinta vastaa"
    (let [tutkinto (lisaa-tutkinto!)
      rahoitusmuotoid 1 ; koodistodata
      kyselykerta-ilman-tunnuksia (lisaa-kyselykerta!)
      kyselykerta (lisaa-kyselykerta!)]
      (let [response (rest-kutsu (str "/api/kyselykerta/" (:kyselykertaid kyselykerta)) :get {})]
        (is (= (:status (:response response)) 200))
        (is (true? (json-find  (:body (:response response)) :kyselykertaid (:kyselykertaid kyselykerta))))
        ))))


