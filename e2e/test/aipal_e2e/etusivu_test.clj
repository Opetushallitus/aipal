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
            [aitu-e2e.util :refer [with-webdriver]]
            [aipal-e2e.util :refer :all]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]
            [aipal-e2e.sivu.etusivu :as etusivu]))

(defn sivun-sisalto []
  (w/text (w/find-element {:css "body"})))

(deftest etusivu-test
  (with-webdriver
    (testing "etusivu"
      (with-data {:rooli_organisaatio [{:organisaatio "0920632-0"
                                        :rooli "OPL-KAYTTAJA"
                                        :kayttaja "OID.AIPAL-E2E"
                                        :voimassa true}] }
        (etusivu/avaa-sivu)
        (testing "sisältää järjestelmän nimen"
          (is (true? (.contains (sivun-sisalto) "TIEDOTE"))))))))
		    ;;{:tag :img, :id "logo"}

(deftest lisaa-tiedote-test
  (with-webdriver
    (with-data {:rooli_organisaatio [{:organisaatio "0920632-0"
                                      :rooli "YLLAPITAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}] }
      (testing "lisää tiedote"
        (etusivu/avaa-sivu)
        (etusivu/klikkaa-muokkaa-tiedote)
        (etusivu/lisaa-tiedote-teksti-fi "Uusi tiedote")
        (etusivu/lisaa-tiedote-teksti-sv "Ny meddelande")
        (etusivu/lisaa-tiedote-teksti-en "New announcement")
        (etusivu/klikkaa-tallenna-tiedote)
        (is (true? (.contains (sivun-sisalto) "Uusi tiedote")))
        ; vaihda kieli
        (etusivu/vaihda-kieli "SV")
        (is (true? (.contains (sivun-sisalto) "Ny meddelande")))
        (etusivu/vaihda-kieli "EN")
        (is (true? (.contains (sivun-sisalto) "New announcement")))
        (etusivu/vaihda-kieli "FI")))))

(deftest siivoa-etusivu-test
  (with-webdriver
    (with-data {:rooli_organisaatio [{:organisaatio "0920632-0"
                                      :rooli "YLLAPITAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}] }
      (etusivu/avaa-sivu)
      ;siivoa
      (etusivu/klikkaa-muokkaa-tiedote)
      (etusivu/tyhjenna-tiedote-teksti-fi)
      (etusivu/tyhjenna-tiedote-teksti-sv)
      (etusivu/tyhjenna-tiedote-teksti-en)
      (etusivu/klikkaa-tallenna-tiedote))))

(deftest test-ns-hook
  (etusivu-test)
  (lisaa-tiedote-test)
  (siivoa-etusivu-test)
)
