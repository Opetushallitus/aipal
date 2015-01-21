(ns aipal.arkisto.kyselykerta-sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.arkisto.kyselykerta :as arkisto]))

(use-fixtures :each tietokanta-fixture)

;; Kyselykerta on poistettavissa, jos sillä ei ole yhtään vastaajaa.
(deftest ^:integraatio kyselykerta-poistettavissa
  (sql/insert taulut/kyselykerta
    (sql/values {:nimi "", :kyselyid -1, :voimassa_alkupvm (sql/raw "now()")}))
  (is (= (map :poistettavissa (arkisto/hae-kaikki))
         [true])))

;; Kyselykerta ei ole poistettavissa, jos sillä on yksikin vastaaja..
(deftest ^:integraatio kyselykerta-poistettavissa
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
