(ns aipal.toimiala.raportti.kyselykerta-perf
  (:require
    [aipal.toimiala.raportti.perftest-util :refer :all]
    [clj-gatling.core :refer [run-simulation]]
    [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
    [aipal.sql.test-util :refer :all])
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)

(defn kyselykertaid->perf-fn [config id]
  (let [url (str (:base-url config) "/api/raportti/kyselykerta/" id)]
    (url->http-get-fn config url "kyselykerta-raportti")))

(deftest ^:performance kyselykerta-raportti
  (let [config (get-configuration)
        kyselykerta-lkm (* 3 (:request-count config))
        concurrent-users 4
        test-ids (doall (map :kyselykertaid
                             (take kyselykerta-lkm
                                   (kyselykerta-arkisto/hae-kaikki
                                    (get-in config [:kyselykerta-perf :koulutustoimija])))))
        test-reqv (mapv #(kyselykertaid->perf-fn config %1) test-ids)]
    (println test-ids)
    (run-simulation
      [{:name "Satunnaistettu kyselykerta raportin suorituskykytesti"
        :requests test-reqv}]
      concurrent-users {:root "target/perf-report/kyselykerta"
         :timeout-in-ms 10000})))
