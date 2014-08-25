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

(ns aipalvastaus.rest-api.vastaaja-test
  (:require [aipalvastaus.sql.vastaaja :as v]
            [aipal.sql.test-data-util :refer :all]
            [aipal.sql.test-util :refer [tietokanta-fixture]])
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)

(deftest uudet-vastaajat-valideja
  (testing "Vasta luodut vastaajat on valideja"
    (let [vastaajatunnus (lisaa-vastaajatunnus!)
          vastaajaid (v/luo-vastaaja! (:tunnus vastaajatunnus))]
      (is (v/validoi-vastaajaid (:tunnus vastaajatunnus) vastaajaid)))))

(deftest tuplavastaus-ei-validi
  (testing "Päivitetty vastaaja ei ole enää validi"
    (let [vastaajatunnus (lisaa-vastaajatunnus!)
          vastaajaid (v/luo-vastaaja! (:tunnus vastaajatunnus))]
      (v/paivata-vastaaja! vastaajaid)
      (is (not (v/validoi-vastaajaid (:tunnus vastaajatunnus) vastaajaid))))))

(deftest keksitty-vastaajaid-ei-validi
  (testing "Keksitty vastaajaid ei ole validi"
    (let [vastaajatunnus (lisaa-vastaajatunnus!)
          vastaajaid 123123124]
      (is (not (v/validoi-vastaajaid (:tunnus vastaajatunnus) vastaajaid))))))

(deftest vastaajatunnus-keksitty-vastaajaid-olemassa
  (testing "Keksitty vastaajatunnus ja validi vastaajaid ei ole validi"
    (let [vastaajatunnus (lisaa-vastaajatunnus!)
          vastaajaid (v/luo-vastaaja! (:tunnus vastaajatunnus))]
      (is (not (v/validoi-vastaajaid "123456fwae" vastaajaid))))))