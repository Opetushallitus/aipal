(ns arvo.rest-api.move
  (:require [compojure.api.sweet :refer :all]
            [buddy.auth.middleware :refer (wrap-authentication)]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer (jws-backend)]
            [oph.common.util.http-util :refer [response-or-404]]
            [schema.core :as s]
            [arvo.service.move :as move]))

(defn get-shared-secret [asetukset]
  (get-in asetukset [:avopfi-shared-secret]))

(defn auth-backend [asetukset] (jws-backend {:secret (get-shared-secret asetukset) :token-name "Bearer"}))

(defn on-403 [request]
  {:status  403
   :headers {"Content-Type" "application/json"}
   :body   {:status 403
            :detail  (str "Access to " (:uri request) " is forbidden")}})

(defn auth-mw [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      (on-403 request))))

(defroutes reitit
  (POST "/luo-tunnukset" []
    :body [vastaajatunnukset s/Any]
    :middleware [auth-mw]
    :header-params [authorization :- String]
    (response-or-404 (move/luo-tunnukset)))
  (POST "/laheta-viestit" []
    :body [taustatiedot s/Any]
    :middleware [auth-mw]
    :header-params [authorization :- String]
    (response-or-404 (move/laheta-viestit)))
  (POST "/email-test" []
    :body [taustatiedot s/Any]
    :middleware [auth-mw]
    :header-params [authorization :- String]
    (response-or-404 (move/email-test)))
  (GET "/status" []
    :middleware [auth-mw]
    :header-params [authorization :- String]
    (response-or-404 (move/status)))
  (GET "/vastaanottajat" []
    :middleware [auth-mw]
    :header-params [authorization :- String]
    (response-or-404 (move/vastaanottajat)))
  (GET "/muistutus-vastaanottajat" []
    :middleware [auth-mw]
    :header-params [authorization :- String]
    (response-or-404 (move/muistutus-vastaanottajat))))

(defn move-reitit [asetukset]
  (wrap-authentication reitit (auth-backend asetukset)))