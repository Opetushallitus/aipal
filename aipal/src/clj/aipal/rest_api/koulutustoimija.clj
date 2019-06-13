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
  (:require [compojure.api.core :refer [defroutes GET]]
            [aipal.arkisto.koulutustoimija :as koulutustoimija]
            aipal.compojure-util
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [oph.common.util.http-util :refer [response-or-404]]
            [schema.core :as s]))

(defroutes reitit
  (GET "/aktiivinen" []
    :kayttooikeus :katselu
    (response-or-404 (koulutustoimija/hae (:aktiivinen-koulutustoimija *kayttaja*))))
  (GET "/" []
    :kayttooikeus :katselu
    (response-or-404 (koulutustoimija/hae-kaikki)))
  (GET "/koulutusluvalliset" []
    :kayttooikeus :katselu
    (response-or-404 (koulutustoimija/hae-koulutusluvalliset)))
  (GET "/hae-nimella" [termi]
    :kayttooikeus :yllapitaja
    :query-params [termi :- s/Str]
    (response-or-404 (koulutustoimija/hae-nimella termi))))