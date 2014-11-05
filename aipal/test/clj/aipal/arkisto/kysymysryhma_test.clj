(ns aipal.arkisto.kysymysryhma-test
  (:require [clojure.test :refer :all]
            [aipal.arkisto.kysymysryhma :refer [taydenna-kysymys]]))

(deftest taydenna-kysymys-jolla-kylla-jatkokysymys
  (are [kuvaus kysymys odotettu-tulos]
       (testing kuvaus
                (is (= (taydenna-kysymys kysymys) odotettu-tulos)))
       "kysymyksellä ei ole jatkokysymystä"
       {}
       {}

       "kysymyksellä on kyllä-jatkokysymys"
       {:kylla_teksti_fi "kyllä jatkokysymys"}
       {:jatkokysymys {:kylla_jatkokysymys true
                       :kylla_teksti_fi "kyllä jatkokysymys"
                       :ei_jatkokysymys false}}

       "kysymyksellä on ei-jatkokysymys"
       {:ei_teksti_fi "ei jatkokysymys"}
       {:jatkokysymys {:kylla_jatkokysymys false
                       :ei_teksti_fi "ei jatkokysymys"
                       :ei_jatkokysymys true}}

       "kysymyksellä on kyllä-ja ei-jatkokysymykset"
       {:kylla_teksti_fi "kyllä jatkokysymys"
        :ei_teksti_fi "ei jatkokysymys"}
       {:jatkokysymys {:kylla_jatkokysymys true
                       :kylla_teksti_fi "kyllä jatkokysymys"
                       :ei_jatkokysymys true
                       :ei_teksti_fi "ei jatkokysymys"}}

       "kysymyksen ei-jatkokysymyksen vastauksen maksimipituus asetetaan"
       {:ei_teksti_fi "ei jatkokysymys"
        :jatkokysymys_max_vastaus 123}
       {:jatkokysymys {:kylla_jatkokysymys false
                       :ei_teksti_fi "ei jatkokysymys"
                       :max_vastaus 123
                       :ei_jatkokysymys true}}))
