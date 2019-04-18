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

(ns aipal.rest-api.tiedote
  (:require [compojure.api.core :refer [defroutes DELETE GET POST]]
            [schema.core :as s]
            [aipal.arkisto.tiedote :as arkisto]
            [aipal.compojure-util :as cu]
            [oph.common.util.http-util :refer [response-or-404]]))

(defroutes reitit
  (GET "/" []
    :kayttooikeus :katselu
    (response-or-404 (or (arkisto/hae) {})))

  (POST "/" request
    :body-params [fi :- s/Str
                  sv :- s/Str
                  en :- s/Str]
    :kayttooikeus :yllapitaja
    (response-or-404 (arkisto/poista-ja-lisaa! {:fi fi
                                                :sv sv
                                                :en en})))

  (DELETE "/" []
    :kayttooikeus :yllapitaja
    (arkisto/poista!)
    {:status 200}))
