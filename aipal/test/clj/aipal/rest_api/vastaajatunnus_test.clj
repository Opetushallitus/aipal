(ns aipal.rest-api.vastaajatunnus-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as time]
            [peridot.core :as peridot]
            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus-arkisto]
            [aipal.rest-api.rest-util :refer [rest-kutsu body-json session]]))

(use-fixtures :each tietokanta-fixture)

;(deftest ^:integraatio vastaajatunnusten-haku-kyselykerralla
;  (testing "vastaajatunnusten hakurajapinta suodattaa kyselykerralla"
;    (let [kyselykerta-ilman-tunnuksia (:kyselykertaid (lisaa-kyselykerta!))
;          kyselykerta (:kyselykertaid (lisaa-kyselykerta!))]
;      (vastaajatunnus-arkisto/lisaa! {:kyselykertaid kyselykerta
;                                      :vastaajien_lkm 1})
;      (let [tunnuksellinen (rest-kutsu (str "/api/vastaajatunnus/" kyselykerta) :get {})
;            tunnukseton (rest-kutsu (str "/api/vastaajatunnus/" kyselykerta-ilman-tunnuksia) :get {})]
;        (is (= (:status tunnukseton) 200))
;        (is (= (:status tunnuksellinen) 200))
;        (is (= "[]" (slurp (:body tunnukseton) :encoding "UTF-8")))
;        (is (= (:kyselykertaid (first (body-json tunnuksellinen)))
;               kyselykerta))))))
;
;(deftest ^:integraatio vastaajatunnusten-lisays
;  (testing "vastaajatunnuksen lisäys palauttaa lisätyn vastaajatunnuksen tiedot"
;    (let [kyselykerta (lisaa-kyselykerta!)
;          response (-> (session)
;                     (peridot/request (str "/api/vastaajatunnus/" (:kyselykertaid kyselykerta))
;                                      :request-method :post
;                                      :body (str "{\"vastaajien_lkm\": 7}"))
;                     :response)]
;      (is (= (:status response) 200))
;      (is (= (map #(select-keys % [:kyselykertaid :vastaajien_lkm])
;                  (body-json response))
;             [{:kyselykertaid (:kyselykertaid kyselykerta)
;               :vastaajien_lkm 7}])))))

