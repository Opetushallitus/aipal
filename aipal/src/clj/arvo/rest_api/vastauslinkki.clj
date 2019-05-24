(ns arvo.rest-api.vastauslinkki
  (:require [compojure.api.sweet :refer :all]
            [arvo.db.core :refer [*db*] :as db]
            [arvo.schema.export :refer :all]
            [oph.common.util.http-util :refer [response-or-404]]
            [clojure.tools.logging :as log]
            [schema.core :as s]
            [arvo.util :refer [api-response paginated-response]]
            [aipal.asetukset :refer [asetukset]]))

(s/defschema Luo-vastauslinkki
  {:vastaamisajan_alkupvm s/Str ;ISO formaatti
   :kyselyn_tyyppi s/Str
   :tutkintotunnus s/Str ;6 merkkiä
   :tutkinnon_suorituskieli s/Str ;fi, sv, en
   :koulutustoimija_oid s/Str ;organisaatio-oid
   :oppilaitos_oid s/Str ;organisaatio-oid
   :toimipiste_oid (s/maybe s/Str) ;organisaatio-oid
   :hankintakoulutuksen_toteuttaja (s/maybe s/Str)
   :request_id s/Str})

(defroutes v1
  (POST "/" [:as request]
        :body [data Luo-vastauslinkki]
        :return {:kysely_linkki s/Str}
        :summary "Kyselylinkin luominen"
        :description (str "Päivämäärät ovat ISO-formaatin mukaisia. Suorituskieli on fi, sv tai en. Tutkintotunnus
        on opintopolun koulutus koodiston 6 numeroinen koodi.")
        (api-response {:kysely_linkki "Not implemented"})))
