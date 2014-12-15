(ns aipal.toimiala.raportti.kyselykerta-perf
  (:require 
    [clj-gatling.core :refer [run-simulation]]
    [org.httpkit.client :as http]
    [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
    [aipal.sql.test-util :refer :all])
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)
                      
(defn async-http-requ [url user-id context callback]
  (let [check-status (fn [{:keys [status]}] (callback (= 200 status)))]
    (http/get url {:headers {"uid" "T-1001"
                             "x-xsrf-token" "token"
                             "Cookie" "cache=true; XSRF-TOKEN=token"}} check-status)))

(defn kyselekertaid->perf-fn [base-url id]
  (let [url (str base-url "/api/raportti/kyselykerta/" id)
        requ-fn (partial async-http-requ url)
        requ {:name "lol" :fn requ-fn}]
    requ))
  
(deftest ^:performance kyselykerta-raportti []
  (let [base-url (or (System/getenv "AIPAL_URL") "http://192.168.50.1:8082")
        test-ids (doall (map :kyselykertaid (take 100 (kyselykerta-arkisto/hae-kaikki))))
        test-reqv (mapv #(kyselekertaid->perf-fn base-url %1) test-ids)]
    (println test-ids)
    (run-simulation
      [{:name "Localhost kyselykerta test"
        :requests test-reqv}] 4)))