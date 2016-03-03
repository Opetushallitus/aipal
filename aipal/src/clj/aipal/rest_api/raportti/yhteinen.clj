;; Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.rest-api.raportti.yhteinen
  (:require [cheshire.core :as cheshire]
            [oph.common.util.util :refer [muunna-avainsanoiksi]]))

(defn wrap-muunna-raportti-json-param [handler]
  (fn [request]
    (handler (if-let [raportti (get-in request [:query-params "raportti"])]
               (let [muunnettu (muunna-avainsanoiksi (cheshire/parse-string raportti))]
                 (merge-with merge request
                             {:query-params {:parametrit muunnettu}}
                             {:params {:parametrit muunnettu}}))
               request))))

(defn numero? [s]
  (try
    (Integer/parseInt s)
    true
    (catch NumberFormatException _
      false)))

(defn korjaa-numero-avaimet
  "Muuttaa annetusta mapista stringeiksi kaikki keyword-avaimet, jotka koostuvat pelkistä numeroista"
  [m]
  (clojure.walk/postwalk (fn [x]
                           (if (and (keyword? x) (numero? (name x)))
                             (name x)
                             x))
                         m))