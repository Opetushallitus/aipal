(ns aipal.infra.auth-wrapper-test
  (:require [clojure.test :refer :all]
            [aipal.infra.auth-wrapper :refer :all]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            aipal.arkisto.kayttaja
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(use-fixtures :each tietokanta-fixture)

;; with-kayttaja heittää IllegalStateExceptionin jos UIDilla ei löydy
;; voimassaolevaa käyttäjää.
(deftest with-kayttaja-ei-voimassaolevaa-kayttajaa
  (is (thrown? IllegalStateException (with-kayttaja "uid" nil))))

;; Jos UIDilla löytyy voimassaoleva käyttäjä, with-kayttaja ajaa annetun koodin
;; sitoen varin *kayttaja* käyttäjän tietoihin.
(deftest with-kayttaja*-kayttaja
  (let [*kayttaja*-funktiokutsun-aikana (atom nil)]
    (with-redefs [aipal.arkisto.kayttaja/hae-voimassaoleva
                 (constantly {:oid "oid"})]
      (with-kayttaja "uid" nil
        (reset! *kayttaja*-funktiokutsun-aikana *kayttaja*))
      (is (= {:oid "oid"} @*kayttaja*-funktiokutsun-aikana)))))
