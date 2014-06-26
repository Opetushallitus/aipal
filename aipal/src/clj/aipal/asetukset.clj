;; Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
;;
;; This program is free software:  Licensed under the EUPL, Version 1.1 or - as
;; soon as they will be approved by the European Commission - subsequent versions
;; of the EUPL (the "Licence");
;;
;; You may not use this work except in compliance with the Licence.
;; You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; European Union Public Licence for more details.

(ns aipal.asetukset
  (:require [clojure.java.io :refer [file resource]]
            clojure.set
            [clojure.tools.logging :as log]

            [aitu.util :refer [pisteavaimet->puu
                               deep-merge
                               deep-update-vals
                               paths]]
            [schema.core :as s]
            [schema.coerce :as sc])
  (:import [ch.qos.logback.classic.joran JoranConfigurator]
           [org.slf4j LoggerFactory]))

(def Asetukset
  {:server {:port s/Int
            :base-url s/Str}
   :db {:host s/Str
        :port s/Int
        :name s/Str
        :user s/Str
        :password s/Str
        :maximum-pool-size s/Int
        :minimum-pool-size s/Int}
   :cas-auth-server {:url s/Str
                     :unsafe-https Boolean
                     :enabled Boolean}
   :development-mode Boolean
   :logback {:properties-file s/Str}})

(defn string->boolean [x]
  (case x
    "true" true
    "false" false
    x))

(defn string-coercion-matcher [schema]
  (or (sc/string-coercion-matcher schema)
      ({Boolean string->boolean} schema)))

(defn coerce-asetukset [asetukset]
  ((sc/coercer Asetukset string-coercion-matcher) asetukset))

(def oletusasetukset
  {:server {:port 8082
            :base-url ""}
   :db {:host "127.0.0.1"
        :port 3456
        :name "aipal"
        :user "aipal_user"
        :password "aipal"
        :maximum-pool-size 15
        :minimum-pool-size 3}
   :cas-auth-server {:url "https://localhost:9443/cas-server-webapp-3.5.2"
                     :unsafe-https false
                     :enabled true}
   :development-mode false ; oletusarvoisesti ei olla kehitysmoodissa. Pitää erikseen kääntää päälle jos tarvitsee kehitysmoodia.
   :logback {:properties-file "resources/logback.xml"}})

(def build-id (delay (if-let [r (resource "build-id.txt")]
                       (.trim (slurp r))
                       "dev")))

(defn kehitysmoodi?
  [asetukset]
  (true? (:development-mode asetukset)))

(defn konfiguroi-lokitus
  "Konfiguroidaan logback asetukset tiedostosta."
  [asetukset]
  (let [filepath (-> asetukset :logback :properties-file)
        config-file (file filepath)
        config-file-path (.getAbsolutePath config-file)
        configurator (JoranConfigurator.)
        context (LoggerFactory/getILoggerFactory)]
    (log/info "logback configuration reset: " config-file-path)
    (.setContext configurator context)
    (.reset context)
    (.doConfigure configurator config-file-path)))

(defn lue-asetukset-tiedostosta
  [polku]
  (try
    (with-open [reader (clojure.java.io/reader polku)]
      (doto (java.util.Properties.)
        (.load reader)))
    (catch java.io.FileNotFoundException _
      (log/info "Asetustiedostoa ei löydy. Käytetään oletusasetuksia")
      {})))

(defn tulkitse-asetukset
  [property-map]
  (->> property-map
     (into {})
     pisteavaimet->puu))

(defn lue-asetukset
  ([oletukset] (lue-asetukset oletukset "aipal.properties"))
  ([oletukset polku]
    (->>
      (lue-asetukset-tiedostosta polku)
      (tulkitse-asetukset)
      (deep-merge oletukset)
      (coerce-asetukset))))
