(ns arvo.rest-api.export
  (:require [compojure.api.sweet :refer :all]
            [arvo.db.core :refer [*db*] :as db]
            [arvo.schema.export :refer :all]
            [oph.common.util.http-util :refer [response-or-404]]
            [clojure.tools.logging :as log]
            [arvo.integraatio.kyselyynohjaus :as ko]
            [schema.core :as s]))

(defn export-params [request type]
  (let [params {:koulutustoimija (:organisaatio request)
                :vipunen (-> request :oikeudet :vipunen)
                :kyselytyypit (-> request :oikeudet :kyselytyypit)}]
    (log/info "Haetaan" type ":" params)
    params))

(defroutes v1
  (GET "/kysymykset" [:as request]
    :summary "Kysymysten siirtorajapinta"
    :return [Kysymys]
      (response-or-404 (db/export-kysymykset (export-params request "kysymykset"))))
  (GET "/kyselykerrat" [:as request]
    :return [Kyselykerta]
    (response-or-404 (db/export-kyselyt (export-params request "kyselykerrat"))))
  (GET "/vastaukset" [:as request]
    :return [Vastaus]
    :query-params [{alkupvm :- #"([12]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01]))" nil}
                   {loppupvm :- #"([12]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01]))" nil}]
    (response-or-404 (db/export-vastaukset (merge {:alkupvm alkupvm :loppupvm loppupvm} (export-params request "vastaukset")))))
  (GET "/vastaajat" [:as request]
    :return [Vastaajatunnus]
    (response-or-404 (db/export-taustatiedot (export-params request "taustatiedot"))))
  (GET "/opiskeluoikeudet" [:as request]
    :return [Opiskeluoikeus]
    (let [params (export-params request "opiskeluoikeudet")
          oppilaitokset (map :oppilaitoskoodi (db/hae-koulutustoimijan-oppilaitokset params))
          opiskeluoikeudet (ko/get-opiskeluoikeus-data oppilaitokset)]
      (response-or-404 opiskeluoikeudet)))
  (GET "/kysely_kysymysryhma" [:as request]
    :return [Kysely-kysymysryhma]
    (response-or-404 (db/export-kysely-kysymysryhma (export-params request "kysely-kysymysryhma")))))