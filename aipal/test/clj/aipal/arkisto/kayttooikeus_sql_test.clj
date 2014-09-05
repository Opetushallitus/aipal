(ns aipal.arkisto.kayttooikeus-sql-test
  (:require
    [clojure.test :refer :all]
    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
    [aipal.arkisto.kysely :as kysely-arkisto]
    [aipal.toimiala.kayttajaoikeudet :as kayttajaoikeudet]
    [aipal.arkisto.kayttaja :as kayttaja-arkisto]
    [oph.korma.korma-auth :as ka]
    [aipal.toimiala.kayttajaoikeudet :as ko]
    [aipal.infra.auth-wrapper :as auth-wrapper]))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio kyselyn-oikeudet
  (testing "Haku palauttaa lisää-kutsulla luodun koulutustoimijan"
    (let [kysely (kysely-arkisto/lisaa! {:nimi_fi "oletuskysely, testi"
                                         :koulutustoimija "7654321-2"})
          oikeudet (kayttajaoikeus-arkisto/hae-kyselylla (:kyselyid kysely) "OID.8086")]
      (is (not-empty oikeudet)))))

(deftest ^:integraatio omien-kyselyiden-listaus
  (testing "käyttäjän organisaation kautta saadaan vain käyttäjän omat kyselyt"
    (let [vastuukayttaja "OID.8086"
      oman-organisaation-kysely (kysely-arkisto/lisaa! {:nimi_fi "oletuskysely, testi"
                                                        :koulutustoimija "7654321-2"})
      muun-organisaation-kysely (kysely-arkisto/lisaa! {:nimi_fi "testi"
                                                        :koulutustoimija "2345678-0"})
      kyselyt (kysely-arkisto/hae-kyselyt vastuukayttaja)]
      (is (= 1 (count kyselyt)))
      (is (= "oletuskysely, testi" (first (map :nimi_fi kyselyt)))))))

(def kysely-kayttajat
  "Testikäyttäjät, uid, tietokannassa"
  {"8086" [true true true false false] ; luonti + oman organisaation luku/muokkaus
   "6502" [false true false false false] ; oman organisaation luku
   "68000" [true true true false false] ; luonti + oman organisaation luku/muokkaus
  })

(deftest ^:integraatio kyselyn-logiikka
  (testing "Kyselyihin liittyvien oikeuksien logiikka"
    (let [oman-organisaation-kysely (kysely-arkisto/lisaa! {:nimi_fi "oletuskysely, testi"
                                                            :koulutustoimija "7654321-2"})
          muun-organisaation-kysely (kysely-arkisto/lisaa! {:nimi_fi "testi"
                                                            :koulutustoimija "2345678-0"})]
      (doseq [uid (keys kysely-kayttajat)]
        (auth-wrapper/with-user uid nil
          #(let [tulos [(kayttajaoikeudet/kysely-luonti?)
                        (kayttajaoikeudet/kysely-luku? (:kyselyid oman-organisaation-kysely))
                        (kayttajaoikeudet/kysely-muokkaus? (:kyselyid oman-organisaation-kysely))
                        (kayttajaoikeudet/kysely-luku? (:kyselyid muun-organisaation-kysely))
                        (kayttajaoikeudet/kysely-muokkaus? (:kyselyid muun-organisaation-kysely))]]
             (is (= tulos (get kysely-kayttajat uid)))))))))
