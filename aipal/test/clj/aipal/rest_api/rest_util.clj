(ns aipal.rest-api.rest-util
  (:require [peridot.core :as peridot]
            [clj-time.core :as time]
            [cheshire.core :as cheshire]

            [oph.common.infra.i18n :as i18n]
            [aipal.palvelin :as palvelin]
            [aipal.asetukset :refer [hae-asetukset oletusasetukset]]
            [aipal.integraatio.sql.korma :as korma]
            [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
            [aipal.infra.kayttaja.vakiot :refer [default-test-user-uid]]

            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [buddy.sign.jws :as jws]))

(defn with-auth-user [f]
  (with-kayttaja default-test-user-uid nil nil
    (binding [i18n/*locale* testi-locale]
      (f))))

(defn mock-request-uid
  ([app url method uid params]
   (peridot/request app url
                    :request-method method
                    :headers {"uid" uid}
                    :params params))
  ([app url method uid params body]
   (peridot/request app url
                    :request-method method
                    :headers {"uid" uid}
                    :content-type "application/json"
                    :body (cheshire/generate-string body)
                    :params params)))

(defn mock-request-salaisuus
  ([app url method auth-header params body]
    (peridot/request app url 
                    :request-method method
                    :headers {:Authorization auth-header}
                    :content-type "application/json"
                    :body (cheshire/generate-string body)
                    :params params)))

(defn session []
  (let [asetukset (-> oletusasetukset
                    (assoc-in [:cas-auth-server :enabled] false)
                    (assoc :development-mode true))]
    (alusta-korma! asetukset)
    (-> (peridot/session (palvelin/app asetukset)
                         :cookie-jar {"localhost" {"XSRF-TOKEN" {:raw "XSRF-TOKEN=token", :domain "localhost", :path "/", :value "token"}}})
      (peridot/header "uid" testikayttaja-uid)
      (peridot/header "x-xsrf-token" "token")
      (peridot/content-type "application/json"))))

;;TODO : Remove xsrf-token from here
(defn session-no-token []
  (let [asetukset (-> oletusasetukset
                    (assoc-in [:cas-auth-server :enabled] false)
                    (assoc :development-mode true))]
    (alusta-korma! asetukset)
    (-> (peridot/session (palvelin/app asetukset) 
                :cookie-jar {"localhost" {"XSRF-TOKEN" {:raw "XSRF-TOKEN=token", :domain "localhost", :path "/", :value "token"}}})
      (peridot/content-type "application/json"))))

(defn rest-kutsu
  "Tekee yksinkertaisen simuloidun rest-kutsun. Peridot-sessio suljetaan
lopuksi. Soveltuuyksinkertaisiin testitapauksiin."
  ([url method params]
   (-> (session)
       (mock-request-uid url method "T-1001" params)
       :response))
  ([url method params body]
   (-> (session)
       (mock-request-uid url method "T-1001" params body)
       :response)))

(defn body-json [response]
  (if (string? (:body response))
    (cheshire/parse-string (:body response) true)
    (cheshire/parse-string (slurp (:body response)) true)))

(defn rest-avop-kutsu
  "Tekee simuloidun rest-kutsun. kaytetaan oletus jaettu salaisuus siis secret)."
  ([url method params body]
  (let [auth-header (str "Bearer " 
                         (jws/sign {:caller "avopfi"} "secret"))]
   (-> (session-no-token)
       (mock-request-salaisuus url method  auth-header params body)
       :response))))


