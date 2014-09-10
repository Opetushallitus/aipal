(ns aipal.rest-api.kysymysryhma
  (:require [compojure.core :as c]
            [oph.common.util.http-util :refer [json-response]]
            [aipal.compojure-util :as cu]
            [aipal.arkisto.kysymysryhma :as arkisto]))

(c/defroutes reitit
  (cu/defapi :kysymysryhma-listaaminen nil :get "/" []
    (json-response (arkisto/hae-kysymysryhmat)))

  (cu/defapi :kysymysryhma-luonti nil :post "/" request
    (json-response (arkisto/lisaa-kysymysryhma! (select-keys (:params request)
                                                             [:nimi_fi
                                                              :selite_fi
                                                              :nimi_sv
                                                              :selite_sv])))))
