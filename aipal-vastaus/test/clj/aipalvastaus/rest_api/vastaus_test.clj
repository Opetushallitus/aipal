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

(def vastaaja->vastaus (atom {}))
(def jatkokysymysid->jatkovastaus (atom {}))

(defn tyhjaa-fake-kanta! []
  (reset! vastaaja->vastaus {})
  (reset! jatkokysymysid->jatkovastaus {}))

(defn fake-vastaus-fixture [f]
  (tyhjaa-fake-kanta!)
  (with-redefs [aipalvastaus.sql.vastaus/tallenna! (fn [vastaus]
                                                     (swap! vastaaja->vastaus update-in [(:vastaajaid vastaus)] (fnil conj #{}) vastaus))
                aipalvastaus.sql.vastaus/tallenna-jatkovastaus! (fn [vastaus]
                                                                  (swap! jatkokysymysid->jatkovastaus update-in [(:jatkokysymysid vastaus)] (fnil conj #{}) vastaus))]
    (f)))

(use-fixtures :each fake-vastaus-fixture)

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

(deftest vastauksessa-on-validi-jatkokoysymys
  (testing "Vastaukset löytyvät kysymysten joukosta"
    (let [kysymykset [{:kysymysid 1 :jatkokysymysid 2 :kylla_teksti_fi "kysymys"}]
          vastaukset [{:kysymysid 1 :jatkokysymysid 2 :jatkovastaus_kylla "vastaus"}]]
      (is (= vastaukset (v/validoi-vastaukset vastaukset kysymykset))))))

(deftest vastauksessa-on-virheellinen-jatkokoysymys
  (testing "Vastaukset löytyvät kysymysten joukosta"
    (let [kysymykset [{:kysymysid 1 :jatkokysymysid 2 :kylla_teksti_fi "kysymys"}]
          vastaukset [{:kysymysid 1 :jatkokysymysid 1 :jatkovastaus_kylla "vastaus"}]]
      (is (= nil (v/validoi-vastaukset vastaukset kysymykset))))))

(deftest monivalinta-vastaus
  (testing "Yksi valinta tuottaa yhden vastauksen"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "monivalinta"}]
          vastaukset [{:kysymysid 1 :vastaus [1]} ]]
      (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
      (is (= (@vastaaja->vastaus vastaajaid)
             #{{:kysymysid 1
                :vastaajaid vastaajaid
                :jatkovastausid nil
                :numerovalinta 1
                :vapaateksti nil
                :vaihtoehto nil}}))))
  (testing "kaksi valintaa tuottaa kaksi vastausta samalle kysymykselle"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "monivalinta"}]
          vastaukset [{:kysymysid 1 :vastaus [1 2]} ]]
      (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
      (is (= (@vastaaja->vastaus vastaajaid)
             #{{:kysymysid 1
                :vastaajaid vastaajaid
                :jatkovastausid nil
                :numerovalinta 1
                :vapaateksti nil
                :vaihtoehto nil}
               {:kysymysid 1
                :vastaajaid vastaajaid
                :jatkovastausid nil
                :numerovalinta 2
                :vapaateksti nil
                :vaihtoehto nil}})))))

(deftest kylla-ei-vastaus
  (testing "Valinta tuottaa saman vastauksen"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "kylla_ei_valinta"}]
          vastaukset [{:kysymysid 1 :vastaus ["kylla"]} ]]
      (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
      (is (= (@vastaaja->vastaus vastaajaid)
             #{{:kysymysid 1
                :vastaajaid vastaajaid
                :jatkovastausid nil
                :numerovalinta nil
                :vapaateksti nil
                :vaihtoehto "kylla"}})))))

(deftest kylla-jatkovastaus
  (testing "kyllä-jatkovastaus tallentuu"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "kylla_ei_valinta" :jatkokysymysid 2 :kylla_teksti_fi "kysymys?"}]
          vastaukset [{:kysymysid 1 :vastaus ["kylla"] :jatkokysymysid 2 :jatkovastaus_kylla 3} ]]
      (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
      (is (= (@jatkokysymysid->jatkovastaus 2)
             #{{:jatkokysymysid 2
                :kylla_asteikko 3
                :ei_vastausteksti nil}})))))

(deftest ei-jatkovastaus
  (testing "kyllä-jatkovastaus tallentuu"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "kylla_ei_valinta" :jatkokysymysid 2 :ei_teksti_fi "kysymys?"}]
          vastaukset [{:kysymysid 1 :vastaus ["kylla"] :jatkokysymysid 2 :jatkovastaus_ei "vastaus"} ]]
      (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
      (is (= (@jatkokysymysid->jatkovastaus 2)
             #{{:jatkokysymysid 2
                :kylla_asteikko nil
                :ei_vastausteksti "vastaus"}})))))

(deftest vapaateksti-vastaus
  (testing "Vastaus tallentuu vapaateksti kentään"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "vapaateksti"}]
          vastaukset [{:kysymysid 1 :vastaus ["vapaateksti"]} ]]
      (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
      (is (= (@vastaaja->vastaus vastaajaid)
             #{{:kysymysid 1
                :vastaajaid vastaajaid
                :jatkovastausid nil
                :numerovalinta nil
                :vapaateksti "vapaateksti"
                :vaihtoehto nil}})))))

(deftest asteikko-vastaus
  (testing "vastaus tallentuu numerovalinta kenttään"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "asteikko"}]
          vastaukset [{:kysymysid 1 :vastaus [2]} ]]
      (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
      (is (= (@vastaaja->vastaus vastaajaid)
             #{{:kysymysid 1
                :vastaajaid vastaajaid
                :jatkovastausid nil
                :numerovalinta 2
                :vapaateksti nil
                :vaihtoehto nil}})))))

(deftest jatkokysymyksen-kylla-vastauksen-validointi
  (testing "validi kyllä vastaus pitäisi olla validi"
    (let [kysymys {:jatkokysymysid 1 :kylla_teksti_fi "kysymys"}
          vastaus {:jatkokysymysid 1 :jatkovastaus_kylla "vastaus"}]
      (is (v/kylla-jatkovastaus-validi? vastaus kysymys))))
  (testing "väärällä id:llä ei pitäisi olla validi"
    (let [kysymys {:jatkokysymysid 1 :kylla_teksti_fi "kysymys"}
          vastaus {:jatkokysymysid 2 :jatkovastaus_kylla "vastaus"}]
      (is (not (v/kylla-jatkovastaus-validi? vastaus kysymys)))))
  (testing "ilman vastausta ei pitäisi olla validi"
    (let [kysymys {:jatkokysymysid 1 :kylla_teksti_fi "kysymys"}
          vastaus {:jatkokysymysid 1}]
      (is (not (v/kylla-jatkovastaus-validi? vastaus kysymys)))))
  (testing "ilman jatkokysymystä ei pitäisi olla validi"
    (let [kysymys {:jatkokysymysid 1}
          vastaus {:jatkokysymysid 1 :jatkovastaus_kylla "vastaus"}]
      (is (not (v/kylla-jatkovastaus-validi? vastaus kysymys))))))

(deftest jatkokysymyksen-ei-vastauksen-validointi
  (testing "validi kyllä vastaus pitäisi olla validi"
    (let [kysymys {:jatkokysymysid 1 :ei_teksti_fi "kysymys"}
          vastaus {:jatkokysymysid 1 :jatkovastaus_ei "vastaus"}]
      (is (v/ei-jatkovastaus-validi? vastaus kysymys))))
  (testing "väärällä id:llä ei pitäisi olla validi"
    (let [kysymys {:jatkokysymysid 1 :ei_teksti_fi "kysymys"}
          vastaus {:jatkokysymysid 2 :jatkovastaus_ei "vastaus"}]
      (is (not (v/ei-jatkovastaus-validi? vastaus kysymys)))))
  (testing "ilman vastausta ei pitäisi olla validi"
    (let [kysymys {:jatkokysymysid 1 :ei_teksti_fi "kysymys"}
          vastaus {:jatkokysymysid 1}]
      (is (not (v/ei-jatkovastaus-validi? vastaus kysymys)))))
  (testing "ilman jatkokysymystä ei pitäisi olla validi"
    (let [kysymys {:jatkokysymysid 1}
          vastaus {:jatkokysymysid 1 :jatkovastaus_ei "vastaus"}]
      (is (not (v/ei-jatkovastaus-validi? vastaus kysymys))))))

(deftest jatkokysymyksen-validointi
  (testing "pakollinen kysymys on validi kun siihen on vastattu"
    (let [kysymys {:jatkokysymysid 1 :ei_teksti_fi "kysymys" :pakollinen true}
          vastaus {:jatkokysymysid 1 :jatkovastaus_ei "vastaus"}]
      (is (v/jatkovastaus-validi? vastaus kysymys))))
  (testing "pakollinen kysymys ei ole validi kun siihen ei ole vastattu"
    (let [kysymys {:jatkokysymysid 1 :ei_teksti_fi "kysymys" :pakollinen true}
          vastaus {:jatkokysymysid 1}]
      (is (not (v/jatkovastaus-validi? vastaus kysymys)))))
  (testing "vapaaehtoinen kysymys on validi kun siihen on vastattu"
    (let [kysymys {:jatkokysymysid 1 :ei_teksti_fi "kysymys" :pakollinen false}
          vastaus {:jatkokysymysid 1 :jatkovastaus_ei "vastaus"}]
      (is (v/jatkovastaus-validi? vastaus kysymys))))
  (testing "vapaaehtoinen kysymys on validi kun siihen ei ole vastattu"
    (let [kysymys {:jatkokysymysid 1 :ei_teksti_fi "kysymys" :pakollinen false}
          vastaus {:vastaus "vastaus"}]
      (is (v/jatkovastaus-validi? vastaus kysymys))))
  (testing "jatkovastaus ei ole validi jos jatkokysymystä ei ole"
    (let [kysymys {:kysymysid 1}
          vastaus {:jatkokysymysid 1 :jatkovastaus_ei "vastaus"}]
      (is (not (v/jatkovastaus-validi? vastaus kysymys)))))
  (testing "jatkovastausid:tä ei saa olla olemassa jos ei ole vastausta"
    (let [kysymys {:jatkokysymysid 1 :ei_teksti_fi "kysymys"}
          vastaus {:jatkokysymysid 1}]
      (is (not (v/jatkovastaus-validi? vastaus kysymys))))))
