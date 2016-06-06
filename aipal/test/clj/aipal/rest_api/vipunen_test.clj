(ns aipal.rest-api.vipunen-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as time]
            [peridot.core :as peridot]
            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [aipal.rest-api.rest-util :refer [rest-kutsu body-json session]]))

(defn hae-vastaus [peridot-session alkupvm loppupvm]
  (let [response (-> peridot-session
                   (peridot/request "/api/vipunen/valtakunnallinen"
                     :request-method :get
                     :params {:alkupvm alkupvm
                              :loppupvm loppupvm})
                   :response)]
    (body-json response)))
  
(defn tarkista-vastaus [oikea-vastaus-file & testitulokset]
  (let [correct (clojure.edn/read-string (slurp oikea-vastaus-file))]
    (for [result testitulokset]
      (is (= correct result)))))
    
(deftest ^:integraatio vastaajatunnusten-haku-kyselykerralla
  (let [peridot (session)
        response (hae-vastaus peridot "2016-02-07"
                              "2016-03-05")
        response2 (hae-vastaus peridot "2016-02-06" "2016-03-05")
        ei-vastauksia (hae-vastaus peridot "2011-02-07"
                                    "2016-02-03")
        vastauksia (hae-vastaus peridot "2016-01-01" "2016-02-04")
        vastauksia2 (hae-vastaus peridot "2016-02-04" "2016-02-04")]
    (testing "aikavälillä rajattuna ei tule vastauksia"
      (is (empty? ei-vastauksia)))

    (testing "päivämäärävälin rajaus alkupvm toimii oikein"    
      (tarkista-vastaus "test-resources/vipunen-valtakunnalliset-perus.edn" response response2))

    (testing "päivämäärävälin rajaus loppupvm toimii oikein"
      (tarkista-vastaus "test-resources/vipunen-vastauksia.edn" vastauksia vastauksia2))
    
    ; testin käyttämän vastaustiedoston tuottaminen, tähän tapaan.
;    (spit "filetto" (with-out-str (clojure.pprint/pprint vastauksia)))
     
    ))
