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

(ns aipalvastaus.rest-api.i18n
  (:import (java.util Locale
                      ResourceBundle
                      ResourceBundle$Control))
  (:require [compojure.core :as c]
            [schema.core :as schema]
            [oph.common.util.http-util :refer [json-response-nocache]]
            [oph.common.util.util :refer [pisteavaimet->puu]]))

(defn validoi-kieli []
  (schema/pred #(some #{%} ["fi" "sv" "en"])))

(defn hae-tekstit [kieli]
  (ResourceBundle/clearCache)
  (let [bundle (ResourceBundle/getBundle "i18n/tekstit" (Locale. kieli) (ResourceBundle$Control/getNoFallbackControl ResourceBundle$Control/FORMAT_PROPERTIES))]
    (->> (for [key (.keySet bundle)]
           [(keyword key) (.getString bundle key)])
         (into {})
         pisteavaimet->puu)))

(c/defroutes reitit
  (c/GET "/:kieli" [kieli :as req]
    (schema/validate (validoi-kieli) kieli)
    (json-response-nocache (hae-tekstit kieli))))
