;; Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.toimiala.raportti.taustakysymykset-test
  (:require [clojure.test :refer [are deftest is testing]]
            [aipal.toimiala.raportti.taustakysymykset :refer :all]))

(deftest mappaa-kysymysid-test
  (testing "Jos annetaan molemmista taustakysymyksistä löytyvä id, palauttaa molemmat vastaavat "
    (is (= (mappaa-kysymysid 7312034) [7312034 7312027])))
  (testing "Jos annetaan id jota ei löydy molemmista, palauttaa vain sen id:n listana"
    (is (= (mappaa-kysymysid 1) [1]))))

(deftest yhdista-taustakysymysten-vastaukset-test
  (testing "Jos vastaus on hakeutumisvaiheen kysymykseen, vaihdetaan vastaavaan suorittamisvaiheen kysymykseen"
    (is (= (yhdista-taustakysymysten-vastaukset {:kysymysid 7312034}) {:kysymysid 7312027})))
  (testing "Jos vastaus on muuhun kysymykseen, ei vaihdeta kysymysid:tä"
    (is (= (yhdista-taustakysymysten-vastaukset {:kysymysid 1}) {:kysymysid 1}))))

(deftest yhdista-taustakysymysten-kysymykset-test
  (testing "Jos kysymys on hakeutumisvaiheen taustakysymyksissä, vaihdetaan suorittamisvaiheeseen"
    (is (= (yhdista-taustakysymysten-kysymykset {:kysymysryhmaid hakeutumisvaihe-id}) {:kysymysryhmaid suorittamisvaihe-id})))
  (testing "Jos kysymys on muussa kysymysryhmässä, ei vaihdeta kysymysryhmäid:tä"
    (is (= (yhdista-taustakysymysten-kysymykset {:kysymysryhmaid 1}) {:kysymysryhmaid 1}))))

(deftest yhdista-valtakunnalliset-taustakysymysryhmat-test
  (testing "Yhdistää annetuista kysymysryhmistä hakeutumis- ja suorittamisvaiheen kysymysryhmät yhdeksi"
    (let [kysymysryhmat [{:kysymysryhmaid 1} {:kysymysryhmaid hakeutumisvaihe-id} {:kysymysryhmaid suorittamisvaihe-id}]]
      (is (= (map :kysymysryhmaid (yhdista-valtakunnalliset-taustakysymysryhmat kysymysryhmat))
             [suorittamisvaihe-id 1]))))
  (testing "Jos hakeutumis- ja suorittamisvaiheen kysymyksiä ei löydy, ei tee mitään"
    (let [kysymysryhmat [{:kysymysryhmaid 1}]]
      (is (= (yhdista-valtakunnalliset-taustakysymysryhmat kysymysryhmat) kysymysryhmat)))))
