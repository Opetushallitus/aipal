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

(ns aipal.rest-api.tutkinto
  (:require [compojure.api.core :refer [defroutes GET]]
            [aipal.arkisto.tutkinto :as tutkinto]
            aipal.compojure-util
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [oph.common.util.http-util :refer [response-or-404]]
            [schema.core :as s]
            [clojure.tools.logging :as log]))

(defroutes reitit
   (GET "/voimassaolevat-listana" []
        :kayttooikeus :katselu
        (response-or-404 (tutkinto/hae-voimassaolevat-tutkinnot-listana)))
  (GET "/voimassaolevat" []
    :kayttooikeus :katselu
    (response-or-404 (tutkinto/hae-voimassaolevat-tutkinnot)))
  (GET "/vanhentuneet" []
    :kayttooikeus :katselu
    (response-or-404 (tutkinto/hae-vanhentuneet-tutkinnot)))
  (GET "/koulutustoimija" []
       :kayttooikeus :katselu
       :query-params [kyselytyyppi :- String]
       (let [y-tunnus (:aktiivinen-koulutustoimija *kayttaja*)]
         (response-or-404 (tutkinto/hae-koulutustoimijan-voimassaolevat-tutkinnot y-tunnus kyselytyyppi))))
  (GET "/koulutustoimija/:y-tunnus" []
       :kayttooikeus :katselu
       :path-params [y-tunnus :- String]
       :query-params [kyselytyyppi :- String]
       (response-or-404 (tutkinto/hae-koulutustoimijan-voimassaolevat-tutkinnot y-tunnus kyselytyyppi)))
  (GET "/kyselytyyppi/:kyselytyyppi" []
    :kayttooikeus :katselu
    :path-params [kyselytyyppi :- String]
    (response-or-404 (tutkinto/hae-kyselytyypin-tutkinnot kyselytyyppi)))
  (GET "/jarjestajat/:tutkintotunnus" []
    :path-params [tutkintotunnus :- String]
    :kayttooikeus :katselu
    (response-or-404 (tutkinto/hae-tutkinnon-jarjestajat tutkintotunnus)))
  (GET "/tutkinnonosat" []
    :kayttooikeus :katselu
    (response-or-404 (tutkinto/hae-tutkinnon-osat))))
