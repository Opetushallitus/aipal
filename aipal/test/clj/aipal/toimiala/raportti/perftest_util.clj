(ns aipal.toimiala.raportti.perftest-util
  (:require 
    [org.httpkit.client :as http]
    [cheshire.core :refer :all]))

(defn auth-params [uid basic-auth]
  {:headers
   {"uid" uid
    "x-xsrf-token" "token"
    "Cookie" "cache=true; XSRF-TOKEN=token"}
  :basic-auth basic-auth})

(defn get-configuration []
  {:base-url (or (System/getenv "AIPAL_URL") "http://192.168.50.1:8082")
   :userid (or (System/getenv "AIPAL_UID")  "T-1001")
   :basic-auth (or (System/getenv "AIPAL_AUTH") "pfft:thx")
   :request-count (cond 
                    (nil? (System/getenv "AIPAL_URL")) 1
                    :else 50)}
  )

(defn async-http-requ [url uid basic-auth user-id context callback]
  (let [check-status (fn [{:keys [status]}] (callback (= 200 status)))]
    (http/get url (auth-params uid basic-auth) 
      check-status))) 

(defn async-http-json-requ [url uid basic-auth body user-id context callback]
  (let [check-status (fn [{:keys [status]}] (callback (= 200 status)))
        header-map (merge (:headers (auth-params uid basic-auth))
                     {"Content-Type" "application/json;charset=UTF-8"})]
    (http/post url {:headers header-map
                    :basic-auth basic-auth
                    :body body}
      check-status)))
