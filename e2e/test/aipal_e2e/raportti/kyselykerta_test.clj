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

(ns aipal-e2e.raportti.kyselykerta-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [clj-webdriver.taxi :as w]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.data-util :refer [paivamaara-kayttoliittyman-muodossa]]
            [aitu-e2e.util :refer :all]))

(defn kyselykertaraportti-sivu [kyselykertaid] (str "/#/raportit/kyselykerrat/kyselykerta/" kyselykertaid))

(defn sisemman-elementin-kentan-teksti [ulompi-elementti kentta]
  (w/text
    (w/find-element-under ulompi-elementti
                          (-> *ng*
                            (.binding kentta)))))

(defn kyselykerran-tietojen-kentta [kentta]
  (sisemman-elementin-kentan-teksti {:css ".raportti-kyselykerta-tiedot"}
                             (str "tulos.kyselykerta." kentta)))

(defn raportin-luontipvm []
  (sisemman-elementin-kentan-teksti {:css ".raportti-kyselykerta-tiedot"}
                                    "tulos.luontipvm"))

(defn paivamaara [paivamaara-iso-muodossa]
  (time-format/parse-local-date (time-format/formatters :year-month-day)
                                paivamaara-iso-muodossa))
(def tanaan-pvm (time/today))
(def tanaan-kayttoliittyman-muodossa (time-format/unparse-local-date (time-format/formatter "dd.MM.yyyy")
                                                                      tanaan-pvm))

(defn kysymykset []
  (w/find-elements (-> *ng*
                     (.repeater "kysymys in tulos.raportti"))))

(defn kysymyksen-teksti [kysymys-elementti]
  (sisemman-elementin-kentan-teksti kysymys-elementti "kysymys"))

(defn ^:private taulukon-kysymysteksti-kysymykselle [kysymys-elementti]
  (w/text (w/find-element-under kysymys-elementti {:css ".report-table-question"})))

(defn ^:private vapaatekstit-kysymykselle [kysymys-elementti]
  (map w/text
       (w/find-elements-under kysymys-elementti
                              (-> *ng*
                                (.repeater "vastaus in kysymys.vastaukset")
                                (.column "teksti")))))

(defn css-sarakkeet-kysymykselle [css-luokka kysymys-elementti]
  (map w/text
       (w/find-elements-under kysymys-elementti {:css css-luokka})))

(defn vaihtoehdot-kysymykselle [kysymys-elementti]
  (css-sarakkeet-kysymykselle ".e2e-vaihtoehto" kysymys-elementti))

(defn osuudet-kysymykselle [kysymys-elementti]
  (css-sarakkeet-kysymykselle ".e2e-osuus" kysymys-elementti))

(defn lukumaarat-kysymykselle [kysymys-elementti]
  (css-sarakkeet-kysymykselle ".e2e-vastausten-lukumaara" kysymys-elementti))

(defn ^:private lukumaarat-yhteensa-kysymykselle [kysymys-elementti]
  (w/text (w/find-element-under kysymys-elementti {:css ".report-table-amount-header"})))

(defn ^:private hae-jakaumakaavio-kysymykselle [kysymys-elementti]
  (filter w/exists? (w/find-elements-under kysymys-elementti {:css ".jakauma-kaavio"})))

(defn ^:private hae-vaittamakaavio-kysymykselle [kysymys-elementti]
  (filter w/exists? (w/find-elements-under kysymys-elementti {:css ".vaittama-kaavio"})))

(deftest kyselykertaraportti-test
  (with-webdriver
    (testing
      "etusivu:"
      (with-data {:koulutustoimija [{:ytunnus "0000000-0"}]
                  :kysely [{:kyselyid 1
                            :nimi_fi "Kysely 1"
                            :koulutustoimija "0000000-0"}]
                  :kyselykerta [{:kyselykertaid 1
                                 :kyselyid 1
                                 :nimi "Kyselykerta 1"
                                 :voimassa_alkupvm (paivamaara "2014-05-28")
                                 :voimassa_loppupvm (paivamaara "2014-05-29")}]
                  :kysymysryhma [{:kysymysryhmaid 1
                                  :koulutustoimija "0000000-0"}]
                  :kysymys [{:kysymysid 1
                             :kysymysryhmaid 1
                             :kysymys_fi "Kysymys 1"
                             :jarjestys 1
                             :vastaustyyppi "kylla_ei_valinta"}
                            {:kysymysid 2
                             :kysymysryhmaid 1
                             :kysymys_fi "Kysymys 2"
                             :jarjestys 2
                             :vastaustyyppi "kylla_ei_valinta"}
                            {:kysymysid 3
                             :kysymysryhmaid 1
                             :kysymys_fi "Kysymys 3"
                             :jarjestys 3
                             :max_vastaus 500
                             :vastaustyyppi "vapaateksti"}
                            {:kysymysid 4
                             :kysymysryhmaid 1
                             :kysymys_fi "Kysymys 4"
                             :jarjestys 4
                             :vastaustyyppi "asteikko"}
                            {:kysymysid 5
                             :kysymysryhmaid 1
                             :kysymys_fi "Kysymys 5"
                             :jarjestys 5
                             :vastaustyyppi "monivalinta"}
                            {:kysymysid 6
                             :kysymysryhmaid 1
                             :kysymys_fi "Kysymys 6: ei mukana kyselyssä"
                             :jarjestys 6
                             :vastaustyyppi "kylla_ei_valinta"}]
                  :kysely-kysymysryhma [{:kyselyid 1
                                         :kysymysryhmaid 1
                                         :jarjestys 1}]
                  :kysely-kysymys [{:kyselyid 1 :kysymysid 1}
                                   {:kyselyid 1 :kysymysid 2}
                                   {:kyselyid 1 :kysymysid 3}
                                   {:kyselyid 1 :kysymysid 4}
                                   {:kyselyid 1 :kysymysid 5}]
                  :monivalintavaihtoehto [{:kysymysid 5
                                           :jarjestys 1
                                           :teksti_fi "Jotain"}
                                          {:kysymysid 5
                                           :jarjestys 2
                                           :teksti_fi "Muuta"}]
                  :vastaajatunnus [{:vastaajatunnusid 1
                                    :kyselykertaid 1}]
                  :vastaaja [{:vastaajaid 1
                              :vastaajatunnusid 1
                              :kyselykertaid 1}
                             {:vastaajaid 2
                              :vastaajatunnusid 1
                              :kyselykertaid 1}]
                  :vastaus [{:vastausid 1
                             :kysymysid 1
                             :vastaajaid 1
                             :vaihtoehto "kylla"}
                            {:vastausid 2
                             :kysymysid 1
                             :vastaajaid 2
                             :vaihtoehto "ei"}
                            {:vastausid 3
                             :kysymysid 2
                             :vastaajaid 1
                             :vaihtoehto "ei"}
                            {:vastausid 4
                             :kysymysid 2
                             :vastaajaid 2
                             :vaihtoehto "ei"}
                            {:vastausid 5
                             :kysymysid 3
                             :vastaajaid 1
                             :vapaateksti "Vapaa teksti 1"}
                            {:vastausid 6
                             :kysymysid 3
                             :vastaajaid 2
                             :vapaateksti "Vapaa teksti 2"}
                            {:vastausid 7
                             :kysymysid 4
                             :vastaajaid 1
                             :numerovalinta 1}
                            {:vastausid 8
                             :kysymysid 4
                             :vastaajaid 2
                             :numerovalinta 2}
                            {:vastausid 9
                             :kysymysid 5
                             :vastaajaid 1
                             :numerovalinta 1}
                            {:vastausid 10
                             :kysymysid 5
                             :vastaajaid 2
                             :numerovalinta 2}]}
        (avaa (kyselykertaraportti-sivu 1))
        (testing
          "kyselykerran tiedot"
          (is (= (kyselykerran-tietojen-kentta "nimi") "Kyselykerta 1"))
          (is (= (kyselykerran-tietojen-kentta "kyselyid") "1"))
          (is (= (kyselykerran-tietojen-kentta "voimassa_alkupvm") "28.05.2014 - 29.05.2014")))
        (testing
          "raportin luontipäivä"
          (is (= (raportin-luontipvm) tanaan-kayttoliittyman-muodossa)))
        (testing
          "ensimmäisen valintakysymyksen vastausten jakauma"
          (let [kysymys (nth (kysymykset) 0)]
            (is (= (kysymyksen-teksti kysymys) "1. Kysymys 1"))
            (is (= (taulukon-kysymysteksti-kysymykselle kysymys) "Kysymys 1"))
            (is (= (vaihtoehdot-kysymykselle kysymys) ["Kyllä" "Ei"]))
            (is (= (osuudet-kysymykselle kysymys) ["50%" "50%"]))
            (is (= (lukumaarat-kysymykselle kysymys) ["1" "1"]))
            (is (= (lukumaarat-yhteensa-kysymykselle kysymys) "n=2"))
            (is (= (count (hae-jakaumakaavio-kysymykselle kysymys)) 1))))
        (testing
          "toisen valintakysymyksen vastausten jakauma"
          (let [kysymys (nth (kysymykset) 1)]
            (is (= (kysymyksen-teksti kysymys) "2. Kysymys 2"))
            (is (= (taulukon-kysymysteksti-kysymykselle kysymys) "Kysymys 2"))
            (is (= (vaihtoehdot-kysymykselle kysymys) ["Kyllä" "Ei"]))
            (is (= (osuudet-kysymykselle kysymys) ["0%" "100%"]))
            (is (= (lukumaarat-kysymykselle kysymys) ["0" "2"]))
            (is (= (lukumaarat-yhteensa-kysymykselle kysymys) "n=2"))
            (is (= (count (hae-jakaumakaavio-kysymykselle kysymys)) 1))))
        (testing
          "avoimen kysymyksen vastaukset"
          (let [kysymys (nth (kysymykset) 2)]
            (is (= (kysymyksen-teksti kysymys) "3. Kysymys 3"))
            (is (= (set (vapaatekstit-kysymykselle kysymys)) #{"Vapaa teksti 1" "Vapaa teksti 2"}))))
        (testing
          "väittämän vastausten jakauma"
          (let [kysymys (nth (kysymykset) 3)]
            (is (= (kysymyksen-teksti kysymys) "4. Kysymys 4"))
            (is (= (taulukon-kysymysteksti-kysymykselle kysymys) "Kysymys 4"))
            (is (= (vaihtoehdot-kysymykselle kysymys) ["Ei / en lainkaan" "Hieman" "Jonkin verran" "Melko paljon" "Erittäin paljon"]))
            (is (= (osuudet-kysymykselle kysymys) ["50%" "50%" "0%" "0%" "0%"]))
            (is (= (lukumaarat-kysymykselle kysymys) ["1" "1" "0" "0" "0"]))
            (is (= (lukumaarat-yhteensa-kysymykselle kysymys) "n=2"))
            (is (= (count (hae-vaittamakaavio-kysymykselle kysymys)) 1))))
        (testing
          "monivalinnan vastausten jakauma"
          (let [kysymys (nth (kysymykset) 4)]
            (is (= (kysymyksen-teksti kysymys) "5. Kysymys 5"))
            (is (= (taulukon-kysymysteksti-kysymykselle kysymys) "Kysymys 5"))
            (is (= (vaihtoehdot-kysymykselle kysymys) ["Jotain" "Muuta"]))
            (is (= (osuudet-kysymykselle kysymys) ["50%" "50%"]))
            (is (= (lukumaarat-kysymykselle kysymys) ["1" "1"]))
            (is (= (lukumaarat-yhteensa-kysymykselle kysymys) "n=2"))
            (is (= (count (hae-jakaumakaavio-kysymykselle kysymys)) 1))))
        (testing
          "sisältää vain kyselyyn valitut kysymykset"
          (is (= (count (kysymykset)) 5)))))))
