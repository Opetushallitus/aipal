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

(ns aipalvastaus-e2e.vastaus-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clj-webdriver.taxi :as w]
            [aitu-e2e.util :refer :all]
            [aipalvastaus-e2e.util :refer [avaa-vastaus]]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]))

(use-fixtures :once tietokanta/muodosta-yhteys)

(defn vastaus-sivu [tunnus] (str "/fi/#/vastaus/" tunnus))
(defn kyselyn-otsikko [] (w/text {:css "#content h1"}))
(defn kyselyn-selite [] (w/text {:css "p.selite"}))
(defn kyselyn-tutkinto [] (w/text {:css "p.tutkinto"}))

(deftest ^:vastaus vastaussivu-test
  (testing "Vastaussivulla on kyselyn perustiedot"
    (with-webdriver
      (with-data {:kysely [{:kyselyid 1
                            :nimi_fi "Kysely 1"
                            :selite_fi "Selite 1"}]
                  :kyselykerta [{:kyselykertaid 1
                                 :kyselyid 1}]
                  :tutkinto [{:tutkintotunnus "1"
                              :nimi_fi "Tutkinto yksi"}]
                  :vastaajatunnus [{:vastaajatunnusid 1
                                    :kyselykertaid 1
                                    :tunnus "tunnus1"
                                    :tutkintotunnus "1"}]}
        (avaa-vastaus (vastaus-sivu "tunnus1"))
        (is (= (clojure.string/upper-case "Kysely 1") (kyselyn-otsikko)))
        (is (= "Selite 1" (kyselyn-selite)))
        (is (= "1 Tutkinto yksi" (kyselyn-tutkinto)))))))
