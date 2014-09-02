(ns aipal.rest-api.kysymysryhma
  (:require [compojure.core :as c]
            [aipal.compojure-util :as cu]
            [oph.common.util.http-util :refer [json-response]]))

(c/defroutes reitit
  (cu/defapi :kysymysryhma-luku nil :get "/" []
    (json-response [{:nimi "foo"}
                    {:nimi "bar"}
                    {:nimi "baz"}])))
