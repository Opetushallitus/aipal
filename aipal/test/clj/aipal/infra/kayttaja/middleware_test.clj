(ns aipal.infra.kayttaja.middleware-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.sql.test-util :refer [tietokanta-fixture]]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.middleware :refer :all]
            aipal.infra.kayttaja.vaihto))

(defn ldap-fixture [f]
  (with-redefs [aipal.infra.kayttaja.vaihto/hae-kayttaja-kayttoikeuspalvelusta (constantly nil)]
    (f)))

(use-fixtures :each (compose-fixtures tietokanta-fixture ldap-fixture))

;; wrap-kayttaja palauttaa 403, jos CAS-käyttäjä ei ole voimassaoleva Aipal-käyttäjä.
(deftest ^:integraatio wrap-kayttaja-403
  (is (= (:status ((wrap-kayttaja (constantly nil)) {:username "haxor"}))
         403)))

;; Jos käyttäjä on voimassa, suoritetaan seuraava handler.
(deftest ^:integraatio wrap-kayttaja-voimassa
  (sql/insert taulut/kayttaja
    (sql/values {:uid "kayttaja"
                 :oid "k123"
                 :voimassa true}))
  (is (= ((wrap-kayttaja #(:x %)) {:username "kayttaja", :x 123})
         123)))

;; Handlerin suorituksen aikana *kayttaja* on sidottu käyttäjän tietoihin.
(deftest ^:integraatio wrap-kayttaja-sidonta
  (sql/insert taulut/kayttaja
    (sql/values {:uid "kayttaja"
                 :oid "k123"
                 :voimassa true}))
  (is (= ((wrap-kayttaja (fn [_] (:oid *kayttaja*))) {:username "kayttaja"})
         "k123")))

;; Oletuksena ei impersonoida ketään.
(deftest ^:integraatio wrap-kayttaja-ei-impersonointia
  (sql/insert taulut/kayttaja
    (sql/values {:uid "kayttaja"
                 :oid "k123"
                 :voimassa true}))
  (is (= ((wrap-kayttaja (fn [_] (:impersonoidun-kayttajan-nimi *kayttaja*)))
          {:username "kayttaja"})
         "")))

;; Jos istuntoon on tallennettu impersonoitu OID, impersonoidaan ko. käyttäjää.
(deftest ^:integraatio wrap-kayttaja-impersonointi
  (sql/insert taulut/kayttaja
    (sql/values [{:uid "toinen"
                  :oid "t456"
                  :voimassa true
                  :etunimi "Matti"
                  :sukunimi "Meikäläinen"}
                 {:uid "kayttaja"
                  :oid "k123"
                  :voimassa true}]))
  (is (= ((wrap-kayttaja (fn [_] (:impersonoidun-kayttajan-nimi *kayttaja*)))
          {:username "kayttaja"
           :session {:impersonoitu-oid "t456"}})
         "Matti Meikäläinen")))
