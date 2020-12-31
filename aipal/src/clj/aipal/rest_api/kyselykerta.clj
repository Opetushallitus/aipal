;; Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.rest-api.kyselykerta
  (:require [compojure.api.core :refer [defroutes DELETE GET POST PUT]]
            [schema.core :as s]
            [aipal.arkisto.kyselykerta :as arkisto]
            aipal.compojure-util
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [oph.common.util.http-util :refer [parse-iso-date response-or-404]]
            [oph.common.util.util :refer [paivita-arvot]]))

(defroutes reitit
  (GET "/" []
    :kayttooikeus :katselu
    (response-or-404 (arkisto/hae-koulutustoimijan-kyselykerrat (:aktiivinen-koulutustoimija *kayttaja*))))

  (GET "/:kyselykertaid/vastaustunnustiedot" []
       :path-params [kyselykertaid :- s/Int]
       :kayttooikeus [:katselu {:kyselykertaid kyselykertaid}]
       (response-or-404 (arkisto/hae-kyselykerran-oppilaitokset kyselykertaid)))

  (GET "/:kyselykertaid" []
    :path-params [kyselykertaid :- s/Int]
    :kayttooikeus [:katselu {:kyselykertaid kyselykertaid}]
    (response-or-404 (arkisto/hae-yksi kyselykertaid)))

  (POST "/" []
    :body-params [kyselyid :- s/Int
                  kyselykerta]
    :kayttooikeus [:kyselykerta {:kyselyid kyselyid}]
    (let [kyselykerta-parsittu (paivita-arvot kyselykerta [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)]
         (if (arkisto/samanniminen-kyselykerta? kyselyid (:nimi kyselykerta))
           {:status 400
            :body "kyselykerta.samanniminen_kyselykerta"}
           (response-or-404 (arkisto/lisaa! kyselyid kyselykerta-parsittu)))))

  (POST "/:kyselykertaid" []
    :path-params [kyselykertaid :- s/Int]
    :body [kyselykerta s/Any]
    :kayttooikeus [:kyselykerta {:kyselykertaid kyselykertaid}]
    (let [kyselykerta-parsittu (paivita-arvot kyselykerta [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)]
      (if (arkisto/samanniminen-kyselykerta? (:kyselyid kyselykerta) (:nimi kyselykerta))
        {:status 400
         :body "kyselykerta.samanniminen_kyselykerta"}
        (response-or-404 (arkisto/paivita! kyselykertaid kyselykerta-parsittu)))))

  (PUT "/:kyselykertaid/lukitse" []
    :path-params [kyselykertaid :- s/Int]
    :body-params [lukitse :- Boolean]
    :kayttooikeus [:kyselykerta {:kyselykertaid kyselykertaid}]
    (response-or-404 (arkisto/aseta-lukittu! kyselykertaid lukitse)))

  (DELETE "/:kyselykertaid" []
    :path-params [kyselykertaid :- s/Int]
    :kayttooikeus [:kyselykerta {:kyselykertaid kyselykertaid}]
    (arkisto/poista! kyselykertaid)
    {:status 204}))