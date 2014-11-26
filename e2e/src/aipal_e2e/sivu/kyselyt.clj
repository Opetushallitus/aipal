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

(ns aipal-e2e.sivu.kyselyt
  (:require [clj-webdriver.taxi :as w]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.util :refer [odota-angular-pyyntoa]]))

(def kyselyt-sivu "/#/kyselyt")

(defn avaa-sivu []
  (avaa kyselyt-sivu))

(defn luo-uusi-kysely []
  (w/click {:css ".e2e-luo-uusi-kysely"}))

(defn avaa-ensimmainen-kysely []
  (w/click {:css ".e2e-kysely-nimi"}))

(defn julkaise-kysely []
  (w/click {:css ".e2e-julkaise-kysely"})
  (odota-angular-pyyntoa))

(defn vahvista-kyselyn-julkaisu []
  (w/click {:css ".e2e-palvelut-varmistus-vahvista"})
  (odota-angular-pyyntoa))

(defn luo-uusi-kyselykerta []
  (w/click {:css ".e2e-uusi-kyselykerta"}))
