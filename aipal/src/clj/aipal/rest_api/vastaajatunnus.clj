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
            [oph.common.util.util :refer [paivita-arvot]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(c/defroutes reitit
  (cu/defapi :vastaajatunnus-luonti kyselykertaid :post "/:kyselykertaid" [kyselykertaid & vastaajatunnus]
    (let [vastaajatunnus (paivita-arvot vastaajatunnus [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)]
      (json-response (vastaajatunnus/lisaa!
                       (assoc vastaajatunnus :kyselykertaid (Integer/parseInt kyselykertaid))))))

  (cu/defapi :vastaajatunnus-tilamuutos kyselykertaid :post "/:kyselykertaid/tunnus/:vastaajatunnusid/lukitse" [kyselykertaid vastaajatunnusid lukitse]
    (json-response (vastaajatunnus/aseta-lukittu! (Integer/parseInt kyselykertaid) (Integer/parseInt vastaajatunnusid) lukitse)))

  (cu/defapi :vastaajatunnus-muokkaus kyselykertaid :post "/:kyselykertaid/tunnus/:vastaajatunnusid/muokkaa-lukumaaraa" [kyselykertaid vastaajatunnusid lukumaara]
    (let [kyselykertaid (Integer/parseInt kyselykertaid)
          vastaajatunnusid (Integer/parseInt vastaajatunnusid)
          vastaajatunnus (vastaajatunnus/hae kyselykertaid vastaajatunnusid)
          vastaajat (vastaajatunnus/laske-vastaajat vastaajatunnusid)]
      (if (not (:muokattavissa vastaajatunnus))
        (throw (IllegalArgumentException. "Vastaajatunnus ei ole enää muokattavissa")))
      (if (and (> lukumaara 0) (>= lukumaara vastaajat))
        (json-response (vastaajatunnus/muokkaa-lukumaaraa kyselykertaid vastaajatunnusid lukumaara))
        {:status 403})))

  (cu/defapi :vastaajatunnus-poisto kyselykertaid :delete "/:kyselykertaid/tunnus/:vastaajatunnusid" [kyselykertaid vastaajatunnusid]
    (let [vastaajatunnusid (Integer/parseInt vastaajatunnusid)
          vastaajat (vastaajatunnus/laske-vastaajat vastaajatunnusid)]
      (if (= vastaajat 0)
        (json-response (vastaajatunnus/poista! (Integer/parseInt kyselykertaid) vastaajatunnusid))
        {:status 403})))

  (cu/defapi :vastaajatunnus nil :get "/:kyselykertaid" [kyselykertaid]
    (json-response (vastaajatunnus/hae-kyselykerralla (java.lang.Integer/parseInt kyselykertaid))))

  (cu/defapi :vastaajatunnus nil :get "/:kyselykertaid/tutkinto" [kyselykertaid]
    (if-let [tutkinto (vastaajatunnus/hae-viimeisin-tutkinto (java.lang.Integer/parseInt kyselykertaid) (:aktiivinen-koulutustoimija *kayttaja*))]
      (json-response tutkinto)
      {:status 200})))
