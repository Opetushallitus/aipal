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
  (testing "Yhdistää pelkän hakeutumisvaiheen kysymysryhmän"
    (let [kysymysryhmat [{:kysymysryhmaid hakeutumisvaihe-id}]]
      (is (= (map :kysymysryhmaid (yhdista-valtakunnalliset-taustakysymysryhmat kysymysryhmat))
             [suorittamisvaihe-id]))))
  (testing "Yhdistää pelkän suorittamisvaiheen kysymysryhmän"
     (let [kysymysryhmat [{:kysymysryhmaid suorittamisvaihe-id}]]
       (is (= (map :kysymysryhmaid (yhdista-valtakunnalliset-taustakysymysryhmat kysymysryhmat))
              [suorittamisvaihe-id]))))
  (testing "Jos hakeutumis- ja suorittamisvaiheen kysymyksiä ei löydy, ei tee mitään"
    (let [kysymysryhmat [{:kysymysryhmaid 1}]]
      (is (= (yhdista-valtakunnalliset-taustakysymysryhmat kysymysryhmat) kysymysryhmat)))))

(deftest poista-kysymys-kysymysryhmasta-test
  (let [poista-kysymys-kysymysryhmasta #'aipal.toimiala.raportti.taustakysymykset/poista-kysymys-kysymysryhmasta]
    (is (= (poista-kysymys-kysymysryhmasta {:kysymykset [{:kysymysid 123}]} 123)
           {:kysymykset []}))
    (is (= (poista-kysymys-kysymysryhmasta {:kysymykset [{:kysymysid 123}]} 456)
           {:kysymykset [{:kysymysid 123}]}))
    (is (= (poista-kysymys-kysymysryhmasta {:kysymykset [{:kysymysid 123} {:kysymysid 456}]} 456)
           {:kysymykset [{:kysymysid 123}]}))))

(deftest poista-taustakysymys-raportista-test
  (let [poista-taustakysymys-raportista #'aipal.toimiala.raportti.taustakysymykset/poista-taustakysymys-raportista]
    (with-redefs [aipal.toimiala.raportti.taustakysymykset/poista-kysymys-kysymysryhmasta
                  (fn [kysymysryhma kysymys]
                    (assoc kysymysryhma :poistettu-kysymys kysymys))]
      (is (= (poista-taustakysymys-raportista {:raportti [{:kysymysryhmaid suorittamisvaihe-id}]} 123)
             {:raportti [{:poistettu-kysymys 123 :kysymysryhmaid suorittamisvaihe-id}]}))
      (is (= (poista-taustakysymys-raportista {:raportti [{:kysymysryhmaid 123456}]} 123)
             {:raportti [{:kysymysryhmaid 123456}]})))))

(deftest hae-raportista-taustakysymys-6-test
  (let [hae-raportista-taustakysymys-6 #'aipal.toimiala.raportti.taustakysymykset/hae-raportista-taustakysymys-6]
    (is (= (hae-raportista-taustakysymys-6 {:raportti [{:kysymykset [{:kysymysid taustakysymys-6a-id}]}]})
           taustakysymys-6a-id))
    (is (= (hae-raportista-taustakysymys-6 {:raportti [{:kysymykset [{:kysymysid taustakysymys-6b-id}]}]})
           taustakysymys-6b-id))
    (is (= (hae-raportista-taustakysymys-6 {:raportti [{:kysymykset [{:kysymysid 1}]}]})
           nil))))
