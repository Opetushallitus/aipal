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

(ns aipal-e2e.etusivu-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clj-webdriver.taxi :as w]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.util :refer [with-webdriver]]))

(use-fixtures :once tietokanta/muodosta-yhteys)

(def etusivu "/fi/#/")

(defn sivun-sisalto []
  (w/text (w/find-element {:css "body"})))

(deftest etusivu-test
  (with-webdriver
    (testing
      "etusivu"
      (avaa etusivu)
      (testing
        "sisältää järjestelmän nimen"
        (is (true? (.contains (sivun-sisalto) "AIPAL")))))))
