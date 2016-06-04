;; Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
;;
;; This program is free software:  Licensed under the EUPL, Version 1.1 or - as
;; soon as they will be approved by the European Commission - subsequent versions
;; of the EUPL (the "Licence");
;;
;; You may not use this work except in compliance with the Licence.
;; You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; European Union Public Licence for more details.

(ns aipal-e2e.sivu.etusivu
  (:require [clj-webdriver.taxi :as w]
            [clj-webdriver.core :as webdriver]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.util :refer [odota-angular-pyyntoa odota-kunnes]]))

(defn avaa-sivu []
  (avaa "/#/"))

(defn valitse-rooli [index]
  (avaa-sivu)
  (w/click {:css ".avaa-valikko-e2e"})
  (w/click {:css ".vaihda-roolia-e2e"})
  (w/select-option {:css ".rooli-select-e2e"} {:index index})
  (odota-angular-pyyntoa)
  (w/click {:css ".tallenna-rooli-e2e"})
  (odota-angular-pyyntoa))

(defn vaihda-kieli [kieli]
  (when (= kieli "SV")
    (let [kieli-sv (w/find-element {:tag :a, :text "SV"})]
      (w/click kieli-sv)))
  (when (= kieli "EN")
    (let [kieli-en (w/find-element {:tag :a, :text "EN"})]
      (w/click kieli-en)))
  (when (= kieli "FI")
    (let [kieli-fi (w/find-element {:tag :a, :text "FI"})]
      (w/click kieli-fi)))
  (odota-angular-pyyntoa))

(defn klikkaa-muokkaa-tiedote []
  (let [tiedote-painike (w/find-element {:xpath "//*[@id=\"content\"]/div/div/div/div[1]/div/div[3]/button"})]
    (w/click tiedote-painike)
    (odota-kunnes (w/present? {:tag :h3, :text "FI"}))))

(defn lisaa-tiedote-teksti-fi [teksti]
  (let [tiedote-area-fi (w/find-element (w/find-element {:tag :h3, :text "FI"}) {:xpath "../textarea"})]
    (w/input-text tiedote-area-fi teksti)))

(defn lisaa-tiedote-teksti-sv [teksti]
  (let [tiedote-area-sv (w/find-element (w/find-element {:tag :h3, :text "SV"}) {:xpath "../textarea"})]
    (w/input-text tiedote-area-sv teksti)))

(defn lisaa-tiedote-teksti-en [teksti]
  (let [tiedote-area-en (w/find-element (w/find-element {:tag :h3, :text "EN"}) {:xpath "../textarea"})]
    (w/input-text tiedote-area-en teksti)))

(defn klikkaa-tallenna-tiedote []
  (let [tiedote-tallenna-painike (w/find-element {:xpath "//*[@id=\"content\"]/div/div/div/div[2]/div/div[5]/button"})]
    (w/click tiedote-tallenna-painike))
  (odota-angular-pyyntoa))

(defn tyhjenna-tiedote-teksti-fi []
  (let [tiedote-area-fi (w/find-element (w/find-element {:tag :h3, :text "FI"}) {:xpath "../textarea"})]
    (w/clear tiedote-area-fi)))

(defn tyhjenna-tiedote-teksti-sv []
  (let [tiedote-area-sv (w/find-element (w/find-element {:tag :h3, :text "SV"}) {:xpath "../textarea"})]
    (w/clear tiedote-area-sv)))

(defn tyhjenna-tiedote-teksti-en []
  (let [tiedote-area-en (w/find-element (w/find-element {:tag :h3, :text "EN"}) {:xpath "../textarea"})]
    (w/clear tiedote-area-en)))
