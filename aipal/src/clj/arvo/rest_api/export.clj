(ns arvo.rest-api.export
  (:require [compojure.api.sweet :refer :all]
            [arvo.db.core :refer [*db*] :as db]
            [arvo.schema.export :refer :all]
            [oph.common.util.http-util :refer [response-or-404]]
            [clojure.tools.logging :as log]))

(def csc-ytunnus "0920632-0")

(defn export-params [request type]
  (let [params {:koulutustoimija (:organisaatio request)
                :vipunen (= (:organisaatio request) csc-ytunnus)}]
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
    (response-or-404 (db/export-vastaukset (export-params request "vastaukset"))))
  (GET "/taustatiedot" [:as request]
    :return [Vastaajatunnus]
    (response-or-404 (db/export-taustatiedot (export-params request "taustatiedot")))))

