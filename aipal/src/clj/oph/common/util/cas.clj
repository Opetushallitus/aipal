(ns oph.common.util.cas
  (:require [clj-http.client :as http]
            [aipal.asetukset :refer [asetukset]]))

(defn ^:private hae-ticket-granting-url [url user password unsafe-https]
  (let [{:keys [status headers]} (http/post (str url "/v1/tickets")
                                            {:form-params {:username user
                                                           :password password}
                                             :insecure? unsafe-https})]
    (if (= status 201)
      (headers "location")
      (throw (RuntimeException. "CAS-kirjautuminen epäonnistui")))))

(defn ^:private hae-service-ticket [url palvelu-url unsafe-https]
  (let [{:keys [status body]} (http/post url
                                         {:form-params {:service palvelu-url}
                                          :insecure? unsafe-https})]
    (if (= status 200)
      body
      (throw (RuntimeException. "Service ticketin pyytäminen CASilta epäonnistui")))))

(defn request-with-cas-auth [palvelu options]
  (let [{cas-url :url
         unsafe-https :unsafe-https
         cas-enabled :enabled} (:cas-auth-server @asetukset)
        {palvelu-url :url
         user :user
         password :password} (get @asetukset palvelu)]
    (if cas-enabled
      (let [ticket-granting-url (hae-ticket-granting-url cas-url user password unsafe-https)
            service-ticket (hae-service-ticket ticket-granting-url palvelu-url unsafe-https)]
        (http/request (assoc-in options [:query-params :ticket] service-ticket)))
      (http/request options))))

(defn get-with-cas-auth
  ([palvelu url]
    (get-with-cas-auth palvelu url {}))
  ([palvelu url options]
    (request-with-cas-auth palvelu (merge options {:method :get
                                                   :url url}))))
