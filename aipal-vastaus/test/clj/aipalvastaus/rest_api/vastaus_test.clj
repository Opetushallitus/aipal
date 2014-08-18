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

(ns aipalvastaus.rest-api.vastaus-test
  (:require [aipalvastaus.rest-api.vastaus :as v])
  (:use clojure.test))

(def vastaajaid 3679)

(deftest vastaukset-tasmaavat-kysymyksiin
  (testing "Vastaukset löytyvät kysymysten joukosta"
    (let [kysymykset [{:kysymysid 1} {:kysymysid 2} {:kysymysid 3}]
          vastaukset [{:kysymysid 1} {:kysymysid 3}]]
      (is (= vastaukset (v/validoi-vastaukset vastaukset kysymykset))))))

(deftest vastaukset-eivat-tasmaa-kysymyksiin
  (testing "Vastaukset löytyvät kysymysten joukosta"
    (let [kysymykset [{:kysymysid 1} {:kysymysid 2} {:kysymysid 3}]
          vastaukset [{:kysymysid 1} {:kysymysid 4}]]
      (is (= nil (v/validoi-vastaukset vastaukset kysymykset))))))

(deftest monivalinta-vastaus
  (testing "Yksi valinta tuottaa yhden vastauksen"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "monivalinta"}]
          vastaukset [{:kysymysid 1 :vastaus [1]} ]]
      (is (= (list {:kysymysid 1
                    :vastaajaid vastaajaid
                    :vastaustyyppi "monivalinta"
                    :numerovalinta 1
                    :vapaateksti nil
                    :vaihtoehto nil})
             (v/muodosta-tallennettavat-vastaukset vastaukset kysymykset)))))
  (testing "kaksi valintaa tuottaa kaksi vastausta samalle kysymykselle"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "monivalinta"}]
          vastaukset [{:kysymysid 1 :vastaus [1 2]} ]]
      (is (= (list {:kysymysid 1
                    :vastaajaid vastaajaid
                    :vastaustyyppi "monivalinta"
                    :numerovalinta 1
                    :vapaateksti nil
                    :vaihtoehto nil}
                   {:kysymysid 1
                    :vastaajaid vastaajaid
                    :vastaustyyppi "monivalinta"
                    :numerovalinta 2
                    :vapaateksti nil
                    :vaihtoehto nil})
             (v/muodosta-tallennettavat-vastaukset vastaukset kysymykset))))))

(deftest kylla-ei-vastaus
  (testing "Valinta tuottaa saman vastauksen"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "kylla_ei_valinta"}]
          vastaukset [{:kysymysid 1 :vastaus ["kylla"]} ]]
      (is (= (list {:kysymysid 1
                    :vastaajaid vastaajaid
                    :vastaustyyppi "kylla_ei_valinta"
                    :numerovalinta nil
                    :vapaateksti nil
                    :vaihtoehto "kylla"})
             (v/muodosta-tallennettavat-vastaukset vastaukset kysymykset))))))

(deftest vapaateksti-vastaus
  (testing "Vastaus tallentuu vapaateksti kentään"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "vapaateksti"}]
          vastaukset [{:kysymysid 1 :vastaus ["vapaateksti"]} ]]
      (is (= (list {:kysymysid 1
                    :vastaajaid vastaajaid
                    :vastaustyyppi "vapaateksti"
                    :numerovalinta nil
                    :vapaateksti "vapaateksti"
                    :vaihtoehto nil})
             (v/muodosta-tallennettavat-vastaukset vastaukset kysymykset))))))

(deftest asteikko-vastaus
  (testing "vastaus tallentuu numerovalinta kenttään"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "asteikko"}]
          vastaukset [{:kysymysid 1 :vastaus [2]} ]]
      (is (= (list {:kysymysid 1
                    :vastaajaid vastaajaid
                    :vastaustyyppi "asteikko"
                    :numerovalinta 2
                    :vapaateksti nil
                    :vaihtoehto nil})
             (v/muodosta-tallennettavat-vastaukset vastaukset kysymykset))))))
