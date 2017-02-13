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
            [aipal-e2e.util :refer :all]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]
            [aipal-e2e.sivu.kysymysryhma :as kysymysryhma-sivu]
            [aipal-e2e.sivu.kysymysryhmat :as kysymysryhmat-sivu]))

(deftest kysymysryhma-kopiointo-test
  (with-webdriver
    (with-data {:rooli_organisaatio [{:organisaatio "9876543-2"
                                      :rooli "OPL-VASTUUKAYTTAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}] }
      ;; kysymysryhma luonti
      (kysymysryhmat-sivu/avaa-sivu)
      (kysymysryhmat-sivu/luo-uusi)
      (kysymysryhma-sivu/aseta-kysymysryhman-nimi-suomeksi "Uusi kysymysryhmä")
      (kysymysryhma-sivu/luo-uusi-kysymys)
      (kysymysryhma-sivu/aseta-kysymys-suomeksi "Uusi kysymys")
      (kysymysryhma-sivu/lisaa-kysymys)
      (kysymysryhma-sivu/tallenna-kysymysryhma)
      (kysymysryhmat-sivu/julkaise)
      (kysymysryhmat-sivu/vahvista-julkaisu)

      ;;  kysymysryhma kopiointi
      (kysymysryhmat-sivu/avaa-sivu)
      (testing
        "Kysymysryhman kopiointi"
        (w/click {:css ".e2e-kopioi-kysymysryhma"})
        (odota-angular-pyyntoa)
        (testing
          "Kysymysryhmän tiedot"
          (is (= (kysymysryhma-sivu/kysymysryhman-nimi-fi) "Uusi kysymysryhmä")))
        (testing
          "Kysymyksen tiedot"
          (w/click {:css ".e2e-muokkaa-kysymysta"})
          (is (= (kysymysryhma-sivu/kysymys-fi) "Uusi kysymys"))
          (kysymysryhma-sivu/tallenna-kysymys))
        (odota-angular-pyyntoa)
        (testing
          "Kysymysryhmän tallentaminen"
          (syota-kenttaan "kysymysryhma.nimi_fi" "Muokattu kysymysryhmä")
          (kysymysryhma-sivu/tallenna-kysymysryhma)
          (odota-angular-pyyntoa)
          (is (= (count (kysymysryhmat-sivu/nimella "Uusi kysymysryhmä")) 1))
          (is (= (count (kysymysryhmat-sivu/nimella "Muokattu kysymysryhmä")) 1))))

      ;; siivoa kysymysryhma
      (kysymysryhmat-sivu/avaa-sivu)
      ; poista ensimmäinen luonnos
      (kysymysryhmat-sivu/poista)
      (kysymysryhmat-sivu/vahvista-poisto)
      ; palauta ensimmäinen julkaistu
      (kysymysryhmat-sivu/palauta)
      (kysymysryhmat-sivu/vahvista-palautus)
      ; poista ensimmäinen luonnos
      (kysymysryhmat-sivu/poista)
      (kysymysryhmat-sivu/vahvista-poisto))))
