(ns aipal.rest-api.raportti.valtakunnallinen-test
  (:require [clojure.test :refer :all]
    [peridot.core :as peridot]
    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.rest-api.rest-util :refer [rest-kutsu body-json session]]
    [aipal.rest-api.raportti.valtakunnallinen :refer :all :as valtakunnallinen]))

(use-fixtures :each tietokanta-fixture)

(def perustapaus-json
  (str "{\"kieli\":\"fi\",\"tyyppi\":\"vertailu\",\"tutkintorakennetaso\":\"tutkinto\",\"koulutusalat\":[],\"opintoalat\":[],"
       "\"tutkinnot\":[\"X00002\",\"X00001\"],"
       "\"koulutuksen_jarjestajat\":[],\"jarjestavat_oppilaitokset\":[],\"koulutustoimijat\":[],"
       "\"oppilaitokset\":[],\"taustakysymysryhmaid\":\"3341885\",\"kysymykset\":{\"7312027\":{\"monivalinnat\":{}},\"7312028\":"
       "{\"monivalinnat\":{}},\"7312029\":{\"monivalinnat\":{}},\"7312030\":{\"monivalinnat\":{}},\"7312031\":{\"monivalinnat\":{}},\"7312032\":"
       "{\"monivalinnat\":{}},\"7312033\":{\"monivalinnat\":{}},\"7312039\":{\"monivalinnat\":{}}}}"))

(def kehitysraportti-json
  (str "{\"kieli\":\"fi\",\"tyyppi\":\"kehitys\",\"tutkintorakennetaso\":\"tutkinto\",\"koulutusalat\":[],\"opintoalat\":[],"
       "\"tutkinnot\":[\"X00001\"],"
       "\"koulutuksen_jarjestajat\":[],\"jarjestavat_oppilaitokset\":[],\"koulutustoimijat\":[],"
       "\"oppilaitokset\":[],\"taustakysymysryhmaid\":\"3341885\",\"kysymykset\":{\"7312027\":{\"monivalinnat\":{}},\"7312028\":"
       "{\"monivalinnat\":{}},\"7312029\":{\"monivalinnat\":{}},\"7312030\":{\"monivalinnat\":{}},\"7312031\":{\"monivalinnat\":{}},\"7312032\":"
       "{\"monivalinnat\":{}},\"7312033\":{\"monivalinnat\":{}},\"7312039\":{\"monivalinnat\":{}}}}"))
       
(defn poista-luontipvm-kentat [raportti]
  (clojure.walk/postwalk #(if (map? %) (dissoc % :luontipvm) %) raportti))
  
(deftest ^:integraatio muodosta-vertailuraportti
  (testing "vertailuraportin perustapaus"
    (let [response (-> (session)
                     (peridot/request "/api/raportti/valtakunnallinen"
                                      :request-method :post
                                      :body perustapaus-json)
                     :response)
          vastaus (body-json response)]
      (is (= (:status response) 200))
      (let [oikea-raportti (clojure.edn/read-string (slurp "test/resources/vertailuraportin-perustapaus.edn"))]
        (is (= (poista-luontipvm-kentat oikea-raportti) (poista-luontipvm-kentat vastaus)))        
        ; (spit "filetto" (with-out-str (clojure.pprint/pprint vastaus)))
        ))))
      
(deftest ^:integraatio muodosta-kehitysraportti
  (testing "Kehitysraportin perustapaus"
    (let [response (-> (session)
                     (peridot/request "/api/raportti/valtakunnallinen"
                                      :request-method :post
                                      :body kehitysraportti-json)
                     :response)
          vastaus (body-json response)]
      (is (= (:status response) 200))
      (let [oikea-raportti (clojure.edn/read-string (slurp "test/resources/kehitysraportin-perustapaus.edn"))]
        (is (= (poista-luontipvm-kentat oikea-raportti) (poista-luontipvm-kentat vastaus)))        
        ))))

(deftest ^:integraatio muodosta-tutkintovertailun-parametrit-test
  (are [opintoalat koulutusalat odotettu-tulos]
    (= (#'valtakunnallinen/muodosta-tutkintovertailun-parametrit opintoalat koulutusalat)
       odotettu-tulos)
    [799]     []    {:tutkintorakennetaso "opintoala", :opintoalat [799]}
    [799 799] []    {:tutkintorakennetaso "opintoala", :opintoalat [799]}
    [799 801] [7 7] {:tutkintorakennetaso "koulutusala", :koulutusalat [7]}
    [603 703] [6 7] {:tutkintorakennetaso "koulutusala", :koulutusalat []}))
