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

(ns aipal.rest-api.http-util
  (:require
    [cheshire.core :as cheshire]
    [clj-time.format :refer [formatter parse-local-date]]
    [clj-time.coerce :as time-coerce]
    [clj-time.core :as time]
    [schema.core :as s]))

(defn json-response
  ([data]
   (if (nil? data)
     {:status 404}
     {:status 200
      :body (cheshire/generate-string data)
      :headers {"Content-Type" "application/json"}}))
  ([data schema]
   (json-response (s/validate (s/maybe schema) data))))

(defn convert-instances-of [c f m]
  (clojure.walk/postwalk #(if (instance? c %) (f %) %) m))

(defn joda-date->sql-date [m]
  (convert-instances-of org.joda.time.LocalDate
                        time-coerce/to-sql-date
                        m))

(defn try-parse-local-date
  [f d]
  (try
    (joda-date->sql-date (parse-local-date (formatter f) d))
    (catch IllegalArgumentException e
      nil)))

(defn parse-iso-date
  [d]
  (when d
    (or
      (try-parse-local-date "yyyy-MM-dd'T'HH:mm:ss.sssZ" d)
      (try-parse-local-date "yyyy-MM-dd" d)
      (try-parse-local-date "dd.MM.yyyy" d)
      (throw (IllegalArgumentException. "Virheellinen pvm formaatti")))))
