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

(defn css-elementin-teksti [css]
  (w/text
    (w/find-element {:css css})))

(defn kyselykerran-tietojen-kentta [kentta]
  (css-elementin-teksti (str ".e2e-yhteenveto-" kentta)))

(defn raportin-luontipvm []
  (css-elementin-teksti ".e2e-luontipvm"))

(defn paivamaara [paivamaara-iso-muodossa]
  (time-format/parse-local-date (time-format/formatters :year-month-day)
                                paivamaara-iso-muodossa))
(def tanaan-pvm (time/today))
(def tanaan-kayttoliittyman-muodossa (time-format/unparse-local-date (time-format/formatter "dd.MM.yyyy")
                                                                      tanaan-pvm))

(defn kysymykset []
  (w/find-elements {:css ".e2e-kysymysryhma-kysymykset-repeat"}))

(defn kysymyksen-teksti [kysymys-elementti]
  (w/text
    (w/find-element-under kysymys-elementti {:css ".e2e-kysymys-otsikko"})))

(defn ^:private taulukon-kysymysteksti-kysymykselle [kysymys-elementti]
  (w/text (w/find-element-under kysymys-elementti {:css ".e2e-kysymys-taulukossa"})))

(defn ^:private vapaatekstit-kysymykselle [kysymys-elementti]
  (map w/text
       (w/find-elements-under kysymys-elementti {:css ".e2e-kysymys-vapaatekstivastaukset-repeat"})))

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
                  :rooli_organisaatio [{:organisaatio "0000000-0"
                                      :rooli "OPL-KAYTTAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}]
                  :kysely [{:kyselyid 1
                            :nimi_fi "Kysely 1"
                            :koulutustoimija "0000000-0"}]
                  :kyselykerta [{:kyselykertaid 1
                                 :kyselyid 1
                                 :nimi "Kyselykerta 1"
                                 :voimassa_alkupvm (paivamaara "2014-05-28")
                                 :voimassa_loppupvm (paivamaara "2014-05-29")}]
                  :kysymysryhma [{:kysymysryhmaid 10
                                  :koulutustoimija "0000000-0"}]
                  :kysymys [{:kysymysid 1
                             :kysymysryhmaid 10
                             :kysymys_fi "Kysymys 1"
                             :jarjestys 0
                             :vastaustyyppi "kylla_ei_valinta"}
                            {:kysymysid 2
                             :kysymysryhmaid 10
                             :kysymys_fi "Kysymys 2"
                             :jarjestys 1
                             :vastaustyyppi "kylla_ei_valinta"}
                            {:kysymysid 3
                             :kysymysryhmaid 10
                             :kysymys_fi "Kysymys 3"
                             :jarjestys 2
                             :max_vastaus 500
                             :vastaustyyppi "vapaateksti"}
                            {:kysymysid 4
                             :kysymysryhmaid 10
                             :kysymys_fi "Kysymys 4"
                             :jarjestys 3
                             :vastaustyyppi "asteikko"}
                            {:kysymysid 5
                             :kysymysryhmaid 10
                             :kysymys_fi "Kysymys 5"
                             :jarjestys 4
                             :vastaustyyppi "monivalinta"}
                            {:kysymysid 6
                             :kysymysryhmaid 10
                             :kysymys_fi "Kysymys 6"
                             :jarjestys 5
                             :vastaustyyppi "likert_asteikko"}
                            {:kysymysid 99
                             :kysymysryhmaid 10
                             :kysymys_fi "Kysymys 99: ei mukana kyselyssä"
                             :jarjestys 99
                             :vastaustyyppi "kylla_ei_valinta"}]
                  :kysely-kysymysryhma [{:kyselyid 1
                                         :kysymysryhmaid 10
                                         :jarjestys 1}]
                  :kysely-kysymys [{:kyselyid 1 :kysymysid 1}
                                   {:kyselyid 1 :kysymysid 2}
                                   {:kyselyid 1 :kysymysid 3}
                                   {:kyselyid 1 :kysymysid 4}
                                   {:kyselyid 1 :kysymysid 5}
                                   {:kyselyid 1 :kysymysid 6}]
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
                  :vastaus [{:vastausid 1001
                             :kysymysid 1
                             :vastaajaid 1
                             :vaihtoehto "kylla"}
                            {:vastausid 1002
                             :kysymysid 1
                             :vastaajaid 2
                             :vaihtoehto "ei"}
                            {:vastausid 1003
                             :kysymysid 2
                             :vastaajaid 1
                             :vaihtoehto "ei"}
                            {:vastausid 1004
                             :kysymysid 2
                             :vastaajaid 2
                             :vaihtoehto "ei"}
                            {:vastausid 1005
                             :kysymysid 3
                             :vastaajaid 1
                             :vapaateksti "Vapaa teksti 1"}
                            {:vastausid 1006
                             :kysymysid 3
                             :vastaajaid 2
                             :vapaateksti "Vapaa teksti 2"}
                            {:vastausid 1007
                             :kysymysid 4
                             :vastaajaid 1
                             :numerovalinta 1}
                            {:vastausid 1008
                             :kysymysid 4
                             :vastaajaid 2
                             :numerovalinta 2}
                            {:vastausid 1009
                             :kysymysid 5
                             :vastaajaid 1
                             :numerovalinta 1}
                            {:vastausid 1010
                             :kysymysid 5
                             :vastaajaid 2
                             :numerovalinta 2}
                            {:vastausid 1011
                             :kysymysid 6
                             :vastaajaid 1
                             :numerovalinta 3}
                            {:vastausid 1012
                             :kysymysid 6
                             :vastaajaid 2
                             :numerovalinta 4}]}
        (avaa (kyselykertaraportti-sivu 1))
        (testing
          "kyselykerran tiedot"
          (is (= (kyselykerran-tietojen-kentta "kyselykerta") "Kyselykerta 1")))
        (testing
          "raportin luontipäivä"
          (is (= (subs (raportin-luontipvm) 0 10) tanaan-kayttoliittyman-muodossa))
          (is (= (count (raportin-luontipvm)) 16))) ; kellonaika on mukana. Ei voi vastaavalla tavalla testata kuin päivämäärää
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
          "Likert-asteikon väittämän vastausten jakauma"
          (let [kysymys (nth (kysymykset) 5)]
            (is (= (kysymyksen-teksti kysymys) "6. Kysymys 6"))
            (is (= (taulukon-kysymysteksti-kysymykselle kysymys) "Kysymys 6"))
            (is (= (vaihtoehdot-kysymykselle kysymys) ["Täysin eri mieltä"
                                                       "Jokseenkin eri mieltä"
                                                       "Ei samaa eikä eri mieltä"
                                                       "Jokseenkin samaa mieltä"
                                                       "Täysin samaa mieltä"]))
            (is (= (osuudet-kysymykselle kysymys) ["0%" "0%" "50%" "50%" "0%"]))
            (is (= (lukumaarat-kysymykselle kysymys) ["0" "0" "1" "1" "0"]))
            (is (= (lukumaarat-yhteensa-kysymykselle kysymys) "n=2"))
            (is (= (count (hae-vaittamakaavio-kysymykselle kysymys)) 1))))
        (testing
          "sisältää vain kyselyyn valitut kysymykset"
          (is (= (count (kysymykset)) 6)))))))
