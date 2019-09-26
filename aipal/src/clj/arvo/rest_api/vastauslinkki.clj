(ns arvo.rest-api.vastauslinkki
  (:require [compojure.api.sweet :refer :all]
            [arvo.db.core :refer [*db*] :as db]
            [arvo.schema.export :refer :all]
            [oph.common.util.http-util :refer [response-or-404]]
            [clojure.tools.logging :as log]
            [schema.core :as s]

            [arvo.rest-api.automaattitunnus :refer [amispalaute-tunnus]]
            [aipal.asetukset :refer [asetukset]]))



