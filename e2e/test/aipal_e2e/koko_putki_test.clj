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

(ns aipal-e2e.koko-putki-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clj-webdriver.taxi :as w]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]
            [aipal-e2e.util :refer :all]
            [aipalvastaus-e2e.util :as aipalvastaus]
            [aitu-e2e.util :refer :all]))

(def kyselyt-sivu "/#/kyselyt")
(def kysymysryhmat-sivu "/#/kysymysryhmat")

(defn sivun-sisalto []
  (w/text (w/find-element {:css "body"})))

(defn valitse-ainoan-kysymyksen-ensimmainen-vaihtoehto []
  (w/select (nth (w/find-elements {:tag :radio}) 0)))

; Disabloitu väliaikaisesti
(deftest koko-putki-test
  (with-webdriver
    (with-data {:koulutustoimija [{:ytunnus "0000000-0"}]
                :rooli_organisaatio [{:organisaatio "0000000-0"
                                      :rooli "OPL-VASTUUKAYTTAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}]}
      ;; luo kysymysryhma
      (avaa kysymysryhmat-sivu)
      (w/click {:css ".e2e-luo-uusi-kysymysryhma"})
      (syota-kenttaan "kysymysryhma.nimi_fi" "Uusi kysymysryhmä")
      (w/click {:css ".e2e-luo-uusi-kysymys"})
      (syota-kenttaan "aktiivinenKysymys.kysymys_fi" "Uusi kysymys")
      (w/click {:css ".e2e-lisaa-kysymys"})
      (w/click {:css ".e2e-tallenna-kysymysryhma"})
      (odota-angular-pyyntoa)

      ;; julkaise kysymysryhmä
      (w/click {:css ".e2e-julkaise-kysymysryhma"})
      (w/click {:css ".e2e-vahvista-kysymysryhman-julkaisu"})

      ;; luo kysely
      (avaa kyselyt-sivu)
      (w/click {:css ".e2e-luo-uusi-kysely"})
      (syota-kenttaan "kysely.nimi_fi" "Uusi kysely")
      (w/click {:css ".e2e-lisaa-kysymysryhma"})
      (odota-angular-pyyntoa)
      (w/select-by-text ".e2e-valittavat-kysymysryhmat" "Uusi kysymysryhmä")
      (odota-kunnes (w/enabled? {:css ".e2e-lisaa-valittu-kysymysryhma"}))
      (w/click {:css ".e2e-lisaa-valittu-kysymysryhma"})
      (odota-angular-pyyntoa)
      (w/click {:css ".e2e-tallenna-kysely"})
      (odota-angular-pyyntoa)

      ;; julkaise kysely
      (w/click {:css ".e2e-kysely-nimi"})
      (w/click {:css ".e2e-julkaise-kysely"})
      (odota-angular-pyyntoa)
      (w/click {:css ".e2e-palvelut-varmistus-vahvista"})
      (odota-angular-pyyntoa)

      ;; luo kyselykerta
      (w/click {:css ".e2e-uusi-kyselykerta"})
      (syota-kenttaan "kyselykerta.nimi" "Uusi kyselykerta")
      (w/click {:css ".e2e-direktiivit-tallenna"})
      (odota-angular-pyyntoa)

      ;; luo vastaajatunnuksia
      (odota-kunnes (w/present? {:css ".e2e-luo-vastaajatunnuksia"}))
      (w/click {:css ".e2e-luo-vastaajatunnuksia"})
      (w/select-by-text ".e2e-vastaajatunnuksen-rahoitusmuoto" "Oppisopimus")
      (w/click (w/find-element-under {:css ".e2e-vastaustunnusten-luonti-dialogi"} {:css ".e2e-direktiivit-tallenna"}))
      (odota-angular-pyyntoa)

      ;; vastaa kyselyyn
      (let [vastaajatunnus-url (w/text (w/find-element {:css ".e2e-vastaajatunnus-url"}))]
        (w/wait-until (fn []
                        (w/to vastaajatunnus-url)
                        (.contains (sivun-sisalto) "UUSI KYSELY")) 10000 500)

        (valitse-ainoan-kysymyksen-ensimmainen-vaihtoehto)

        (odota-kunnes (w/enabled? {:css ".e2e-tallenna-vastaukset"}))
        (w/click {:css ".e2e-tallenna-vastaukset"})

        (odota-angular-pyyntoa)
        (is (.contains (sivun-sisalto) "Kiitos vastauksestanne"))

        (aipalvastaus/poista-vastaajat-ja-vastaukset-vastaustunnukselta! vastaajatunnus-url)))))

