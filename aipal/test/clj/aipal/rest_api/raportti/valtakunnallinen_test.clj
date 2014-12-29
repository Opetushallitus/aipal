(ns aipal.rest-api.raportti.valtakunnallinen_test
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [aipal.rest-api.raportti.valtakunnallinen :as valtakunnallinen]))

; Vertailun aineisto vuosi taaksepäin raportointijakson loppupäivästä.
; Eli raportoinnin rajaus voi olla 1kk, mutta vertailuluku lasketaan vuoden aineistosta.
; Jos raportoinnin rajaus on yli 1 vuosi, otetaan vertailuluku samalta ajalta.
(deftest vertailuraportti-vertailujakso-test
  (testing "alkupvm ja loppupvm asetettu ja alle vuosi, vertailu vuosi taaksepäin"
    (is (=
          {:vertailujakso_alkupvm "2013-12-29"
           :vertailujakso_loppupvm "2014-12-29"}
          (valtakunnallinen/vertailuraportti-vertailujakso "2014-01-01" "2014-12-29"))))
  (testing "alkupvm ja loppupvm asetettu ja yli vuosi"
    (is (=
          {:vertailujakso_alkupvm "2013-01-01"
           :vertailujakso_loppupvm "2014-12-01"}
          (valtakunnallinen/vertailuraportti-vertailujakso "2013-01-01" "2014-12-01"))))
  (testing "loppupvm asetettu"
    (is (=
          {:vertailujakso_alkupvm nil
           :vertailujakso_loppupvm "2014-12-31"}
          (valtakunnallinen/vertailuraportti-vertailujakso nil "2014-12-31"))))
  (testing "alkupvm asetettu"
    (is (=
          {:vertailujakso_alkupvm "2013-01-01"
           :vertailujakso_loppupvm nil}
          (valtakunnallinen/vertailuraportti-vertailujakso "2013-01-01" nil))))
  (testing "alkupvm asetettu, ja alle vuoden tästä päivämäärästä"
    (is (=
          {:vertailujakso_alkupvm (.toString (t/minus (t/today) (t/years 1)))
           :vertailujakso_loppupvm nil}
          (valtakunnallinen/vertailuraportti-vertailujakso (.toString (t/today)) nil))))
  (testing "alkupvm ja loppupvm ei asetettu"
    (is (=
          {:vertailujakso_alkupvm nil
           :vertailujakso_loppupvm nil}
          (valtakunnallinen/vertailuraportti-vertailujakso nil nil)))))
