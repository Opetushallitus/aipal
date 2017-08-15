(ns aipal.raportti.raportti-test
  (:require [clojure.test :refer :all]
    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.toimiala.raportti.kysely :as kysely-raportti]
    [aipal.toimiala.raportti.valtakunnallinen :as valtakunnallinen-raportti]))

(use-fixtures :each tietokanta-fixture)

;                :kysymysryhmaid (:kysymysryhmaid ryhma)
;                 :jarjestys (:jarjestys ryhma)})))

(def ^:private kysely1-arvosanatulos
  {:jakauma '({:vaihtoehto-avain "1", :lukumaara 0, :osuus 0} 
              {:vaihtoehto-avain "2", :lukumaara 1, :osuus 20}
              {:vaihtoehto-avain "3", :lukumaara 1, :osuus 20}
              {:vaihtoehto-avain "4", :lukumaara 0, :osuus 0}
              {:vaihtoehto-avain "5", :lukumaara 3, :osuus 60}),
   :eos_vastaus_sallittu nil, :keskiarvo 4.0000000000000000M, 
   :kysymys_en nil,
   :jarjestys 10, :vastaajien_lukumaara 5, 
   :kysymysid 7312026, :vastaustyyppi "arvosana", 
   :kysymys_sv "Hur bedömer du att ansökningsskedet lyckades som helhet?", 
   :kysymys_fi "Miten arvioit hakeutumisen onnistuneen kokonaisuutena?", 
   :keskihajonta 1.4142135623730950M})

;(deftest ^:integraatio muodosta-raportteja
;  (testing "bla bla bla"
;    (let [kysely1 (kysely-raportti/muodosta-raportti -1 nil)
;          kysely2 (kysely-raportti/muodosta-raportti -2 nil)]
;    (is (= 5 (:vastaajien_lukumaara kysely2)))
;    (is (= 5 (:vastaajien_maksimimaara kysely2)))
;    (is (= 5 (:vastaajien_lukumaara kysely1)))
;    (is (= 5 (:vastaajien_maksimimaara kysely1)))
;    (is (= kysely1-arvosanatulos (last (:kysymykset (last (:raportti kysely1))))))
;    )))

(def valtakunnallinen-raportti-params 
 {:taustakysymysryhmaid "3341885", :tyyppi "vertailu", :tutkintorakennetaso "tutkinto",
  :kysymykset {7312027 {:monivalinnat {}}, 
               7312028 {:monivalinnat {}}, 7312029 {:monivalinnat {}}, 
               7312030 {:monivalinnat {}}, 7312031 {:monivalinnat {}}, 
               7312032 {:monivalinnat {}}, 7312033 {:monivalinnat {}}}, 
  :vertailujakso_alkupvm nil, :vertailujakso_loppupvm nil})

;(deftest ^:integraatio muodosta-valtakunnallinen-vertailu
;  (testing "bla bla bla"
;    (let [ei-rajauksia (valtakunnallinen-raportti/muodosta valtakunnallinen-raportti-params)
;          tutkinto2 (valtakunnallinen-raportti/muodosta (assoc valtakunnallinen-raportti-params :tutkinnot ["X00002"]))
;          tutkinto1 (valtakunnallinen-raportti/muodosta (assoc valtakunnallinen-raportti-params :tutkinnot ["X00001"]))]
;      (is (= 16 (:vastaajien_lukumaara ei-rajauksia)))
;      (is (= 16 (:vastaajien_maksimimaara ei-rajauksia)))
;      (is (= 4 (:vastaajien_lukumaara tutkinto2)))
;      (is (= 4 (:vastaajien_maksimimaara tutkinto2)))
;      (is (= 7 (:vastaajien_lukumaara tutkinto1)))
;      (is (= 7 (:vastaajien_maksimimaara tutkinto1))))))
      
     
