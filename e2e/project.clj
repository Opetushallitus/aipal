;; Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
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

(defproject aipal-e2e "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [korma "0.3.2"]
                 [org.postgresql/postgresql "9.3-1101-jdbc41"]
                 [solita/opetushallitus-aitu-e2e "0.14.0"]]
  :plugins [[test2junit "1.0.1"]]

  :test-selectors {:ie (fn [m] (not (or (:no-ie m) (:vastaus m))))
                   :no-ie :no-ie
                   :vastaus :vastaus
                   :default (complement :vastaus)})

(require '[robert.hooke :refer [add-hook]])
(require 'leiningen.test)

(add-hook #'leiningen.test/form-for-testing-namespaces
          (fn [f & args]
            (binding [leiningen.test/*exit-after-tests* false]
              (let [form (apply f args)]
                `(do
                   (require 'aitu-e2e.util)
                   (System/exit (aitu-e2e.util/with-webdriver ~form)))))))
