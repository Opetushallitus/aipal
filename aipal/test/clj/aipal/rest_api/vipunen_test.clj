(ns aipal.rest-api.vipunen-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as time]
            [peridot.core :as peridot]
            [schema.core :as s]
            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [aipal.rest-api.rest-util :refer [rest-kutsu body-json session]]))

(defn hae-vastaus [peridot-session alkupvm loppupvm uri-path]
  (let [response (-> peridot-session
                   (peridot/request uri-path
                     :request-method :post
                     :body (str "{\"alkupvm\": \"" alkupvm "\"," "\"loppupvm\": \"" loppupvm "\"}"))
                   :response)
        clj-json (body-json response)]
    (is (= (:status response) 200))
    clj-json))
  

(defn hae-vastaus-valtakunnallinen [peridot-session alkupvm loppupvm]
  (hae-vastaus peridot-session alkupvm loppupvm "/api/vipunen/valtakunnallinen"))

(defn hae-vastaus-kaikki [peridot-session alkupvm loppupvm]
  (hae-vastaus peridot-session alkupvm loppupvm "/api/vipunen"))

(defn tarkista-vastaus [oikea-vastaus-file & testitulokset]
  (let [correct (clojure.edn/read-string (slurp oikea-vastaus-file))]
    (doseq [tulos testitulokset] 
      (is (= correct tulos)))))
    
(deftest ^:integraatio valtakunnalliset-kysymykset-rajapinta
  (let [peridot (session)
        response (hae-vastaus-valtakunnallinen peridot "2016-02-07" "2016-03-05")
        response2 (hae-vastaus-valtakunnallinen peridot "2016-02-06" "2016-03-05")
        ei-vastauksia (hae-vastaus-valtakunnallinen peridot "2011-02-07"
                                    "2016-02-03")
        vastauksia (hae-vastaus-valtakunnallinen peridot "2016-01-01" "2016-02-04")
        vastauksia2 (hae-vastaus-valtakunnallinen peridot "2016-02-04" "2016-02-04")]
        
    (testing "aikavälillä rajattuna ei tule vastauksia"
      (is (empty? ei-vastauksia)))

    (testing "päivämäärävälin rajaus alkupvm toimii oikein"    
      (tarkista-vastaus "test-resources/vipunen-valtakunnalliset-perus.edn" response response2))

    (testing "päivämäärävälin rajaus loppupvm toimii oikein"
      (tarkista-vastaus "test-resources/vipunen-vastauksia.edn" vastauksia vastauksia2))
    
    ; testin käyttämän vastaustiedoston tuottaminen, tähän tapaan.
;    (spit "filetto" (with-out-str (clojure.pprint/pprint vastauksia)))
     
    ))

(deftest ^:integraatio kaikki-vastaukset-rajapinta
  (let [peridot (session)
        response (hae-vastaus-kaikki peridot "2016-02-07" "2016-06-07")
        response2 (hae-vastaus-kaikki peridot "2016-02-06" "2016-06-07")
        ei-vastauksia (hae-vastaus-kaikki peridot "2011-02-07"
                                    "2016-02-03")
        vastauksia (hae-vastaus-kaikki peridot "2016-01-01" "2016-02-04")
        vastauksia2 (hae-vastaus-kaikki peridot "2016-02-04" "2016-02-04")]
        
    (testing "aikavälillä rajattuna ei tule vastauksia"
      (is (empty? ei-vastauksia)))
    
    (testing "päivämäärävälin rajaus alkupvm toimii oikein"    
      (tarkista-vastaus "test-resources/vipunen-koulutuksenjarjestajan-kysymykset.edn" response response2))

    (testing "päivämäärävälin rajaus loppupvm toimii oikein"
      (tarkista-vastaus "test-resources/vipunen-vastauksia.edn" vastauksia vastauksia2))
    
    ; testin käyttämän vastaustiedoston tuottaminen, tähän tapaan.
;     (spit "filetto" (with-out-str (clojure.pprint/pprint vastauksia)))     
    ))
