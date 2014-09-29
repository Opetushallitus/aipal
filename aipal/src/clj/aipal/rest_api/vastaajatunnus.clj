;; Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.rest-api.vastaajatunnus
  (:require [compojure.core :as c]
    [aipal.compojure-util :as cu]
    [oph.common.util.http-util :refer [json-response parse-iso-date]]
    [aipal.arkisto.vastaajatunnus :as vastaajatunnus]))

(c/defroutes reitit
  (cu/defapi :vastaajatunnus nil :post "/:kyselykertaid" [kyselykertaid henkilokohtainen & vastaajatunnus]
    (json-response (vastaajatunnus/lisaa-vastaajatunnuksia
                     (Integer/parseInt kyselykertaid)
                     henkilokohtainen
                     (merge vastaajatunnus {:voimassa_loppupvm (parse-iso-date (:voimassa_loppupvm vastaajatunnus))
                                            :voimassa_alkupvm (parse-iso-date (:voimassa_alkupvm vastaajatunnus))}))))

  (cu/defapi :vastaajatunnus nil :get "/:kyselykertaid" [kyselykertaid]
    (json-response (vastaajatunnus/hae-kyselykerralla (java.lang.Integer/parseInt kyselykertaid)))))
