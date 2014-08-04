(ns aipal.rest-api.vastaajatunnus
  (:require [compojure.core :as c]
    [aipal.compojure-util :as cu]
    [oph.common.util.http-util :refer [json-response]]
    [aipal.arkisto.vastaajatunnus :as vastaajatunnus]))

(c/defroutes reitit
  (cu/defapi :vastaajatunnus nil :get "/" []
    (json-response (vastaajatunnus/hae-kaikki))))


