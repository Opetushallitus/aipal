(ns arvo.rest-api.export
  (:require [compojure.api.sweet :refer :all]
            [arvo.db.core :refer [*db*] :as db]
            [arvo.schema.export :refer :all]
            [oph.common.util.http-util :refer [response-or-404]]
            [clojure.tools.logging :as log]
            [arvo.integraatio.kyselyynohjaus :as ko]
            [schema.core :as s]
            [arvo.util :refer [api-response paginated-response]]
            [aipal.asetukset :refer [asetukset]]))

(defn export-params [request type]
  (let [params {:koulutustoimija (:organisaatio request)
                :vipunen (-> request :oikeudet :vipunen)
                :kyselytyypit (-> request :oikeudet :kyselytyypit)}]
    (log/info "Haetaan" type ":" params)
    params))

(s/defschema Pagination
  {(s/optional-key :next_url) (s/maybe s/Str)})

(def api-location "/api/export/v1/")

(defroutes v1
  (GET "/kysymykset" [:as request]
    :summary "Kysymysten siirtorajapinta"
    :return {:data [Kysymys]}
    (api-response {:data (db/export-kysymykset (export-params request "kysymykset"))}))
  (GET "/kyselykerrat" [:as request]
    :return  {:data [Kyselykerta]}
    (api-response {:data (db/export-kyselyt (export-params request "kyselykerrat"))}))
  (GET "/vastaukset" [:as request]
    :return {:data [Vastaus] :pagination Pagination}
    :query-params [{alkupvm :- #"([12]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01]))" nil}
                   {loppupvm :- #"([12]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01]))" nil}
                   {since :- s/Int nil}
                   {limit :- s/Int nil}]
    (let [query-params {:alkupvm alkupvm :loppupvm loppupvm :since since :limit limit}
          page-length (if limit (min limit (:api-page-length @asetukset)) (:api-page-length @asetukset))
          data (db/export-vastaukset (apply merge {:pagelength page-length} query-params (export-params request "vastaukset")))]
      (paginated-response data :vastausid page-length (str api-location "vastaukset") query-params)))
  (GET "/vastaajat" [:as request]
    :return {:data [Vastaajatunnus] :pagination Pagination}
    :query-params [{since :- s/Int nil}
                   {limit :- s/Int nil}]
    (let [page-length (if limit (min limit (:api-page-length @asetukset)) (:api-page-length @asetukset))
          sql-params (merge {:pagelength page-length :since since}(export-params request "taustatiedot"))]
      (paginated-response (db/export-taustatiedot sql-params) :vastaajaid page-length (str api-location "vastaajat") {:limit limit})))
  (GET "/opiskeluoikeudet" [:as request]
    :return {:data [Opiskeluoikeus]}
    (let [params (export-params request "opiskeluoikeudet")
          oppilaitokset (map :oppilaitoskoodi (db/hae-koulutustoimijan-oppilaitokset params))
          opiskeluoikeudet (ko/get-opiskeluoikeus-data oppilaitokset)]
      (api-response {:data opiskeluoikeudet})))
  (GET "/kysely_kysymysryhma" [:as request]
    :return {:data [Kysely-kysymysryhma]}
    (api-response {:data (db/export-kysely-kysymysryhma (export-params request "kysely-kysymysryhma"))}))
  (GET "/luodut_tunnukset" [:as request]
    :return {:data [Luodut-tunnukset]}
    (api-response {:data (db/export-luodut-tunnukset (export-params request "luodut-tunnukset"))})))
