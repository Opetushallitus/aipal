;; Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
;;
;; This program is free software:  Licensed under the EUPL, Version 1.1 or - as
;; soon as they will be approved by the European Commission - subsequent versions
;; of the EUPL (the "Licence");
;;
;; You may not use this work except in compliance with the Licence.
;; You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; European Union Public Licence for more details.

(ns aipal.palvelin
  (:gen-class)
  (:require [cheshire.generate :as json-gen]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [compojure.core :as c]
            [org.httpkit.server :as hs]
            [ring.util.request :refer [path-info]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.util.request :refer [path-info request-url]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.x-headers :refer [wrap-frame-options]]
            [ring.util.response :as resp]
            schema.core

            [arvo.auth.cas-auth-middleware :refer [cas]]
            [arvo.auth.cas-middleware :refer [wrap-cas-single-sign-out]]

            [oph.common.infra.asetukset :refer [konfiguroi-lokitus]]
            [oph.common.infra.anon-auth :as anon-auth]

            [oph.common.infra.common-audit-log :refer [req-metadata-saver-wrapper konfiguroi-common-audit-lokitus]]
            [oph.common.infra.print-wrapper :refer [log-request-wrapper]]
            [oph.korma.common :refer [luo-db]]

            [aipal.asetukset :refer [asetukset oletusasetukset common-audit-log-asetukset hae-asetukset build-id kehitysmoodi? service-path] :rename {asetukset asetukset-promise}]
            aipal.reitit
            [aipal.infra.kayttaja.middleware :refer [wrap-kayttaja]]
            [aipal.integraatio.kayttooikeuspalvelu :as kop]
            [aipal.infra.eraajo :as eraajo]
            [aipal.infra.kayttaja.vakiot :refer [default-test-user-uid]]
            [mount.core :as mount]
            [arvo.db.migrations :as migrations]))

(schema.core/set-fn-validation! true)

(defn cas-server-url [asetukset]
  (:url (:cas-auth-server asetukset)))

(defn service-url [asetukset]
  (let [base-url (get-in asetukset [:server :base-url])
        port (get-in asetukset [:server :port])]
    (cond
      (empty? base-url) (str "http://localhost:" port "/")
      (.endsWith base-url "/") base-url
      :else (str base-url "/"))))

(defn ajax-request? [request]
  (get-in request [:headers "angular-ajax-request"]))

(defn auth-removeticket
  [handler asetukset]
  (fn [request]
    (if (get-in request [:query-params "ticket"])
      (resp/redirect (service-url asetukset))
      (handler request))))

(def swagger-resources
  "Swagger API resources, not authenticated using CAS"
  #{"/api-docs" "/swagger.json" "/fi/swagger.json"})

(def external-service-api
  #{"/api/vipunen" "/api/export" "/api/vastauslinkki" "/api/admin"})

(def public-api
  "Public API methods, not authenticated using CAS"
  #{"/api/public"})

(defn auth-middleware
  [handler asetukset]
  (when (and (kehitysmoodi? asetukset)
          (:unsafe-https (:cas-auth-server asetukset))
          (:enabled (:cas-auth-server asetukset)))
    (anon-auth/enable-development-mode!))

  (fn [request]
      (let [cas-handler (wrap-kayttaja handler)
            anon-auth-handler (anon-auth/auth-cas-user cas-handler default-test-user-uid)
            fake-auth-handler (anon-auth/auth-cas-user cas-handler ((:headers request) "uid"))
            auth-handler (cas cas-handler #(cas-server-url asetukset) #(service-url asetukset) :no-redirect? ajax-request?)]
       (cond
         (some #(.startsWith (path-info request) %) external-service-api)
         (do
           (log/info "Vipunen or Export API, only Basic Auth")
           (handler request))
         (some #(.startsWith (path-info request) %) swagger-resources)
         (do
           (log/info "swagger API docs are public, no auth")
           (handler request))
         (some #(.startsWith (path-info request) %) public-api)
         (do
           (log/info "public API method, no CAS auth")
           (handler request))
         (and (kehitysmoodi? asetukset) (not (:enabled (:cas-auth-server asetukset))))
         (do
          (log/info "development, no CAS")
          (anon-auth-handler request))
         (and (kehitysmoodi? asetukset) ((:headers request) "uid"))
         (do
           (log/info "development, fake CAS")
           (fake-auth-handler request))
         :else (auth-handler request)))))

(defn sammuta [palvelin]
  ((:sammuta palvelin)))

(defn wrap-expires [handler]
  (fn [request]
    (assoc-in (handler request) [:headers "Expires"] "-1")))

(defn wrap-kayttooikeudet-forbidden [handler]
  (fn [request]
    (try
      (handler request)
      (catch clojure.lang.ExceptionInfo e
        (if (= :kayttooikeudet (-> e ex-data :cause))
          {:status 403
           :headers {"Content-Type" "text/plain; charset=utf-8"
                     "X-Kayttooikeudet-Forbidden" "true"}
           :body "yleiset.virhe_kayttoikeudet_eivat_riita"}
          (throw e))))))

(defn wrap-internal-forbidden [handler]
  (fn [request]
    (let [response (handler request)]
      (if (= (:status response) 403)
        (assoc response :headers {"X-Aipal-Error" "true"})
        response))))

(defn app
  "Ring-wrapperit ja compojure-reitit ilman HTTP-palvelinta"
  [asetukset]

  (let [hostname (-> asetukset :server :base-url java.net.URL. .getHost)
        audit-asetukset (assoc common-audit-log-asetukset :hostname hostname)]
    (konfiguroi-common-audit-lokitus audit-asetukset))

  (json-gen/add-encoder org.joda.time.DateTime
                        (fn [c json-generator]
                          (.writeString json-generator (.toString c))))
  (json-gen/add-encoder org.joda.time.LocalDate
                        (fn [c json-generator]
                          (.writeString json-generator (.toString c "yyyy-MM-dd"))))

  (let [session-map (atom {})
        session-store (memory-store session-map)]
    (-> (aipal.reitit/reitit asetukset)
      wrap-internal-forbidden
      (wrap-resource "public/app")
      (auth-removeticket asetukset)
      wrap-content-type
      wrap-not-modified
      wrap-expires

      (auth-middleware asetukset)
      log-request-wrapper
      req-metadata-saver-wrapper   ;; Huom: Tämän oltava "auth-middleware":n jälkeen

      (wrap-frame-options :deny)
      (wrap-cas-single-sign-out session-store session-map)
      (wrap-session {:store session-store
                     :cookie-attrs {:http-only true
                                    :path (service-path (get-in asetukset [:server :base-url]))
                                    :secure (not (:development-mode asetukset))}})
      wrap-kayttooikeudet-forbidden)))


(defn ^:integration-api kaynnista-eraajon-ajastimet! [asetukset]
  (eraajo/kaynnista-ajastimet! asetukset))

(defn ^:integration-api kaynnista! [alkuasetukset args]
  (try
    (log/info "Käynnistetään ARVO, versio " @build-id)
    (let [asetukset (hae-asetukset alkuasetukset)
          _ (deliver asetukset-promise asetukset)
          _ (konfiguroi-lokitus asetukset)
          _ (luo-db (:db asetukset))
          _ (mount/start)
          sammuta (hs/run-server (app asetukset)
                                 {:port (get-in asetukset [:server :port])})]
      (cond
        (some #{"migrate" "rollback"} args)
        (do (migrations/migrate args) (System/exit 0))
        :else
        (migrations/migrate ["migrate"]))
      (when (or (not (:development-mode asetukset))
                (:eraajo asetukset))
        (kaynnista-eraajon-ajastimet! asetukset))
      (log/info "Palvelin käynnistetty:" (service-url asetukset))
      {:sammuta sammuta})
    (catch Throwable t
      (let [virheviesti "Palvelimen käynnistys epäonnistui"]
        (log/error t virheviesti)
        (binding [*out* *err*]
          (println virheviesti))
        (.printStackTrace t *err*)
        (System/exit 1)))))

(defn -main [& args]
  (kaynnista! oletusasetukset args))
