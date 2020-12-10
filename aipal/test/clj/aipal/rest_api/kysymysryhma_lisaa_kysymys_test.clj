(ns aipal.rest-api.kysymysryhma-lisaa-kysymys-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.arkisto.kysymysryhma :as arkisto]
            [aipal.rest-api.kysymysryhma :refer [lisaa-kysymys!]]))

(defn arkisto-stub-fixture [f]
  (with-redefs [arkisto/hae (fn [kysymysryhmaid] {})
                arkisto/poista-kysymys! (fn [kysymysid])
                arkisto/poista-kysymyksen-monivalintavaihtoehdot! (fn [kysymysid])
                arkisto/lisaa-kysymys! (fn [kysymys] {})
                arkisto/lisaa-monivalintavaihtoehto! (fn [vaihtoehto] {})
                arkisto/paivita! (fn [kysymysryhma] kysymysryhma)]
    (f)))

(use-fixtures :each arkisto-stub-fixture)

(deftest lisaa-kysymys-lisaa-kysymyksen
  (let [kysymys {:kysymys_fi "Kysymys"}
        lisatty-kysymys (atom nil)]
    (with-redefs [arkisto/lisaa-kysymys! (partial reset! lisatty-kysymys)]
      (lisaa-kysymys! {:kysymys_fi "Kysymys"} 1)
      (is (= (:kysymys_fi @lisatty-kysymys) "Kysymys"))
      (is (= (:kysymysryhmaid @lisatty-kysymys) 1)))))

(deftest lisaa-kysymys-lisaa-monivalintakysymyksen-vaihtoehdon
  (let [lisatyt-monivalintavaihtoehdot (atom [])]
    (with-redefs [arkisto/lisaa-kysymys!
                  (fn [kysymys] (assoc kysymys :kysymysid 2))

                  arkisto/lisaa-monivalintavaihtoehto!
                  (partial swap! lisatyt-monivalintavaihtoehdot conj)]
      (lisaa-kysymys! {:vastaustyyppi "monivalinta"
                       :monivalintavaihtoehdot [{:teksti_fi "Vaihtoehto 1"}
                                                {:teksti_fi "Vaihtoehto 2"}]}
                      1)
      (is (= [{:kysymysid 2, :teksti_fi "Vaihtoehto 1", :jarjestys 0}
              {:kysymysid 2, :teksti_fi "Vaihtoehto 2", :jarjestys 1}]
             @lisatyt-monivalintavaihtoehdot)))))
