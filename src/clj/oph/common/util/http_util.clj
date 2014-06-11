(ns oph.common.util.http-util
  (:require
    [cheshire.core :as cheshire]
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
