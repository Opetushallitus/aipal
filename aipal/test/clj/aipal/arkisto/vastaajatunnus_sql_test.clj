(ns aipal.arkisto.vastaajatunnus-sql-test
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clj-time.core :as time]
            clj-time.coerce
            [korma.core :as sql]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]
            [aipal.arkisto.vastaajatunnus :refer :all]))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio kyselykerralla-haku
  (testing "hae-kyselykerralla palauttaa vain annetun kyselykerran vastaajatunnukset."
    (let [k1 (:kyselykertaid (lisaa-kyselykerta!))
          k2 (:kyselykertaid (lisaa-kyselykerta!))]
      (lisaa! {:kyselykertaid k1
               :vastaajien_lkm 1})
      (lisaa! {:kyselykertaid k2
               :vastaajien_lkm 2})
      (is (= (map :vastaajien_lkm (hae-kyselykerralla k1))
             [1])))))

(deftest ^:integraatio haku-idlla []
  (testing "hae palauttaa vastaajatunnuksen, jolla on annettu id"
    (let [k (:kyselykertaid (lisaa-kyselykerta!))]
      (lisaa! {:kyselykertaid k
               :vastaajatunnusid 1
               :vastaajien_lkm 11})
      (lisaa! {:kyselykertaid k
               :vastaajatunnusid 2
               :vastaajien_lkm 22})
      (is (= (:vastaajien_lkm (hae 1))
             11)))))

(deftest ^:integraatio lisays
  (testing "henkilökohtaisten vastaajatunnusten lisääminen lisää vastaajien_lkm kpl vastaajatunnuksia"
    (let [k (:kyselykertaid (lisaa-kyselykerta!))]
      (lisaa! {:kyselykertaid k
               :henkilokohtainen true
               :vastaajien_lkm 3})
      (is (= (map :vastaajien_lkm (hae-kyselykerralla k))
             [1 1 1]))))

  (testing "jaetun vastaajatunnuksen lisääminen lisää vain yhden vastaajatunnuksen"
    (let [k (:kyselykertaid (lisaa-kyselykerta!))]
      (lisaa! {:kyselykertaid k
               :henkilokohtainen false
               :vastaajien_lkm 3})
      (is (= (map :vastaajien_lkm (hae-kyselykerralla k))
             [3])))))

(defn pvm-gen [min-pvm max-pvm]
  {:pre [(not (time/after? min-pvm max-pvm))]}
  (let [paivia-valissa (time/in-days (time/interval
                                       (clj-time.coerce/to-date-time min-pvm)
                                       (clj-time.coerce/to-date-time max-pvm)))]
    (gen/fmap #(time/plus min-pvm (time/days %))
              (gen/choose 0 paivia-valissa))))

(defn aikavali-gen [min-pvm max-pvm]
  {:pre [(time/before? min-pvm max-pvm)]}
  (gen/bind (pvm-gen min-pvm (time/minus max-pvm (time/days 1)))
            (fn [alkupvm]
              (gen/tuple (gen/return alkupvm)
                         (pvm-gen (time/plus alkupvm (time/days 1))
                                  max-pvm)))))

(defn vastaajatunnus-gen [kyselykertaid]
  (gen/bind (aikavali-gen (time/local-date 1900 1 1)
                          (time/local-date 2100 1 1))
            (fn [[alkupvm loppupvm]]
              (gen/hash-map :kyselykertaid (gen/return kyselykertaid)
                            :henkilokohtainen gen/boolean
                            :vastaajien_lkm gen/s-pos-int
                            :voimassa_alkupvm (gen/return alkupvm)
                            :voimassa_loppupvm (gen/return loppupvm)))))

(defspec ^:integraatio lisays-paluuarvo 50
  (testing "lisaa-kyselykerta! palauttaa vastaajatunnukset samassa muodossa kuin hae-kyselykerralla"
    (let [k (:kyselykertaid (lisaa-kyselykerta!))]
      (prop/for-all [vastaajatunnus (vastaajatunnus-gen k)]
        (sql/delete taulut/vastaajatunnus)
        (= (lisaa! vastaajatunnus)
           (hae-kyselykerralla k))))))
