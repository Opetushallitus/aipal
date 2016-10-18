(ns aipal.basic-auth
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str])
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
    (let [{:keys [tunnus salasana]} (:basic-auth asetukset)]
      (if-not (and tunnus salasana)
        (throw (IllegalStateException. "Basic-autentikaation tunnusta ja salasanaa ei ole asetettu"))
        (if (= (hae-tunnus request) [tunnus salasana])
          (handler request)
          {:status 401
           :headers {"www-authenticate" "Basic realm=\"restricted\""}})))))