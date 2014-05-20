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
            [clj-webdriver.taxi :as w]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.util :refer :all]))

(use-fixtures :once tietokanta/muodosta-yhteys)

(defn kyselykertaraportti-sivu [kyselykertaid] (str "/fi/#/raportti/kyselykerta/" kyselykertaid))

(defn kysymykset []
  (w/find-elements (-> *ng*
                     (.repeater "kysymys in tulos.raportti"))))

(defn kysymyksen-teksti [kysymys-elementti]
  (w/text
    (w/find-element-under kysymys-elementti
                          (-> *ng*
                            (.binding "kysymys.kysymys_fi")))))

(defn ^:private hae-jakauman-sarake-kysymykselle [sarake kysymys-elementti]
  (map w/text
       (w/find-elements-under kysymys-elementti
                              (-> *ng*
                                (.repeater "alkio in kysymys.jakauma")
                                (.column sarake)))))

(defn ^:private vapaatekstit-kysymykselle [kysymys-elementti]
  (map w/text
       (w/find-elements-under kysymys-elementti
                              (-> *ng*
                                (.repeater "vastaus in kysymys.vastaukset")
                                (.column "teksti")))))

(defn vaihtoehdot-kysymykselle [kysymys-elementti]
  (hae-jakauman-sarake-kysymykselle "alkio.vaihtoehto" kysymys-elementti))

(defn osuudet-kysymykselle [kysymys-elementti]
  (hae-jakauman-sarake-kysymykselle "alkio.osuus" kysymys-elementti))

(defn lukumaarat-kysymykselle [kysymys-elementti]
  (hae-jakauman-sarake-kysymykselle "alkio.lukumaara" kysymys-elementti))

(defn ^:private hae-kaavio-kysymykselle [kysymys-elementti]
  (filter w/exists? (w/find-elements-under kysymys-elementti {:class "jakauma-kaavio"})))

(deftest kyselykertaraportti-test
  (with-webdriver
    (testing
      "etusivu:"
      (with-data {:kysely [{:kyselyid 1
                            :nimi_fi "Kysely 1"}]
                  :kyselykerta [{:kyselykertaid 1 :kyselyid 1}]
                  :kysymysryhma [{:kysymysryhmaid 1}]
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
        (avaa-aipal (kyselykertaraportti-sivu 1))
        (testing
          "ensimmäisen valintakysymyksen vastausten jakauma"
          (let [kysymys (nth (kysymykset) 0)]
            (is (= (kysymyksen-teksti kysymys) "Kysymys 1"))
            (is (= (vaihtoehdot-kysymykselle kysymys) ["Kyllä" "Ei"]))
            (is (= (osuudet-kysymykselle kysymys) ["50%" "50%"]))
            (is (= (lukumaarat-kysymykselle kysymys) ["1" "1"]))
            (is (= (count (hae-kaavio-kysymykselle kysymys)) 1))))
        (testing
          "toisen valintakysymyksen vastausten jakauma"
          (let [kysymys (nth (kysymykset) 1)]
            (is (= (kysymyksen-teksti kysymys) "Kysymys 2"))
            (is (= (vaihtoehdot-kysymykselle kysymys) ["Kyllä" "Ei"]))
            (is (= (osuudet-kysymykselle kysymys) ["0%" "100%"]))
            (is (= (lukumaarat-kysymykselle kysymys) ["0" "2"]))
            (is (= (count (hae-kaavio-kysymykselle kysymys)) 1))))
        (testing
          "avoimen kysymyksen vastaukset"
          (let [kysymys (nth (kysymykset) 2)]
            (is (= (kysymyksen-teksti kysymys) "Kysymys 3"))
            (is (= (vapaatekstit-kysymykselle kysymys) ["Vapaa teksti 1" "Vapaa teksti 2"]))))
        (testing
          "väittämän vastausten jakauma"
          (let [kysymys (nth (kysymykset) 3)]
            (is (= (kysymyksen-teksti kysymys) "Kysymys 4"))
            (is (= (vaihtoehdot-kysymykselle kysymys) ["En / ei lainkaan" "Hieman" "Jonkin verran" "Melko paljon" "Erittäin paljon"]))
            (is (= (lukumaarat-kysymykselle kysymys) ["1" "1" "0" "0" "0"]))))
        (testing
          "monivalinnan vastausten jakauma"
          (let [kysymys (nth (kysymykset) 4)]
            (is (= (kysymyksen-teksti kysymys) "Kysymys 5"))
            (is (= (vaihtoehdot-kysymykselle kysymys) ["Jotain" "Muuta"]))
            (is (= (osuudet-kysymykselle kysymys) ["50%" "50%"]))
            (is (= (lukumaarat-kysymykselle kysymys) ["1" "1"]))
            (is (= (count (hae-kaavio-kysymykselle kysymys)) 1))))
        (testing
          "sisältää vain kyselyyn valitut kysymykset"
          (is (= (count (kysymykset)) 5)))))))
