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

(ns oph.source-test
  "Tarkistuksia lähdekoodille."
  (:import java.io.PushbackReader)
  (:require [clojure.test :refer [deftest testing is]]
            [oph.source-util :refer :all]))

(deftest audit-log-kutsut-ovat-olemassa
  (is (empty? (vastaavat-muodot "src/clj" audit-log-kutsu-puuttuu?
                :ohita ["src/clj/aipal/auditlog.clj"
                        "src/clj/aipal/rest_api/kysely.clj"
                        "src/clj/aipal/rest_api/kysymysryhma.clj"
                        "src/clj/aipal/rest_api/avopvastaajatunnus.clj"
                        "src/clj/aipal/arkisto/vastaajatunnus.clj"]))))

(deftest js-debug-test
  (is (empty? (js-console-log-calls))))

(deftest properties-encoding-test
  (testing "etsitään merkkejä jotka eivät ole ns. printable charactereita. Ääkköset ovat näitä enkoodaussyistä"
    (is (empty? (vastaavat-rivit "resources/i18n"
                                 #".*\.properties"
                                 [#"[^\p{Print}\p{Space}]+"])))))

(defn properties-duplicat-keys? [r]
  (let [dup (doto (oph.util.DuplicateAwareProperties.)
                  (.load r)
                  )
        duplicates (.getDuplicates dup)]
    duplicates))

(deftest properties-duplicate-keys-test
  (testing "Etsitään properties tiedostoista tupla-avaimia"
    (is (empty? (vastaavat-tiedostot "resources/i18n" #".*\.properties"
                  properties-duplicat-keys?)))))

(deftest pre-post-oikeassa-paikassa-test
  (is (empty? (vastaavat-muodot "src/clj" pre-post-vaarassa-paikassa?))))

(deftest pre-post-vektori-test
  (is (empty? (vastaavat-muodot "src/clj" pre-post-ei-vektori?))))

(defn load-props [filename]
  (with-open [fs (java.io.FileInputStream. filename)]
    (let [ prop (java.util.Properties.)]
      (.load prop fs)
      (into {} prop))))

(deftest kielikaannokset-loytyvat
  (testing "Tarkistetaan että kaikissa kielissä on samat lokalisointiavaimet"
    (let [suomi-avaimet  (set (keys (load-props "resources/i18n/tekstit.properties")))
          ruotsi-avaimet (set (keys (load-props "resources/i18n/tekstit_sv.properties")))
          enkku-avaimet (set (keys (load-props "resources/i18n/tekstit_en.properties")))]
      (is (empty? (clojure.set/difference (clojure.set/union suomi-avaimet ruotsi-avaimet enkku-avaimet) (clojure.set/intersection suomi-avaimet ruotsi-avaimet enkku-avaimet)))))))