(ns aipal.arkisto.kysymysryhma-test
  (:require [clojure.test :refer :all]
            [aipal.arkisto.kysymysryhma :refer [taydenna-kysymys]]))

(deftest taydenna-kysymys-jolla-kylla-jatkokysymys
  (are [kuvaus kysymys odotettu-tulos]
       (is (= odotettu-tulos (taydenna-kysymys kysymys)) kuvaus)
       "ei jatkokysymystä" {} {}
       "kyllä jatkokysymys" {:kylla_teksti_fi "kyllä jatkokysymys"} {:jatkokysymys {:kylla_jatkokysymys true
                                                                                    :kylla_teksti_fi "kyllä jatkokysymys"
                                                                                    :ei_jatkokysymys false}}
       "ei jatkokysymys" {:ei_teksti_fi "ei jatkokysymys"} {:jatkokysymys {:kylla_jatkokysymys false
                                                                           :ei_teksti_fi "ei jatkokysymys"
                                                                           :ei_jatkokysymys true}}
       "kyllä ja ei jatkokysymykset" {:kylla_teksti_fi "kyllä jatkokysymys" :ei_teksti_fi "ei jatkokysymys"} {:jatkokysymys {:kylla_jatkokysymys true
                                                                                                                             :kylla_teksti_fi "kyllä jatkokysymys"
                                                                                                                             :ei_jatkokysymys true
                                                                                                                             :ei_teksti_fi "ei jatkokysymys"}}
       "ei jatkokysymyksen maksimipituus" {:ei_teksti_fi "ei jatkokysymys" :jatkokysymys_max_vastaus 123} {:jatkokysymys {:kylla_jatkokysymys false
                                                                                                                          :ei_teksti_fi "ei jatkokysymys"
                                                                                                                          :max_vastaus 123
                                                                                                                          :ei_jatkokysymys true}}))
