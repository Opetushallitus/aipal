(ns aipal.arkisto.vastaajatunnus-sql-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clj-time.core :as ctime]
            [clj-time.coerce :as c]
            [korma.core :as sql]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [aipal.arkisto.vastaajatunnus :refer :all]))

(use-fixtures :each tietokanta-fixture)

;(deftest ^:integraatio kyselykerralla-haku
;  (testing "hae-kyselykerralla palauttaa vain annetun kyselykerran vastaajatunnukset."
;    (let [k1 (:kyselykertaid (lisaa-kyselykerta!))
;          k2 (:kyselykertaid (lisaa-kyselykerta!))]
;      (lisaa! {:kyselykertaid k1
;               :vastaajien_lkm 1})
;      (lisaa! {:kyselykertaid k2
;               :vastaajien_lkm 2})
;      (is (= (map :vastaajien_lkm (hae-kyselykerralla k1))
;             [1])))))

;(deftest ^:integraatio haku-idlla
;  (testing "hae palauttaa vastaajatunnuksen, jolla on annettu id"
;    (let [k (:kyselykertaid (lisaa-kyselykerta!))]
;      (lisaa! {:kyselykertaid k
;               :vastaajatunnusid 1
;               :vastaajien_lkm 11})
;      (lisaa! {:kyselykertaid k
;               :vastaajatunnusid 2
;               :vastaajien_lkm 22})
;      (is (= (:vastaajien_lkm (hae k 1))
;             11)))))

;(deftest ^:integraatio lisays
;  (testing "henkilökohtaisten vastaajatunnusten lisääminen lisää vastaajien_lkm kpl vastaajatunnuksia"
;    (let [k (:kyselykertaid (lisaa-kyselykerta!))]
;      (lisaa! {:kyselykertaid k
;               :henkilokohtainen true
;               :vastaajien_lkm 3})
;      (is (= (map :vastaajien_lkm (hae-kyselykerralla k))
;             [1 1 1]))))
;  (testing "jaetun vastaajatunnuksen lisääminen lisää vain yhden vastaajatunnuksen"
;    (let [k (:kyselykertaid (lisaa-kyselykerta!))]
;      (lisaa! {:kyselykertaid k
;               :henkilokohtainen false
;               :vastaajien_lkm 3})
;      (is (= (map :vastaajien_lkm (hae-kyselykerralla k))
;             [3])))))

;(deftest ^:integraatio olemassaolevien-vastaajatunnusten-tilalle-generoidaan-uudet
;  (with-redefs [luo-tunnuksia (constantly (range))
;                vastaajatunnus-olemassa? #{2 4 6}]
;    (let [k (:kyselykertaid (lisaa-kyselykerta!))]
;      (is (= (map :tunnus (lisaa! {:kyselykertaid k
;                                   :henkilokohtainen true
;                                   :vastaajien_lkm 6}))
;             ["0" "1" "3" "5" "7" "8"])))))

;(deftest ^:integraatio vastaajatunnuksia-tallennetaan-vain-haluttu-maara-chunkingista-riippumatta
;  ;; Vaihdetaan luo-tunnuksia palauttamaan sekvenssi jossa on chunking
;  (with-redefs [luo-tunnuksia (constantly (range))]
;    (let [k (:kyselykertaid (lisaa-kyselykerta!))]
;      (lisaa! {:kyselykertaid k
;               :henkilokohtainen true
;               :vastaajien_lkm 6})
;      (is (= (count (hae-kyselykerralla k))
;             6)))))

;(deftest ^:integraatio muokattavissa-tieto-lasketaan-oikein
;  (let [testaa (fn [selite kysely_loppupvm kyselykerta_loppupvm vastaajatunnus_loppupvm voimassa_odotettu]
;                 (let [kysely (lisaa-kysely! {:voimassa_loppupvm (c/to-date-time kysely_loppupvm)})
;                       kyselykerta (lisaa-kyselykerta! {:voimassa_loppupvm (c/to-date-time kyselykerta_loppupvm)} kysely)
;                       vastaajatunnus (first (lisaa-vastaajatunnus! {:voimassa_loppupvm (c/to-date-time vastaajatunnus_loppupvm)} kyselykerta))]
;                   (testing selite
;                     (is (= (:muokattavissa vastaajatunnus) voimassa_odotettu)))))]
;    (testaa "vastaajatunnus.voimassa_loppupvm tänään" nil nil (ctime/now) true)
;    (testaa "vastaajatunnus.voimassa_loppupvm liian vanha" nil nil (ctime/date-time 2015 1 1) false)
;    (testaa "kysely.voimassa_loppupvm tänään, muut null" (ctime/now) nil nil true)
;    (testaa "kysely/kyselykerta/vastaajatunnus voimassa_loppupvm null kaikissa" nil nil nil true)))
;
;(defn vastaajatunnus-gen [kyselykertaid]
;  (gen/hash-map :kyselykertaid (gen/return kyselykertaid)
;                :henkilokohtainen gen/boolean
;                :vastaajien_lkm gen/s-pos-int))
;
;(defspec ^:integraatio lisays-paluuarvo 50
;  (testing "lisaa-kyselykerta! palauttaa vastaajatunnukset samassa muodossa kuin hae-kyselykerralla"
;    (let [k (:kyselykertaid (lisaa-kyselykerta!))]
;      (prop/for-all [vastaajatunnus (vastaajatunnus-gen k)]
;        (sql/delete taulut/vastaajatunnus
;          (sql/where (> :vastaajatunnusid 15)))
;        (= (set (lisaa! vastaajatunnus))
;           (set (hae-kyselykerralla k)))))))
