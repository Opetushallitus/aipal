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

(ns aipal-e2e.kysymysryhma-luonti-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clj-webdriver.taxi :as w]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.sivu.kysymysryhma :as kysymysryhma-sivu]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.util :refer :all]))

(def kysymysryhmat-sivu "/#/kysymysryhmat")

(defn kysymysryhman-nimi-fi []
  (w/value (w/find-element {:css ".e2e-kysymysryhma-nimi-fi"})))

(defn kysymys-fi []
  (w/value (w/find-element {:css ".e2e-kysymys-suomeksi"})))

(deftest
  kysymysryhman-luonti-test
  (with-webdriver
    (with-data {:koulutustoimija [{:ytunnus "0000000-0"}]
                :rooli_organisaatio [{:organisaatio "0000000-0"
                                      :rooli "OPL-VASTUUKAYTTAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}]}
      (testing
        "kysymysryhman luonti:"
        (avaa kysymysryhmat-sivu)
        (w/click {:css ".e2e-luo-uusi-kysymysryhma"})
        (syota-kenttaan "kysymysryhma.nimi_fi" "Uusi kysymysryhmä")
        (kysymysryhma-sivu/luo-uusi-kysymys)
        (kysymysryhma-sivu/aseta-kysymys-suomeksi "Uusi kysymys")
        (kysymysryhma-sivu/lisaa-kysymys)
        (kysymysryhma-sivu/tallenna-kysymysryhma)
        (testing
          "kysymyksen muokkaus:"
          (w/click {:css ".e2e-muokkaa-kysymysryhmaa"})
          (odota-angular-pyyntoa)
          (testing
            "kysymysryhmän tiedot"
            (is (= (kysymysryhman-nimi-fi) "Uusi kysymysryhmä")))
          (testing
            "kysymyksen tiedot"
            (w/click {:css ".e2e-muokkaa-kysymysta"})
            (is (= (kysymys-fi) "Uusi kysymys"))))))))
