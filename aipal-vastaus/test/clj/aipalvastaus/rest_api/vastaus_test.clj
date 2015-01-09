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
  (let [kysymykset [{:kysymysid 1} {:kysymysid 2} {:kysymysid 3}]
        vastaukset [{:kysymysid 1} {:kysymysid 3}]]
    (is (= vastaukset (v/validoi-vastaukset vastaukset kysymykset)))))

(deftest vastaukset-eivat-tasmaa-kysymyksiin
  (let [kysymykset [{:kysymysid 1} {:kysymysid 2} {:kysymysid 3}]
        vastaukset [{:kysymysid 1} {:kysymysid 4}]]
    (is (= nil (v/validoi-vastaukset vastaukset kysymykset)))))

(deftest vastauksessa-on-validi-jatkokysymys
  (let [kysymykset [{:kysymysid 1 :jatkokysymysid 2 :kylla_kysymys true :kylla_teksti_fi "kysymys"}]
        vastaukset [{:kysymysid 1 :jatkokysymysid 2 :jatkovastaus_kylla "vastaus"}]]
    (is (= vastaukset (v/validoi-vastaukset vastaukset kysymykset)))))

(deftest vastauksessa-on-virheellinen-jatkokoysymys
  (let [kysymykset [{:kysymysid 1 :jatkokysymysid 2 :kylla_kysymys true :kylla_teksti_fi "kysymys"}]
        vastaukset [{:kysymysid 1 :jatkokysymysid 1 :jatkovastaus_kylla "vastaus"}]]
    (is (= nil (v/validoi-vastaukset vastaukset kysymykset)))))

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
                :vaihtoehto nil
                :en_osaa_sanoa false}}))))
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
                :vaihtoehto nil
                :en_osaa_sanoa false}
               {:kysymysid 1
                :vastaajaid vastaajaid
                :jatkovastausid nil
                :numerovalinta 2
                :vapaateksti nil
                :vaihtoehto nil
                :en_osaa_sanoa false}})))))

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
                :vaihtoehto "kylla"
                :en_osaa_sanoa false}})))))

(deftest kylla-jatkovastaus-tallentuu
  (let [kysymykset [{:kysymysid 1 :vastaustyyppi "kylla_ei_valinta" :jatkokysymysid 2 :kylla_teksti_fi "kysymys?"}]
        vastaukset [{:kysymysid 1 :vastaus ["kylla"] :jatkokysymysid 2 :jatkovastaus_kylla 3} ]]
    (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
    (is (= (@jatkokysymysid->jatkovastaus 2)
           #{{:jatkokysymysid 2
              :kylla_asteikko 3
              :ei_vastausteksti nil}}))))

(deftest ei-jatkovastaus-tallentuu
  (let [kysymykset [{:kysymysid 1 :vastaustyyppi "kylla_ei_valinta" :jatkokysymysid 2 :ei_teksti_fi "kysymys?"}]
        vastaukset [{:kysymysid 1 :vastaus ["kylla"] :jatkokysymysid 2 :jatkovastaus_ei "vastaus"} ]]
    (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
    (is (= (@jatkokysymysid->jatkovastaus 2)
           #{{:jatkokysymysid 2
              :kylla_asteikko nil
              :ei_vastausteksti "vastaus"}}))))

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
                :vaihtoehto nil
                :en_osaa_sanoa false}})))))

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
                :vaihtoehto nil
                :en_osaa_sanoa false}})))))

(deftest likert-asteikko-vastaus
  (testing "vastaus tallentuu numerovalinta kenttään"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "likert_asteikko"}]
          vastaukset [{:kysymysid 1 :vastaus [2]} ]]
      (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
      (is (= (@vastaaja->vastaus vastaajaid)
             #{{:kysymysid 1
                :vastaajaid vastaajaid
                :jatkovastausid nil
                :numerovalinta 2
                :vapaateksti nil
                :vaihtoehto nil
                :en_osaa_sanoa false}})))))

(deftest jatkokysymyksen-kylla-vastauksen-validointi
  (are [tulos vastaus kysymys] (= tulos (v/kylla-jatkovastaus-validi? vastaus kysymys))
       true {:jatkokysymysid 1 :jatkovastaus_kylla "vastaus"} {:jatkokysymysid 1 :kylla_kysymys true :kylla_teksti_fi "kysymys"} ; validi kyllä vastaus
       false {:jatkokysymysid 1 :jatkovastaus_kylla "vastaus" :jatkovastaus_ei "vastaus"} {:jatkokysymysid 1 :kylla_kysymys true :kylla_teksti_fi "kysymys"} ; molemmat vastaukset
       false {:jatkokysymysid 2 :jatkovastaus_kylla "vastaus"} {:jatkokysymysid 1 :kylla_kysymys true :kylla_teksti_fi "kysymys"} ; vastauksessa väärä jatkokysymys
       false {:jatkokysymysid 1} {:jatkokysymysid 1 :kylla_kysymys true :kylla_teksti_fi "kysymys"} ; ei vastausta mutta jatkokysymysid
       false {:jatkokysymysid 1 :jatkovastaus_kylla "vastaus"} {} ; vastaus olemattomaan jatkokysymykseen
       ))

(deftest jatkokysymyksen-ei-vastauksen-validointi
  (are [tulos vastaus kysymys] (= tulos (v/ei-jatkovastaus-validi? vastaus kysymys))
       true {:jatkokysymysid 1 :jatkovastaus_ei "vastaus"} {:jatkokysymysid 1 :ei_kysymys true :ei_teksti_fi "kysymys"} ; validi ei vastaus
       false {:jatkokysymysid 1 :jatkovastaus_ei "vastaus" :jatkovastaus_kylla "vastaus"} {:jatkokysymysid 1 :ei_kysymys true :ei_teksti_fi "kysymys"} ; molemmat vastaukset
       false {:jatkokysymysid 2 :jatkovastaus_ei "vastaus"} {:jatkokysymysid 1 :ei_kysymys true :ei_teksti_fi "kysymys"} ; vastauksessa väärä jatkokysymys
       false {:jatkokysymysid 1} {:jatkokysymysid 1 :ei_kysymys true :ei_teksti_fi "kysymys"} ; ei vastausta mutta jatkokysymysid
       false {:jatkokysymysid 1 :jatkovastaus_ei "vastaus"} {} ; vastaus olemattomaan jatkokysymykseen
       ))

(deftest jatkokysymyksen-validointi
  (are [tulos vastaus kysymys] (= tulos (v/jatkovastaus-validi? vastaus kysymys))
       true {:jatkokysymysid 1 :jatkovastaus_ei "vastaus"} {:jatkokysymysid 1 :ei_kysymys true :ei_teksti_fi "kysymys" :pakollinen true} ; jatkokysymys validi kun siihen vastattu ja kysymys on pakollinen
       true {:jatkokysymysid 1 :jatkovastaus_ei "vastaus"} {:jatkokysymysid 1 :ei_kysymys true :ei_teksti_fi "kysymys" :pakollinen false} ; jatkokysymys validi kun siihen vastattu ja kysymys ei ole pakollinen
       true {} {:jatkokysymysid 1 :ei_kysymys true :ei_teksti_fi "kysymys" :pakollinen true} ; jatkokysymys on validi kun ei ole vastattu ja kysymys on pakollinen
       true {} {:jatkokysymysid 1 :ei_kysymys true :ei_teksti_fi "kysymys" :pakollinen false} ; jatkokysymys on validi kun ei ole vastattu ja kysymys ei ole pakollinen
       true {} {} ; jatkokysymys on validi kun ei jatkokysymykseen ei ole vastattu ja ei ole jatkokysymystä
       false {:jatkokysymysid 1} {} ; jatkokysymykseen ei saa olla vastausta jos ei ole jatkokysymystä
       false {:jatkokysymysid 1 :jatkovastaus_kylla "vastaus" :jatkovastaus_ei "vastaus"} {:jatkokysymysid 1 :ei_kysymys true :ei_teksti_fi "kysymys" :kylla_kysymys true :kylla_teksti_fi "kysymys"} ; jatkokysymykseen ei saa olla vastattu molempiin
       ))

(deftest jatkovastaus-tallennetaan
  (testing "jatkovastaus tallennetaan jos jatkokysymysid löytyy"
    (v/tallenna-jatkovastaus! {:jatkokysymysid 1 :jatkovastaus_ei "vastaus"})
    (is (= (@jatkokysymysid->jatkovastaus 1)
           #{{:jatkokysymysid 1
              :kylla_asteikko nil
              :ei_vastausteksti "vastaus"}}))))

(deftest jatkovastausta-ei-tallenneta
  (testing "jatkovastausta ei tallenneta jos jatkokysymysid:tä ei löydy"
    (v/tallenna-jatkovastaus! {:jatkovastaus_kylla "vastaus"})
    (is (= @jatkokysymysid->jatkovastaus {}))))
