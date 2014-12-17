(ns aipal.toimiala.raportti.kyselykerta-perf
  (:require 
    [aipal.toimiala.raportti.perftest-util :refer :all]
    [clj-gatling.core :refer [run-simulation]]
    [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
    [aipal.sql.test-util :refer :all])
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)
                      
(defn kyselykertaid->perf-fn [config id ]
  (let [url (str (:base-url config) "/api/raportti/kyselykerta/" id)
        requ-fn (partial async-http-requ url (:userid config) (:basic-auth config))
        requ {:name "kyselykerta-raportti" :fn requ-fn}]
    requ))
  
(deftest ^:performance kyselykerta-raportti []
  (let [config (get-configuration)
        kyselykerta-lkm (* 3 (:request-count config))
        concurrent-users 4
        test-ids (doall (map :kyselykertaid (take kyselykerta-lkm (kyselykerta-arkisto/hae-kaikki))))
        test-reqv (mapv #(kyselykertaid->perf-fn config %1) test-ids)]
    (println test-ids)
    (run-simulation
      [{:name "Satunnaistettu kyselykerta raportin suorituskykytesti"
        :requests test-reqv}]
      concurrent-users {:root "target/perf-report/kyselykerta"
         :timeout-in-ms 10000})))
        
        