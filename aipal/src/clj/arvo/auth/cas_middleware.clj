(ns arvo.auth.cas-middleware
  "Based on https://github.com/solita/cas-single-sign-out/blob/master/src/cas_single_sign_out/middleware.clj"
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.data.zip.xml :refer [xml1-> text]]
            [clojure.tools.logging :as log]
            [ring.middleware.params :refer [params-request]]
            [ring.middleware.session.store :refer [delete-session]]
            [clojure.tools.logging :as log]
            [clj-cas-client.core :refer [ticket]])
  (:import java.io.ByteArrayInputStream))

(defn single-sign-out-ticket [request]
  (let [request (params-request request)]
    (when-let [logout-request (-> request :form-params (get "logoutRequest"))]
      ;; clojure.xml / clojure.data.zip.xml don't properly support XML
      ;; namespaces. If the CAS server changes the "samlp" prefix, this will
      ;; fail.
;      https://apereo.github.io/cas/4.2.x/installation/Logout-Single-Signout.html#slo-requests
      (-> logout-request .getBytes ByteArrayInputStream. xml/parse zip/xml-zip
          (xml1-> :samlp:SessionIndex text)))))

(def single-sign-out-response {:status 200
                               :headers {"Content-Type" "text/plain"}
                               :body "OK"})

(defn handle-sign-out
  "Cas server sent us logout request including user's service ticket (ST)"
  [request session-store session-map]
  (when-let [ticket (single-sign-out-ticket request)]
    (let [matching-sessions (filter #(= ((second %) :ticket) ticket) @session-map)
          session-key (first (map first matching-sessions))]
      (log/info (str "Single sign out request received for ticket " ticket ", destroying session " session-key))
      (delete-session session-store session-key)
      single-sign-out-response)))

(defn handle-sign-in
  "When receiving service ticket (ST) save it to current ring-session as :ticket attribute"
  [handler request]
  (when-let [ticket (ticket (params-request request))]
    (when (nil? (get-in request [:session :ticket]) ))
      (let [response (handler request)
            session (assoc (response :session) :ticket ticket)]
        (assoc response :session session))))

(defn wrap-cas-single-sign-out
  "Middleware that adds :ticket to ring-session and destroys user's session when the CAS server notifies the client
  that the user should be signed out.

  Must be provided as a handler for wrap-session since session data is removed from request/response after wrap-session call"
  [handler session-store session-map]
  (fn [request]
    (or (handle-sign-out request session-store session-map)
        (handle-sign-in handler request)
        (handler request))))
