(ns aipal.infra.kayttaja.sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.sql.test-util :refer [exec-raw-fixture]]
            [aipal.arkisto.kayttaja :as kayttaja-arkisto]
            [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
            [aipal.asetukset :refer [asetukset oletusasetukset]]
            [aipal.integraatio.kayttooikeuspalvelu :as kayttooikeuspalvelu]
            [aipal.infra.kayttaja.sql :refer :all]))

(use-fixtures :each exec-raw-fixture)

;; with-sql-kayttaja asettaa käyttäjän PostgreSQL-parametriin.
(deftest ^:integraatio with-sql-kayttaja-test
  (with-sql-kayttaja "foobar"
    (is (= (:current_setting (first (sql/exec-raw "select current_setting('aipal.kayttaja');"
                                                  :results)))
           "foobar"))))

(defn runtime-throws []
  (let [fo (kayttaja-arkisto/hae-voimassaoleva "uid")]
    (throw (RuntimeException. "forced"))))

(defn checked-throws []
  (let [fo (kayttaja-arkisto/hae-voimassaoleva "uid")]
    (throw (Exception. "forced"))))

;; poikkeuksien käsittely
(deftest ^:integraatio with-sql-kayttaja-poikkeukset-test
  (is (thrown-with-msg? RuntimeException #"forced"
        (with-sql-kayttaja "foobar"
          (runtime-throws)))))

;; poikkeuksien käsittely
(deftest ^:integraatio with-sql-kayttaja-poikkeukset-test2
  (is (thrown-with-msg? Exception #"forced"
        (with-sql-kayttaja "foobar"
          (runtime-throws)))))

; Testataan LDAP-haku reaaliaikaisesti
(defn with-ldap-haku-mock [f]
  (let [foobar-ldap {:oid "foobar"
                     :uid "foobar-uid"
                     :etunimi "etunimi"
                     :sukunimi "sukunimi"
                     :voimassa true
                     :roolit {}}
        testi-asetukset (-> oletusasetukset
             (assoc-in [:cas-auth-server :enabled] false)
             (assoc :development-mode true))]
    (deliver asetukset testi-asetukset)
    (try
      (with-redefs [kayttooikeuspalvelu/kayttaja (constantly foobar-ldap)]
        (f))
      (finally
        (sql/exec-raw (str "delete from kayttaja where uid = 'foobar-uid'"))))))

(deftest ^:integraatio with-kayttaja-ldap-reaaliaikainen-haku
  (with-ldap-haku-mock
    (is #(with-kayttaja "foobar-uid" nil nil
           true))))

(deftest ^:integraatio ldap-kayttajaa-ei-loydy
 (is (thrown-with-msg? IllegalStateException #"Ei voimassaolevaa käyttäjää impossiblator"
       (with-ldap-haku-mock
         #(with-kayttaja "impossiblator" nil nil
            true)))))
