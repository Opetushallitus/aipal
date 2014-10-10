(ns aipal.rest-api.kysymysryhma_test
  (:require [aipal.rest-api.kysymysryhma :as api])
  (:use clojure.test))

(deftest kysymyksiin-lisataan-jarjestys
  (testing "kysymysten järjestys lisää järjestyksen kysymyksiin"
    (is (= (api/lisaa-jarjestys [{:kysymys "a"} {:kysymys "b"}])
           [{:kysymys "a" :jarjestys 0} {:kysymys "b" :jarjestys 1}]))))

(deftest kysymyksesta-valitaan-oikeat-kentat
  (testing "valitaan kaikki kysymykseen liittyvät kentät ja pudotetaan muut pois"
    (is (= (api/valitse-kysymyksen-kentat {:foo "bar"
                                           :pakollinen true
                                           :poistettava false
                                           :vastaustyyppi "tyyppi"
                                           :kysymys_fi "k1"
                                           :kysymys_sv "k2"
                                           :max_vastaus 10
                                           :monivalinta_max 20
                                           :jarjestys 1
                                           :jotain "muuta"})
           {:pakollinen true
            :poistettava false
            :vastaustyyppi "tyyppi"
            :kysymys_fi "k1"
            :kysymys_sv "k2"
            :max_vastaus 10
            :monivalinta_max 20
            :jarjestys 1}))))

(deftest jatkokysymyksen-luonti
  (testing "jatkokysymystä ei luoda kun kyllä/ei vastaustyypille"
    (are [odotettu saatu] (= odotettu saatu)
         {:kylla_teksti_fi "kysymys"} (api/muodosta-jatkokysymys {:vastaustyyppi "kylla_ei_valinta" :jatkokysymys {:kylla_teksti_fi "kysymys"}})
         nil (api/muodosta-jatkokysymys {:vastaustyyppi "asteikko" :jatkokysymys {:kylla_teksti_fi "kysymys"}})
         nil (api/muodosta-jatkokysymys {:vastaustyyppi "monivalinta" :jatkokysymys {:kylla_teksti_fi "kysymys"}})
         nil (api/muodosta-jatkokysymys {:vastaustyyppi "vapaateksti" :jatkokysymys {:kylla_teksti_fi "kysymys"}})))
  (testing "jatkokysymys ei ole välttämätön kyllä/ei vastaustyypillä"
    (is (nil? (api/muodosta-jatkokysymys {:vastaustyyppi "kylla_ei_valinta" :jatkokysymys nil})))))
