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

(ns aipal-e2e.sivu.kysely
  (:require [clj-webdriver.taxi :as w]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.util :refer [odota-angular-pyyntoa odota-kunnes syota-kenttaan]]))

(defn aseta-kyselyn-nimi-suomeksi [nimi]
  (syota-kenttaan "kysely.nimi_fi" nimi))

(defn lisaa-kysymysryhma []
  (w/click {:css ".e2e-lisaa-kysymysryhma"})
  (odota-angular-pyyntoa))

(defn valitse-kysymysryhma [nimi]
  (odota-kunnes (w/present? {:css ".e2e-valittavat-kysymysryhmat"}))
  (odota-angular-pyyntoa)
  (w/implicit-wait 3000)
  (w/select-option {:css ".e2e-valittavat-kysymysryhmat"} {:text nimi}))

(defn lisaa-valittu-kysymysryhma []
  (let [lisaa-valittu {:css ".e2e-lisaa-valittu-kysymysryhma"}]
    (odota-kunnes (w/enabled? lisaa-valittu))
    (w/click lisaa-valittu)
    (odota-angular-pyyntoa)))

(defn tallenna-kysely []
  (w/click {:css ".e2e-tallenna-kysely"})
  (odota-angular-pyyntoa))
