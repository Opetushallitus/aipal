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
            [aipal.sql.test-data-util :refer :all]))

(defn with-auth-user [f]
  (with-kayttaja default-test-user-uid nil nil
    (binding [i18n/*locale* testi-locale]
      (f))))

(defn mock-request-uid [app url method uid params]
  (peridot/request app url
    :request-method method
    :headers {"uid" uid}
    :params params))

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

(defn rest-kutsu
  "Tekee yksinkertaisen simuloidun rest-kutsun. Peridot-sessio suljetaan
lopuksi. Soveltuu yksinkertaisiin testitapauksiin."
  [url method params]
  (-> (session)
    (mock-request-uid url method "T-1001" params)
    :response))

(defn body-json [response]
  (cheshire/parse-string (slurp (:body response)) true))
