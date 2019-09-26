(ns arvo.auth.api
  (:require [clojure.string :as str]
            [arvo.db.core :refer [*db*] :as db]
            [buddy.hashers :as hashers]
            [clojure.tools.logging :as log])
  (:import java.util.Base64))

(defn parse-credentials [request]
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

(defn check-credentials [credentials]
  (let [saved-credentials (db/hae-api-kayttaja {:tunnus (first credentials)})]
    (when (hashers/check (second credentials) (:salasana saved-credentials))
      saved-credentials)))

(defn wrap-authentication
  [required-right handler]
  (fn [request]
    (let [credentials (parse-credentials request)
          api-user (check-credentials credentials)]
      (if (and api-user (get-in api-user [:oikeudet required-right]))
        (handler (merge request (select-keys api-user [:organisaatio :oikeudet])))
        {:status 401
         :headers {"www-authenticate" "Basic realm=\"restricted\""}}))))


