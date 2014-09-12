(ns aipal.reitit
  (:require [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [compojure.core :as c]
            [compojure.route :as r]
            [stencil.core :as s]

            aipal.rest-api.i18n
            aipal.rest-api.kysely
            aipal.rest-api.kyselykerta
            aipal.rest-api.kyselypohja
            aipal.rest-api.kysymysryhma
            aipal.rest-api.raportti.kyselykerta
            aipal.rest_api.js-log
            aipal.rest-api.vastaajatunnus
            aipal.rest-api.kayttaja
            [aipal.toimiala.kayttajaoikeudet :refer [*current-user-authmap*]]

            [aitu.infra.status :refer [status]]))

(def build-id (delay (if-let [resource (io/resource "build-id.txt")]
                       (.trim (slurp resource))
                       "dev")))

(defn reitit [asetukset]
  (c/routes
    (c/GET "/" [] (s/render-file "public/app/index.html" (merge {:base-url (-> asetukset :server :base-url)
                                                                 :current-user (:kayttajan_nimi *current-user-authmap*)
                                                                 :build-id @build-id
                                                                 :development-mode (pr-str (:development-mode asetukset))}
                                                                (when-let [cas-url (-> asetukset :cas-auth-server :url)]
                                                                  {:logout-url (str cas-url "/logout")}))))
    (c/GET "/status" [] (s/render-file "status" (assoc (status)
                                                  :asetukset (with-out-str
                                                               (-> asetukset
                                                                   (assoc-in [:db :password] "*****")
                                                                   pprint))
                                                  :build-id @build-id)))
    (c/context "/api/jslog" [] aipal.rest_api.js-log/reitit)

    (c/context "/api/i18n" [] aipal.rest-api.i18n/reitit)
    (c/context "/api/kyselykerta" [] aipal.rest-api.kyselykerta/reitit)
    (c/context "/api/kyselypohja" [] aipal.rest-api.kyselypohja/reitit)
    (c/context "/api/raportti/kyselykerta" [] aipal.rest-api.raportti.kyselykerta/reitit)
    (c/context "/api/kysely" [] aipal.rest-api.kysely/reitit)
    (c/context "/api/kysymysryhma" [] aipal.rest-api.kysymysryhma/reitit)
    (c/context "/api/vastaajatunnus" [] aipal.rest-api.vastaajatunnus/reitit)
    (c/context "/api/kayttaja" [] aipal.rest-api.kayttaja/reitit)
    (r/not-found "Not found")))
