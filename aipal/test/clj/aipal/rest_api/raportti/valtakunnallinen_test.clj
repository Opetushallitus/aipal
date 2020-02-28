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
       "\"jarjestavat_oppilaitokset\":[],\"koulutustoimijat\":[],"
       "\"oppilaitokset\":[],\"taustakysymysryhmaid\":\"3341885\",\"kysymykset\":{\"7312027\":{\"monivalinnat\":{}},\"7312028\":"
       "{\"monivalinnat\":{}},\"7312029\":{\"monivalinnat\":{}},\"7312030\":{\"monivalinnat\":{}},\"7312031\":{\"monivalinnat\":{}},\"7312032\":"
       "{\"monivalinnat\":{}},\"7312033\":{\"monivalinnat\":{}},\"7312039\":{\"monivalinnat\":{}}}}"))

(def kehitysraportti-json
  (str "{\"kieli\":\"fi\",\"tyyppi\":\"kehitys\",\"tutkintorakennetaso\":\"tutkinto\",\"koulutusalat\":[],\"opintoalat\":[],"
       "\"tutkinnot\":[\"X00001\"],"
       "\"jarjestavat_oppilaitokset\":[],\"koulutustoimijat\":[],"
       "\"oppilaitokset\":[],\"taustakysymysryhmaid\":\"3341885\",\"kysymykset\":{\"7312027\":{\"monivalinnat\":{}},\"7312028\":"
       "{\"monivalinnat\":{}},\"7312029\":{\"monivalinnat\":{}},\"7312030\":{\"monivalinnat\":{}},\"7312031\":{\"monivalinnat\":{}},\"7312032\":"
       "{\"monivalinnat\":{}},\"7312033\":{\"monivalinnat\":{}},\"7312039\":{\"monivalinnat\":{}}}}"))

(def kehitysraportti-ketjutettu-json
  (str "{\"kieli\":\"fi\",\"tyyppi\":\"kehitys-ketjutettu\",\"tutkintorakennetaso\":\"tutkinto\",\"koulutusalat\":[],\"opintoalat\":[],"
       "\"tutkinnot\":[\"X00001\",\"X00002\"],"
       "\"jarjestavat_oppilaitokset\":[],\"koulutustoimijat\":[],"
       "\"oppilaitokset\":[],\"taustakysymysryhmaid\":\"3341885\",\"kysymykset\":{\"7312027\":{\"monivalinnat\":{}},\"7312028\":"
       "{\"monivalinnat\":{}},\"7312029\":{\"monivalinnat\":{}},\"7312030\":{\"monivalinnat\":{}},\"7312031\":{\"monivalinnat\":{}},\"7312032\":"
       "{\"monivalinnat\":{}},\"7312033\":{\"monivalinnat\":{}},\"7312039\":{\"monivalinnat\":{}}}}"))

(def kehitysraportti-ei-vastaajia-json
  (str "{\"kieli\":\"fi\",\"tyyppi\":\"kehitys\",\"tutkintorakennetaso\":\"tutkinto\",\"koulutusalat\":[],\"opintoalat\":[],"
       "\"tutkinnot\":[\"344102\"],"
       "\"jarjestavat_oppilaitokset\":[],\"koulutustoimijat\":[],"
       "\"oppilaitokset\":[],\"taustakysymysryhmaid\":\"3341885\",\"kysymykset\":{\"7312027\":{\"monivalinnat\":{}},\"7312028\":"
       "{\"monivalinnat\":{}},\"7312029\":{\"monivalinnat\":{}},\"7312030\":{\"monivalinnat\":{}},\"7312031\":{\"monivalinnat\":{}},\"7312032\":"
       "{\"monivalinnat\":{}},\"7312033\":{\"monivalinnat\":{}},\"7312039\":{\"monivalinnat\":{}}}}"))

(defn poista-luontipvm-kentat [raportti]
  (clojure.walk/postwalk #(if (map? %) (dissoc % :luontipvm) %) raportti))

(defn tarkista-valtakunnallinen-raportti
  [input-json output-file]
  (let [response (-> (session)
                   (peridot/request "/api/raportti/valtakunnallinen"
                                    :request-method :post
                                    :body input-json)
                   :response)]
    (is (= (:status response) 200))
    (let [vastaus (body-json response)]
      (let [oikea-raportti (clojure.edn/read-string (slurp output-file :encoding "UTF-8"))]
       ; (spit "filetto" (with-out-str (clojure.pprint/pprint vastaus)))
        (is (= (poista-luontipvm-kentat oikea-raportti) (poista-luontipvm-kentat vastaus)))))))


;(deftest ^:integraatio muodosta-vertailuraportti
;  (testing "vertailuraportin perustapaus"
;    (tarkista-valtakunnallinen-raportti perustapaus-json "test-resources/vertailuraportin-perustapaus.edn")))

;(deftest ^:integraatio muodosta-kehitysraportti
;  (testing "Kehitysraportin perustapaus"
;    (tarkista-valtakunnallinen-raportti kehitysraportti-json "test-resources/kehitysraportin-perustapaus.edn")))

;(deftest ^:integraatio muodosta-kehitysraportti-ei-vastaajia
;  (testing "Kehitysraportti, tutkintorajauksella ei löydy tarpeeksi vastaajia"
;    (tarkista-valtakunnallinen-raportti kehitysraportti-ei-vastaajia-json "test-resources/kehitysraportti-eivastaajia.edn")))

;(deftest ^:integraatio muodosta-ketjutettu-kehitysraportti
;  (testing "Kehitysraportin ketjuttaminen"
;    (tarkista-valtakunnallinen-raportti kehitysraportti-ketjutettu-json "test-resources/kehitysraportti-ketjutettu.edn")))

(deftest ^:integraatio muodosta-tutkintovertailun-parametrit-test
  (are [opintoalat koulutusalat odotettu-tulos]
    (= (#'valtakunnallinen/muodosta-tutkintovertailun-parametrit opintoalat koulutusalat)
       odotettu-tulos)
    [799]     []    {:tutkintorakennetaso "opintoala", :opintoalat [799]}
    [799 799] []    {:tutkintorakennetaso "opintoala", :opintoalat [799]}
    [799 801] [7 7] {:tutkintorakennetaso "koulutusala", :koulutusalat [7]}
    [603 703] [6 7] {:tutkintorakennetaso "koulutusala", :koulutusalat []}))
