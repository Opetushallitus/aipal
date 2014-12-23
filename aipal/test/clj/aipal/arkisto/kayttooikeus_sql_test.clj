(ns aipal.arkisto.kayttooikeus-sql-test
  (:require
    [clojure.test :refer :all]
    [korma.core :as sql]
    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
    [aipal.arkisto.kysely :as kysely-arkisto]
    [aipal.toimiala.kayttajaoikeudet :as kayttajaoikeudet]
    [aipal.arkisto.kayttaja :as kayttaja-arkisto]
    [aipal.toimiala.kayttajaoikeudet :as ko]
    [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
    [aipal.integraatio.sql.korma :as taulut]))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio kyselyn-oikeudet
  (testing "Haku palauttaa lisää-kutsulla luodun koulutustoimijan"
    (let [kysely (kysely-arkisto/lisaa! {:nimi_fi "oletuskysely, testi"
                                         :koulutustoimija "7654321-2"})
          koulutustoimija (:koulutustoimija (kysely-arkisto/hae-organisaatiotieto (:kyselyid kysely)))]
      (is (= "7654321-2" koulutustoimija)))))

(deftest ^:integraatio omien-kyselyiden-listaus
  (testing "käyttäjän organisaation kautta saadaan vain käyttäjän omat kyselyt"
    (let [koulutustoimija "7654321-2"
      oman-organisaation-kysely (kysely-arkisto/lisaa! {:nimi_fi "oletuskysely, testi"
                                                        :koulutustoimija koulutustoimija})
      muun-organisaation-kysely (kysely-arkisto/lisaa! {:nimi_fi "testi"
                                                        :koulutustoimija "2345678-0"})
      kyselyt (kysely-arkisto/hae-kyselyt koulutustoimija)]
      (is (= 1 (count kyselyt)))
      (is (= "oletuskysely, testi" (first (map :nimi_fi kyselyt)))))))

(def kysely-kayttajat
  "Testikäyttäjät, uid, tietokannassa"
  {"8086" [true true true false false] ; luonti + oman organisaation luku/muokkaus
   "6502" [false true false false false] ; oman organisaation luku
   "68000" [false true false false false] ; luonti + oman organisaation luku/muokkaus
  })

;; Testaa ennen kaikkea, että näkymän muodostava SQL on oikein.
(deftest ^:integraatio kyselyn-logiikka
  (testing "Kyselyihin liittyvien oikeuksien logiikka"
    (let [oman-organisaation-kysely (kysely-arkisto/lisaa! {:nimi_fi "oletuskysely, testi"
                                                            :koulutustoimija "7654321-2"})
          muun-organisaation-kysely (kysely-arkisto/lisaa! {:nimi_fi "testi"
                                                            :koulutustoimija "2345678-0"})]
      (doseq [uid (keys kysely-kayttajat)]
        (with-kayttaja uid nil nil
          (let [tulos [(kayttajaoikeudet/kysely-luonti?)
                       (kayttajaoikeudet/kysely-luku? (:kyselyid oman-organisaation-kysely))
                       (kayttajaoikeudet/kysely-muokkaus? (:kyselyid oman-organisaation-kysely))
                       (kayttajaoikeudet/kysely-luku? (:kyselyid muun-organisaation-kysely))
                       (kayttajaoikeudet/kysely-muokkaus? (:kyselyid muun-organisaation-kysely))]]
            (is (= tulos (get kysely-kayttajat uid)))))))))

(deftest ^:integraatio hae-roolit-palauttaa-vain-annetun-kayttajan-roolit
  (sql/insert taulut/koulutustoimija
    (sql/values {:ytunnus "org"
                 :nimi_fi "Organisaatio"}))
  (sql/insert taulut/kayttaja
    (sql/values [{:oid "oid1"}
                 {:oid "oid2"}]))
  (sql/insert taulut/kayttajarooli
    (sql/values [{:roolitunnus "testirooli1"}
                 {:roolitunnus "testirooli2"}]))
  (sql/insert taulut/rooli-organisaatio
    (sql/values [{:organisaatio "org"
                  :rooli "testirooli1"
                  :kayttaja "oid1"
                  :voimassa true}
                 {:organisaatio "org"
                  :rooli "testirooli2"
                  :kayttaja "oid2"
                  :voimassa true}]))
  (is (= (map #(dissoc % :rooli_organisaatio_id) (kayttajaoikeus-arkisto/hae-roolit "oid1"))
         [{:organisaatio "org"
           :rooli "testirooli1"}]))
  (sql/delete taulut/rooli-organisaatio
    (sql/where {:rooli [like "testirooli%"]}))
  (sql/delete taulut/kayttajarooli
    (sql/where {:roolitunnus [like "testirooli%"]})))

(deftest ^:integraatio hae-roolit-palauttaa-vain-voimassaolevat-roolit
  (sql/insert taulut/koulutustoimija
    (sql/values {:ytunnus "org"
                 :nimi_fi "Organisaatio"}))
  (sql/insert taulut/kayttaja
    (sql/values [{:oid "oid1"}]))
  (sql/insert taulut/kayttajarooli
    (sql/values [{:roolitunnus "testirooli1"}
                 {:roolitunnus "testirooli2"}]))
  (sql/insert taulut/rooli-organisaatio
    (sql/values [{:organisaatio "org"
                  :rooli "testirooli1"
                  :kayttaja "oid1"
                  :voimassa true}
                 {:organisaatio "org"
                  :rooli "testirooli2"
                  :kayttaja "oid1"
                  :voimassa false}]))
  (is (= (map #(dissoc % :rooli_organisaatio_id) (kayttajaoikeus-arkisto/hae-roolit "oid1"))
         [{:organisaatio "org"
           :rooli "testirooli1"}]))
  (sql/delete taulut/rooli-organisaatio
    (sql/where {:rooli [like "testirooli%"]}))
  (sql/delete taulut/kayttajarooli
    (sql/where {:roolitunnus [like "testirooli%"]})))
