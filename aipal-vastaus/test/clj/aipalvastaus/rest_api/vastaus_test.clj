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
          vastaukset [{:kysymysid 1 :vastaus [1]}]]
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
          vastaukset [{:kysymysid 1 :vastaus [1 2]}]]
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
          vastaukset [{:kysymysid 1 :vastaus ["kylla"]}]]
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
        vastaukset [{:kysymysid 1 :vastaus ["kylla"] :jatkokysymysid 2 :jatkovastaus_kylla 3}]]
    (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
    (is (= (@jatkokysymysid->jatkovastaus 2)
           #{{:jatkokysymysid 2
              :kylla_asteikko 3
              :ei_vastausteksti nil}}))))

(deftest ei-jatkovastaus-tallentuu
  (let [kysymykset [{:kysymysid 1 :vastaustyyppi "kylla_ei_valinta" :jatkokysymysid 2 :ei_teksti_fi "kysymys?"}]
        vastaukset [{:kysymysid 1 :vastaus ["kylla"] :jatkokysymysid 2 :jatkovastaus_ei "vastaus"}]]
    (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
    (is (= (@jatkokysymysid->jatkovastaus 2)
           #{{:jatkokysymysid 2
              :kylla_asteikko nil
              :ei_vastausteksti "vastaus"}}))))

(deftest vapaateksti-vastaus
  (testing "Vastaus tallentuu vapaateksti kentään"
    (let [kysymykset [{:kysymysid 1 :vastaustyyppi "vapaateksti"}]
          vastaukset [{:kysymysid 1 :vastaus ["vapaateksti"]}]]
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
          vastaukset [{:kysymysid 1 :vastaus [2]}]]
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
          vastaukset [{:kysymysid 1 :vastaus [2]}]]
      (v/tallenna-vastaukset! vastaukset vastaajaid kysymykset)
      (is (= (@vastaaja->vastaus vastaajaid)
             #{{:kysymysid 1
                :vastaajaid vastaajaid
                :jatkovastausid nil
                :numerovalinta 2
                :vapaateksti nil
                :vaihtoehto nil
                :en_osaa_sanoa false}})))))
(deftest pakollisiin-kysymyksiin-on-vastaukset
  (let [kysymykset [{:kysymysid 1
                     :pakollinen true
                     :vastaustyyppi "arvosana"}]]
    (testing "pakollisiin kysymyksiin on vastaukset"
      (is (some? (v/validoi-vastaukset [{:kysymysid 1 :vastaus [1]}] kysymykset)))
      (is (nil? (v/validoi-vastaukset [] kysymykset))))))

(deftest monivalintavastaukset-tasmaavat-monivalintavaihtoehtoihin
  (let [kysymykset [{:kysymysid 1
                     :vastaustyyppi "monivalinta"
                     :eos_vastaus_sallittu true
                     :monivalintavaihtoehdot [{:monivalintavaihtoehtoid 12345 :jarjestys 1}, {:monivalintavaihtoehtoid 23456 :jarjestys 2}]}]]
    (testing "monivalinta-kysymysten vastaukset viittaavat oikeisiin monivalintavaihtoehtoihin"
      (is (some? (v/validoi-vastaukset [{:kysymysid 1 :vastaus ["EOS"]}] kysymykset)))
      (is (some? (v/validoi-vastaukset [{:kysymysid 1 :vastaus [1 2]}] kysymykset)))
      (is (nil? (v/validoi-vastaukset [{:kysymysid 1 :vastaus [-1]}] kysymykset)))
      (is (nil? (v/validoi-vastaukset [{:kysymysid 1 :vastaus [9999]}] kysymykset))))))

(deftest numerovalintavastaukset-tasmaavat-vaihtoehtoihin
  (let [kysymykset [{:kysymysid 1 :vastaustyyppi "arvosana"}
                    {:kysymysid 2 :vastaustyyppi "asteikko"}
                    {:kysymysid 3 :vastaustyyppi "likert_asteikko"}
                    {:kysymysid 4 :vastaustyyppi "arvosana" :eos_vastaus_sallittu true}]]
    (testing "numerovalinta-vastaukset täsmäävät arvosana/asteikko/likert_asteikko -vaihtoehtoihin"
      (is (nil? (v/validoi-vastaukset [{:kysymysid 1 :vastaus [-1]}] kysymykset)))
      (is (nil? (v/validoi-vastaukset [{:kysymysid 2 :vastaus [6]}] kysymykset)))
      (is (nil? (v/validoi-vastaukset [{:kysymysid 3 :vastaus [-1]}] kysymykset)))
      (is (some? (v/validoi-vastaukset [{:kysymysid 4 :vastaus [3]}] kysymykset)))
      (is (some? (v/validoi-vastaukset [{:kysymysid 4 :vastaus ["EOS"]}] kysymykset))))))

(deftest eos-vastaukset
  (let [vastaustyypit ["arvosana" "asteikko" "likert_asteikko" "kylla_ei_valinta" "monivalinta"]
        luo-eos-kysymykset (fn [eos_vastaus_sallittu]
                             (for [[kysymysid vastaustyyppi] (map vector (iterate inc 1) vastaustyypit)]
                               {:kysymysid kysymysid :vastaustyyppi vastaustyyppi :eos_vastaus_sallittu eos_vastaus_sallittu}))]
    (testing "EOS-vastausta ei saa antaa kysymyksille joissa EOS-vastaus ei ole sallittu"
      (let [kysymykset (luo-eos-kysymykset false)]
        (doseq [kysymys kysymykset]
          (testing (str ", vastaustyyppi: " (:vastaustyyppi kysymys))
            (is (nil?
                  (v/validoi-vastaukset [{:kysymysid (:kysymysid kysymys) :vastaus ["EOS"]}] kysymykset)))))))
    (testing "EOS-vastauksen saa antaa kysymyksille joissa EOS-vastaus on sallittu"
      (let [kysymykset (luo-eos-kysymykset true)]
        (doseq [kysymys kysymykset]
          (testing (str ", vastaustyyppi: " (:vastaustyyppi kysymys))
            (is (some?
                  (v/validoi-vastaukset [{:kysymysid (:kysymysid kysymys) :vastaus ["EOS"]}] kysymykset)))))))))
