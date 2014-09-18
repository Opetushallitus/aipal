(ns aipal.rest-api.kysymysryhma
  (:require [compojure.core :as c]
            [oph.common.util.http-util :refer [json-response]]
            [aipal.compojure-util :as cu]
            [aipal.arkisto.kysymysryhma :as arkisto]))

(defn jarjesta-kysymykset [kysymykset]
  (map #(assoc %1 :jarjestys %2) kysymykset (range)))

(c/defroutes reitit
  (cu/defapi :kysymysryhma-listaaminen nil :get "/" []
    (json-response (arkisto/hae-kysymysryhmat)))

  (cu/defapi :kysymysryhma-luonti nil :post "/" [nimi_fi selite_fi nimi_sv selite_sv kysymykset]
    (let [kysymysryhma (arkisto/lisaa-kysymysryhma! {:nimi_fi nimi_fi
                                                     :selite_fi selite_fi
                                                     :nimi_sv nimi_sv
                                                     :selite_sv selite_sv})]
      (doseq [k (jarjesta-kysymykset kysymykset)
              :let [kysymys (dissoc k :muokattava)
                    kysymys (assoc kysymys :kysymysryhmaid (:kysymysryhmaid kysymysryhma))]]
        (arkisto/lisaa-kysymys! kysymys))
      (json-response kysymysryhma))))
