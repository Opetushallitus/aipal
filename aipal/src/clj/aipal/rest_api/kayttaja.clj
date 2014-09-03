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

(ns aipal.rest-api.kayttaja
  (:require [compojure.core :as c]
            [oph.korma.korma-auth :as ka]
            [aipal.arkisto.kayttaja :as arkisto]
            [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
            [aipal.toimiala.kayttajaroolit :refer [kayttajaroolit]]
            [oph.common.util.http-util :refer [json-response]]
            [aipal.toimiala.kayttajaoikeudet :as ko]
            [aipal.compojure-util :as cu]
            [korma.db :as db]))

(c/defroutes reitit
  (cu/defapi :impersonointi nil :post "/impersonoi" [:as {session :session}, oid]
    {:status 200
     :session (assoc session :impersonoitu-oid oid)})

  (cu/defapi :impersonointi nil :post "/lopeta-impersonointi" {session :session}
    {:status 200
     :session (dissoc session :impersonoitu-oid)})

  (cu/defapi :impersonointi nil :get "/impersonoitava" [termi]
    (json-response (arkisto/hae-impersonoitava-termilla termi)))

  (cu/defapi :omat_tiedot nil :get "/" []
    (json-response (kayttajaoikeus-arkisto/hae-oikeudet)))

  (cu/defapi :kayttajan_tiedot oid :get "/:oid" [oid]
   true))
