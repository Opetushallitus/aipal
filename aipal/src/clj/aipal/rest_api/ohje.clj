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

(ns aipal.rest-api.ohje
  (:require [compojure.api.core :refer [defroutes GET PUT]]
            [schema.core :as s]
            [aipal.arkisto.ohje :as arkisto]
            aipal.compojure-util
            [oph.common.util.http-util :refer [response-or-404]]))

(defroutes reitit
  (GET "/:ohjetunniste" []
    :path-params [ohjetunniste :- s/Str]
    :kayttooikeus :katselu
    (if-let [ohje (arkisto/hae ohjetunniste)]
      (response-or-404 ohje)
      {:status 200}))

  (PUT "/:ohjetunniste" []
    :body-params [ohjetunniste :- s/Str
                  teksti_fi :- s/Str
                  teksti_sv :- s/Str
                  teksti_en :- s/Str]
    :kayttooikeus :yllapitaja
    (arkisto/muokkaa-tai-luo-uusi! {:ohjetunniste ohjetunniste
                                    :teksti_fi teksti_fi
                                    :teksti_sv teksti_sv
                                    :teksti_en teksti_en})
    {:status 200}))
