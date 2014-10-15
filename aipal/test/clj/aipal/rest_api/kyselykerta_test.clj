(ns aipal.rest-api.kyselykerta-test
  (:require
    [clj-time.core :as time]

    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
    [aipal.rest-api.rest-util :refer [rest-kutsu body-json]]
    )
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio kyselykertojen-haku-id
  (testing "kyselykertojen hakurajapinta suodattaa kyselykerran ID:llä"
    (let [tutkinto (lisaa-tutkinto!)
          rahoitusmuotoid 1 ; koodistodata
          kyselykerta-ilman-tunnuksia (lisaa-kyselykerta!)
          kyselykerta (lisaa-kyselykerta!)]
      (let [response (rest-kutsu (str "/api/kyselykerta/" (:kyselykertaid kyselykerta)) :get {})]
        (is (= (:status response) 200))
        (is (= (:kyselykertaid (body-json response))
               (:kyselykertaid kyselykerta)))))))
