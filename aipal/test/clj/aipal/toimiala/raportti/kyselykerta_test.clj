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
        "ei vastauksia" [] {:kylla 0 :ei 0}
        "kyllä-vastaus" [{:vaihtoehto "kylla"}] {:kylla 1 :ei 0}
        "ei-vastaus" [{:vaihtoehto "ei"}] {:kylla 0 :ei 1}
        "molempia valintoja" [{:vaihtoehto "kylla"} {:vaihtoehto "ei"}] {:kylla 1 :ei 1}
        "joku muu vastaus" [{:vaihtoehto "jokumuu"}] {:jokumuu 1 :kylla 0 :ei 0})))

(deftest kysymyksen-kasittelija-test
  (testing
    "kysymyksen käsittelijä:"
    (let [lisaa-asteikon-jakauma (fn [kysymys vastaukset] kysymys)
          lisaa-monivalinnan-jakauma (fn [kysymys vastaukset] kysymys)
          lisaa-vaihtoehtojen-jakauma (fn [kysymys vastaukset] kysymys)
          lisaa-vapaatekstit (fn [kysymys vastaukset] kysymys)]
      (with-redefs [aipal.toimiala.raportti.kyselykerta/lisaa-asteikon-jakauma lisaa-asteikon-jakauma
                    aipal.toimiala.raportti.kyselykerta/lisaa-monivalinnan-jakauma lisaa-monivalinnan-jakauma
                    aipal.toimiala.raportti.kyselykerta/lisaa-vaihtoehtojen-jakauma lisaa-vaihtoehtojen-jakauma
                    aipal.toimiala.raportti.kyselykerta/lisaa-vastausten-vapaateksti lisaa-vapaatekstit]
        (testing
          "valitsee oikean funktion:"
          (are [kuvaus kysymys odotettu-tulos]
               (is (= (kysymyksen-kasittelija kysymys) odotettu-tulos) kuvaus)
               "asteikko" {:vastaustyyppi "asteikko"} lisaa-asteikon-jakauma
               "kyllä/ei valinta" {:vastaustyyppi "kylla_ei_valinta"} lisaa-vaihtoehtojen-jakauma
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

(deftest muodosta-kylla-ei-jakauman-esitys-test
  (testing
    "muodosta kyllä/ei jakauman esitys:"
    (let [esitys (fn [kylla-lkm kylla-osuus ei-lkm ei-osuus]
                   [{:vaihtoehto "Kyllä"
                     :lukumaara kylla-lkm
                     :osuus kylla-osuus}
                    {:vaihtoehto "Ei"
                     :lukumaara ei-lkm
                     :osuus ei-osuus}])]
      (are [kuvaus jakauma odotettu-tulos]
           (is (= (muodosta-kylla-ei-jakauman-esitys jakauma) odotettu-tulos) kuvaus)
           "ei vastauksia" {:kylla 0 :ei 0} (esitys 0 0 0 0)
           "yksi vastaus: kyllä" {:kylla 1 :ei 0} (esitys 1 100 0 0)
           "yksi vastaus: ei" {:kylla 0 :ei 1} (esitys 0 0 1 100)
           "monta vastausta, sama vaihtoehto" {:kylla 2 :ei 0} (esitys 2 100 0 0)
           "monta vastausta, eri vaihtoehto" {:kylla 1 :ei 1} (esitys 1 50 1 50)))))

(deftest muodosta-monivalinta-jakauman-esitys-test
  (testing
    "muodosta monivalintajakauman esitys:"
    (let [vaihtoehdot [{:jarjestys 1 :teksti_fi "vaihtoehto 1"}
                       {:jarjestys 2 :teksti_fi "vaihtoehto 2"}]
          esitys (fn [lukumaara-1 osuus-1 lukumaara-2 osuus-2]
                   [{:vaihtoehto "vaihtoehto 1"
                     :lukumaara lukumaara-1
                     :osuus osuus-1}
                    {:vaihtoehto "vaihtoehto 2"
                     :lukumaara lukumaara-2
                     :osuus osuus-2}])]
      (are [kuvaus jakauma odotettu-tulos]
           (is (= (muodosta-monivalinta-jakauman-esitys vaihtoehdot jakauma) odotettu-tulos) kuvaus)
           "ei vastauksia" {} (esitys 0 0 0 0)
           "yksi vastaus, vaihtoehto 1" {1 1} (esitys 1 100 0 0)
           "yksi vastaus, vaihtoehto 2" {2 1} (esitys 0 0 1 100)
           "monta vastausta, sama vaihtoehto" {1 2} (esitys 2 100 0 0)
           "monta vastausta, eri vaihtoehto" {1 1 2 1} (esitys 1 50 1 50)))))
