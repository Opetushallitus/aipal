;; Copyright (c) 2016 The Finnish National Board of Education - Opetushallitus
;;
;; This program is free software:  Licensed under the EUPL, Version 1.1 or - as
;; soon as they will be approved by the European Commission - subsequent versions
;; of the EUPL (the "Licence");
;;
;; You may not use this work except in compliance with the Licence.
;; You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; European Union Public Licence for more details.

(ns aipal.rest-api.vipunen
  (:require [compojure.api.core :refer [defroutes GET POST]]
            [cheshire.core :as json]
            [schema.core :as s]
            [clojure.java.io :refer [make-writer]]
            [ring.util.io :refer [piped-input-stream]]
            [aipal.arkisto.vipunen :as vipunen]
            [aipal.toimiala.vipunen :as vipunen-skeema]
            aipal.compojure-util
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [oph.common.util.http-util :refer [parse-iso-date]]))

(def maxrows 50000)

; TODO: testidata
(defroutes reitit
  (POST "/" []
    :body-params [alkupvm :- s/Str
                  loppupvm :- s/Str]
    :summary "Vastausten siirtorajapinta Vipuseen"
    :return [vipunen-skeema/VastauksenTiedot]
    (let [alkupv (parse-iso-date alkupvm)
          loppupv (parse-iso-date loppupvm)
          rivimaara (:lkm (first (vipunen/laske-kaikki alkupv loppupv)))]

      (if (< rivimaara maxrows)
        {:status 200
         :body (vipunen/hae-kaikki alkupv loppupv)      
         :headers {"Content-Type" "application/json"}}
      ; liian monta riviä
      {:status 500 
       :body (str "Rivimäärä " rivimaara " liian iso. Rajaa aikaväliä.")
       :headers {"Content-Type" "application/json"}})))

  
  (POST "/valtakunnallinen" []
    :body-params [alkupvm :- s/Str
                  loppupvm :- s/Str]
    :summary "Valtakunnallisten kysymysten vastausten siirtorajapinta Vipuseen"
    :return [vipunen-skeema/VastauksenTiedot]
    (let [alkupv (parse-iso-date alkupvm)
          loppupv (parse-iso-date loppupvm)
          rivimaara (:lkm (first (vipunen/laske-valtakunnalliset alkupv loppupv)))]

      (if (< rivimaara maxrows)
        {:status 200
         :body (vipunen/hae-valtakunnalliset alkupv loppupv)      
         :headers {"Content-Type" "application/json"}}
        ; liian monta riviä
        {:status 500 
         :body (str "Rivimäärä " rivimaara " liian iso. Rajaa aikaväliä.")
         :headers {"Content-Type" "application/json"}}))))
 ;     )))