(ns aipal.rest-api.kysymysryhma-muokkaus-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.rest-api.kysymysryhma :refer [paivita-kysymysryhma!]]))

(def oletus-kysymysryhma
  {:kysymysryhmaid 1 :nimi_fi "Nimi (fi)" :nimi_sv "Nimi (sv)"})

(def oletus-kysymys
  {:kysymysid 11})

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
  (let [kysymysryhma (-> oletus-kysymysryhma
                       (assoc :kysymykset [oletus-kysymys]))
        poistettu-kysymysid (atom nil)]
    (with-redefs [aipal.arkisto.kysymysryhma/hae (constantly kysymysryhma)
                  aipal.arkisto.kysymysryhma/poista-kysymys! (partial reset! poistettu-kysymysid)]
      (paivita-kysymysryhma! kysymysryhma)
      (is (= (:kysymysid oletus-kysymys) @poistettu-kysymysid)))))

(deftest paivita-kysymysryhma-poistaa-monivalintakysymyksen-vaihtoehdot
  (let [kysymys (-> oletus-kysymys
                  (assoc :vastaustyyppi "monivalinta"))
        kysymysryhma (-> oletus-kysymysryhma
                       (assoc :kysymykset [kysymys]))
        poista-vaihtoehdot-kysymykselta (atom nil)]
    (with-redefs [aipal.arkisto.kysymysryhma/hae (constantly kysymysryhma)
                  aipal.arkisto.kysymysryhma/poista-kysymyksen-monivalintavaihtoehdot! (partial reset! poista-vaihtoehdot-kysymykselta)]
      (paivita-kysymysryhma! kysymysryhma)
      (is (= (:kysymysid kysymys) @poista-vaihtoehdot-kysymykselta)))))

(deftest paivita-kysymysryhma-lisaa-kysymyksen
 (let [kysymysryhma (-> oletus-kysymysryhma
                      (assoc :kysymykset [oletus-kysymys]))
       lisatty-kysymys (atom nil)
       uusi-kysymysid 12]
   (with-redefs [aipal.arkisto.kysymysryhma/hae (constantly kysymysryhma)
                 aipal.arkisto.kysymysryhma/lisaa-kysymys! #(reset! lisatty-kysymys (assoc % :kysymysid uusi-kysymysid))]
     (paivita-kysymysryhma! kysymysryhma)
     (is (= uusi-kysymysid (:kysymysid @lisatty-kysymys))))))

(deftest paivita-kysymysryhma-paivittaa-perustiedot
  (with-redefs [aipal.arkisto.kysymysryhma/hae (constantly oletus-kysymysryhma)]
    (let [kysymysryhma (assoc oletus-kysymysryhma :nimi_fi "Uusi nimi")]
      (is (= "Uusi nimi"
             (-> (paivita-kysymysryhma! kysymysryhma)
               :nimi_fi))))))
