(ns aipal.reitit
  (:require [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]

            [cheshire.core :as cheshire]
            [compojure.api.exception :as ex]
            [compojure.api.sweet :refer [api context swagger-routes GET]]
            [compojure.route :as r]
            [stencil.core :as s]

            [oph.common.infra.csrf-token :refer [aseta-csrf-token wrap-tarkasta-csrf-token]]
            [aipal.asetukset :refer [service-path]]
            aipal.rest-api.i18n
            aipal.rest-api.kieli
            aipal.rest-api.kysely
            aipal.rest-api.kyselykerta
            aipal.rest-api.kyselypohja
            aipal.rest-api.kysymysryhma
            aipal.rest-api.ohje
            aipal.rest-api.oppilaitos
            aipal.rest-api.toimipaikka
            aipal.rest-api.rahoitusmuoto
            aipal.rest-api.raportti.kysely
            aipal.rest-api.raportti.kyselykerta
            aipal.rest-api.raportti.valtakunnallinen
            aipal.rest_api.js-log
            aipal.rest-api.vastaajatunnus
            aipal.rest-api.kayttaja
            aipal.rest-api.tutkinto
            aipal.rest-api.koulutustoimija
            aipal.rest-api.tiedote
            [aipal.infra.kayttaja :refer [*kayttaja*]]

            [oph.common.infra.status :refer [status]]))

(def build-id (delay (if-let [resource (io/resource "build-id.txt")]
                       (.trim (slurp resource))
                       "dev")))

(defn reitit [asetukset]

  (c/routes
    (c/GET "/" [] {:status 200
                   :headers {"Content-type" "text/html; charset=utf-8"
                             "Set-cookie" (aseta-csrf-token (-> asetukset :server :base-url service-path))}
                   :body (s/render-file
                           "public/app/index.html"
                           (merge {:base-url (-> asetukset :server :base-url)
                                   :vastaus-base-url (-> asetukset :vastaus-base-url)
                                   :current-user (:nimi *kayttaja*)
                                   :build-id @build-id
                                   :development-mode (pr-str (:development-mode asetukset))
                                   :ominaisuus (cheshire/generate-string (:ominaisuus asetukset))}
                                  (when-let [cas-url (-> asetukset :cas-auth-server :url)]
                                    {:logout-url (str cas-url "/logout")})))})
    (c/GET "/status" [] (s/render-file "status" (assoc (status)
                                                  :asetukset (with-out-str
                                                               (pprint
                                                                 (clojure.walk/postwalk (fn [elem]
                                                                                          (if (and (coll? elem)
                                                                                                   (= (first elem) :password))
                                                                                            [:password "*****"]
                                                                                            elem))
                                                                                        asetukset)))
                                                  :build-id @build-id)))
    (c/context "/api/jslog" [] (wrap-tarkasta-csrf-token aipal.rest_api.js-log/reitit))

    (c/context "/api/i18n" [] aipal.rest-api.i18n/reitit)
    (c/context "/api/kieli" [] aipal.rest-api.kieli/reitit)
    (c/context "/api/kyselykerta" [] (wrap-tarkasta-csrf-token aipal.rest-api.kyselykerta/reitit))
    (c/context "/api/kyselypohja" [] (wrap-tarkasta-csrf-token aipal.rest-api.kyselypohja/reitit))
    (c/context "/api/ohje" [] (wrap-tarkasta-csrf-token aipal.rest-api.ohje/reitit))
    (c/context "/api/oppilaitos" [] (wrap-tarkasta-csrf-token aipal.rest-api.oppilaitos/reitit))
    (c/context "/api/toimipaikka" [] (wrap-tarkasta-csrf-token aipal.rest-api.toimipaikka/reitit))
    (c/context "/api/rahoitusmuoto" [] (wrap-tarkasta-csrf-token aipal.rest-api.rahoitusmuoto/reitit))
    (c/context "/api/raportti/kysely" [] (aipal.rest-api.raportti.kysely/csv-reitit asetukset))
    (c/context "/api/raportti/kysely" [] (wrap-tarkasta-csrf-token (aipal.rest-api.raportti.kysely/reitit asetukset)))
    (c/context "/api/raportti/kyselykerta" [] (aipal.rest-api.raportti.kyselykerta/csv-reitit asetukset))
    (c/context "/api/raportti/kyselykerta" [] (wrap-tarkasta-csrf-token (aipal.rest-api.raportti.kyselykerta/reitit asetukset)))
    (c/context "/api/raportti/valtakunnallinen" [] (aipal.rest-api.raportti.valtakunnallinen/csv-reitit asetukset))
    (c/context "/api/raportti/valtakunnallinen" [] (wrap-tarkasta-csrf-token (aipal.rest-api.raportti.valtakunnallinen/reitit asetukset)))
    (c/context "/api/kysely" [] (wrap-tarkasta-csrf-token aipal.rest-api.kysely/reitit))
    (c/context "/api/kysymysryhma" [] (wrap-tarkasta-csrf-token aipal.rest-api.kysymysryhma/reitit))
    (c/context "/api/vastaajatunnus" [] (wrap-tarkasta-csrf-token aipal.rest-api.vastaajatunnus/reitit))
    (c/context "/api/kayttaja" [] (wrap-tarkasta-csrf-token aipal.rest-api.kayttaja/reitit))
    (c/context "/api/tutkinto" [] (wrap-tarkasta-csrf-token aipal.rest-api.tutkinto/reitit))
    (c/context "/api/koulutustoimija" [] (wrap-tarkasta-csrf-token aipal.rest-api.koulutustoimija/reitit))
    (c/context "/api/tiedote" [] (wrap-tarkasta-csrf-token aipal.rest-api.tiedote/reitit))
    (r/not-found "Not found")))
