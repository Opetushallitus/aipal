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
  (:require [compojure.core :as c]
            [korma.db :as db]
            [schema.core :as schema]
            [aipal.compojure-util :as cu]
            [aipal.arkisto.kyselykerta :as arkisto]
            [oph.common.util.http-util :refer [json-response parse-iso-date]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(defn paivita-arvot [m avaimet f]
  (reduce #(update-in % [%2] f) m avaimet))

(c/defroutes reitit
  (cu/defapi :kysely nil :get "/" []
    (json-response (arkisto/hae-kaikki (:aktiivinen-koulutustoimija *kayttaja*))))

  (cu/defapi :kyselykerta-luku kyselykertaid :get "/:kyselykertaid" [kyselykertaid]
    (json-response (arkisto/hae-yksi (Integer/parseInt kyselykertaid))))

  (cu/defapi :kyselykerta-luonti kyselyid :post "/" [kyselyid kyselykerta]
    (let [kyselykerta-parsittu (paivita-arvot kyselykerta [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)]
      (json-response (arkisto/lisaa! kyselyid kyselykerta-parsittu))))

  (cu/defapi :kyselykerta-muokkaus kyselykertaid :post "/:kyselykertaid" [kyselykertaid & kyselykerta]
    (let [kyselykerta-parsittu (paivita-arvot kyselykerta [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)]
      (json-response (arkisto/paivita! (Integer/parseInt kyselykertaid) kyselykerta-parsittu))))

  (cu/defapi :kyselykerta-tilamuutos kyselykertaid :put "/:kyselykertaid/lukitse" [kyselykertaid]
    (json-response (arkisto/lukitse! (Integer/parseInt kyselykertaid))))

  (cu/defapi :kyselykerta-tilamuutos kyselykertaid :put "/:kyselykertaid/avaa" [kyselykertaid]
    (json-response (arkisto/avaa! (Integer/parseInt kyselykertaid)))))
