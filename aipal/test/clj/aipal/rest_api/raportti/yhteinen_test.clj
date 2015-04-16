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

(ns aipal.rest-api.raportti.yhteinen-test
  (:require [aipal.rest-api.raportti.yhteinen :refer :all])
  (:use clojure.test))

(def ^:private handler identity)

(deftest wrap-muunna-raportti-json-param-test
  (testing
    "ei muunnosta parametrien puuttuessa"
    (is (= (get-in ((wrap-muunna-raportti-json-param handler) {:query-params {}}) [:params :parametrit])
          nil)))
  (testing
    "muuntaa raportin json-parametrit"
    (is (= (get-in ((wrap-muunna-raportti-json-param handler) {:query-params {"raportti" "{\"a\": 1}"}}) [:params :parametrit])
          {:a 1})))
  (testing
    "lisää myös query-parametrit"
    (is (= (get-in ((wrap-muunna-raportti-json-param handler) {:query-params {"raportti" "{\"a\": 1}"}}) [:query-params :parametrit])
          {:a 1}))))
