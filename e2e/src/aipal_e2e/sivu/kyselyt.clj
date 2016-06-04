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
            [aitu-e2e.util :refer [odota-angular-pyyntoa odota-ja-klikkaa odota-kunnes]]
            [aipal-e2e.util :refer :all]))

(def kyselyt-sivu "/#/kyselyt")

(defn avaa-sivu []
  (avaa kyselyt-sivu)
  (odota-kunnes (w/present? {:xpath "//*[@id=\"content\"]"})))

(defn luo-uusi-kysely []
  (odota-ja-klikkaa {:css ".e2e-luo-uusi-kysely"}))

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

(defn nayta-raportti []
  (w/click ".e2e-nayta-raportti"))

(defn kysely-linkki [kysely-elementti]
  (w/find-element-under kysely-elementti
                        {:css ".panel-heading"}))

(defn avaa-kysely [kyselyn-nimi]
  (let [kysely-elementti (w/find-element {:tag :h3, :text kyselyn-nimi})]
    ;(w/flash (w/find-element kysely-elementti {:xpath "../../.."}))
    (let [kysely-auki (-> (w/find-element kysely-elementti {:xpath "../../../../.."}) (w/attribute :class) (.contains "panel-open"))]
      (when (not kysely-auki)
        ; voi klikata nimeäkin, mutta *elementti* olisi 5 ylös
        ;(w/click (w/find-element kysely-elementti {:xpath "../../../../.."}))
        (w/click kysely-elementti)))))

(defn palauta-luonnokseksi [kyselyn-nimi]
  (let [kysely-elementti (w/find-element {:tag :h3, :text kyselyn-nimi})]
    (-> (w/find-element kysely-elementti {:xpath "../../../../.."})
      (w/click "button[ng-click=\"palautaLuonnokseksi(kysely)\"]"))))

(defn poista-kysely []
  (w/click {:css ".e2e-poista-kysely"})
  (odota-angular-pyyntoa))

(defn vahvista-kyselyn-poisto []
  (w/click {:css ".e2e-palvelut-varmistus-vahvista"})
  (odota-angular-pyyntoa))

(defn poista-kyselykerta []
  (odota-kunnes (w/present? {:xpath "//*[@id=\"content\"]/div/div[3]/uib-accordion/div/div/div[2]/div/div[2]/div[2]/span[3]/span/span/button"}))
  (w/click {:xpath "//*[@id=\"content\"]/div/div[3]/uib-accordion/div/div/div[2]/div/div[2]/div[2]/span[3]/span/span/button"}))

(defn vahvista-kyselykerran-poisto []
  (w/click {:css ".e2e-palvelut-varmistus-vahvista"})
  (odota-angular-pyyntoa))
