(ns aipal.arkisto.kyselykerta-sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.arkisto.kyselykerta :as arkisto]
            [oph.common.util.util :refer [some-value
                                          some-value-with]]
            [aipal.sql.test-data-util :as test-data]))

(use-fixtures :each tietokanta-fixture)

(defn lisaa-kysely!
  ([]
   (lisaa-kysely! {}))
  ([kysely]
   (lisaa-kysely! kysely (test-data/lisaa-koulutustoimija!)))
  ([kysely koulutustoimija]
   (test-data/lisaa-kysely! kysely koulutustoimija)))

(defn lisaa-kyselykerta-johon-on-vastattu!
  ([kyselykerta]
   (lisaa-kyselykerta-johon-on-vastattu! kyselykerta (lisaa-kysely!)))
  ([{:keys [kyselykertaid]} {:keys [kyselyid]}]
    (sql/insert taulut/kyselykerta
                (sql/values {:nimi "", :kyselyid kyselyid, :voimassa_alkupvm (sql/raw "now()"),
                             :kyselykertaid kyselykertaid}))
    (sql/insert taulut/vastaajatunnus
                (sql/values {:vastaajatunnusid 1, :kyselykertaid kyselykertaid, :tunnus "",
                             :vastaajien_lkm 1}))
    (sql/insert taulut/vastaaja
                (sql/values {:kyselykertaid kyselykertaid, :vastaajatunnusid 1}))))

(defn lisaa-kyselykerta-ilman-vastaajia!
  ([kyselykerta]
   (lisaa-kyselykerta-ilman-vastaajia! kyselykerta (lisaa-kysely!)))
  ([{:keys [kyselykertaid]} {:keys [kyselyid]}]
   (sql/insert taulut/kyselykerta
     (sql/values {:nimi "", :kyselyid kyselyid, :voimassa_alkupvm (sql/raw "now()"),
                  :kyselykertaid kyselykertaid}))))

;; Kyselykerta on poistettavissa, jos sillä ei ole yhtään vastaajaa.
(deftest ^:integraatio hae-kaikki-kyselykerta-poistettavissa
  (let [kysely (lisaa-kysely!)]
    (lisaa-kyselykerta-johon-on-vastattu! {:kyselykertaid 1} kysely)
    (lisaa-kyselykerta-ilman-vastaajia! {:kyselykertaid 2} kysely)
    (is (:poistettavissa (some-value-with :kyselykertaid 2
                                          (arkisto/hae-kaikki (:koulutustoimija kysely)))))))

(deftest ^:integraatio kyselykerta-poistettavissa
  (let [kysely (lisaa-kysely!)]
    (lisaa-kyselykerta-johon-on-vastattu! {:kyselykertaid 1} kysely)
    (lisaa-kyselykerta-ilman-vastaajia! {:kyselykertaid 2} kysely)
    (is (arkisto/poistettavissa? 2))))

;; Kyselykerta ei ole poistettavissa, jos sillä on yksikin vastaaja.
(deftest ^:integraatio hae-kaikki-kyselykerta-ei-poistettavissa
  (let [kysely (lisaa-kysely!)]
    (lisaa-kyselykerta-johon-on-vastattu! {:kyselykertaid 1} kysely)
    (is (not (:poistettavissa (some-value-with :kyselykertaid 1
                                               (arkisto/hae-kaikki (:koulutustoimija kysely))))))))

(deftest ^:integraatio kyselykerta-ei-poistettavissa
  (lisaa-kyselykerta-johon-on-vastattu! {:kyselykertaid 1})
  (is (not (arkisto/poistettavissa? 1))))

;; Poistaminen poistaa kyselykerran.
(deftest ^:integraatio poista-kyselykerta
  (let [kysely (lisaa-kysely!)]
    (lisaa-kyselykerta-ilman-vastaajia! {:kyselykertaid 1} kysely)
    (lisaa-kyselykerta-ilman-vastaajia! {:kyselykertaid 2} kysely)
    (arkisto/poista! 1)
    (is (= (set (map :kyselykertaid (sql/select taulut/kyselykerta)))
           #{-1 2}))))

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
    (let [kysely (lisaa-kysely!)]
      (sql/insert taulut/kyselykerta
                  (sql/values {:nimi "", :kyselyid (:kyselyid kysely), :voimassa_alkupvm (sql/raw "now()"),
                               :kyselykertaid 1}))
      (sql/insert taulut/vastaajatunnus
                  (sql/values {:vastaajatunnusid 1, :kyselykertaid 1, :tunnus "",
                               :vastaajien_lkm 2}))
      (sql/insert taulut/vastaaja
                  (sql/values {:kyselykertaid 1, :vastaajatunnusid 1}))
      (sql/insert taulut/vastaaja
                  (sql/values {:kyselykertaid 1, :vastaajatunnusid 1}))

      (is (= (count (filter #(= (:kyselykertaid %) 1) (arkisto/hae-kaikki (:koulutustoimija kysely)))) 1)))))
