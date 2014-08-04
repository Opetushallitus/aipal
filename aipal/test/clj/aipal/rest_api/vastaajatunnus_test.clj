(ns aipal.rest-api.vastaajatunnus-test  
  (:require 
    [aipal.sql.test-util :refer :all]
    [aipal.asetukset :refer [hae-asetukset oletusasetukset]]
    [aipal.palvelin :as palvelin]
    [peridot.core :as peridot]
    [oph.korma.korma-auth :as ka]
    [oph.korma.korma-auth :as auth]
    [oph.common.infra.i18n :as i18n]
    [aipal.integraatio.sql.korma :as korma]
    [aipal.toimiala.kayttajaoikeudet :refer [*current-user-authmap*]]
    [aipal.toimiala.kayttajaroolit :refer [kayttajaroolit]]
    )
  (:use clojure.test))

(defn with-auth-user [f]
  (let [olemassaoleva-kayttaja {:roolitunnus (:yllapitaja kayttajaroolit), :oid auth/default-test-user-oid, :uid auth/default-test-user-uid }]
    (binding [ka/*current-user-uid* (:uid olemassaoleva-kayttaja)
              ka/*current-user-oid* (promise)
              i18n/*locale* testi-locale 
              *current-user-authmap* olemassaoleva-kayttaja]
      (deliver ka/*current-user-oid* (:oid olemassaoleva-kayttaja))
      (f))))

(defn mock-request [app url method params]
  (with-auth-user #(peridot/request app url
      :request-method method
      :headers { "x-xsrf-token" "token"}
      :cookies { "XSRF-TOKEN" {:value "token"}}
      :params params)))

(deftest ^:integraatio vastaajatunnusten-haku
 (let [asetukset 
     (-> oletusasetukset
       (assoc-in [:cas-auth-server :enabled] false)
       (assoc :development-mode true))
      _ (alusta-korma! asetukset)
     crout (palvelin/app asetukset)]
      
    (let [response (->  (peridot/session  crout)
                     (mock-request "/api/vastaajatunnus"  :get {}))]
      ;(println response)
      (is (= (:status (:response response)) 200)))))
