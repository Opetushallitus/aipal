(defproject aipal "0.1.0-SNAPSHOT"
  :description "AIPAL"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.1"]
                 [http-kit "2.1.10"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [cheshire "5.2.0"]
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
             :uberjar {:main aipal.palvelin
                       :aot :all}}
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :jar-name "aipal.jar"
  :uberjar-name "aipal-standalone.jar"
  :main aipal.palvelin
  :repl-options {:init-ns user})
