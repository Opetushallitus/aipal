(ns arvo.rest-api.uraseuranta
  (:require [buddy.auth.middleware :refer (wrap-authentication)]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :as response]
            [buddy.auth.backends.token :refer (jws-backend)]
            [schema.core :as s]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [arvo.db.core :refer [*db*] :as db]
            [oph.common.util.http-util :refer [response-or-404]]
            [clj-time.core :as time]))

(defn get-shared-secret [asetukset]
  (get-in asetukset [:avopfi-shared-secret]))

(defn auth-backend [asetukset] (jws-backend {:secret (get-shared-secret asetukset) :token-name "Bearer"}))

(defn on-response [message]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body {:tunnus message}})

(defn uraseuranta-vastaajatunnus-defaults []
  {:taustatiedot nil
   :vastaajien_lkm 1
   :toimipaikka nil
   :voimassa_alkupvm (time/today)
   :voimassa_loppupvm nil
   :kayttaja "JARJESTELMA"})

(defn format-tunnukset [tunnukset]
  (let [defaults (uraseuranta-vastaajatunnus-defaults)]
    (->> tunnukset
         (map #(merge % defaults))
         (map #(assoc % :valmistavan_koulutuksen_oppilaitos (:oppilaitos %))))))

(defn format-vastaajatunnus-response [vastaajatunnukset]
  (let [grouped (group-by :kyselykertaid vastaajatunnukset)]
    (map (fn [[k v]] {:kyselykertaid k
                      :vastaajia (count v)
                      :oppilaitoskoodi (:oppilaitoskoodi (first v))
                      :uraseuranta_tyyppi (:uraseuranta_tyyppi (first v))}) grouped)))

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
  (POST "/luotunnuksia" []
    :body [vastaajatunnukset s/Any]
    :middleware [auth-mw]
    :header-params [authorization :- String]
    (-> (format-tunnukset vastaajatunnukset)
        vastaajatunnus/lisaa-massana!
        format-vastaajatunnus-response
        response-or-404))
  (POST "/taustatiedot" []
    :body [taustatiedot s/Any]
    :middleware [auth-mw]
    :header-params [authorization :- String]
    (-> taustatiedot
        vastaajatunnus/liita-taustatiedot!
        response-or-404))
  (GET "/kyselyt" []
    :middleware [auth-mw]
    :header-params [authorization :- String]
    (response-or-404 (db/hae-uraseurannat)))
  (GET "/vastanneet/:kyselykertaid" []
    :path-params [kyselykertaid :- s/Int]
    :middleware [auth-mw]
    :header-params [authorization :- String]
    (map :tunnus (db/hae-vastaajat {:kyselykertaid kyselykertaid}))))

(defn uraseuranta-reitit [asetukset]
  (wrap-authentication reitit (auth-backend asetukset)))