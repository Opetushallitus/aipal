(ns aipal.rest-api.raportti.valtakunnallinen-test
  (:require [clojure.test :refer :all]
    [peridot.core :as peridot]
    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.rest-api.rest-util :refer [rest-kutsu body-json session]]
    [aipal.rest-api.raportti.valtakunnallinen :refer :all :as valtakunnallinen]))

(use-fixtures :each tietokanta-fixture)

(def jsonni
  (str "{\"kieli\":\"fi\",\"tyyppi\":\"vertailu\",\"tutkintorakennetaso\":\"tutkinto\",\"koulutusalat\":[],\"opintoalat\":[],"
       "\"tutkinnot\":[\"X00002\",\"X00001\"],"
       "\"koulutuksen_jarjestajat\":[],\"jarjestavat_oppilaitokset\":[],\"koulutustoimijat\":[],"
       "\"oppilaitokset\":[],\"taustakysymysryhmaid\":\"3341885\",\"kysymykset\":{\"7312027\":{\"monivalinnat\":{}},\"7312028\":"
       "{\"monivalinnat\":{}},\"7312029\":{\"monivalinnat\":{}},\"7312030\":{\"monivalinnat\":{}},\"7312031\":{\"monivalinnat\":{}},\"7312032\":"
       "{\"monivalinnat\":{}},\"7312033\":{\"monivalinnat\":{}},\"7312039\":{\"monivalinnat\":{}}}}"))


(deftest ^:integraatio muodosta-vertailuraportti
  (testing "bla bla bla"
    (println jsonni)
    (let [response (-> (session)
                     (peridot/request "/api/raportti/valtakunnallinen"
                                      :request-method :post
                                      :body jsonni)
                     :response)]
      (println response)
      (is (= (:status response) 200))
      (println response))))
;      (is (= (map #(select-keys % [:kyselykertaid :vastaajien_lkm])
 ;                 (body-json response))
 ;            [{:kyselykertaid (:kyselykertaid kyselykerta)
 ;              :vastaajien_lkm 7}])))))

      
(deftest muodosta-tutkintovertailun-parametrit-test
  (are [opintoalat koulutusalat odotettu-tulos]
    (= (#'valtakunnallinen/muodosta-tutkintovertailun-parametrit opintoalat koulutusalat)
       odotettu-tulos)
    [799]     []    {:tutkintorakennetaso "opintoala", :opintoalat [799]}
    [799 799] []    {:tutkintorakennetaso "opintoala", :opintoalat [799]}
    [799 801] [7 7] {:tutkintorakennetaso "koulutusala", :koulutusalat [7]}
    [603 703] [6 7] {:tutkintorakennetaso "koulutusala", :koulutusalat []}))
