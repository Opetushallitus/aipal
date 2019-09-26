(ns arvo.rest-api.koodisto
  (:require [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [oph.common.util.http-util :refer [response-or-404]]
            [arvo.db.core :refer [*db*] :as db]))

(defroutes reitit
  (GET "/oppilaitos/:oppilaitosnumero" []
    :path-params [oppilaitosnumero :- String]
    (let [oppilaitos (db/oppilaitos {:oppilaitoskoodi oppilaitosnumero})]
      (response-or-404 (into {}
                         (filter second {"fi" (:nimi_fi oppilaitos)
                                         "sv" (:nimi_sv oppilaitos)
                                         "en" (:nimi_en oppilaitos)})))))
  (GET "/koulutus/:koulutuskoodi" []
    :path-params [koulutuskoodi :- String]
    (let [tutkinto (db/tutkinto {:tutkintotunnus koulutuskoodi})]
      (response-or-404 (into {}
                             (filter second {"fi" (:nimi_fi tutkinto)
                                             "sv" (:nimi_sv tutkinto)
                                             "en" (:nimi_en tutkinto)}))))))
