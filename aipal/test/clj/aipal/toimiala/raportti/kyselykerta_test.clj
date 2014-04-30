(ns aipal.toimiala.raportti.kyselykerta-test
  (:require [clojure.test :refer [are deftest is testing]]
            [aipal.toimiala.raportti.kyselykerta :refer :all]))

(deftest jaottele-asteikko-test
 (testing
   "jaottele asteikko:"
   (let [tyhja-jakauma {1 0 2 0 3 0 4 0 5 0}]
     (are [kuvaus vastaukset odotettu-tulos]
          (is (= (jaottele-asteikko vastaukset) odotettu-tulos) kuvaus)
          "ei vastauksia" [] tyhja-jakauma
          "yksi vastaus" [{:numerovalinta 1}] (merge tyhja-jakauma {1 1})
          "useampi sama vastaus" [{:numerovalinta 1} {:numerovalinta 1}] (merge tyhja-jakauma {1 2})
          "eri vastaukset" [{:numerovalinta 1} {:numerovalinta 2}] (merge tyhja-jakauma {1 1 2 1})))))

(deftest jaottele-vaihtoehdot-test
 (testing
   "jaottele vaihtoehdot:"
   (are [kuvaus vastaukset odotettu-tulos]
        (is (= (jaottele-vaihtoehdot vastaukset) odotettu-tulos) kuvaus)
        "ei vastauksia" [] {:kylla 0 :ei 0}
        "kyllä-vastaus" [{:vaihtoehto "kylla"}] {:kylla 1 :ei 0}
        "ei-vastaus" [{:vaihtoehto "ei"}] {:kylla 0 :ei 1}
        "molempia valintoja" [{:vaihtoehto "kylla"} {:vaihtoehto "ei"}] {:kylla 1 :ei 1}
        "joku muu vastaus" [{:vaihtoehto "jokumuu"}] {:jokumuu 1 :kylla 0 :ei 0})))

(deftest kysymyksen-kasittelija-test
  (testing
    "kysymyksen käsittelijä:"
    (let [lisaa-asteikon-jakauma (fn [kysymys vastaukset] kysymys)
          lisaa-vaihtoehtojen-jakauma (fn [kysymys vastaukset] kysymys)
          lisaa-vapaatekstit (fn [kysymys vastaukset] kysymys)]
      (with-redefs [aipal.toimiala.raportti.kyselykerta/lisaa-asteikon-jakauma lisaa-asteikon-jakauma
                    aipal.toimiala.raportti.kyselykerta/lisaa-vaihtoehtojen-jakauma lisaa-vaihtoehtojen-jakauma
                    aipal.toimiala.raportti.kyselykerta/lisaa-vastausten-vapaateksti lisaa-vapaatekstit]
        (testing
          "valitsee oikean funktion:"
          (are [kuvaus kysymys odotettu-tulos]
               (is (= (kysymyksen-kasittelija kysymys) odotettu-tulos))
               "asteikko" {:vastaustyyppi "asteikko"} lisaa-asteikon-jakauma
               "kyllä/ei valinta" {:vastaustyyppi "kylla_ei_valinta"} lisaa-vaihtoehtojen-jakauma
               "vapaateksti" {:vastaustyyppi "vapaateksti"} lisaa-vapaatekstit))))))
