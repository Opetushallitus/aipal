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
(deftest with-kayttaja-sidonta
  (let [k (atom nil)]
    (with-redefs [arkisto/hae-voimassaoleva
                 (constantly {:oid "oid"})]
      (with-kayttaja "uid" nil
        (reset! k *kayttaja*))
      (is (= (:oid @k) "oid")))))

;; Impersonoinnin aikana effective-oid = impersonoitavan käyttäjän oid.
(deftest with-kayttaja-effective-oid-impersonointi
  (let [k (atom nil)]
    (with-redefs [arkisto/hae-voimassaoleva (constantly {:oid "oid"})]
      (with-kayttaja "uid" "impersonoitava-oid"
        (reset! k *kayttaja*))
      (is (= (:effective-oid @k) "impersonoitava-oid")))))

;; Ilman impersonointia effective-oid = käyttäjän oid.
(deftest with-kayttaja-effective-oid-ei-impersonointia
  (let [k (atom nil)]
    (with-redefs [arkisto/hae-voimassaoleva (constantly {:oid "oid"})]
      (with-kayttaja "uid" nil
        (reset! k *kayttaja*))
      (is (= (:effective-oid @k) "oid")))))
