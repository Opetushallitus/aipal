(ns arvo.auth.cas-middleware
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.data.zip.xml :refer [xml1-> text]]
            [clojure.tools.logging :as log]
            [ring.middleware.params :refer [params-request]]
            [ring.middleware.session.store :refer [delete-session]]
            [ring.middleware.cookies :refer [cookies-response]]
            [robert.hooke :refer [add-hook]]
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

(defn handle-sign-out [request ticket->session-key session-store]
  (when-let [t (single-sign-out-ticket request)]
    (let [s (@ticket->session-key t)]
      (log/info (str "Single sign out request received for ticket " t
                     ", destroying session " s))
      (delete-session session-store s)
      single-sign-out-response)))

;; The use of thread-local bindings as auxiliary return values is described in
;; O'Reilly's Clojure Programming book on page 205.
(def ^:dynamic *cookies*)
(defn capture-cookies-hook [f response & _]
  (when (thread-bound? #'*cookies*)
    (set! *cookies* (:cookies response)))
  (f response {}))

(add-hook #'cookies-response #'capture-cookies-hook)

(defn handle-sign-in [handler request ticket->session-key session-store]
  (when-let [t (ticket (params-request request))]
    (when (not (contains? @ticket->session-key t))
      ;; The session is created just before wrap-session returns the
      ;; response. When it passes the session cookie data to
      ;; cookies-response, we intercept it with capture-cookies-hook, dig out
      ;; the session key and associate it with the CAS ticket.
      (binding [*cookies* nil]
        (let [response (handler request)
              ;; This will break if the user changes the session cookie's
              ;; name. If someone really does that, we'll have to make the
              ;; cookie name configurable.
              s (get-in *cookies* ["ring-session" :value])]
          (swap! ticket->session-key assoc t s)
          response)))))

(defn wrap-cas-single-sign-out
  "Middleware to destroy a user's session when the CAS server notifies the
  client that the user has signed out.

  Must be used *outside* wrap-session!"
  [handler session-store]
  (let [ticket->session-key (atom {})]
    (fn [request]
      (or (handle-sign-out request ticket->session-key session-store)
          (handle-sign-in handler request ticket->session-key session-store)
          (handler request)))))
