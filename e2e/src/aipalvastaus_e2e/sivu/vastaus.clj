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

(ns aipalvastaus-e2e.sivu.vastaus
  (:require [clj-webdriver.taxi :as w]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.tietokanta.yhteys :as tietokanta]
            [aipalvastaus-e2e.util :refer :all]
            [aitu-e2e.util :refer [odota-angular-pyyntoa odota-kunnes]]))

(declare sivun-sisalto)

(defn avaa-sivu [vastaajatunnus-url kyselyn-nimi]
  (w/wait-until
    (fn []
      (w/to vastaajatunnus-url)
      (odota-angular-pyyntoa)
      (.contains (sivun-sisalto) kyselyn-nimi)) 10000 1000))

(defn sivun-sisalto []
  (w/text (w/find-element {:css "body"})))

(defn vastaaminen-onnistui? []
  (w/present? {:css ".e2e-kiitos"}))

(defn valitse-ensimmaisen-kysymyksen-ensimmainen-vaihtoehto []
  (w/select (nth (w/find-elements {:tag :radio}) 0)))

(defn tallenna-vastaukset []
  (let [tallenna-vastaukset {:css ".e2e-tallenna-vastaukset"}]
    (odota-kunnes (w/enabled? tallenna-vastaukset))
    (w/click tallenna-vastaukset)
    (odota-angular-pyyntoa)))
