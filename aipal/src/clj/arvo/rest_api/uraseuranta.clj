(ns arvo.rest-api.uraseuranta
  (:require [buddy.auth.middleware :refer (wrap-authentication)]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :as response]
            [buddy.auth.backends.token :refer (jws-backend)]
            [schema.core :as s]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus]
            [arvo.db.core :refer [*db*] :as db]
            [oph.common.util.http-util :refer [response-or-404]]))

(defn get-shared-secret [asetukset]
  (get-in asetukset [:avopfi-shared-secret]))

(defn auth-backend [asetukset] (jws-backend {:secret (get-shared-secret asetukset) :token-name "Bearer"}))

(defn on-response [message]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body {:tunnus message}})


(defn format-vastaajatunnus-response [vastaajatunnukset]
  (let [grouped (group-by :kyselykertaid vastaajatunnukset)]
    (map (fn [[k v]] {:kyselykertaid k
                      :vastaajia (count v)
                      :oppilaitoskoodi (:oppilaitoskoodi (first v))
                      :uraseuranta_tyyppi (:uraseuranta_tyyppi (first v))}) grouped)))

(defroutes reitit
  (POST "/luotunnuksia" []
    :body [vastaajatunnukset s/Any]
    :middleware [arvo.rest-api.avopvastaajatunnus/auth-mw]
    :header-params [authorization :- String]
    (-> vastaajatunnukset
        vastaajatunnus/lisaa-massana!
        format-vastaajatunnus-response
        response-or-404))

  (GET "/kyselyt" []
    :middleware [arvo.rest-api.avopvastaajatunnus/auth-mw]
    :header-params [authorization :- String]
    (response-or-404 (db/hae-uraseurannat))))

(defn uraseuranta-reitit [asetukset]
  (wrap-authentication reitit (auth-backend asetukset)))