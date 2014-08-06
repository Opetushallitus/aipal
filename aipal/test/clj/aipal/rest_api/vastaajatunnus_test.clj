(ns aipal.rest-api.vastaajatunnus-test  
  (:require 
    [cheshire.core :as cheshire]
    [clj-time.core :as time]    

    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.arkisto.vastaajatunnus :as vastaajatunnus-arkisto]
    [aipal.rest-api.rest-util :refer [rest-kutsu]]
    )
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio vastaajatunnusten-haku
  (testing "vastaajatunnusten hakurajapinta vastaa"
    (let [response (rest-kutsu "/api/vastaajatunnus" :get {})]
      (is (= (:status (:response response)) 200)))))

(defn json-find
  "Etsii haluttua avain-arvo paria vastaavaa osumaa json-rakenteesta"
  [json-str avain arvo]
  (let [s (cheshire/parse-string json-str true)]
    (some #(= arvo (get % avain)) s))) 

(deftest ^:integraatio vastaajatunnusten-haku-kyselykerralla
  (testing "vastaajatunnusten hakurajapinta suodattaa kyselykerralla"
    (let [tutkinto (lisaa-tutkinto!)
          rahoitusmuotoid 1 ; koodistodata
          kyselykerta-ilman-tunnuksia (lisaa-kyselykerta!)
          kyselykerta (lisaa-kyselykerta!)
          vastaajatunnus (vastaajatunnus-arkisto/lisaa! (:kyselykertaid kyselykerta)
                           rahoitusmuotoid (:tutkintotunnus tutkinto)
                           (time/now)
                           nil
                           )]
      (let [tunnuksellinen (rest-kutsu (str "/api/vastaajatunnus/" (:kyselykertaid kyselykerta)) :get {})
            tunnukseton (rest-kutsu (str "/api/vastaajatunnus/" (:kyselykertaid kyselykerta-ilman-tunnuksia)) :get {})]
        (is (= (:status (:response tunnukseton)) 200))
        (is (= (:status (:response tunnuksellinen)) 200))
        (is (= "[]" (:body (:response tunnukseton))))       
        (is (true? (json-find (:body (:response tunnuksellinen)) :kyselykertaid (:kyselykertaid kyselykerta))))
        ))))
