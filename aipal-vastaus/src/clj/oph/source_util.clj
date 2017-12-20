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

(ns oph.source-util
  "Tarkistuksia lähdekoodille."
  (:import java.io.PushbackReader)
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :refer [file reader]]
            [clojure.walk :as cw]
            [clojure.string :refer [trim]]))

(defn tiedostot [hakemisto polku-re ohita]
  (let [ohita (set (map file ohita))]
    (for [polku (file-seq (file hakemisto))
          :when (not (or (.isDirectory polku)
                         (ohita polku)))
          :when (re-matches polku-re (str polku))]
      polku)))

(defn vastaavat-tiedostot [hakemisto polku-re f & {:keys [ohita]
                                                     :or {ohita #{}}}]
  (apply concat
    (for [polku (tiedostot hakemisto polku-re ohita)]
       (with-open [r (reader polku)]
         (f r)))))

(defn vastaavat-rivit [hakemisto polku-re mallit & {:keys [ohita]
                                                    :or {ohita #{}}}]
  (apply concat
         (for [polku (tiedostot hakemisto polku-re ohita)]
           (with-open [r (reader polku)]
             (doall
               (for [[nro rivi] (map vector
                                     (iterate inc 1)
                                     (line-seq r))
                     :when (some #(re-find % rivi) mallit)]
                 (str polku ":" nro ": " (trim rivi))))))))

(defn vastaavat-muodot [hakemisto ehto & {:keys [ohita polku-re]
                                          :or {ohita #{}
                                               polku-re #".*\.clj"}}]
    (apply concat
           (for [polku (tiedostot hakemisto polku-re ohita)]
             (with-open [r (PushbackReader. (reader polku))]
               (doall
                 (for [muoto (repeatedly #(read r false ::eof))
                       :while (not= muoto ::eof)
                       :when (ehto muoto)]
                   (str polku ": " muoto)))))))

(defn pre-post [muoto]
  (when (= 'defn (nth muoto 0))
    (some #(and (map? %)
                (or (contains? % :pre)
                    (contains? % :post))
                %)
          muoto)))

(defn pre-post-vaarassa-paikassa? [muoto]
  (when-let [pp (pre-post muoto)]
    (not (or (and (symbol? (nth muoto 1))
                  (vector? (nth muoto 2))
                  (= pp (nth muoto 3)))
             (and (symbol? (nth muoto 1))
                  (string? (nth muoto 2))
                  (vector? (nth muoto 3))
                  (= pp (nth muoto 4)))))))

(defn pre-post-ei-vektori? [muoto]
  (when-let [pp (pre-post muoto)]
    (not (every? vector? (vals pp)))))

(defn get-meta
  "http://stackoverflow.com/questions/12432561/how-to-get-the-metadata-of-clojure-function-arguments"
  [o]
  (->> *ns* ns-map (filter (fn [[_ v]] (and (var? v) (= o (var-get v))))) first second meta))

(defn defn-without-meta?
  "tarkistaa että muoto ei sisällä määriteltyjä keywordeja, esim. :test-api metatietoa"
  [muoto kwset]
  (and
    (= 'defn (nth muoto 0))
    (empty? (clojure.set/intersection (set (keys (meta (nth muoto 1)))) kwset))))

(defn ei-audit-logitettava-funktio?
  "test-api ja integraatioiden käyttämät arkistofunktiot eivät ole auditlokituksen piirissä"
  [muoto]
  (defn-without-meta? muoto #{:test-api :integration-api}))

(defn public-function? [form]
  (defn-without-meta? form #{:private}))

(defn sivuvaikutuksellinen-funktio?
  "Jos funktion nimi loppuu huutomerkkiin, tulkitaan että sillä on sivuvaikutuksia."
  [muoto]
  (and
    (= 'defn (nth muoto 0))
    (let [fn-name (name (nth muoto 1))]
      (.endsWith fn-name "!"))))

(defn audit-log-kutsu-puuttuu?
  "tarkistaa puuttuuko audit-log kutsu muodosta jossa sellainen pitäisi olla"
  [muoto]
  (let [sisaltaa-audit-kutsun? (some #(and (symbol? %)
                                        (= "auditlog" (.getNamespace %)))
                                 (flatten muoto))]
    (when (and
            (ei-audit-logitettava-funktio? muoto)
            (sivuvaikutuksellinen-funktio? muoto)
            (public-function? muoto)
            (not sisaltaa-audit-kutsun?))
      (println "! AUDITLOG kutsu puuttuu: " (nth muoto 1))
      (str (nth muoto 1)))))

(defn js-console-log-calls []
  (vastaavat-rivit "resources/public/js"
                               #".*\.js"
                               [#"console\.log"
                                #"debugger"
                                (re-pattern (str \u00a0)) ; non-breaking space
                                ]
                               :ohita ["resources/public/js/vendor/angular.js"
                                       "resources/public/js/vendor/stacktrace.js"]))
