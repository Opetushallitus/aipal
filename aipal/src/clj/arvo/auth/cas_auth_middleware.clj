(ns arvo.auth.cas-auth-middleware
  "Extends cas authentication middleware with caller-id header in validation request"
  (:require [clj-cas-client.core :refer [user-principal-filter authentication-filter ticket-validation-filter-maker Validator]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.tools.logging :as log])
  (:import
    (org.jasig.cas.client.validation Cas10TicketValidator TicketValidator AbstractUrlBasedTicketValidator AbstractUrlBasedTicketValidator TicketValidationException AssertionImpl)
    (java.io InputStreamReader StringReader BufferedReader IOException)
    (java.lang StringBuilder)
    (javax.net.ssl HttpsURLConnection)
    (java.net HttpURLConnection)))


(defn- get-response-from-server [validation-url ticket]
  (let [conn (.openConnection validation-url)]
    (try
      (when (instance? HttpsURLConnection conn)
        ; this.hostNameVerifier ja this.encoding ovat private/protected takana. Jos niitä halutaan hyödyntää
        ; täytyy käyttää clojure.reflect toiminnallisuutta.
        (.setHostnameVerifier conn (HttpsURLConnection/getDefaultHostnameVerifier)))
      (.setRequestProperty conn "Caller-Id" "1.2.246.562.10.2013112012294919827487.arvo")
      (let [input-stream-reader (InputStreamReader. (.getInputStream conn))
            buffered-reader (BufferedReader. input-stream-reader)
            string-buffer (StringBuilder. 255)]
        (loop [line (.readLine buffered-reader)]
          (when (not (nil? line))
            (.append string-buffer line)
            (.append string-buffer "\n")
            (recur (.readLine buffered-reader))))
        (.toString string-buffer))
      (catch Exception e
        (log/error (.getMessage e))
        (throw (RuntimeException. e)))
      (finally
        (when (and (some? conn) (instance? HttpURLConnection conn))
          (.disconnect conn))))))


(defn- parse-response-from-server [response]
  (when (not (clojure.string/starts-with? response "yes"))
    (throw (TicketValidationException. "CAS Server could not validate ticket.")))
  (try
    (let [string-reader (StringReader. response)
          buffered-reader (BufferedReader. string-reader)]
      (.readLine buffered-reader)
      (AssertionImpl. (.readLine buffered-reader)))
    (catch IOException e
      (throw (TicketValidationException. "Unable to parse response." e)))))

(extend-type AbstractUrlBasedTicketValidator
  Validator
  (validate [validator ticket service] (.validate validator ticket service)))


(defn cas10-ticket-validator [cas-server-url]
  (proxy [AbstractUrlBasedTicketValidator TicketValidator] [cas-server-url]
         (retrieveResponseFromServer [validation-url ticket] (get-response-from-server validation-url ticket))
         (parseResponseFromServer [response] (parse-response-from-server response))
         (getUrlSuffix [] "validate")
         (setDisableXmlSchemaValidation [disable])))

(defn validator-maker [cas-server-fn]
  (cas10-ticket-validator (cas-server-fn)))


(def ticket-validation-filter (ticket-validation-filter-maker validator-maker))


(defn cas
  "Middleware that requires the user to authenticate with a CAS server.

  The users's username is added to the request map under the :username key.

  Accepts the following options:

    :enabled      - when false, the middleware does nothing
    :no-redirect? - if this predicate function returns true for a request, a
                    403 Forbidden response will be returned instead of a 302
                    Found redirect"
  [handler cas-server-fn service-fn & {:as options}]
  (let [options (merge {:enabled true
                        :no-redirect? (constantly false)}
                       options)]
    (if (:enabled options)
      (-> handler
          user-principal-filter
          (authentication-filter cas-server-fn service-fn (:no-redirect? options))
          (ticket-validation-filter cas-server-fn service-fn)
          wrap-params)
      handler)))
