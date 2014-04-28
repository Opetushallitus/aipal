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

(defn sivun-sisalto []
  (w/text (w/find-element {:css "body"})))

(defn kysymykset []
  (w/find-elements (-> *ng*
                     (.repeater "kysymys in tulos.raportti"))))

(defn kysymysten-tekstit []
  (map w/text (w/find-elements (-> *ng*
                                 (.repeater "kysymys in tulos.raportti")
                                 (.column "kysymys.kysymys_fi")))))

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
                                (.repeater "vapaateksti in kysymys.vastaukset")))))

(defn vaihtoehdot-kysymykselle [kysymys-elementti]
  (hae-jakauman-sarake-kysymykselle "alkio.vaihtoehto" kysymys-elementti))

(defn lukumaarat-kysymykselle [kysymys-elementti]
  (hae-jakauman-sarake-kysymykselle "alkio.lukumaara" kysymys-elementti))

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
                             :vastaustyyppi "vapaateksti"}]
                  :kysely-kysymysryhma [{:kyselyid 1
                                         :kysymysryhmaid 1
                                         :jarjestys 1}]
                  :kysely-kysymys [{:kyselyid 1 :kysymysid 1}
                                   {:kyselyid 1 :kysymysid 2}
                                   {:kyselyid 1 :kysymysid 3}]
                  :vastaustunnus [{:vastaustunnusid 1
                                   :kyselykertaid 1}
                                  {:vastaustunnusid 2
                                   :kyselykertaid 1}]
                  :vastaus [{:vastausid 1
                             :kysymysid 1
                             :vastaustunnusid 1
                             :vaihtoehto "kylla"}
                            {:vastausid 2
                             :kysymysid 1
                             :vastaustunnusid 2
                             :vaihtoehto "ei"}
                            {:vastausid 3
                             :kysymysid 2
                             :vastaustunnusid 1
                             :vaihtoehto "ei"}
                            {:vastausid 4
                             :kysymysid 2
                             :vastaustunnusid 2
                             :vaihtoehto "ei"}
                            {:vastausid 5
                             :kysymysid 3
                             :vastaustunnusid 1
                             :vapaateksti "Vapaa teksti 1"}
                            {:vastausid 6
                             :kysymysid 3
                             :vastaustunnusid 2
                             :vapaateksti "Vapaa teksti 2"}]}
        (avaa-aipal (kyselykertaraportti-sivu 1))
        (testing
          "sisältää kysymykset"
          (is (= (kysymysten-tekstit) ["Kysymys 1" "Kysymys 2" "Kysymys 3"])))
        (testing
          "ensimmäisen valintakysymyksen vastausten jakauma"
          (let [kysymys (nth (kysymykset) 0)]
            (is (= (vaihtoehdot-kysymykselle kysymys) ["kyllä" "ei"]))
            (is (= (lukumaarat-kysymykselle kysymys) ["1" "1"]))))
        (testing
          "toisen valintakysymyksen vastausten jakauma"
          (let [kysymys (nth (kysymykset) 1)]
            (is (= (vaihtoehdot-kysymykselle kysymys) ["kyllä" "ei"]))
            (is (= (lukumaarat-kysymykselle kysymys) ["0" "2"]))))
        (testing
          "avoimen kysymyksen vastaukset"
          (let [kysymys (nth (kysymykset) 2)]
            (is (= (vapaatekstit-kysymykselle kysymys) ["Vapaa teksti 1" "Vapaa teksti 2"]))))))))
