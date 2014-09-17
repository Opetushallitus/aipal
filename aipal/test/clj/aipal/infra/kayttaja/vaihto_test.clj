(ns aipal.infra.kayttaja.vaihto-test
  (:require [clojure.test :refer :all]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vaihto :refer :all]
            [aipal.arkisto.kayttaja :as arkisto]))

(use-fixtures :each tietokanta-fixture)

;; with-kayttaja heittää IllegalStateExceptionin jos UIDilla ei löydy
;; voimassaolevaa käyttäjää.
(deftest with-kayttaja-ei-voimassaolevaa-kayttajaa
  (is (thrown? IllegalStateException (with-kayttaja "uid" nil))))

;; Jos UIDilla löytyy voimassaoleva käyttäjä, with-kayttaja ajaa annetun koodin
;; sitoen varin *kayttaja* käyttäjän tietoihin.
(deftest with-kayttaja-kayttaja
  (let [*kayttaja*-funktiokutsun-aikana (atom nil)]
    (with-redefs [arkisto/hae-voimassaoleva
                 (constantly {:oid "oid"})]
      (with-kayttaja "uid" nil
        (reset! *kayttaja*-funktiokutsun-aikana *kayttaja*))
      (is (= {:oid "oid"} @*kayttaja*-funktiokutsun-aikana)))))
