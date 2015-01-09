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

(ns aipal.rest-api.koulutustoimija
  (:require [compojure.core :as c]
            [aipal.compojure-util :as cu]
            [aipal.arkisto.koulutustoimija :as koulutustoimija]
            [aipal.arkisto.oppilaitos :as oppilaitos]
            [oph.common.util.http-util :refer [json-response]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(c/defroutes reitit
  (cu/defapi :koulutustoimija nil :get "/aktiivinen" []
    (json-response (koulutustoimija/hae (:aktiivinen-koulutustoimija *kayttaja*))))
  (cu/defapi :koulutustoimija nil :get "/" []
    (json-response (koulutustoimija/hae-kaikki)))
  (cu/defapi :koulutustoimija nil :get "/:ytunnus/oppilaitos" [ytunnus]
    (json-response (oppilaitos/hae-koulutustoimijan-oppilaitokset ytunnus))))
