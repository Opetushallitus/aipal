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

(ns aipal-e2e.kyselyt-sivu-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clj-webdriver.taxi :as w]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.util :refer :all]))

(use-fixtures :once tietokanta/muodosta-yhteys)

(def kyselyt-sivu "/fi/#/kyselyt")

(defn kyselyt []
  (w/find-elements (-> *ng*
                     (.repeater "kysely in kyselyt"))))

(defn kyselyn-nimi [kysely-elementti]
  (w/text
    (w/find-element-under kysely-elementti
                          (-> *ng*
                            (.binding "kysely.nimi_fi")))))

(defn ^:private kyselykerrat-kyselylle [kysely-elementti]
  (map w/text
       (w/find-elements-under kysely-elementti
                              (-> *ng*
                                (.repeater "kyselykerta in kysely.kyselykerrat")
                                (.column "kyselykerta.nimi_fi")))))

(deftest kyselyt-sivu-test
  (with-webdriver
    (with-data {:kysely [{:kyselyid 1
                          :nimi_fi "Kysely 1"}
                         {:kyselyid 2
                          :nimi_fi "Kysely 2"}]
                :kyselykerta [{:kyselykertaid 1
                               :kyselyid 1
                               :nimi_fi "Kyselykerta 1-1"}
                              {:kyselykertaid 2
                               :kyselyid 1
                               :nimi_fi "Kyselykerta 1-2"}
                              {:kyselykertaid 3
                               :kyselyid 2
                               :nimi_fi "Kyselykerta 2-3"}]}
      (avaa-aipal kyselyt-sivu)
      (testing
        "ensimmäisellä kyselyllä on kaksi kyselykertaa"
        (let [kysely (nth (kyselyt) 0)]
          (is (= (kyselyn-nimi kysely) "1 Kysely 1"))
          (is (= (kyselykerrat-kyselylle kysely) ["Kyselykerta: 1 Kyselykerta 1-1" "Kyselykerta: 2 Kyselykerta 1-2"]))))
      (testing
        "toisella kyselyllä on yksi kyselykerta"
        (let [kysely (nth (kyselyt) 1)]
          (is (= (kyselyn-nimi kysely) "2 Kysely 2"))
          (is (= (kyselykerrat-kyselylle kysely) ["Kyselykerta: 3 Kyselykerta 2-3"])))))))
