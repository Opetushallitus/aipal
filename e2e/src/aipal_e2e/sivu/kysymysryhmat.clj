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

(ns aipal-e2e.sivu.kysymysryhmat
  (:require [clj-webdriver.taxi :as w]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.util :refer [odota-kunnes odota-ja-klikkaa]]))

(def kysymysryhmat-sivu "/#/kysymysryhmat")

(defn avaa-sivu []
  (avaa kysymysryhmat-sivu)
  (odota-kunnes (w/present? {:xpath "//*[@id=\"content\"]"})))

(defn luo-uusi []
  (odota-ja-klikkaa {:css ".e2e-luo-uusi-kysymysryhma"}))

(defn julkaise []
  (odota-ja-klikkaa {:css ".e2e-julkaise-kysymysryhma"}))

(defn vahvista-julkaisu []
  (odota-kunnes (w/present? {:css ".modal-dialog"}))
  (odota-ja-klikkaa {:css ".e2e-vahvista-kysymysryhman-julkaisu"}))

(defn palauta []
  (odota-ja-klikkaa {:xpath "//*[@id=\"content\"]/div/div[3]/table/tbody/tr[2]/td[3]/span[2]/span/button[1]"}))

(defn vahvista-palautus []
  (odota-kunnes (w/present? {:css ".modal-dialog"}))
  (odota-ja-klikkaa {:css ".e2e-palvelut-varmistus-vahvista"}))

(defn poista []
  (odota-ja-klikkaa {:xpath "//*[@id=\"content\"]/div/div[5]/table/tbody/tr[2]/td[3]/span[3]/span/button[2]"}))

(defn vahvista-poisto []
  (odota-kunnes (w/present? {:css ".modal-dialog"}))
  (odota-ja-klikkaa {:css ".e2e-palvelut-varmistus-vahvista"}))

(defn nimella [nimi]
  (w/find-elements {:text nimi
                    :tag :td}))
