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
    (let [esitys (fn [lkm-eos osuus-eos & lukumaarat-ja-osuudet]
                   (let [lukumaarat (take-nth 2 lukumaarat-ja-osuudet)
                         osuudet (take-nth 2 (rest lukumaarat-ja-osuudet))
                         index-lkm-osuus (map vector (map inc (range)) lukumaarat osuudet)]
                     (conj (vec (for [[index lkm osuus] index-lkm-osuus]
                             {:vaihtoehto-avain (str index)
                              :lukumaara        lkm
                              :osuus            osuus}))
                           {:vaihtoehto-avain "eos"
                            :lukumaara        lkm-eos
                            :osuus            osuus-eos}))
                   )]
      (are [kuvaus jakauma odotettu-tulos]
           (is (= (muodosta-asteikko-jakauman-esitys jakauma (-> jakauma count (- 1))) odotettu-tulos) kuvaus)
           "ei vastauksia" {1 0 2 0 3 0 4 0 5 0 :eos 0} (esitys 0 0 0 0 0 0 0 0 0 0 0 0)
           "yksi vastaus" {1 1 2 0 3 0 4 0 5 0 :eos 0} (esitys 0 0 1 100 0 0 0 0 0 0 0 0)
           "monta vastausta, sama vaihtoehto" {1 2 2 0 3 0 4 0 5 0 :eos 0} (esitys 0 0 2 100 0 0 0 0 0 0 0 0)
           "monta vastausta, eri vaihtoehto" {1 1 2 1 3 0 4 0 5 0 :eos 0} (esitys 0 0 1 50 1 50 0 0 0 0 0 0)
           "EOS-vastaus" {1 0 2 0 3 0 4 0 5 0 :eos 1} (esitys 1 100 0 0 0 0 0 0 0 0 0 0)
           "ei vastauksia 7" {1 0 2 0 3 0 4 0 5 0 6 0 7 0 :eos 0} (esitys 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)
           "yksi vastaus 7" {1 0 2 0 3 0 4 0 5 0 6 0 7 1 :eos 0} (esitys 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 100)
           "monta vastausta, sama vaihtoehto 7" {1 0 2 0 3 0 4 0 5 0 6 0 7 2 :eos 0} (esitys 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2 100)
           "monta vastausta, eri vaihtoehto 7" {1 0 2 0 3 0 4 0 5 0 6 1 7 1 :eos 0} (esitys 0 0 0 0 0 0 0 0 0 0 0 0 1 50 1 50)
           "EOS-vastaus 7" {1 0 2 0 3 0 4 0 5 0 6 0 7 0 :eos 1} (esitys 1 100 0 0 0 0 0 0 0 0 0 0 0 0 0 0)))))

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
                     :vaihtoehto_en nil
                     :lukumaara lukumaara-1
                     :osuus osuus-1
                     :jarjestys 1}
                    {:vaihtoehto_fi "vaihtoehto 2"
                     :vaihtoehto_sv nil
                     :vaihtoehto_en nil
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

; Vertailun aineisto vuosi taaksepäin raportointijakson loppupäivästä.
; Eli raportoinnin rajaus voi olla 1kk, mutta vertailuluku lasketaan vuoden aineistosta.
; Jos raportoinnin rajaus on yli 1 vuosi, otetaan vertailuluku samalta ajalta.
(deftest valtakunnallinen-raportti-vertailujakso-test
  (testing "alkupvm ja loppupvm asetettu ja alle vuosi, vertailu vuosi taaksepäin"
    (is (=
          {:vertailujakso_alkupvm "2013-12-30"
           :vertailujakso_loppupvm "2014-12-29"}
          (valtakunnallinen-raportti-vertailujakso "2014-01-01" "2014-12-29"))))
  (testing "alkupvm ja loppupvm asetettu ja yli vuosi"
    (is (=
          {:vertailujakso_alkupvm "2013-01-01"
           :vertailujakso_loppupvm "2014-12-01"}
          (valtakunnallinen-raportti-vertailujakso "2013-01-01" "2014-12-01"))))
  (testing "loppupvm asetettu"
    (is (=
          {:vertailujakso_alkupvm nil
           :vertailujakso_loppupvm "2014-12-31"}
          (valtakunnallinen-raportti-vertailujakso nil "2014-12-31"))))
  (testing "alkupvm asetettu"
    (is (=
          {:vertailujakso_alkupvm "2013-01-01"
           :vertailujakso_loppupvm nil}
          (valtakunnallinen-raportti-vertailujakso "2013-01-01" nil))))
  (testing "alkupvm asetettu, ja alle vuoden tästä päivämäärästä"
    (is (=
          {:vertailujakso_alkupvm (.toString (-> (t/today) (t/minus (t/years 1)) (t/plus (t/days 1))))
           :vertailujakso_loppupvm nil}
          (valtakunnallinen-raportti-vertailujakso (.toString (t/today)) nil))))
  (testing "alkupvm ja loppupvm ei asetettu"
    (is (=
          {:vertailujakso_alkupvm nil
           :vertailujakso_loppupvm nil}
          (valtakunnallinen-raportti-vertailujakso nil nil)))))
