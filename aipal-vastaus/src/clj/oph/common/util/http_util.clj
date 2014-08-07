(ns oph.common.util.http-util
  (:require
    [cheshire.core :as cheshire]
    [clj-time.format :refer [formatter parse-local-date]]
    [clj-time.coerce :as time-coerce]
    [schema.core :as s] ))

(defn json-response
  ([data cache-max-age]
   (if (nil? data)
     {:status 404}
     {:status 200
      :body (cheshire/generate-string data)
      :headers {"Content-Type" "application/json"
                "Cache-control" (str "max-age=")}}))
  ([data schema cache-max-age]
   (json-response (s/validate (s/maybe schema) data) cache-max-age)))

(defn json-response-nocache
  [data]
  (assoc-in (json-response data 0) [:headers "Cache-control"] "max-age=0"))

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
