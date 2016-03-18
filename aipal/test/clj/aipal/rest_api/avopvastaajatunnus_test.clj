(ns aipal.rest-api.avopvastaajatunnus-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as time]
            [peridot.core :as peridot]
            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus-arkisto]
            [aipal.rest-api.rest-util :refer [rest-kutsu body-json session]]))

(use-fixtures :each tietokanta-fixture)
;;FIXME: Create test once specs are clear
(deftest ^:integraatio avopvastaajatunnusten-lisays
  (testing "vastaajatunnuksen lisäys palauttaa lisätyn vastaajatunnuksen tiedot"
    (let [kyselykerta (lisaa-kyselykerta!)
          response (-> (session)
                     (peridot/request (str "/api/public/luovastaajatunnus/")
                                      :request-method :post
                                      :body (str "{\"vastaajien_lkm\": 3}"))
                     :response)]
      (is (= (:status response) 200))
      (is (= (map #(select-keys % [:kyselykertaid :vastaajien_lkm])
                  (body-json response))
             [{:kyselykertaid (:kyselykertaid kyselykerta)
               :vastaajien_lkm 3}])))))

