(ns aipal.arkisto.kyselykerta-sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.arkisto.kyselykerta :as arkisto]
            [oph.common.util.util :refer [some-value
                                          some-value-with]]))

(use-fixtures :each tietokanta-fixture)

(defn lisaa-kyselykerta-johon-on-vastattu! [id]
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                 :kyselykertaid id}))
  (sql/insert taulut/vastaajatunnus
    (sql/values {:vastaajatunnusid 1, :kyselykertaid id, :tunnus "",
                 :vastaajien_lkm 1}))
  (sql/insert taulut/vastaaja
    (sql/values {:kyselykertaid id, :vastaajatunnusid 1})))

(defn lisaa-kyselykerta-ilman-vastaajia! [id]
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                 :kyselykertaid id})))

;; Kyselykerta on poistettavissa, jos sillä ei ole yhtään vastaajaa.
(deftest ^:integraatio hae-kaikki-kyselykerta-poistettavissa
  (lisaa-kyselykerta-johon-on-vastattu! 1)
  (lisaa-kyselykerta-ilman-vastaajia! 2)
  (is (:poistettavissa (some-value-with :kyselykertaid 2
                                        (arkisto/hae-kaikki)))))

(deftest ^:integraatio kyselykerta-poistettavissa
  (lisaa-kyselykerta-johon-on-vastattu! 1)
  (lisaa-kyselykerta-ilman-vastaajia! 2)
  (is (arkisto/poistettavissa? 2)))

;; Kyselykerta ei ole poistettavissa, jos sillä on yksikin vastaaja.
(deftest ^:integraatio hae-kaikki-kyselykerta-ei-poistettavissa
  (lisaa-kyselykerta-johon-on-vastattu! 1)
  (is (not (:poistettavissa (some-value-with :kyselykertaid 1
                                             (arkisto/hae-kaikki))))))

(deftest ^:integraatio kyselykerta-ei-poistettavissa
  (lisaa-kyselykerta-johon-on-vastattu! 1)
  (is (not (arkisto/poistettavissa? 1))))

;; Poistaminen poistaa kyselykerran.
(deftest ^:integraatio poista-kyselykerta
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                 :kyselykertaid 1}))
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                 :kyselykertaid 2}))
  (arkisto/poista! 1)
  (is (nil? (some-value-with :kyselykertaid 1
                             (sql/select taulut/kyselykerta)))))

;; Poistaminen poistaa kyselykertaan liittyvät vastaajatunnukset.
(deftest ^:integraatio poista-vastaajatunnukset
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()"),
                 :kyselykertaid 1}))
  (sql/insert taulut/vastaajatunnus
    (sql/values {:vastaajatunnusid 1, :kyselykertaid 1, :tunnus "1",
                 :vastaajien_lkm 1}))
  (sql/insert taulut/vastaajatunnus
    (sql/values {:vastaajatunnusid 2, :kyselykertaid -1, :tunnus "2",
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

    (is (= (count (filter #(= (:kyselykertaid %) 1) (arkisto/hae-kaikki))) 1))))
