(ns aipal.raportti.raportti-test
  (:require [clojure.test :refer :all]
    [peridot.core :as peridot]
    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.toimiala.raportti.kysely :refer :all :as kysely-raportti]))

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
   :jarjestys 10, :vastaajien_lukumaara 5, 
   :kysymysid 7312026, :vastaustyyppi "arvosana", 
   :kysymys_sv "Hur bedömer du att ansökningsskedet lyckades som helhet?", 
   :kysymys_fi "Miten arvioit hakeutumisen onnistuneen kokonaisuutena?", 
   :keskihajonta 1.4142135623730950M})

(deftest ^:integraatio muodosta-raportteja
  (testing "bla bla bla"
    (let [kysely1 (kysely-raportti/muodosta-raportti 1 nil)
          kysely2 (kysely-raportti/muodosta-raportti 2 nil)]
    (is (= 5 (:vastaajien_lukumaara kysely2)))
    (is (= 5 (:vastaajien_maksimimaara kysely2)))
    (is (= 5 (:vastaajien_lukumaara kysely1)))
    (is (= 5 (:vastaajien_maksimimaara kysely1)))
    (is (= kysely1-arvosanatulos (last (:kysymykset (last (:raportti (muodosta-raportti 1 nil)))))))
    )))
    
