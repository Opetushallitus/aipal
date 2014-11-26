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
            [aipal-e2e.sivu.kysely :as kysely-sivu]
            [aipal-e2e.sivu.kyselyt :as kyselyt-sivu]
            [aipal-e2e.sivu.kysymysryhma :as kysymysryhma-sivu]
            [aipal-e2e.sivu.kysymysryhmat :as kysymysryhmat-sivu]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]
            [aipal-e2e.util :refer :all]
            [aipalvastaus-e2e.util :as aipalvastaus]
            [aitu-e2e.util :refer :all]))

(defn sivun-sisalto []
  (w/text (w/find-element {:css "body"})))

(defn valitse-ainoan-kysymyksen-ensimmainen-vaihtoehto []
  (w/select (nth (w/find-elements {:tag :radio}) 0)))

(deftest koko-putki-test
  (with-webdriver
    (with-data {:koulutustoimija [{:ytunnus "0000000-0"}]
                :rooli_organisaatio [{:organisaatio "0000000-0"
                                      :rooli "OPL-VASTUUKAYTTAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}]}
      ;; luo kysymysryhma
      (kysymysryhmat-sivu/avaa-sivu)
      (kysymysryhmat-sivu/luo-uusi)

      (kysymysryhma-sivu/aseta-kysymysryhman-nimi-suomeksi "Uusi kysymysryhmä")
      (kysymysryhma-sivu/luo-uusi-kysymys)
      (kysymysryhma-sivu/aseta-kysymys-suomeksi "Uusi kysymys")
      (kysymysryhma-sivu/lisaa-kysymys)
      (kysymysryhma-sivu/tallenna-kysymysryhma)

      ;; julkaise kysymysryhmä
      (kysymysryhmat-sivu/julkaise)
      (kysymysryhmat-sivu/vahvista-julkaisu)

      ;; luo kysely
      (kyselyt-sivu/avaa-sivu)
      (kyselyt-sivu/luo-uusi-kysely)

      (kysely-sivu/aseta-kyselyn-nimi-suomeksi "Uusi kysely")

      (kysely-sivu/lisaa-kysymysryhma)
      (kysely-sivu/valitse-kysymysryhma "Uusi kysymysryhmä")
      (kysely-sivu/lisaa-valittu-kysymysryhma)
      (kysely-sivu/tallenna-kysely)

      ;; julkaise kysely
      (kyselyt-sivu/avaa-ensimmainen-kysely)
      (kyselyt-sivu/julkaise-kysely)
      (kyselyt-sivu/vahvista-kyselyn-julkaisu)

      ;; luo kyselykerta
      (kyselyt-sivu/luo-uusi-kyselykerta)
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
                        (odota-angular-pyyntoa)
                        (.contains (sivun-sisalto) "UUSI KYSELY")) 10000 1000)

        (try
          (valitse-ainoan-kysymyksen-ensimmainen-vaihtoehto)

          (odota-kunnes (w/enabled? {:css ".e2e-tallenna-vastaukset"}))
          (w/click {:css ".e2e-tallenna-vastaukset"})

          (odota-angular-pyyntoa)
          (is (.contains (sivun-sisalto) "Kiitos vastauksestanne"))
          (finally
            (aipalvastaus/poista-vastaajat-ja-vastaukset-vastaustunnukselta! vastaajatunnus-url)))))))
