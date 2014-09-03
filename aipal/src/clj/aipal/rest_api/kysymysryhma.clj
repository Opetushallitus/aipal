(ns aipal.rest-api.kysymysryhma
  (:require [compojure.core :as c]
            [oph.common.util.http-util :refer [json-response]]
            [aipal.compojure-util :as cu]
            [aipal.arkisto.kysymysryhma :as arkisto]))

(c/defroutes reitit
  (cu/defapi :kysymysryhma-luku nil :get "/" []
    (json-response (arkisto/hae-kysymysryhmat))))
