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

(ns aipal-e2e.kysymysryhma-kopiointi-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clj-webdriver.taxi :as w]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.util :refer :all]))

(def kysymysryhmat-sivu "/#/kysymysryhmat")

(defn kysymysryhman-nimi-fi []
  (w/value (w/find-element {:css ".e2e-kysymysryhma-nimi-fi"})))

(defn kysymys-fi []
  (w/value (w/find-element {:css ".e2e-kysymys-suomeksi"})))

(defn kysymysryhmat-nimella [nimi]
  (w/find-elements {:text nimi
                    :tag :td}))

(deftest
  kysymysryhman-kopiointi-test
  (with-webdriver
    (with-data {:koulutustoimija [{:ytunnus "0000000-0"}]
                :rooli_organisaatio [{:organisaatio "0000000-0"
                                      :rooli "OPL-VASTUUKAYTTAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}]
                :kysymysryhma [{:kysymysryhmaid 1000
                                :nimi_fi "Kysymysryhmä"
                                :koulutustoimija "0000000-0"}]
                :kysymys [{:kysymysryhmaid 1000
                           :vastaustyyppi "likert_asteikko"
                           :kysymys_fi "Kysymys suomeksi"}]}
      (testing
        "kysymysryhman kopiointi:"
        (avaa kysymysryhmat-sivu)
        (w/click {:css ".e2e-kopioi-kysymysryhma"})
        (odota-angular-pyyntoa)
        (testing
          "kysymysryhmän tiedot"
          (is (= (kysymysryhman-nimi-fi) "Kysymysryhmä")))
        (testing
          "kysymyksen tiedot"
          (w/click {:css ".e2e-muokkaa-kysymysta"})
          (is (= (kysymys-fi) "Kysymys suomeksi"))
          (w/click {:css ".e2e-tallenna-kysymys"}))
        (odota-angular-pyyntoa)
        (testing
          "Kysymysryhmän tallentaminen"
          (syota-kenttaan "kysymysryhma.nimi_fi" "Muokattu kysymysryhmä")
          (w/click {:css ".e2e-tallenna-kysymysryhma"})
          (odota-angular-pyyntoa)
          (is (= (count (kysymysryhmat-nimella "Kysymysryhmä")) 1))
          (is (= (count (kysymysryhmat-nimella "Muokattu kysymysryhmä")) 1)))))))
