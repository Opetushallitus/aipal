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
            aipal.rest-api.avopvastaajatunnus
            aipal.rest-api.kayttaja
            aipal.rest-api.tutkinto
            aipal.rest-api.tutkintotyyppi
            aipal.rest-api.koulutustoimija
            aipal.rest-api.tiedote
            aipal.rest-api.vipunen
            [aipal.infra.kayttaja :refer [*kayttaja*]]

            [oph.common.infra.status :refer [status]]))


(def build-id (delay (if-let [resource (io/resource "build-id.txt")]
                       (.trim (slurp resource))
                       "dev")))

(defn reitit [asetukset]
  (api
    {:exceptions {:handlers {:schema.core/error ex/schema-error-handler}}}
    (swagger-routes
        {:ui "/api-docs"
         :spec "/swagger.json"
         :data {:info {:title "AIPAL API"
                       :description "AIPALin rajapinnat. Sisältää sekä integraatiorajapinnat muihin järjestelmiin, että AIPALin sisäiseen käyttöön tarkoitetut rajapinnat."}
                :basePath (str (service-path (get-in asetukset [:server :base-url])))}})
    (GET "/" [] {:status 200
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
    (GET "/status" [] (s/render-file "status" (assoc (status)
                                                :asetukset (with-out-str
                                                             (pprint
                                                               (clojure.walk/postwalk (fn [elem]
                                                                                        (if (and (coll? elem)
                                                                                                 (= (first elem) :password))
                                                                                          [:password "*****"]
                                                                                          elem))
                                                                                      asetukset)))
                                                :build-id @build-id)))
    (context "/api/jslog" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest_api.js-log/reitit)
    (context "/api/i18n" [] aipal.rest-api.i18n/reitit)
    (context "/api/kieli" [] aipal.rest-api.kieli/reitit)
    (context "/api/kyselykerta" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.kyselykerta/reitit)
    (context "/api/kyselypohja" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.kyselypohja/reitit)
    (context "/api/ohje" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.ohje/reitit)
    (context "/api/oppilaitos" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.oppilaitos/reitit)
    (context "/api/rahoitusmuoto" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.rahoitusmuoto/reitit)
    (context "/api/toimipaikka" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.toimipaikka/reitit)
    (context "/api/raportti/kysely" [] (aipal.rest-api.raportti.kysely/csv-reitit asetukset))
    (context "/api/raportti/kysely" [] :middleware [wrap-tarkasta-csrf-token] (aipal.rest-api.raportti.kysely/reitit asetukset))
    (context "/api/raportti/kyselykerta" [] (aipal.rest-api.raportti.kyselykerta/csv-reitit asetukset))
    (context "/api/raportti/kyselykerta" [] :middleware [wrap-tarkasta-csrf-token] (aipal.rest-api.raportti.kyselykerta/reitit asetukset))
    (context "/api/raportti/valtakunnallinen" [] (aipal.rest-api.raportti.valtakunnallinen/csv-reitit asetukset))
    (context "/api/raportti/valtakunnallinen" [] :middleware [wrap-tarkasta-csrf-token] (aipal.rest-api.raportti.valtakunnallinen/reitit asetukset))
    (context "/api/kysely" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.kysely/reitit)
    (context "/api/kysymysryhma" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.kysymysryhma/reitit)
    (context "/api/vastaajatunnus" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.vastaajatunnus/reitit)
    (context "/api/kayttaja" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.kayttaja/reitit)
    (context "/api/tutkinto" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.tutkinto/reitit)
    (context "/api/tutkintotyyppi" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.tutkintotyyppi/reitit)
    (context "/api/koulutustoimija" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.koulutustoimija/reitit)
    (context "/api/public/luovastaajatunnus" [] (aipal.rest-api.avopvastaajatunnus/reitit asetukset))
    (context "/api/tiedote" [] :middleware [wrap-tarkasta-csrf-token] aipal.rest-api.tiedote/reitit)
    ;(context "/api/vipunen" [] aipal.rest-api.vipunen/reitit)
    (r/not-found "Not found")))
