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
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(c/defroutes reitit
  (cu/defapi :vastaajatunnus-luonti kyselykertaid :post "/:kyselykertaid" [kyselykertaid & vastaajatunnus]
    (json-response (vastaajatunnus/lisaa!
                     (assoc vastaajatunnus :kyselykertaid (Integer/parseInt kyselykertaid)))))

  (cu/defapi :vastaajatunnus-tilamuutos kyselykertaid :post "/:kyselykertaid/tunnus/:vastaajatunnusid/lukitse" [kyselykertaid vastaajatunnusid lukitse]
    (json-response (vastaajatunnus/aseta-lukittu! (Integer/parseInt kyselykertaid) (Integer/parseInt vastaajatunnusid) lukitse)))

  (cu/defapi :vastaajatunnus nil :get "/:kyselykertaid" [kyselykertaid]
    (json-response (vastaajatunnus/hae-kyselykerralla (java.lang.Integer/parseInt kyselykertaid))))

  (cu/defapi :vastaajatunnus nil :get "/:kyselykertaid/tutkinto" [kyselykertaid]
    (if-let [tutkinto (vastaajatunnus/hae-viimeisin-tutkinto (java.lang.Integer/parseInt kyselykertaid) (:aktiivinen-koulutustoimija *kayttaja*))]
      (json-response tutkinto)
      {:status 200})))
