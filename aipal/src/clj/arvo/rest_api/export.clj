(ns arvo.rest-api.export
  (:require [compojure.api.sweet :refer :all]
            [arvo.db.core :refer [*db*] :as db]
            [oph.common.util.http-util :refer [response-or-404]]
            [clojure.tools.logging :as log]))

(defroutes v1
  (GET "/kysymykset" [:as request]
    :summary "Kysymysten siirtorajapinta"
    (response-or-404 (db/export-kysymykset {})))
  (GET "/kyselykerrat" []
    (response-or-404 (db/export-kyselyt {})))
  (GET "/vastaukset" []
    (response-or-404 (db/export-vastaukset)))
  (GET "/taustatiedot" []
    (response-or-404 (db/export-taustatiedot))))

