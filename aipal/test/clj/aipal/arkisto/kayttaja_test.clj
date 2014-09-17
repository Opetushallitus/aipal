(ns aipal.arkisto.kayttaja_test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.arkisto.kayttaja :refer :all]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            [aipal.integraatio.sql.korma :refer [kayttaja]]))

(use-fixtures :each tietokanta-fixture)

;; hae-voimassaoleva palauttaa nil jos UIDilla ei löydy käyttäjää.
(deftest hae-voimassaoleva-ei-kayttajaa
  (sql/insert kayttaja
    (sql/values {:oid "oid"
                 :uid "uid"
                 :voimassa true}))
  (is (nil? (hae-voimassaoleva "tuntematonuid"))))

;; hae-voimassaoleva palauttaa voimassaolevan käyttäjän kaikki tiedot.
(deftest hae-voimassaoleva-kayttaja-voimassa
  (sql/insert kayttaja
    (sql/values {:oid "oid"
                 :uid "uid"
                 :voimassa true}))
  (is (= (select-keys (hae-voimassaoleva "uid") [:oid :uid :voimassa])
         {:oid "oid"
          :uid "uid"
          :voimassa true})))

;; hae-voimassaoleva palauttaa nil jos käyttäjä ei ole voimassa.
(deftest hae-voimassaoleva-ei-voimassa
  (sql/insert kayttaja
    (sql/values {:oid "uid"
                 :uid "uid"
                 :voimassa false}))
  (is (nil? (hae-voimassaoleva "uid"))))
