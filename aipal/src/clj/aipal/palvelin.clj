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
            [compojure.core :as c]
            [org.httpkit.server :as hs]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.x-headers :refer [wrap-frame-options]]
            [ring.util.response :as resp]
            schema.core
            [stencil.core :as s]

            [aipal.asetukset :refer [lue-asetukset oletusasetukset build-id kehitysmoodi? konfiguroi-lokitus]]
            aipal.rest-api.i18n
            [clj-cas-client.core :refer [cas]]
            [aitu.infra.anon-auth :as anon-auth]
            aipal.rest-api.kysely
            aipal.rest-api.kyselykerta
            aipal.rest-api.raportti.kyselykerta
            [aitu.infra.i18n :refer [wrap-locale]]
            [aitu.infra.print-wrapper :refer [log-request-wrapper]]
            [aitu.infra.status :refer [status]]
            [aitu.poikkeus :refer [wrap-poikkeusten-logitus]]
            [aitu.integraatio.sql.korma]
            [oph.korma.korma-auth :as korma-auth]))

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

(defn service-path [base-url]
  (let [path (drop 3 (clojure.string/split base-url #"/"))]
    (str "/" (clojure.string/join "/" path))))

(defn ajax-request? [request]
  (get-in request [:headers "angular-ajax-request"]))

(defn auth-middleware
  [handler asetukset]
  (when (and (kehitysmoodi? asetukset)
             (:unsafe-https (:cas-auth-server asetukset))
             (:enabled (:cas-auth-server asetukset)))
    (anon-auth/enable-development-mode!))
  (if (and (kehitysmoodi? asetukset)
           (not (:enabled (:cas-auth-server asetukset))))
    (anon-auth/auth-cas-user handler)
    (fn [request]
      (let [auth-handler (if (and (kehitysmoodi? asetukset)
                                  ((:headers request) "uid"))
                           (anon-auth/auth-cas-user handler)
                           (cas handler #(cas-server-url asetukset) #(service-url asetukset) :no-redirect? ajax-request?))]
        (auth-handler request)))))

(defn ^:private reitit [asetukset]
  (c/routes
    (c/GET "/" [] (s/render-file "public/app/index.html" (merge {:base-url (-> asetukset :server :base-url)
                                                                 :build-id @build-id}
                                                                (when-let [cas-url (-> asetukset :cas-auth-server :url)]
                                                                  {:logout-url (str cas-url "/logout")}))))
    (c/GET "/status" [] (s/render-file "status" (assoc (status)
                                                  :asetukset (with-out-str
                                                               (-> asetukset
                                                                   (assoc-in [:db :password] "*****")
                                                                   pprint))
                                                  :build-id @build-id)))
    (c/context "/api/i18n" [] aipal.rest-api.i18n/reitit)
    (c/context "/api/kyselykerta" [] aipal.rest-api.kyselykerta/reitit)
    (c/context "/api/raportti/kyselykerta" [] aipal.rest-api.raportti.kyselykerta/reitit)
    (c/context "/api/kysely" [] aipal.rest-api.kysely/reitit)))

(defn ^:private wrap-set-db-user
  "Asettaa käyttäjän tietokantaistuntoon."
  [ring-handler]
  (fn [request]
    (binding [korma-auth/*current-user-uid* korma-auth/jarjestelmakayttaja
              korma-auth/*current-user-oid* (promise)]
      (ring-handler request))))

(defn sammuta [palvelin]
  ((:sammuta palvelin)))

(defn kaynnista! [oletusasetukset]
  (try
    (let [asetukset (lue-asetukset oletusasetukset)
          _ (konfiguroi-lokitus asetukset)
          _ (log/info "Käynnistetään Aipal, versio " @build-id)
          _ (aitu.integraatio.sql.korma/luo-db (:db asetukset))
          _ (json-gen/add-encoder org.joda.time.LocalDate
              (fn [c json-generator]
                (.writeString json-generator (.toString c "yyyy-MM-dd"))))
          session-store (memory-store)
          sammuta (hs/run-server (->
                                   (reitit asetukset)
                                   wrap-set-db-user
                                   wrap-keyword-params
                                   wrap-json-params
                                   (wrap-locale
                                     :ei-redirectia #"/api/.*"
                                     :base-url (-> asetukset :server :base-url))
                                   (auth-middleware asetukset)
                                   wrap-params
                                   (wrap-resource "public/app")
                                   wrap-content-type
                                   (wrap-frame-options :deny)
                                   (wrap-session {:store session-store
                                                  :cookie-attrs {:http-only true
                                                                 :path (service-path(get-in asetukset [:server :base-url]))
                                                                 :secure (not (:development-mode asetukset))}})
                                   log-request-wrapper
                                   wrap-poikkeusten-logitus)
                                 {:port (get-in asetukset [:server :port])})]
      (log/info "Palvelin käynnistetty:" (service-url asetukset))
      {:sammuta sammuta})
    (catch Throwable t
      (let [virheviesti "Palvelimen käynnistys epäonnistui"]
        (log/error t virheviesti)
        (binding [*out* *err*]
          (println virheviesti))
        (.printStackTrace t *err*)
        (System/exit 1)))))

(defn -main []
  (kaynnista! oletusasetukset))
