(defproject aipal-perf "0.1.0-SNAPSHOT"
  :description "Aipal suorituskykytestit"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-core "1.1.8"]
                 [http-kit "2.1.16"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [cheshire "5.2.0"]
                 [stencil "0.3.3"]
                 [org.clojure/tools.logging "0.2.6"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [org.slf4j/slf4j-api "1.7.5"]
                 [clj-time "0.8.0"]
                 [stencil "0.3.2"]
                 [clj-gatling "0.4.0"]]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :main perf.runner
  :test-selectors {:all (constantly true)
                   :default  (complement (some-fn :integration :performance)) 
                   :performance :performance
                   :integration :integration})


