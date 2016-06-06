(ns aipal.rest-api.vipunen-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as time]
            [peridot.core :as peridot]
            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [aipal.rest-api.rest-util :refer [rest-kutsu body-json session]]))

(defn hae-vastaus [alkupvm loppupvm]
  (let [response (-> (session)
                   (peridot/request "/api/vipunen/valtakunnallinen"
                     :request-method :get
                     :params {:alkupvm alkupvm
                              :loppupvm loppupvm})
                   :response)]
    (body-json response)))
  
(deftest ^:integraatio vastaajatunnusten-haku-kyselykerralla
  (let [response (hae-vastaus "2016-02-07"
                              "2016-03-05")
        response2 (hae-vastaus "2016-02-06" "2016-03-05")
        ei-vastauksia (hae-vastaus "2011-02-07"
                                    "2016-02-03")]
    (testing "aikavälillä rajattuna ei tule vastauksia"
      (is (empty? ei-vastauksia)))
    
    (let [output-file "test-resources/vipunen-valtakunnalliset-perus.edn"
          oikea-vastaus (clojure.edn/read-string (slurp output-file))]
      (testing "päivämäärävälin rajaus alkupvm toimii oikein"
        (is (= oikea-vastaus response))
        (is (= oikea-vastaus response2)))
      
    
    ; testin käyttämän vastaustiedoston tuottaminen, tähän tapaan.
    ; (spit "filetto" (with-out-str (clojure.pprint/pprint response)))
     
    ))
