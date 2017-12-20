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
  :dependencies [[ch.qos.logback/logback-classic "1.1.5"]
                 [cheshire "5.5.0"]
                 [clj-http "2.1.0"]
                 [clj-time "0.11.0"]
                 [com.cemerick/valip "0.3.2"]
                 [com.jolbox/bonecp "0.8.0.RELEASE"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.19"]
                 [korma "0.4.2"]
                 [metosin/compojure-api "1.0.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/core.cache "0.6.4"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [org.slf4j/slf4j-api "1.7.16"]
                 [org.flatland/useful "0.11.5"]
                 [prismatic/schema "1.0.5"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-headers "0.1.3"]
                 [ring/ring-json "0.4.0"]
                 [stencil "0.5.0"]]
  :plugins [[test2junit "1.0.1"]
            [jonase/eastwood "0.2.3"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [clj-webdriver "0.7.2"]
                                  [ring/ring-mock "0.3.0"]]}
             :uberjar {:main aipalvastaus.palvelin
                       :aot :all}}
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :test-paths ["test/clj"]
  :main aipalvastaus.palvelin
  :repl-options {:init-ns user}
  :jar-name "aipalvastaus.jar"
  :uberjar-name "aipalvastaus-standalone.jar")
