(ns aipal.rest-api.rest-util  (:require 
    [peridot.core :as peridot]
    [clj-time.core :as time]
 
    [oph.korma.korma-auth :as ka]
    [oph.common.infra.i18n :as i18n]
    [aipal.integraatio.sql.korma-auth :as auth]
    [aipal.palvelin :as palvelin]
    [aipal.asetukset :refer [hae-asetukset oletusasetukset]]
    [aipal.integraatio.sql.korma :as korma]
    [aipal.toimiala.kayttajaoikeudet :refer [*current-user-authmap*]]
    [aipal.toimiala.kayttajaroolit :refer [kayttajaroolit]]

    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
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

(defn rest-kutsu
  "Tekee yksinkertaisen simuloidun rest-kutsun. Peridot-sessio suljetaan lopuksi. Soveltuu yksinkertaisiin testitapauksiin. "
  [url method params]
  (let [asetukset 
        (-> oletusasetukset
          (assoc-in [:cas-auth-server :enabled] false)
          (assoc :development-mode true))
       _ (alusta-korma! asetukset)
       crout (palvelin/app asetukset)]
    
    (let [response (->  (peridot/session  crout)
                     (mock-request "/api/vastaajatunnus"  :get {}))]
      response)))

