(ns aipal.rest-api.vastaajatunnus-test  
  (:require 
    [aipal.rest-api.rest-util :refer [rest-kutsu]]
    )
  (:use clojure.test))

(deftest ^:integraatio vastaajatunnusten-haku
  (testing "vastaajatunnusten hakurajapinta vastaa"
    (let [response (rest-kutsu "/api/vastaajatunnus" :get {})]
      (is (= (:status (:response response)) 200)))))
