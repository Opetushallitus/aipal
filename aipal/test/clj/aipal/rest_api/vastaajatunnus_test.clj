(ns aipal.rest-api.vastaajatunnus-test  
  (:require 
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
    [aipal.arkisto.vastaajatunnus :as vastaajatunnus-arkisto]
    )
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)

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

(deftest ^:integraatio tunnuksen-luonti
  (testing "Haku palauttaa lisää-kutsulla luodun vastaajatunnuksen"
    (let [tutkinto (lisaa-tutkinto!)
          rahoitusmuotoid 1 ; koodistodata
          kyselykerta (lisaa-kyselykerta!)
          vastaajatunnus (vastaajatunnus-arkisto/lisaa! (:kyselykertaid kyselykerta)
                           rahoitusmuotoid (:tutkintotunnus tutkinto)
                           (time/now)
                           nil
                           )
          viimeksi-lisatty (first (vastaajatunnus-arkisto/hae-kaikki))]
      (is (= (:kyselykertaid viimeksi-lisatty) (:kyselykertaid vastaajatunnus)))
      (is (= (:tutkintotunnus viimeksi-lisatty) (:tutkintotunnus vastaajatunnus))))))


(deftest ^:integraatio kyselykerralla-haku
  (testing "Haku filtteröi oikein kyselykerran perusteella"
    (let [tutkinto (lisaa-tutkinto!)
          rahoitusmuotoid 1 ; koodistodata
          kyselykerta-ilman-tunnuksia (lisaa-kyselykerta!)
          kyselykerta (lisaa-kyselykerta!)
          vastaajatunnus (vastaajatunnus-arkisto/lisaa! (:kyselykertaid kyselykerta)
                           rahoitusmuotoid (:tutkintotunnus tutkinto)
                           (time/now)
                           nil
                           )
          viimeksi-lisatyt (vastaajatunnus-arkisto/hae-kyselykerralla (:kyselykertaid kyselykerta))
          tyhja (vastaajatunnus-arkisto/hae-kyselykerralla (:kyselykertaid kyselykerta-ilman-tunnuksia))]
      (is (= (count viimeksi-lisatyt) 1))
      (is (empty? tyhja)))))
