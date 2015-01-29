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

(ns aipal.toimiala.raportti.raportointi-test
  (:require [clojure.test :refer [are deftest is testing]]
            [aipal.toimiala.raportti.raportointi :refer :all]
            [clj-time.core :as t]))

(deftest jaottele-asteikko-test
 (testing
   "jaottele asteikko:"
   (let [tyhja-jakauma {1 0 2 0 3 0 4 0 5 0 :eos 0}]
     (are [kuvaus vastaukset odotettu-tulos]
          (is (= (jaottele-asteikko vastaukset) odotettu-tulos) kuvaus)
          "ei vastauksia" [] tyhja-jakauma
          "yksi vastaus" [{:numerovalinta 1}] (merge tyhja-jakauma {1 1})
          "useampi sama vastaus" [{:numerovalinta 1} {:numerovalinta 1}] (merge tyhja-jakauma {1 2})
          "eri vastaukset" [{:numerovalinta 1} {:numerovalinta 2}] (merge tyhja-jakauma {1 1 2 1})))))

(deftest jaottele-jatkokysymys-asteikko-test
 (let [tyhja-jakauma {1 0 2 0 3 0 4 0 5 0 :eos 0}]
   (are [kuvaus vastaukset odotettu-tulos] (is (= (jaottele-jatkokysymys-asteikko vastaukset) odotettu-tulos) kuvaus)
        "ei vastauksia" [] tyhja-jakauma
        "tyhja vastaus" [nil] tyhja-jakauma
        "yksi vastaus" [{:kylla_asteikko 1}] (merge tyhja-jakauma {1 1})
        "useampi sama vastaus" [{:kylla_asteikko 1} {:kylla_asteikko 1}] (merge tyhja-jakauma {1 2})
        "useampi vastaus, mukana tyhja vastaus" [{:kylla_asteikko 1} nil {:kylla_asteikko 1}] (merge tyhja-jakauma {1 2})
        "eri vastaukset" [{:kylla_asteikko 1} {:kylla_asteikko 2}] (merge tyhja-jakauma {1 1 2 1}))))

(deftest jaottele-monivalinta-test
 (testing
   "jaottele monivalinta:"
   (are [kuvaus vastaukset odotettu-tulos]
        (is (= (jaottele-monivalinta vastaukset) odotettu-tulos) kuvaus)
        "ei vastauksia" [] {}
        "yksi vastaus" [{:numerovalinta 1}] {1 1}
        "monta vastausta, sama valinta" [{:numerovalinta 1} {:numerovalinta 1}] {1 2}
        "monta vastausta, eri valinnat" [{:numerovalinta 1} {:numerovalinta 2}] {1 1 2 1})))

(deftest jaottele-vaihtoehdot-test
 (testing
   "jaottele vaihtoehdot:"
   (are [kuvaus vastaukset odotettu-tulos]
        (is (= (jaottele-vaihtoehdot vastaukset) odotettu-tulos) kuvaus)
        "ei vastauksia" [] {:kylla 0 :ei 0 :eos 0}
        "kyllä-vastaus" [{:vaihtoehto "kylla"}] {:kylla 1 :ei 0 :eos 0}
        "ei-vastaus" [{:vaihtoehto "ei"}] {:kylla 0 :ei 1 :eos 0}
        "molempia valintoja" [{:vaihtoehto "kylla"} {:vaihtoehto "ei"}] {:kylla 1 :ei 1 :eos 0}
        "joku muu vastaus" [{:vaihtoehto "jokumuu"}] {:jokumuu 1 :kylla 0 :ei 0 :eos 0})))

(deftest kysymyksen-kasittelija-test
  (testing
    "kysymyksen käsittelijä:"
    (let [lisaa-asteikon-jakauma (fn [kysymys vastaukset] kysymys)
          lisaa-monivalinnan-jakauma (fn [kysymys vastaukset] kysymys)
          lisaa-vaihtoehtojen-jakauma (fn [kysymys vastaukset] kysymys)
          lisaa-vapaatekstit (fn [kysymys vastaukset] kysymys)]
      (with-redefs [aipal.toimiala.raportti.raportointi/lisaa-asteikon-jakauma lisaa-asteikon-jakauma
                    aipal.toimiala.raportti.raportointi/lisaa-monivalinnan-jakauma lisaa-monivalinnan-jakauma
                    aipal.toimiala.raportti.raportointi/lisaa-vaihtoehtojen-jakauma lisaa-vaihtoehtojen-jakauma
                    aipal.toimiala.raportti.raportointi/lisaa-vastausten-vapaateksti lisaa-vapaatekstit]
        (testing
          "valitsee oikean funktion:"
          (are [kuvaus kysymys odotettu-tulos]
               (is (= (kysymyksen-kasittelija kysymys) odotettu-tulos) kuvaus)
               "asteikko" {:vastaustyyppi "asteikko"} lisaa-asteikon-jakauma
               "kyllä/ei valinta" {:vastaustyyppi "kylla_ei_valinta"} lisaa-vaihtoehtojen-jakauma
               "likert-asteikko" {:vastaustyyppi "likert_asteikko"} lisaa-asteikon-jakauma
               "monivalinta" {:vastaustyyppi "monivalinta"} lisaa-monivalinnan-jakauma
               "vapaateksti" {:vastaustyyppi "vapaateksti"} lisaa-vapaatekstit))))))

(deftest prosentteina-test
  (testing
    "prosentteina:"
    (are [kuvaus osuus odotettu-tulos]
         (is (= (prosentteina osuus) odotettu-tulos) kuvaus)
         "nolla" 0 0
         "murtoluku" 1/3 33
         "pyöristettävä alaspäin" 0.333 33
         "puolet, pyöristettävä ylöspäin" 0.335 34
         "pyöristettävä ylöspäin" 0.336 34
         "yksi" 1 100)))

(deftest muodosta-asteikko-jakauman-esitys-test
  (testing
    "muodosta asteikko-jakauman esitys:"
    (let [esitys (fn [lkm-eos osuus-eos lkm-1 osuus-1 lkm-2 osuus-2 lkm-3 osuus-3 lkm-4 osuus-4 lkm-5 osuus-5]
                   [{:vaihtoehto-avain "1"
                     :lukumaara lkm-1
                     :osuus osuus-1}
                    {:vaihtoehto-avain "2"
                     :lukumaara lkm-2
                     :osuus osuus-2}
                    {:vaihtoehto-avain "3"
                     :lukumaara lkm-3
                     :osuus osuus-3}
                    {:vaihtoehto-avain "4"
                     :lukumaara lkm-4
                     :osuus osuus-4}
                    {:vaihtoehto-avain "5"
                     :lukumaara lkm-5
                     :osuus osuus-5}
                    {:vaihtoehto-avain "eos"
                     :lukumaara lkm-eos
                     :osuus osuus-eos}])]
      (are [kuvaus jakauma odotettu-tulos]
           (is (= (muodosta-asteikko-jakauman-esitys jakauma) odotettu-tulos) kuvaus)
           "ei vastauksia" {1 0 2 0 3 0 4 0 5 0 :eos 0} (esitys 0 0 0 0 0 0 0 0 0 0 0 0)
           "yksi vastaus" {1 1 2 0 3 0 4 0 5 0 :eos 0} (esitys 0 0 1 100 0 0 0 0 0 0 0 0)
           "monta vastausta, sama vaihtoehto" {1 2 2 0 3 0 4 0 5 0 :eos 0} (esitys 0 0 2 100 0 0 0 0 0 0 0 0)
           "monta vastausta, eri vaihtoehto" {1 1 2 1 3 0 4 0 5 0 :eos 0} (esitys 0 0 1 50 1 50 0 0 0 0 0 0)
           "EOS-vastaus" {1 0 2 0 3 0 4 0 5 0 :eos 1} (esitys 1 100 0 0 0 0 0 0 0 0 0 0)))))

(deftest muodosta-kylla-ei-jakauman-esitys-test
  (testing
    "muodosta kyllä/ei jakauman esitys:"
    (let [esitys (fn [eos-lkm eos-osuus kylla-lkm kylla-osuus ei-lkm ei-osuus]
                   [{:vaihtoehto-avain "kylla"
                     :lukumaara kylla-lkm
                     :osuus kylla-osuus}
                    {:vaihtoehto-avain "ei"
                     :lukumaara ei-lkm
                     :osuus ei-osuus}
                    {:vaihtoehto-avain "eos"
                     :lukumaara eos-lkm
                     :osuus eos-osuus}])]
      (are [kuvaus jakauma odotettu-tulos]
           (is (= (muodosta-kylla-ei-jakauman-esitys jakauma) odotettu-tulos) kuvaus)
           "ei vastauksia" {:kylla 0 :ei 0 :eos 0} (esitys 0 0 0 0 0 0)
           "yksi vastaus: kyllä" {:kylla 1 :ei 0 :eos 0} (esitys 0 0 1 100 0 0)
           "yksi vastaus: ei" {:kylla 0 :ei 1 :eos 0} (esitys 0 0 0 0 1 100)
           "monta vastausta, sama vaihtoehto" {:kylla 2 :ei 0 :eos 0} (esitys 0 0 2 100 0 0)
           "monta vastausta, eri vaihtoehto" {:kylla 1 :ei 1 :eos 0} (esitys 0 0 1 50 1 50)
           "EOS-vastaus" {:kylla 0 :ei 0 :eos 1} (esitys 1 100 0 0 0 0)))))

(deftest muodosta-monivalinta-jakauman-esitys-test
  (testing
    "muodosta monivalintajakauman esitys:"
    (let [vaihtoehdot [{:jarjestys 1 :teksti_fi "vaihtoehto 1"}
                       {:jarjestys 2 :teksti_fi "vaihtoehto 2"}]
          esitys (fn [lukumaara-1 osuus-1 lukumaara-2 osuus-2]
                   [{:vaihtoehto_fi "vaihtoehto 1"
                     :vaihtoehto_sv nil
                     :lukumaara lukumaara-1
                     :osuus osuus-1
                     :jarjestys 1}
                    {:vaihtoehto_fi "vaihtoehto 2"
                     :vaihtoehto_sv nil
                     :lukumaara lukumaara-2
                     :osuus osuus-2
                     :jarjestys 2}])]
      (are [kuvaus jakauma odotettu-tulos]
           (is (= (muodosta-monivalinta-jakauman-esitys vaihtoehdot jakauma) odotettu-tulos) kuvaus)
           "ei vastauksia" {} (esitys 0 0 0 0)
           "yksi vastaus, vaihtoehto 1" {1 1} (esitys 1 100 0 0)
           "yksi vastaus, vaihtoehto 2" {2 1} (esitys 0 0 1 100)
           "monta vastausta, sama vaihtoehto" {1 2} (esitys 2 100 0 0)
           "monta vastausta, eri vaihtoehto" {1 1 2 1} (esitys 1 50 1 50)))))

(deftest keraa-ei-jatkovastaukset-test
  (let [ei-kysymys {:ei_kysymys true :ei_teksti_fi "kysymys"}]
    (are [kuvaus vastaukset odotettu-tulos] (is (= (:vapaatekstivastaukset (keraa-ei-jatkovastaukset ei-kysymys vastaukset)) odotettu-tulos) kuvaus)
         "ei vastauksia" [] []
         "yksi vastaus" [{:ei_vastausteksti "vastaus1"}] [{:teksti "vastaus1"}]
         "useampi vastaus" [{:ei_vastausteksti "vastaus1"} {:ei_vastausteksti "vastaus2"}] [{:teksti "vastaus1"} {:teksti "vastaus2"}]
         "yksi vastaus ja vastaamaton" [{:ei_vastausteksti "vastaus1"} {:ei_vastausteksti nil}] [{:teksti "vastaus1"}])))

(deftest kysymysryhmaan-vastanneiden-lukumaara-test
  (testing
    "kaksi eri vastaajaa samalla kysymyksellä"
    (is (= (kysymysryhmaan-vastanneiden-lukumaara {:kysymykset [{:vastaukset [{:vastaajaid 1}
                                                                              {:vastaajaid 2}]}]})
           2)))
  (testing
    "kaksi eri vastaajaa kahdella eri kysymyksellä"
    (is (= (kysymysryhmaan-vastanneiden-lukumaara {:kysymykset [{:vastaukset [{:vastaajaid 1}]}
                                                                {:vastaukset [{:vastaajaid 2}]}]})
           2)))
  (testing
    "sama vastaaja kahdella eri kysymyksellä"
    (is (= (kysymysryhmaan-vastanneiden-lukumaara {:kysymykset [{:vastaukset [{:vastaajaid 1}]}
                                                                {:vastaukset [{:vastaajaid 1}]}]})
           1))))

(deftest ryhmittele-kysymykset-ja-vastaukset-kysymysryhmittain-test
  (is (= (ryhmittele-kysymykset-ja-vastaukset-kysymysryhmittain [{:kysymysid 1 :kysymysryhmaid 101}
                                                                 {:kysymysid 2 :kysymysryhmaid 102}]
                                                                [{:vastausid 11 :kysymysid 1}
                                                                 {:vastausid 12 :kysymysid 2}]
                                                                [{:kysymysryhmaid 101}
                                                                 {:kysymysryhmaid 102}])
         [{:kysymykset [{:kysymysid 1
                         :kysymysryhmaid 101
                         :vastaukset [{:vastausid 11, :kysymysid 1}]}]
           :kysymysryhmaid 101}
          {:kysymykset [{:kysymysid 2
                         :kysymysryhmaid 102
                         :vastaukset [{:vastausid 12, :kysymysid 2}]}]
           :kysymysryhmaid 102}])))

(deftest keskihajonta-test
  (is (= (int (keskihajonta [1000 2000 3000 4000 5000])) 1581)))

; Vertailun aineisto vuosi taaksepäin raportointijakson loppupäivästä.
; Eli raportoinnin rajaus voi olla 1kk, mutta vertailuluku lasketaan vuoden aineistosta.
; Jos raportoinnin rajaus on yli 1 vuosi, otetaan vertailuluku samalta ajalta.
(deftest vertailuraportti-vertailujakso-test
  (testing "alkupvm ja loppupvm asetettu ja alle vuosi, vertailu vuosi taaksepäin"
    (is (=
          {:vertailujakso_alkupvm "2013-12-29"
           :vertailujakso_loppupvm "2014-12-29"}
          (vertailuraportti-vertailujakso "2014-01-01" "2014-12-29"))))
  (testing "alkupvm ja loppupvm asetettu ja yli vuosi"
    (is (=
          {:vertailujakso_alkupvm "2013-01-01"
           :vertailujakso_loppupvm "2014-12-01"}
          (vertailuraportti-vertailujakso "2013-01-01" "2014-12-01"))))
  (testing "loppupvm asetettu"
    (is (=
          {:vertailujakso_alkupvm nil
           :vertailujakso_loppupvm "2014-12-31"}
          (vertailuraportti-vertailujakso nil "2014-12-31"))))
  (testing "alkupvm asetettu"
    (is (=
          {:vertailujakso_alkupvm "2013-01-01"
           :vertailujakso_loppupvm nil}
          (vertailuraportti-vertailujakso "2013-01-01" nil))))
  (testing "alkupvm asetettu, ja alle vuoden tästä päivämäärästä"
    (is (=
          {:vertailujakso_alkupvm (.toString (t/minus (t/today) (t/years 1)))
           :vertailujakso_loppupvm nil}
          (vertailuraportti-vertailujakso (.toString (t/today)) nil))))
  (testing "alkupvm ja loppupvm ei asetettu"
    (is (=
          {:vertailujakso_alkupvm nil
           :vertailujakso_loppupvm nil}
          (vertailuraportti-vertailujakso nil nil)))))
