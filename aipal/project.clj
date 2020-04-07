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

(defproject aipal "2020.1.0"
  :description "Arvo"
  :dependencies [[cas-single-sign-out "0.1.3" :exclusions [clj-cas-client]]
                 [ch.qos.logback/logback-classic "1.1.5"]
                 [cheshire "5.5.0"]
                 [clj-http "3.10.0"]
                 [clj-time "0.11.0"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [clojurewerkz/quartzite "2.0.0"]
                 [com.cemerick/valip "0.3.2"]
                 [com.jolbox/bonecp "0.8.0.RELEASE"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.19"]
                 [korma "0.4.3"]
                 [metosin/compojure-api "1.0.0"]
                 [buddy/buddy-core "0.12.1"]
                 [buddy/buddy-auth "0.13.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [org.clojars.noidi/clj-cas-client "0.0.6-4ae43963cb458579a3813f9dda4fba52ad4d9607-ring-1.2.1" :exclusions [ring]]
                 [org.clojars.pntblnk/clj-ldap "0.0.9"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/core.cache "0.6.4"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [org.slf4j/slf4j-api "1.7.16"]
                 [peridot "0.4.3"]
                 [prismatic/schema "1.0.5"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-headers "0.1.3"]
                 [ring/ring-json "0.4.0"]
                 [robert/hooke "1.3.0"]
                 [stencil "0.5.0"]
                 [org.clojure/java.jdbc "0.7.4"]
                 [com.layerware/hugsql "0.4.8"]
                 [mount "0.1.11"]
                 [conman "0.6.6"]
                 [org.flatland/useful "0.11.5"]
                 [migratus "1.0.6"]
                 [listora/again "1.0.0"]]

  :plugins [[test2junit "1.0.1"]
            [codox "0.8.12"]
            [jonase/eastwood "0.2.3"]]
  :profiles {:dev {:source-paths ["dev"]
                   :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [clj-webdriver "0.6.0"]
                                  [ring-mock "0.1.5"]
                                  [clj-gatling "0.7.9"]
                                  [org.clojure/test.check "0.5.9"]]}
             :uberjar {:main aipal.palvelin
                       :aot :all}
             :test {:resource-paths ["test-resources"]}}
  :source-paths ["src/clj"]
  :jvm-opts ["-Duser.timezone=UTC"]
  :java-source-paths ["src/java"]
  :javac-options ["-target" "1.7" "-source" "1.7"]
  :test-paths ["test/clj"]
  :test-selectors {:default  (complement (some-fn :integraatio :performance))
                   :integraatio (complement (some-fn :performance))
                   :performance :performance}
  :jar-name "aipal.jar"
  :uberjar-name "aipal-standalone.jar"
  :main aipal.palvelin
  :repl-options {:init-ns user})
