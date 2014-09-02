(ns aipal.arkisto.kayttooikeus-sql-test  
  (:require 
    [aipal.sql.test-util :refer :all]
    [aipal.sql.test-data-util :refer :all]
    [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
    [aipal.arkisto.kysely :as kysely-arkisto]
  )
  (:use clojure.test))

(use-fixtures :each tietokanta-fixture)

(deftest ^:integraatio kyselyn-oikeudet
  (testing "Haku palauttaa lisää-kutsulla luodun koulutustoimijan"
    (let [kysely (kysely-arkisto/lisaa! {:nimi_fi "oletuskysely, testi"
                            :koulutustoimija "7654321-2"}
                            )
          oikeudet (kayttajaoikeus-arkisto/hae-kyselylla (:kyselyid kysely) "OID.8086")]
      (is (not-empty oikeudet)))))
