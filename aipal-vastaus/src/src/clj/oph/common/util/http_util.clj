(ns oph.common.util.http-util
  (:require
    [cheshire.core :as cheshire]
    [valip.core :refer [validate]]
    [clj-time.format :refer [formatter formatters parse unparse parse-local-date with-locale]]
    [clj-time.coerce :as time-coerce]
    [clj-time.core :as time]
    [oph.common.util.util :refer [uusin-muokkausaika-tai-nykyhetki]]
    [schema.core :as s] )
  (:import java.io.ByteArrayInputStream))

(def ^:private http-date-format
  (->
    (formatter "EEE, dd MMM yyyy HH:mm:ss 'GMT'")
    (with-locale java.util.Locale/US)))

(defn parse-http-date
  [d]
  (parse http-date-format d))

(defn format-http-date
  [d]
  (unparse http-date-format d))

(defn get-cache-date
  [req]
  (parse-http-date (get-in req [:headers "if-modified-since"] "Thu, 01 Jan 1970 00:00:00 GMT")))

(defn try-parse-local-date
  [f d]
  (try
    (parse-local-date (formatter f) d)
    (catch IllegalArgumentException e
      nil)))

(defn try-parse-local-date-with-tz
  [f d]
  (try
    (time-coerce/to-local-date (time/to-time-zone (parse (formatter f) d) (time/default-time-zone)))
    (catch IllegalArgumentException e
      nil)))

(defn parse-iso-date
  "Does not support all valid ISO date format presentations: https://en.wikipedia.org/wiki/ISO_8601#Dates"
  [d]
  (when d
    (or
      (try-parse-local-date-with-tz "yyyy-MM-dd'T'HH:mm:ss.SSSZ" d)
      (try-parse-local-date-with-tz "yyyy-MM-dd'T'HH:mm:ssZ" d)
      (try-parse-local-date "yyyy-MM-dd" d)
      (try-parse-local-date "dd.MM.yyyy" d)
      (throw (IllegalArgumentException. (str "Virheellinen pvm formaatti: " d))))))

(defn get-cache-headers
  [last-modified]
  {"Cache-Control" "max-age=0, must-revalidate, proxy-revalidate"
   "Last-Modified" (format-http-date last-modified)
   "Content-Type" "application/json"})

; Jos sisällön viimeisintä muokkausaikaa ei ole, käytetään nykyhetkeä
(defn cachable-response
  ([req vseq]
   (let [cache-muokattu (get-cache-date req)
         vseq-muokattu (->
                         (uusin-muokkausaika-tai-nykyhetki vseq [:muutettuaika])
                         (.withMillisOfSecond 0))]
     (if (> 0 (compare cache-muokattu vseq-muokattu))
       {:status 200
        :body vseq
        :headers (get-cache-headers vseq-muokattu)}
       {:status 304}))))

(defn response-or-404
  ([data]
    (if (nil? data)
      {:status 404}
      {:status 200
       :body data})))

(defn korvaa-virheteksti [virhetekstit virhe]
  (let [[virhe & parametrit ] (if (keyword? virhe)
                                [virhe]
                                virhe)]
    (apply format (virhetekstit virhe (name virhe)) parametrit)))

(defn korvaa-virhetekstit [valip-errors virhetekstit]
  (into {} (for [[kentta virheet] valip-errors]
             [kentta (map (partial korvaa-virheteksti virhetekstit) virheet)])))

(defn file-download-response
  ([data filename content-type]
    (file-download-response data filename content-type {}))
  ([data filename content-type options]
    {:status 200
     :body (if-let [charset (:charset options)]
             (ByteArrayInputStream. (.getBytes (str data) charset))
             (ByteArrayInputStream. data))
     :headers {"Content-type" content-type
               "Content-Disposition" (str "attachment; filename=\"" filename "\"")}}))

(defn csv-download-response
  [data filename]
  (file-download-response data filename "text/csv" {:charset "CP1252"}))

(defn file-upload-response
  [data]
  (assoc (response-or-404 data) :headers {"Content-Type" "text/html"}))

(defn validoi-entity-saannoilla
  [entity saannot]
  (apply validate entity saannot))

(defn luo-validoinnin-virhevastaus
  [virheet virhetekstit]
  {:status 400
   :headers {"Content-Type" "application/json"}
   :body (cheshire/generate-string
           {:errors (korvaa-virhetekstit virheet virhetekstit)})})

(defn validoi*
  [m saannot virhetekstit f]
  (if-let [valip-errors (validoi-entity-saannoilla m saannot)]
    (luo-validoinnin-virhevastaus valip-errors virhetekstit)
    (f)))

(defmacro validoi
  [m saannot virhetekstit & body]
  `(validoi* ~m ~saannot ~virhetekstit (fn [] ~@body)))

(defmacro sallittu-jos [ehto & body]
  ;; 403 olisi kuvaavampi koodi, mutta sillä ilmoitetaan myös CAS-istunnon
  ;; puuttumisesta, ja käyttöliittymä lataa sivun uudelleen 403:n saatuaan.
  ;; Speksi sanoo 403:sta "If the server does not wish to make this information
  ;; available to the client, the status code 404 (Not Found) can be used
  ;; instead.", joten käytetään 404:ää.
  `(if ~ehto (do ~@body) {:status 404}))

(defn response-nocache
  [data]
  (assoc-in (response-or-404 data) [:headers "Cache-control"] "max-age=0"))