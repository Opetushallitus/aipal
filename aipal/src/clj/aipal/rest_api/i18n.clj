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

(ns aipal.rest-api.i18n
  (:import (java.util Locale
                      ResourceBundle
                      ResourceBundle$Control))
  (:require [compojure.api.core :refer [defroutes GET]]
            [schema.core :as schema]
            aipal.compojure-util
            [oph.common.util.http-util :refer [response-or-404]]
            [oph.common.util.util :refer [pisteavaimet->puu]]))

(defn validoi-kieli []
  (schema/pred #(some #{%} ["fi" "sv" "en"])))

(def Kieli (schema/enum "fi" "sv" "en"))

(defn hae-tekstit [kieli]
  (ResourceBundle/clearCache)
  (let [bundle (ResourceBundle/getBundle "i18n/tekstit" (Locale. kieli) (ResourceBundle$Control/getNoFallbackControl ResourceBundle$Control/FORMAT_PROPERTIES))]
    (->> (for [key (.keySet bundle)]
           [(keyword key) (.getString bundle key)])
         (into {})
         pisteavaimet->puu)))

(defroutes reitit
  (GET "/:kieli" [kieli :as req]
    :path-params [kieli :- Kieli]
    :kayttooikeus :katselu
    (response-or-404 (hae-tekstit kieli))))
