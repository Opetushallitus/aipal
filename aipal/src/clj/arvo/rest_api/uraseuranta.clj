(ns arvo.rest-api.uraseuranta
  (:require [buddy.auth.middleware :refer (wrap-authentication)]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :as response]
            [buddy.auth.backends.token :refer (jws-backend)]
            [schema.core :as s]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus]
            [arvo.db.core :refer [*db*] :as db]))

(defn get-shared-secret [asetukset]
  (get-in asetukset [:avopfi-shared-secret]))

(defn auth-backend [asetukset] (jws-backend {:secret (get-shared-secret asetukset) :token-name "Bearer"}))

(defn on-response [message]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body {:tunnus message}})

(defroutes reitit
  (POST "/luotunnuksia" []
    :body [vastaajatunnukset s/Any]
    :middleware [arvo.rest-api.avopvastaajatunnus/auth-mw]
    :header-params [authorization :- String]
    (response/ok (vastaajatunnus/lisaa-massana! vastaajatunnukset)))
  (GET "/kyselyt" []
    :middleware [arvo.rest-api.avopvastaajatunnus/auth-mw]
    :header-params [authorization :- String]
    (response/ok (db/hae-uraseurannat))))

(defn uraseuranta-reitit [asetukset]
  (wrap-authentication reitit (auth-backend asetukset)))