(ns aipal.rest-api.vastaajatunnus-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as time]
            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus-arkisto]
            [aipal.rest-api.rest-util :refer [rest-kutsu body-json]]))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio vastaajatunnusten-haku
  (testing "vastaajatunnusten hakurajapinta vastaa"
    (let [response (rest-kutsu "/api/vastaajatunnus" :get {})]
      (is (= (:status response) 200)))))

(deftest ^:integraatio vastaajatunnusten-haku-kyselykerralla
  (testing "vastaajatunnusten hakurajapinta suodattaa kyselykerralla"
    (let [kyselykerta-ilman-tunnuksia (lisaa-kyselykerta!)
          kyselykerta (lisaa-kyselykerta!)]
      (vastaajatunnus-arkisto/lisaa! (:kyselykertaid kyselykerta) nil nil
                                     (time/now) nil)
      (let [tunnuksellinen (rest-kutsu (str "/api/vastaajatunnus/" (:kyselykertaid kyselykerta)) :get {})
            tunnukseton (rest-kutsu (str "/api/vastaajatunnus/" (:kyselykertaid kyselykerta-ilman-tunnuksia)) :get {})]
        (is (= (:status tunnukseton) 200))
        (is (= (:status tunnuksellinen) 200))
        (is (= "[]" (:body tunnukseton)))
        (is (= (:kyselykertaid (first (body-json tunnuksellinen)))
               (:kyselykertaid kyselykerta)))))))
