(ns aipal.infra.kayttaja.vaihto-test
  (:require [clojure.test :refer :all]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vaihto :refer :all]
            [aipal.infra.kayttaja.sql :refer [with-sql-kayttaja*]]
            [aipal.arkisto.kayttaja :as kayttaja-arkisto]
            [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]))

(defn stub-fixture [f]
  (with-redefs [kayttaja-arkisto/hae (constantly {})
                kayttaja-arkisto/hae-voimassaoleva (constantly {})
                kayttajaoikeus-arkisto/hae-roolit (constantly [])
                with-sql-kayttaja* (fn [_ f] (f))]
    (f)))

(use-fixtures :each stub-fixture)

;; with-kayttaja heittää IllegalStateExceptionin jos UIDilla ei löydy
;; voimassaolevaa käyttäjää.
(deftest with-kayttaja-ei-voimassaolevaa-kayttajaa
  (with-redefs [kayttaja-arkisto/hae-voimassaoleva (constantly nil)
                hae-kayttaja-kayttoikeuspalvelusta (constantly nil)]
    (is (thrown? IllegalStateException (with-kayttaja "uid" nil nil)))))

;; Jos UIDilla löytyy voimassaoleva käyttäjä, with-kayttaja ajaa annetun koodin
;; sitoen varin *kayttaja* käyttäjän tietoihin.
(deftest with-kayttaja-sidonta
  (let [k (atom nil)]
    (with-redefs [kayttaja-arkisto/hae-voimassaoleva (constantly {:oid "oid"})]
      (with-kayttaja "uid" nil nil
        (reset! k *kayttaja*))
      (is (= (:oid @k) "oid")))))

;; Impersonoinnin aikana aktiivinen OID = impersonoitavan käyttäjän OID.
(deftest with-kayttaja-aktiivinen-oid-impersonointi
  (let [k (atom nil)]
    (with-kayttaja "uid" "impersonoitava-oid" nil
      (reset! k *kayttaja*))
      (is (= (:aktiivinen-oid @k) "impersonoitava-oid"))))

;; Impersonoinnin aikana aktiiviset roolit = impersonoitavan käyttäjän roolit.
(deftest with-kayttaja-aktiiviset-roolit-impersonointi
  (let [k (atom nil)]
    (with-redefs [kayttajaoikeus-arkisto/hae-roolit
                  {"impersonoitava-oid" [:...impersonoidut-roolit...]}]
      (with-kayttaja "uid" "impersonoitava-oid" nil
        (reset! k *kayttaja*))
      (is (= (:aktiiviset-roolit @k) [:...impersonoidut-roolit...])))))

;; Impersonoinnin aikana aktiivinen koulutustoimija = impersonoitavan käyttäjän koulutustoimija.
(deftest with-kayttaja-aktiivinen-koulutustoimija-impersonointi
  (let [k (atom nil)]
    (with-redefs [kayttajaoikeus-arkisto/hae-roolit {"impersonoitava-oid" [{:rooli "rooli" :organisaatio "impersonoitu-koulutustoimija"}]}]
      (with-kayttaja "uid" "impersonoitava-oid" nil
        (reset! k *kayttaja*))
      (is (= (:aktiivinen-koulutustoimija @k) "impersonoitu-koulutustoimija")))))

;; Ilman impersonointia aktiivinen OID = käyttäjän oma OID.
(deftest with-kayttaja-aktiivinen-oid-ei-impersonointia
  (let [k (atom nil)]
    (with-redefs [kayttaja-arkisto/hae-voimassaoleva (constantly {:oid "oid"})
                  kayttajaoikeus-arkisto/hae-roolit {}]
      (with-kayttaja "uid" nil nil
        (reset! k *kayttaja*))
      (is (= (:aktiivinen-oid @k) "oid")))))

;; Ilman impersonointia aktiiviset roolit = käyttäjän omat roolit.
(deftest with-kayttaja-aktiiviset-roolit-ei-impersonointia
  (let [k (atom nil)]
    (with-redefs [kayttaja-arkisto/hae-voimassaoleva (constantly {:oid "oid"})
                  kayttajaoikeus-arkisto/hae-roolit {"oid" [:...omat-roolit...]}]
      (with-kayttaja "uid" nil nil
        (reset! k *kayttaja*))
      (is (= (:aktiiviset-roolit @k) [:...omat-roolit...])))))

;; Ilman impersonointia aktiivinen koulutustoimija = käyttäjän oma koulutustoimija.
(deftest with-kayttaja-aktiivinen-koulutustoimija-ei-impersonointia
  (let [k (atom nil)]
    (with-redefs [kayttaja-arkisto/hae-voimassaoleva (constantly {:oid "oid"})
                  kayttajaoikeus-arkisto/hae-roolit {"oid" [{:rooli "rooli" :organisaatio "koulutustoimija"}]}]
      (with-kayttaja "uid" nil nil
        (reset! k *kayttaja*))
      (is (= (:aktiivinen-koulutustoimija @k) "koulutustoimija")))))

;; with-kayttaja muodostaa käyttäjän koko nimen.
(deftest with-kayttaja-nimi
  (let [k (atom nil)]
    (with-redefs [kayttaja-arkisto/hae-voimassaoleva
                  (constantly {:etunimi "Matti"
                               :sukunimi "Meikäläinen"})]
      (with-kayttaja "uid" nil nil
        (reset! k *kayttaja*))
      (is (= (:nimi @k) "Matti Meikäläinen")))))

;; with-kayttaja muodostaa impersonoidun käyttäjän koko nimen.
(deftest with-kayttaja-impersonoidun-kayttajan-nimi
  (let [k (atom nil)]
    (with-redefs [kayttaja-arkisto/hae
                  (constantly {:etunimi "Maija"
                               :sukunimi "Mallikas"})]
      (with-kayttaja "uid" "impersonoitu-oid" nil
        (reset! k *kayttaja*))
      (is (= (:impersonoidun-kayttajan-nimi @k) "Maija Mallikas")))))

; Ilman impersonointia with-kayttaja jättää impersonoidun käyttäjän nimen
; tyhjäksi.
(deftest with-kayttaja-impersonoidun-kayttajan-nimi-ilman-impersonointia
  (let [k (atom nil)]
    (with-redefs [kayttaja-arkisto/hae
                  (constantly {:etunimi "Maija"
                               :sukunimi "Mallikas"})]
      (with-kayttaja "uid" nil nil
        (reset! k *kayttaja*))
      (is (= (:impersonoidun-kayttajan-nimi @k) "")))))
