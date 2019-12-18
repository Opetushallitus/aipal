(ns oph.common.util.cas
  (:require [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [aipal.asetukset :refer [asetukset]]
            [oph.common.util.util :refer [oletus-header]]
            [cheshire.core :as cheshire]))

(defn ^:private hae-ticket-granting-url [url user password unsafe-https]
  (let [{:keys [status headers]} (http/post (str url "/v1/tickets")
                                            (oletus-header {:form-params {:username user
                                                                          :password password}
                                                            :insecure? unsafe-https}))]
    (if (= status 201)
      (headers "location")
      (throw (RuntimeException. "CAS-kirjautuminen epäonnistui")))))

(defn ^:private hae-service-ticket [url palvelu-url unsafe-https]
  (let [{:keys [status body]} (http/post url
                                         (oletus-header {:form-params {:service palvelu-url}
                                                        :insecure? unsafe-https}))]
    (if (= status 200)
      body
      (throw (RuntimeException. "Service ticketin pyytäminen CASilta epäonnistui")))))

(def cookie-store (clj-http.cookies/cookie-store))

(defn request-with-cas-auth [palvelu options]
  (let [{cas-url :url
         unsafe-https :unsafe-https
         cas-enabled :enabled} (:cas-auth-server @asetukset)
        {palvelu-url :url
         user :user
         password :password} (get @asetukset palvelu)]
    (if cas-enabled
      (let [ticket-granting-url (hae-ticket-granting-url cas-url user password unsafe-https)
            service-ticket (hae-service-ticket ticket-granting-url (str palvelu-url "/j_spring_cas_security_check") unsafe-https)
            prequel-url (format "%s/cas/prequel" palvelu-url)]
;        Lämmittelypyyntö. Ilman tätä muut kuin get-pyynnöt epäonnistuvat (ohjaa kirjautumissivulle)
        (http/get prequel-url (oletus-header {:query-params {"ticket" service-ticket} :cookie-store cookie-store}))
        (http/request (oletus-header (assoc options :cookie-store cookie-store))))
      (http/request (oletus-header options)))))

(defn get-with-cas-auth
  ([palvelu url]
   (get-with-cas-auth palvelu url {}))
  ([palvelu url options]
   (request-with-cas-auth palvelu (merge options {:method :get
                                                  :url url}))))

(defn post-with-cas-auth
  ([palvelu url]
   (post-with-cas-auth palvelu url {}))
  ([palvelu url options]
   (request-with-cas-auth palvelu (merge options {:method :post
                                                  :url url}))))
