(ns aipal.rest-api.kysymysryhma-lisaa-kysymys-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.rest-api.kysymysryhma :refer [lisaa-kysymys! paivita-kysymysryhma!]]))

(defn arkisto-stub-fixture [f]
  (with-redefs [aipal.arkisto.kysymysryhma/hae (fn [kysymysryhmaid] {})
                aipal.arkisto.kysymysryhma/poista-kysymys! (fn [kysymysid])
                aipal.arkisto.kysymysryhma/poista-kysymyksen-monivalintavaihtoehdot! (fn [kysymysid])
                aipal.arkisto.kysymysryhma/lisaa-jatkokysymys! (fn [jatkokysymys] {})
                aipal.arkisto.kysymysryhma/lisaa-kysymys! (fn [kysymys] {})
                aipal.arkisto.kysymysryhma/lisaa-monivalintavaihtoehto! (fn [vaihtoehto] {})
                aipal.arkisto.kysymysryhma/paivita! (fn [kysymysryhma] kysymysryhma)]
    (f)))

(use-fixtures :each arkisto-stub-fixture)

(deftest lisaa-kysymys-lisaa-jatkokysymyksen
  (let [lisatty-jatkokysymys (atom nil)
        lisatty-kysymys (atom nil)]
    (with-redefs [aipal.arkisto.kysymysryhma/lisaa-kysymys!
                  (partial reset! lisatty-kysymys)

                  aipal.arkisto.kysymysryhma/lisaa-jatkokysymys!
                  (fn [jatkokysymys]
                    (reset! lisatty-jatkokysymys
                            (assoc jatkokysymys :jatkokysymysid 3)))]
      (lisaa-kysymys! {:vastaustyyppi "kylla_ei_valinta"
                       :jatkokysymys {:kylla_teksti_fi "Jatkokysymys"}} 1)
      (is (= (:kylla_teksti_fi @lisatty-jatkokysymys) "Jatkokysymys"))
      (is (= (:jatkokysymysid @lisatty-kysymys) 3)))))

(deftest lisaa-kysymys-lisaa-kysymyksen
  (let [kysymys {:kysymys_fi "Kysymys"}
        lisatty-kysymys (atom nil)]
    (with-redefs [aipal.arkisto.kysymysryhma/lisaa-kysymys! (partial reset! lisatty-kysymys)]
      (lisaa-kysymys! {:kysymys_fi "Kysymys"} 1)
      (is (= (:kysymys_fi @lisatty-kysymys) "Kysymys"))
      (is (= (:kysymysryhmaid @lisatty-kysymys) 1)))))

(deftest lisaa-kysymys-lisaa-monivalintakysymyksen-vaihtoehdon
  (let [lisatyt-monivalintavaihtoehdot (atom [])]
    (with-redefs [aipal.arkisto.kysymysryhma/lisaa-kysymys!
                  (fn [kysymys] (assoc kysymys :kysymysid 2))

                  aipal.arkisto.kysymysryhma/lisaa-monivalintavaihtoehto!
                  (partial swap! lisatyt-monivalintavaihtoehdot conj)]
      (lisaa-kysymys! {:vastaustyyppi "monivalinta"
                       :monivalintavaihtoehdot [{:teksti_fi "Vaihtoehto 1"}
                                                {:teksti_fi "Vaihtoehto 2"}]}
                      1)
      (is (= [{:kysymysid 2, :teksti_fi "Vaihtoehto 1", :jarjestys 0}
              {:kysymysid 2, :teksti_fi "Vaihtoehto 2", :jarjestys 1}]
             @lisatyt-monivalintavaihtoehdot)))))
