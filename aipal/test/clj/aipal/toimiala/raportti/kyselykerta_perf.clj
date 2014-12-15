(ns aipal.toimiala.raportti.kyselykerta-perf
  (:require 
    [clj-gatling.core :refer [run-simulation]]
    [org.httpkit.client :as http]
    [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
    [aipal.sql.test-util :refer :all])
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)
                      
(defn async-http-requ [url uid basic-auth user-id context callback]
  (let [check-status (fn [{:keys [status]}] (callback (= 200 status)))]
    (http/get url {:headers {"uid" uid
                             "x-xsrf-token" "token"
                             "Cookie" "cache=true; XSRF-TOKEN=token"}
                   :basic-auth basic-auth
                   }
      check-status)))

(defn kyselekertaid->perf-fn [base-url id userid basic-auth]
  (let [url (str base-url "/api/raportti/kyselykerta/" id)
        requ-fn (partial async-http-requ url userid basic-auth)
        requ {:name "lol" :fn requ-fn}]
    requ))
  
(deftest ^:performance kyselykerta-raportti []
  (let [base-url (or (System/getenv "AIPAL_URL") "http://192.168.50.1:8082")
        userid (or (System/getenv "AIPAL_UID")  "T-1001")
        basic-auth (or (System/getenv "AIPAL_AUTH") "pfft:thx")
        test-ids (doall (map :kyselykertaid (take 100 (kyselykerta-arkisto/hae-kaikki))))
        test-reqv (mapv #(kyselekertaid->perf-fn base-url %1 userid basic-auth) test-ids)]
    (println test-ids)
    (run-simulation
      [{:name "Satunnaistettu kyselykerta raportin suorituskykytesti"
        :requests test-reqv}]
      4 {:root "target/perf-report/kyselykerta"})))
        
        