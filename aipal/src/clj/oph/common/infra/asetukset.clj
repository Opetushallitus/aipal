;; Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

(ns oph.common.infra.asetukset
  (:require [clojure.java.io :refer [file]]
            [clojure.tools.logging :as log]

            [oph.common.util.util :refer [pisteavaimet->puu
                                          deep-merge
                                          deep-update-vals
                                          paths]]
            [schema.coerce :as sc])
  (:import [ch.qos.logback.classic.joran JoranConfigurator]
           [org.slf4j LoggerFactory]))

(defn string->boolean [x]
  (case x
    "true" true
    "false" false
    x))

(defn string-coercion-matcher [schema]
  (or (sc/string-coercion-matcher schema)
      ({Boolean string->boolean} schema)))

(defn coerce-asetukset [asetukset tyypit]
  ((sc/coercer tyypit string-coercion-matcher) asetukset))

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
  [oletukset tyypit asetustiedosto-polku]
  (let [asetus-map (tulkitse-asetukset (lue-asetukset-tiedostosta asetustiedosto-polku))
        korjattu-map (deep-merge oletukset asetus-map)
        asetukset (coerce-asetukset korjattu-map tyypit)]
    (if (instance? schema.utils.ErrorContainer asetukset)
      (throw (ex-info "Virheellinen asetustiedosto" (:error asetukset)))
      asetukset)))
