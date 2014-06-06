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

(defproject aipalvastaus "0.1.0-SNAPSHOT"
  :description "Aipalvastaus"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.1"]
                 [http-kit "2.1.10"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [cheshire "5.2.0"]
                 [stencil "0.3.3"]
                 [org.clojure/tools.logging "0.2.6"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [org.slf4j/slf4j-api "1.7.5"]
                 [clj-time "0.6.0"]
                 [com.cemerick/valip "0.3.2"]
                 [prismatic/schema "0.2.0"]
                 [korma "0.3.0-RC6"]
                 [postgresql "9.1-901.jdbc4"]
                 [stencil "0.3.2"]]
  :plugins [[test2junit "1.0.1"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [clj-webdriver "0.6.0"]
                                  [clj-http "0.7.6"]
                                  [ring-mock "0.1.5"]]}
             :uberjar {:main aipalvastaus.palvelin
                       :aot :all}}
  :source-paths ["src/clj"]
  :main aipalvastaus.palvelin
  :repl-options {:init-ns user}
  :jar-name "aipalvastaus.jar"
  :uberjar-name "aipalvastaus-standalone.jar")
