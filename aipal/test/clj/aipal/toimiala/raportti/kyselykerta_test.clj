(ns aipal.toimiala.raportti.kyselykerta-test
  (:require [clojure.test :refer [are deftest is testing]]
            [aipal.toimiala.raportti.kyselykerta :refer :all]))

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
    (let [lisaa-jakauma (fn [kysymys vastaukset] kysymys)
          lisaa-vapaatekstit (fn [kysymys vastaukset] kysymys)]
      (with-redefs [aipal.toimiala.raportti.kyselykerta/lisaa-vaihtoehtojen-jakauma lisaa-jakauma
                    aipal.toimiala.raportti.kyselykerta/lisaa-vastausten-vapaateksti lisaa-vapaatekstit]
        (testing
          "valitsee oikean funktion:"
          (are [kuvaus kysymys odotettu-tulos]
               (is (= (kysymyksen-kasittelija kysymys) odotettu-tulos))
               "kyllä/ei valinta" {:vastaustyyppi "kylla_ei_valinta"} lisaa-jakauma
               "vapaateksti" {:vastaustyyppi "vapaateksti"} lisaa-vapaatekstit))))))
