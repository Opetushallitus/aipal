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
            [clojure.tools.logging :as log]
            [compojure.core :as c]
            [org.httpkit.server :as hs]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :as resp]
            schema.core
            [stencil.core :as s]

            [aipal.asetukset :refer [lue-asetukset oletusasetukset build-id konfiguroi-lokitus]]
            aipal.rest-api.i18n
            aipal.rest-api.kysely
            aipal.rest-api.kyselykerta
            aipal.rest-api.raportti.kyselykerta
            [aitu.infra.i18n :refer [wrap-locale]]
            [aitu.infra.print-wrapper :refer [log-request-wrapper]]
            [aitu.poikkeus :refer [wrap-poikkeusten-logitus]]
            [aitu.integraatio.sql.korma]
            [oph.korma.korma-auth :as korma-auth]))

(schema.core/set-fn-validation! true)

(defn service-url [asetukset]
  (let [base-url (get-in asetukset [:server :base-url])
        port (get-in asetukset [:server :port])]
    (cond
      (empty? base-url) (str "http://localhost:" port "/")
      (.endsWith base-url "/") base-url
      :else (str base-url "/"))))

(defn ^:private reitit [asetukset]
  (c/routes
    (c/GET "/" [] (s/render-file "public/app/index.html" (merge {:base-url (-> asetukset :server :base-url)
                                                                 :build-id @build-id}
                                                                (when-let [cas-url (-> asetukset :cas-auth-server :url)]
                                                                  {:logout-url (str cas-url "/logout")}))))
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
          sammuta (hs/run-server (->
                                   (reitit asetukset)
                                   wrap-set-db-user
                                   wrap-keyword-params
                                   wrap-json-params
                                   wrap-params
                                   (wrap-resource "public/app")
                                   (wrap-locale
                                     :ei-redirectia #"/api/.*"
                                     :base-url (-> asetukset :server :base-url))
                                   wrap-content-type
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
