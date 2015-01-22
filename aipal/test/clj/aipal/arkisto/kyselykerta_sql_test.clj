(ns aipal.arkisto.kyselykerta-sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.arkisto.kyselykerta :as arkisto]))

(use-fixtures :each tietokanta-fixture)

;; Kyselykerta on poistettavissa, jos sillä ei ole yhtään vastaajaa.
(deftest ^:integraatio hae-kaikki-kyselykerta-poistettavissa
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()")}))
  (is (= (map :poistettavissa (arkisto/hae-kaikki))
         [true])))

(deftest ^:integraatio kyselykerta-poistettavissa
  (let [{id :kyselykertaid}
        (sql/insert taulut/kyselykerta
          (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()")}))]
    (is (arkisto/poistettavissa? id))))

;; Kyselykerta ei ole poistettavissa, jos sillä on yksikin vastaaja.
(deftest ^:integraatio hae-kaikki-kyselykerta-ei-poistettavissa
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                 :kyselykertaid 1}))
  (sql/insert taulut/vastaajatunnus
    (sql/values {:vastaajatunnusid 1, :kyselykertaid 1, :tunnus "",
                 :vastaajien_lkm 1}))
  (sql/insert taulut/vastaaja
    (sql/values {:kyselykertaid 1, :vastaajatunnusid 1}))
  (is (= (map :poistettavissa (arkisto/hae-kaikki))
         [false])))

(deftest ^:integraatio kyselykerta-ei-poistettavissa
  (let [{id :kyselykertaid}
        (sql/insert taulut/kyselykerta
          (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                       :kyselykertaid 1}))]
    (sql/insert taulut/vastaajatunnus
      (sql/values {:vastaajatunnusid 1, :kyselykertaid 1, :tunnus "",
                   :vastaajien_lkm 1}))
    (sql/insert taulut/vastaaja
      (sql/values {:kyselykertaid 1, :vastaajatunnusid 1}))
    (is (not (arkisto/poistettavissa? id)))))

;; Poistaminen poistaa kyselykerran.
(deftest ^:integraatio poista-kyselykerta
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                 :kyselykertaid 1}))
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                 :kyselykertaid 2}))
  (arkisto/poista! 1)
  (is (= (map :kyselykertaid (sql/select taulut/kyselykerta))
         [2])))

;; Poistaminen poistaa kyselykertaan liittyvät vastaajatunnukset.
(deftest ^:integraatio poista-vastaajatunnukset
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                 :kyselykertaid 1}))
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                 :kyselykertaid 2}))
  (sql/insert taulut/vastaajatunnus
    (sql/values {:vastaajatunnusid 1, :kyselykertaid 1, :tunnus "1",
                 :vastaajien_lkm 1}))
  (sql/insert taulut/vastaajatunnus
    (sql/values {:vastaajatunnusid 2, :kyselykertaid 2, :tunnus "2",
                 :vastaajien_lkm 1}))
  (arkisto/poista! 1)
  (is (= (map :vastaajatunnusid (sql/select taulut/vastaajatunnus))
         [2])))

(deftest ^:integraatio hae-kaikki-test
  (testing
    "vastaajien määrä ei vaikuta kyselykertoihin (OPH-1254)"
    (sql/insert taulut/kyselykerta
      (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                   :kyselykertaid 1}))
    (sql/insert taulut/vastaajatunnus
      (sql/values {:vastaajatunnusid 1, :kyselykertaid 1, :tunnus "",
                   :vastaajien_lkm 2}))
    (sql/insert taulut/vastaaja
      (sql/values {:kyselykertaid 1, :vastaajatunnusid 1}))
    (sql/insert taulut/vastaaja
      (sql/values {:kyselykertaid 1, :vastaajatunnusid 1}))

    (is (= (map :kyselykertaid (arkisto/hae-kaikki))
           [1]))))
