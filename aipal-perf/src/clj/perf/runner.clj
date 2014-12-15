(ns perf.runner
 (:require [clj-gatling.core :refer [run-simulation]]
  	   [org.httpkit.client :as http]))

(defn async-http-requ [url user-id context callback]
  (let [check-status (fn [{:keys [status]}] (callback (= 200 status)))]
    (http/get url {:headers {"uid" "T-1001"}} check-status)))

(defn -main []
  (let [url "http://localhost:8082/"
        requ-fn (partial async-http-requ url)]
    (run-simulation
      [{:name "Localhost test scenario"
       :requests [{:name "Root request" :options {:headers {:uid "T-1001"}} :fn requ-fn
        	 }]}] 100)))
