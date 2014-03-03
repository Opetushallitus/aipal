(ns aipal.asetukset
  (:require [clojure.java.io :refer [file]]
            clojure.set
            [clojure.tools.logging :as log]

            [aitu.util :refer [pisteavaimet->puu
                               deep-merge
                               deep-update-vals
                               paths]])
  (:import [ch.qos.logback.classic.joran JoranConfigurator]
           [org.slf4j LoggerFactory]))

(def oletusasetukset
  {:server {:port "8082"
            :base-url ""}
   :development-mode false ; oletusarvoisesti ei olla kehitysmoodissa. Pitää erikseen kääntää päälle jos tarvitsee kehitysmoodia.
   :logback {:properties-file "resources/logback.xml"}
   })

(def konversio-map
  {"true" true})

(defn konvertoi-arvo
  [x]
  (get konversio-map x x))

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

(defn tarkista-avaimet
  [m]
  (let [vaarat-avaimet
        (clojure.set/difference (paths m) (paths oletusasetukset))]
    (assert (empty? vaarat-avaimet) (str "Viallisia avaimia asetuksissa: " vaarat-avaimet)))
  m)

(defn tulkitse-asetukset
  [property-map]
  (tarkista-avaimet
    (deep-update-vals konvertoi-arvo
       (->> property-map
          (into {})
          pisteavaimet->puu))))

(defn lue-asetukset
  ([oletukset] (lue-asetukset oletukset "aipal.properties"))
  ([oletukset polku]
    (->>
      (lue-asetukset-tiedostosta polku)
      (tulkitse-asetukset)
      (deep-merge oletukset)
      (deep-update-vals konvertoi-arvo)
      (tarkista-avaimet))))
