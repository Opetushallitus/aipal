(ns aipal.rest-api.kysely-test
  (:require [aipal.rest-api.kysely :refer [lisaa-kysymysryhma! paivita-kysely! lisakysymysten-lukumaara]]
            [aipal.rest-api.rest-util :refer [rest-kutsu body-json]]
            aipal.toimiala.kayttajaoikeudet)
  (:use clojure.test))

(def kysely->kysymys (atom {}))
(def kysely->kysymysryhma (atom {}))
(def kysymysryhma->kysymys (atom {}))

(defn tyhjaa-fake-kanta! []
  (reset! kysely->kysymys {})
  (reset! kysely->kysymysryhma {})
  (reset! kysymysryhma->kysymys {}))

(defn fake-kysely-fixture [f]
  (tyhjaa-fake-kanta!)
  (with-redefs [(swap! kysely->kysymys update-in [kyselyid] (fnil conj #{}) kysymysid)
                aipal.arkisto.kysely/poista-kysymykset! (fn [kyselyid]
                                                          (swap! kysely->kysymys dissoc kyselyid))
                aipal.arkisto.kysely/lisaa-kysymysryhma! (fn [kyselyid kysymysryhma]
                                                           (swap! kysely->kysymysryhma update-in [kyselyid] (fnil conj #{}) (:kysymysryhmaid kysymysryhma)))
                aipal.arkisto.kysely/poista-kysymysryhmat! (fn [kyselyid]
                                                             (swap! kysely->kysymysryhma dissoc kyselyid))
                aipal.arkisto.kysely/hae-kysymysten-poistettavuus (fn [kysymysryhmaid] (@kysymysryhma->kysymys kysymysryhmaid))
                aipal.arkisto.kysely/muokkaa-kyselya! identity
                aipal.toimiala.kayttajaoikeudet/kysely-luonti? (constantly true)
                aipal.toimiala.kayttajaoikeudet/kysymysryhma-luku? (constantly true)
                aipal.toimiala.kayttajaoikeudet/kysymysryhma-on-julkaistu? (constantly true)

                aipal.arkisto.kysely/lisaa! identity
                aipal.arkisto.kysely/samanniminen-kysely? (constantly false)]
    (f)))

(defn aseta-fake-kannan-kysymysryhma! [kysymysryhmaid kysymykset]
  (doseq [kysymys kysymykset]
    (swap! kysymysryhma->kysymys update-in [kysymysryhmaid] (fnil conj #{}) kysymys)))

(use-fixtures :each fake-kysely-fixture)

(deftest kysymysryhma-lisays
  (testing "kyselyyn voi lisätä olemassolevan kysymysryhmän"
    (let [kyselyid 99
          kysymysryhmaid 88]
      (aseta-fake-kannan-kysymysryhma! kysymysryhmaid [{:kysymysid 2}])
      (lisaa-kysymysryhma! kyselyid {:kysymysryhmaid kysymysryhmaid :kysymykset [{:kysymysid 2}]})
      (is (= (@kysely->kysymys kyselyid) #{2}))
      (is (= (@kysely->kysymysryhma kyselyid)  #{kysymysryhmaid})))))

(deftest kysymysryhma-lisays-vaaria-kysymyksia
  (testing "kyselyyn ei voi lisätä tuntemattomia kysymyksiä"
    (let [kyselyid 99
          kysymysryhmaid 88]
      (aseta-fake-kannan-kysymysryhma! kysymysryhmaid [{:kysymysid 2}])
      (lisaa-kysymysryhma! kyselyid {:kysymysryhmaid kysymysryhmaid :kysymykset [{:kysymysid 2}{:kysymysid 3}]})
      (is (= (@kysely->kysymys kyselyid) #{2}))
      (is (= (@kysely->kysymysryhma kyselyid)  #{kysymysryhmaid})))))

(deftest kysymysryhma-lisays-poistettuja-kysymyksia
  (testing "kyselystä voidaan poistaa poistettavia kysymyksiä"
    (let [kyselyid 99
          kysymysryhmaid 88]
      (aseta-fake-kannan-kysymysryhma! kysymysryhmaid [{:kysymysid 2 :poistettava true} {:kysymysid 3 :poistettava true}])
      (lisaa-kysymysryhma! kyselyid {:kysymysryhmaid kysymysryhmaid :kysymykset [{:kysymysid 2 :poistettu true}{:kysymysid 3}]})
      (is (= (@kysely->kysymys kyselyid) #{3})))))

(deftest kysymysryhma-lisays-vaara-poistettu-kysymys
  (testing "kyselystä ei voi poistaa kysymyksiä jotka ei ole merkitty poistettavaksi"
    (let [kyselyid 99
          kysymysryhmaid 88]
      (aseta-fake-kannan-kysymysryhma! kysymysryhmaid [{:kysymysid 2 :poistettava false} {:kysymysid 3}])
      (is (thrown?
            Throwable
            (lisaa-kysymysryhma! kyselyid {:kysymysryhmaid kysymysryhmaid :kysymykset [{:kysymysid 2 :poistettu true}{:kysymysid 3}]}))))))

(deftest kysely-paivitys-uusia-kysymyksia
  (testing "kyselyn kysymykset päivittyvät käyttäjän syötteen mukaiseksi"
    (let [kyselyid 99]
      (aseta-fake-kannan-kysymysryhma! 88 [{:kysymysid 2} {:kysymysid 3}])
      (reset! kysely->kysymys {kyselyid #{1 2 3}})
      (reset! kysely->kysymysryhma {kyselyid #{1 2}})
      (paivita-kysely! {:kyselyid kyselyid :kysymysryhmat [{:kysymysryhmaid 88 :kysymykset [{:kysymysid 2} {:kysymysid 3}]}]})
      (is (= (@kysely->kysymys kyselyid) #{2 3}))
      (is (= (@kysely->kysymysryhma kyselyid) #{88})))))

(deftest lisakysymysten-lukumaara-test
  (testing "pitäisi laskea vain lisäkysymykset"
    (let [kysymysryhmat [{:valtakunnallinen true :kysymykset [{:kysymysid 1}]}
                         {:valtakunnallinen false :kysymykset [{:kysymysid 2}]}]]
      (is (= (lisakysymysten-lukumaara kysymysryhmat) 1))))
  (testing "pitäisi laskea kysymykset kaikista lisäkysymysryhmistä"
    (let [kysymysryhmat [{:valtakunnallinen false :kysymykset [{:kysymysid 1}]}
                         {:valtakunnallinen false :kysymykset [{:kysymysid 2}]}]]
      (is (= (lisakysymysten-lukumaara kysymysryhmat) 2))))
  (testing "ei pitäisi laskea poistettuja"
    (let [kysymysryhmat [{:valtakunnallinen false :kysymykset [{:kysymysid 1 :poistettu true} {:kysymysid 2}]}]]
      (is (= (lisakysymysten-lukumaara kysymysryhmat) 1)))))

; needs to have mocked kayttaja and permissions, disable for now
;(deftest kysely-uusi-kysymys-rest-test
;  (let [params {:url "/api/kysely/" :method :post :params {}
;                 :body {:nimi_fi "f00" :kysymysryhmat [] :voimassa_alkupvm "2015-11-18" :uudelleenohjaus_url "http://www.hs.fi"}}]
;    (testing "pitäisi onnistua normaalisti oikealla datalla"
;      (let [response (rest-kutsu (:url params) (:method params) (:params params))]
;        (is (= (:status response) 200))))
;    (testing "pitäisi epäonnistua väärällä urlilla"
;      (let [response (rest-kutsu (:url params) (:method params) (:params params) (assoc (:body params) :uudelleenohjaus_url "http:/w"))]
;        (is (= (:status response) 400))))))

