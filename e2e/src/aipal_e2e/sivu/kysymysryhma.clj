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

(ns aipal-e2e.sivu.kysymysryhma
  (:require [clj-webdriver.taxi :as w]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.util :refer :all]))

(defn aseta-kysymysryhman-nimi-suomeksi [nimi]
  (syota-kenttaan "kysymysryhma.nimi_fi" nimi))

(defn luo-uusi-kysymys []
  (w/click {:css ".e2e-luo-uusi-kysymys"}))

(defn aseta-kysymys-suomeksi [kysymys]
  (w/input-text {:css ".e2e-kysymys-suomeksi"} kysymys))

(defn lisaa-kysymys []
  (w/click {:css ".e2e-lisaa-kysymys"}))

(defn tallenna-kysymys []
  (w/click {:css ".e2e-tallenna-kysymys"}))

(defn tallenna-kysymysryhma []
  (w/click {:css ".e2e-tallenna-kysymysryhma"})
  (odota-angular-pyyntoa))

(defn kysymysryhman-nimi-fi []
  (w/value (w/find-element {:css ".e2e-kysymysryhma-nimi-fi"})))

(defn kysymys-fi []
  (w/value (w/find-element {:css ".e2e-kysymys-suomeksi"})))
