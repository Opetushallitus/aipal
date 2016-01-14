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
            [aitu-e2e.util :refer :all]
            [clj-time.core :as time]))

(def kyselyt-sivu "/#/kyselyt")

(defn kyselyt []
  (w/find-elements (-> *ng*
                     (.repeater "kysely in suodatettu"))))

(defn kyselyn-nimi [kysely-elementti]
  (w/text
    (w/find-element-under kysely-elementti
                          {:css ".e2e-kysely-nimi"})))

(defn kysely-linkki [kysely-elementti]
  (w/find-element-under kysely-elementti
                        {:css ".panel-heading"}))

(defn kyselykerran-nimi [kyselykerta-elementti]
  (w/find-element-under kyselykerta-elementti {:css ".e2e-kyselykerta-nimi"}))

(defn avaa-kysely [kysely-elementti]
  (w/click (kysely-linkki kysely-elementti)))

(defn ^:private kyselykerrat-kyselylle [kysely-elementti]
  (w/present? (w/find-element-under kysely-elementti {:css ".e2e-kyselykerrat"}))
  (let [kyselykerrat (w/find-elements-under kysely-elementti
                                            {:css ".e2e-kyselykerrat"})]
    (->> kyselykerrat
        (map kyselykerran-nimi)
        (map w/text))))

(defn uusi-kyselykerta-kyselylle [kysely-elementti]
  (odota-kunnes (w/present? (w/find-element-under kysely-elementti {:css ".e2e-uusi-kyselykerta"})))
  (w/find-element-under kysely-elementti {:css ".e2e-uusi-kyselykerta"}))

(defn uusi-kysely []
  (w/find-element {:css ".e2e-luo-uusi-kysely"}))

(deftest kyselyt-sivu-test
  (with-webdriver
    (with-data {:koulutustoimija [{:ytunnus "0000000-0"}]
                :rooli_organisaatio [{:organisaatio "0000000-0"
                                      :rooli "OPL-VASTUUKAYTTAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}]
                :kysely [{:kyselyid 1
                          :nimi_fi "Kysely 1"
                          :koulutustoimija "0000000-0"}
                         {:kyselyid 2
                          :nimi_fi "Kysely 2"
                          :koulutustoimija "0000000-0"}]
                :kyselykerta [{:kyselykertaid 1
                               :kyselyid 1
                               :nimi "Kyselykerta 1-1"}
                              {:kyselykertaid 2
                               :kyselyid 1
                               :nimi "Kyselykerta 1-2"}
                              {:kyselykertaid 3
                               :kyselyid 2
                               :nimi "Kyselykerta 2-3"}]}
      (avaa kyselyt-sivu)
      (testing
        "ensimmäisellä kyselyllä on kaksi kyselykertaa"
        (let [kysely (nth (kyselyt) 1)]
          (avaa-kysely kysely)
          (is (= (kyselyn-nimi kysely) "Kysely 1"))
          (is (= (kyselykerrat-kyselylle kysely) ["Kyselykerta: Kyselykerta 1-1" "Kyselykerta: Kyselykerta 1-2"]))))
      (testing
        "toisella kyselyllä on yksi kyselykerta"
        (let [kysely (nth (kyselyt) 0)]
          (avaa-kysely kysely)
          (is (= (kyselyn-nimi kysely) "Kysely 2"))
          (is (= (kyselykerrat-kyselylle kysely) ["Kyselykerta: Kyselykerta 2-3"])))))))

(deftest luo-kysely-test
  (with-webdriver
    (with-data {:koulutustoimija [{:ytunnus "ABC"
                                   :nimi_fi "Testi"}]
                :rooli_organisaatio [{:organisaatio "ABC"
                                      :rooli "OPL-VASTUUKAYTTAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}]}
      (avaa kyselyt-sivu)
      (testing
        "Luo uusi kysely ohjaa kyselyn luontiin"
        (w/click (uusi-kysely))
        (w/wait-until #(re-matches #".*/#/kyselyt/kysely/uusi" (w/current-url)) 10000)))))

(deftest ^:no-ie kyselyt-sivu-kyselykerran-luonti-test
  (with-webdriver
    (with-data {:koulutustoimija [{:ytunnus "0000000-0"}]
                :rooli_organisaatio [{:organisaatio "0000000-0"
                                      :rooli "OPL-VASTUUKAYTTAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}]
                :kysely [{:kyselyid 1
                          :nimi_fi "Kysely 1"
                          :koulutustoimija "0000000-0"
                          :voimassa_alkupvm (time/local-date 2000 1 1)}
                         {:kyselyid 2
                          :nimi_fi "Kysely 2"
                          :koulutustoimija "0000000-0"
                          :voimassa_alkupvm (time/local-date 2000 1 1)}]}
      (avaa kyselyt-sivu)
      (testing
        "Kyselykerran luonti onnistuu ensimmäiselle kyselylle"
        (let [kysely (nth (kyselyt) 0)]
          (avaa-kysely kysely)
          (w/click (uusi-kyselykerta-kyselylle kysely))
          (syota-kenttaan "kyselykerta.nimi" "Ensimmäinen kyselykerta")
          (tallenna)
          (avaa kyselyt-sivu))
        (let [kysely (nth (kyselyt) 0)]
          (is (= (kyselykerrat-kyselylle kysely) ["Kyselykerta: Ensimmäinen kyselykerta"]))))
      (testing
        "Kyselykerran luonti onnistuu toiselle kyselylle"
        (let [kysely (nth (kyselyt) 1)]
          (avaa-kysely kysely)
          (w/click (uusi-kyselykerta-kyselylle kysely))
          (odota-kunnes (w/displayed? (str "input[ng-model=\"kyselykerta.nimi\"]"))) ; ajastusongelman kierto
          (syota-kenttaan "kyselykerta.nimi" "Toinen kyselykerta")
          (tallenna)
          (avaa kyselyt-sivu))
        (let [kysely (nth (kyselyt) 1)]
          (is (= (kyselykerrat-kyselylle kysely) ["Kyselykerta: Toinen kyselykerta"])))))))
