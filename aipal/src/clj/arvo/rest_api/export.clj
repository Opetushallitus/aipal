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
    (log/info "Export" type ":" params)
    params))

(s/defschema Pagination
  {(s/optional-key :next_url) (s/maybe s/Str)})

(def api-location "/api/export/v1/")

(defroutes v1
  (GET "/kyselykerrat" [:as request]
       :summary "Kyselykerrat ja kyselyt"
       :description "Kyselykerran ja siihen liittyvän kyselyn tarkemmat tiedot"
       :return  {:data [Kyselykerta]}
       (api-response {:data (db/export-kyselyt (export-params request "kyselykerrat"))}))
  (GET "/kysely_kysymysryhma" [:as request]
       :summary "Kyselyt ja niiden kysymysryhmät"
       :description "Kyselyiden ja kysymysryhmien suhteet"
       :return {:data [Kysely-kysymysryhma]}
       :query-params [{kyselyid :- s/Int nil}]
       (api-response {:data (db/export-kysely-kysymysryhma (merge {:kyselyid kyselyid} (export-params request "kysely-kysymysryhma")))}))
  (GET "/kysymykset" [:as request]
       :summary "Kysymysten tiedot"
       :description "Kysymysten keskeiset (jatkokysymykset) suhteet ja kysymysryhmä johon kuuluvat"
       :return {:data [Kysymys]}
       :query-params [{kyselyid :- s/Int nil}]
       (let [query-params (:kyselyid kyselyid)]
         (api-response {:data (db/export-kysymykset (merge (export-params request "kysymykset") query-params))})))
  (GET "/monivalintavaihtoehdot" [:as request]
       :summary "Monivalintakysymykset"
       :description "Vastausvaihtoehdot monivalintakysymykseen"
       :return {:data [Monivalintavaihtoehto]}
       :query-params [{kyselyid :- s/Int nil}]
       (api-response {:data (db/export-monivalintavaihtoehdot (merge {:kyselyid kyselyid} (export-params request "monivalintavaihtoehdot")))}))
  (GET "/vastaukset" [:as request]
       :summary "Kysymysten vastaukset"
       :description "Kysymysten vastaukset ja niihin liittyvä vastaaja ja vastaajatunnustieto. \"koulutustoimija\" ilmoitetaan y-tunnuksella."
       :return {:data [Vastaus] :pagination Pagination}
       :query-params [{alkupvm :- #"([12]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01]))" nil}
                      {loppupvm :- #"([12]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01]))" nil}
                      {since :- s/Int nil}
                      {limit :- s/Int nil}
                      {kyselyid :- s/Int nil}]
       (let [query-params {:alkupvm alkupvm :loppupvm loppupvm :since since :limit limit :kyselyid kyselyid}
             page-length (if limit (min limit (:api-page-length @asetukset)) (:api-page-length @asetukset))
             data (db/export-vastaukset (apply merge {:pagelength page-length} query-params (export-params request "vastaukset")))]
         (paginated-response data :vastausid page-length (str api-location "vastaukset") query-params)))
  (GET "/vastaajat" [:as request]
       :summary "Vastaajat ja niiden vastaajatunnukset"
       :description "Vastaaja ja siiheen liittyvän vastaajatunnuksen tarkemmat tiedot"
       :return {:data [Vastaajatunnus] :pagination Pagination}
       :query-params [{since :- s/Int nil}
                      {limit :- s/Int nil}
                      {kyselyid :- s/Int nil}]
       (let [page-length (if limit (min limit (:api-page-length @asetukset)) (:api-page-length @asetukset))
             sql-params (merge {:pagelength page-length :since since :kyselyid kyselyid}(export-params request "taustatiedot"))]
         (paginated-response (db/export-taustatiedot sql-params) :vastaajaid page-length (str api-location "vastaajat") {:limit limit})))
  (GET "/opiskeluoikeudet" [:as request]
       :summary "Vastaajatunnukset ja sen opiskeluoikeus"
       :description "Vastaajatunnuksen ja virran opiskeluoikeuden suhteet. Vain kyselyynohjauksen kautta vastanneet saavat tämän esim. HAKA."
       :return {:data [Opiskeluoikeus]}
       (let [params (export-params request "opiskeluoikeudet")
             oppilaitokset (map :oppilaitoskoodi (db/hae-koulutustoimijan-oppilaitokset params))
             opiskeluoikeudet (ko/get-opiskeluoikeus-data oppilaitokset)]
         (api-response {:data opiskeluoikeudet})))
  (GET "/luodut_tunnukset" [:as request]
       :summary "Kooste"
       :description "Kooste kuukausittain luoduista tunnuksista kyselykertaa kohden"
       :return {:data [Luodut-tunnukset]}
       :query-params [{kyselyid :- s/Int nil}]
       (api-response {:data (db/export-luodut-tunnukset (merge {:kyselyid kyselyid} (export-params request "luodut-tunnukset")))})))
