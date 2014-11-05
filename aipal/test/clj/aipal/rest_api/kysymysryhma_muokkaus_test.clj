(ns aipal.rest-api.kysymysryhma-muokkaus-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.rest-api.kysymysryhma :refer [paivita-kysymysryhma!]]))

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

(deftest paivita-kysymysryhma-poistaa-kysymyksen
  (let [poistettu-kysymysid (atom nil)]
    (with-redefs [aipal.arkisto.kysymysryhma/hae (constantly {:kysymysryhmaid 1
                                                              :kysymykset [{:kysymysid 2}]})
                  aipal.arkisto.kysymysryhma/poista-kysymys! (partial reset! poistettu-kysymysid)]
      (paivita-kysymysryhma! {:kysymysryhmaid 1})
      (is (= @poistettu-kysymysid 2)))))

(deftest paivita-kysymysryhma-poistaa-monivalintakysymyksen-vaihtoehdot
  (let [poista-vaihtoehdot-kysymykselta (atom nil)]
    (with-redefs [aipal.arkisto.kysymysryhma/hae
                  (constantly {:kysymysryhmaid 1
                               :kysymykset [{:kysymysid 2
                                             :vastaustyyppi "monivalinta"}]})
                  aipal.arkisto.kysymysryhma/poista-kysymyksen-monivalintavaihtoehdot!
                  (partial reset! poista-vaihtoehdot-kysymykselta)]
      (paivita-kysymysryhma! {:kysymysryhmaid 1})
      (is (= @poista-vaihtoehdot-kysymykselta 2)))))

(deftest paivita-kysymysryhma-poistaa-jatkokysymyksen
  (let [poista-jatkokysymysid (atom nil)]
    (with-redefs [aipal.arkisto.kysymysryhma/hae (constantly {:kysymysryhmaid 1
                                                              :kysymykset [{:kysymysid 2
                                                                            :jatkokysymysid 3}]})
                  aipal.arkisto.kysymysryhma/poista-jatkokysymys! (partial reset! poista-jatkokysymysid)]
      (paivita-kysymysryhma! {:kysymysryhmaid 1})
      (is (= @poista-jatkokysymysid 3)))))

(deftest paivita-kysymysryhma-lisaa-kysymyksen
 (let [lisaa-kysymys!-kutsut (atom [])]
   (with-redefs [aipal.arkisto.kysymysryhma/hae (constantly {:kysymysryhmaid 1
                                                             :kysymykset []})
                 aipal.arkisto.kysymysryhma/lisaa-kysymys! #(swap! lisaa-kysymys!-kutsut conj (vec %&))]
     (paivita-kysymysryhma! {:kysymysryhmaid 1
                             :kysymykset [{:kysymys_fi "Uusi"}]})
     (is (= (count @lisaa-kysymys!-kutsut) 1))
     (let [lisatty-kysymys (get-in @lisaa-kysymys!-kutsut [0 0])]
       (is (= (:kysymysryhmaid lisatty-kysymys) 1))
       (is (= (:kysymys_fi lisatty-kysymys) "Uusi"))))))

(deftest paivita-kysymysryhma-paivittaa-perustiedot
  (with-redefs [aipal.arkisto.kysymysryhma/hae (constantly {:kysymysryhmaid 1
                                                            :nimi_fi "Vanha"})]
    (is (= (:nimi_fi (paivita-kysymysryhma! {:kysymysryhmaid 1
                                             :nimi_fi "Uusi"}))
           "Uusi"))))
