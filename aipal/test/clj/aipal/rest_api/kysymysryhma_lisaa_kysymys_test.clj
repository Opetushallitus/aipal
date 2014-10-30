(ns aipal.rest-api.kysymysryhma-lisaa-kysymys-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.rest-api.kysymysryhma :refer [lisaa-kysymys! paivita-kysymysryhma!]]))

(def oletus-kysymys
  {})

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
  (let [kysymys (-> oletus-kysymys
                  (assoc :vastaustyyppi "kylla_ei_valinta")
                  (assoc :jatkokysymys {:kylla_teksti_fi "Jatkokysymys"}))
        lisatty-jatkokysymys (atom nil)
        lisatty-kysymys (atom nil)
        kysymysryhmaid 11
        uusi-jatkokysymysid 111]
    (with-redefs [aipal.arkisto.kysymysryhma/lisaa-kysymys! (partial reset! lisatty-kysymys)
                  aipal.arkisto.kysymysryhma/lisaa-jatkokysymys! (fn [jatkokysymys]
                                                                   (reset! lisatty-jatkokysymys
                                                                           (assoc jatkokysymys :jatkokysymysid uusi-jatkokysymysid)))]
      (lisaa-kysymys! kysymys kysymysryhmaid)
      (is (= "Jatkokysymys" (-> @lisatty-jatkokysymys
                              :kylla_teksti_fi)))
      (is (= uusi-jatkokysymysid (-> @lisatty-kysymys
                                   :jatkokysymysid))))))

(deftest lisaa-kysymys-lisaa-kysymyksen
  (let [kysymys {:kysymys_fi "Kysymys"}
        lisatty-kysymys (atom nil)
        kysymysryhmaid 11]
    (with-redefs [aipal.arkisto.kysymysryhma/lisaa-kysymys! (partial reset! lisatty-kysymys)]
      (lisaa-kysymys! kysymys kysymysryhmaid)
      (is (= {:kysymysryhmaid kysymysryhmaid :kysymys_fi "Kysymys"}
             (-> @lisatty-kysymys
               (select-keys [:kysymysryhmaid :kysymys_fi])))))))

(deftest lisaa-kysymys-lisaa-monivalintakysymyksen-vaihtoehdon
  (let [kysymys (-> oletus-kysymys
                  (assoc :vastaustyyppi "monivalinta")
                  (assoc :monivalintavaihtoehdot [{:teksti_fi "Vaihtoehto 1"} {:teksti_fi "Vaihtoehto 2"}]))
        lisatyt-monivalintavaihtoehdot (atom [])
        kysymysryhmaid 11
        uusi-kysymysid 12]
    (with-redefs [aipal.arkisto.kysymysryhma/lisaa-kysymys! (fn [kysymys] (assoc kysymys :kysymysid uusi-kysymysid))
                  aipal.arkisto.kysymysryhma/lisaa-monivalintavaihtoehto! (partial swap! lisatyt-monivalintavaihtoehdot conj)]
      (lisaa-kysymys! kysymys kysymysryhmaid)
      (is (= [{:kysymysid uusi-kysymysid, :teksti_fi "Vaihtoehto 1", :jarjestys 0}
              {:kysymysid uusi-kysymysid, :teksti_fi "Vaihtoehto 2", :jarjestys 1}]
             @lisatyt-monivalintavaihtoehdot)))))
