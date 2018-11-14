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
  (:require [compojure.api.sweet :refer :all]
            [cheshire.core :as json]
            [schema.core :as s]
            [clojure.java.io :refer [make-writer]]
            [clojure.tools.logging :as log]
            [ring.util.io :refer [piped-input-stream]]
            [aipal.arkisto.vipunen :as vipunen]
            [aipal.toimiala.vipunen :as vipunen-skeema]
            [aipal.compojure-util :refer :all]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [arvo.db.core :refer [*db*] :as db]
            [aipal.asetukset :refer [asetukset]]
            [oph.common.util.http-util :refer [parse-iso-date]]
            [arvo.util :refer [paginated-response]]))

(defn ^:private hae-vipunen-vastaukset [alkupvm loppupvm rivimaara-funktio hae-funktio]
  (let [alkupv (parse-iso-date alkupvm)
        loppupv (parse-iso-date loppupvm)]
    (log/info "Vipunen-API: Haetaan aikaväli" alkupvm "-" loppupvm)
    {:status 200
     :body (let [resp (hae-funktio alkupv loppupv)]
             ;(logita-errorit api-nimi alkupvm loppupvm resp)
             resp)
     :headers {"Content-Type" "application/json; charset=utf-8"}}))



(defn hae-vastaukset [alkupvm loppupvm since]
  (let [page-length (:api-page-length @asetukset)
        result (db/hae-vipunen-vastaukset (merge{:alkupvm alkupvm
                                                 :pagelength page-length
                                                 :loppupvm loppupvm}
                                              (when since {:since since})))]
    (paginated-response result :vastausid page-length "/api/vipunen" {:alkupvm alkupvm :loppupvm loppupvm})))

(defn hae-uraseuranta-vastaukset [since]
  (let [page-length (:api-page-length @asetukset)
        result (db/hae-uraseuranta-vastaukset (merge {:pagelength page-length}
                                                     (when since {:since since})))]
    (paginated-response result :vastausid page-length "/api/vipunen/uraseuranta" {})))

(defroutes reitit
  (GET "/" []
    :query-params [alkupvm :- s/Str
                   loppupvm :- s/Str
                   {since :- s/Int nil}]
    :summary "Vastausten siirtorajapinta Vipuseen"
    :return s/Any
    (hae-vastaukset alkupvm loppupvm since))
  (GET "/uraseuranta" []
    :query-params [{since :- s/Int nil}]
    :summary "Uraseurannan vastausten siirtorajapinta Vipuseen"
    :return s/Any
    (hae-uraseuranta-vastaukset since))


  (POST "/valtakunnallinen" []
    :body-params [alkupvm :- s/Str
                  loppupvm :- s/Str]
    :summary "Valtakunnallisten kysymysten vastausten siirtorajapinta Vipuseen"
    :return [vipunen-skeema/VastauksenTiedot]
    (hae-vipunen-vastaukset alkupvm loppupvm vipunen/laske-valtakunnalliset vipunen/hae-valtakunnalliset)))
