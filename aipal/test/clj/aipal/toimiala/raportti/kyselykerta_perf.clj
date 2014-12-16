(ns aipal.toimiala.raportti.kyselykerta-perf
  (:require 
    [aipal.toimiala.raportti.perftest-util :refer :all]
    [clj-gatling.core :refer [run-simulation]]
    [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
    [aipal.sql.test-util :refer :all])
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)
                      
(defn kyselekertaid->perf-fn [base-url id userid basic-auth]
  (let [url (str base-url "/api/raportti/kyselykerta/" id)
        requ-fn (partial async-http-requ url userid basic-auth)
        requ {:name "kyselykerta-raportti" :fn requ-fn}]
    requ))
  
(deftest ^:performance kyselykerta-raportti []
  (let [config (get-configuration)
        test-ids (doall (map :kyselykertaid (take 100 (kyselykerta-arkisto/hae-kaikki))))
        test-reqv (mapv #(kyselekertaid->perf-fn (:base-url config) %1 (:userid config) (:basic-auth config)) test-ids)]
    (println test-ids)
    (run-simulation
      [{:name "Satunnaistettu kyselykerta raportin suorituskykytesti"
        :requests test-reqv}]
      4 {:root "target/perf-report/kyselykerta"
         :timeout-in-ms 10000})))
        
        