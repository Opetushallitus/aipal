; --- vastaajatunnuksenluonti-testi kommentoitu pois, koska CAS:n kiertäminen testeissä on osoittautunut huomattavan hankalaksi/työlääksi


;(ns aipal.rest-api.avopvastaajatunnus-test
;  (:require [clojure.test :refer :all]
;            [clj-time.core :as time]
;            [peridot.core :as peridot]
;            [aipal.sql.test-util :refer :all]
;            [aipal.sql.test-data-util :refer :all]
;            [aipal.arkisto.vastaajatunnus :as vastaajatunnus-arkisto]
;            [cheshire.core :as cheshire]
;            [aipal.rest-api.rest-util :refer [rest-avop-kutsu body-json session-no-token]]))
;
;(use-fixtures :each tietokanta-fixture)
;
;(deftest ^:integraatio avopvastaajatunnusten-lisays-ilman-salaisuus
;  (testing "avopvastaajatunnusten lisays ilman jaettu salaisuus. palauttaa 403"
;    (let [kyselykerta (lisaa-avop-kyselykerta!)
;          response (-> (session-no-token)
;                     (peridot/request "/api/public/luovastaajatunnus/"
;                                      :request-method :post
;                                      :body "{}")
;                     :response)]
;
;      (is (= (:status response) 403)))))
;
;
;(deftest ^:integraatio avopvastaajatunnusten-lisays
;   (testing "avopvastaajatunnusten lisays jaettu salaisuudella"
;     (let [kyselykerta (:nimi (lisaa-avop-kyselykerta!))]
;     (let [response (rest-avop-kutsu "/api/public/luovastaajatunnus"
;                                      :post
;                                       {}
;                                         {:oppilaitos "11111"
;                                          :koulutus "123456"
;                                          :kyselykerran_nimi kyselykerta
;                                         })]
;      (is (= (:status response) 200))))))
