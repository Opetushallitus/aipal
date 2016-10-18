(ns aipal.basic-auth
  (:require [clojure.string :as str])
  (:import java.util.Base64))

(defn hae-tunnus [request]
  (when-let [auth-header (get-in request [:headers "authorization"])]
    (let [decoder (Base64/getMimeDecoder)
          [_ auth-encoded] (re-matches #"Basic ([A-z0-9+/=]*)" auth-header)]
      (when-not (str/blank? auth-encoded)
        (try
          (->
            (.decode decoder auth-encoded)
            (String. "UTF-8")
            (str/split #":"))
          (catch IllegalArgumentException _))))))

(defn wrap-basic-authentication [handler asetukset]
  (fn [request]
    (if (= (hae-tunnus request) [(get-in asetukset [:basic-auth :tunnus]) (get-in asetukset [:basic-auth :salasana])])
      (handler request)
      {:status 401
       :headers {"www-authenticate" "Basic realm=\"restricted\""}})))