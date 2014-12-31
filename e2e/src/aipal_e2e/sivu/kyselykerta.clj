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

(ns aipal-e2e.sivu.kyselykerta
  (:require [clj-webdriver.taxi :as w]
            [aipal-e2e.util :refer :all]
            [aitu-e2e.util :refer [odota-angular-pyyntoa odota-kunnes syota-kenttaan]]))

(defn aseta-kyselykerran-nimi [nimi]
  (syota-kenttaan "kyselykerta.nimi" nimi))

(defn tallenna-kyselykerta []
  (w/click {:css ".e2e-tallenna-kyselykerta"})
  (odota-angular-pyyntoa))

(defn luo-vastaajatunnuksia []
  (odota-kunnes (w/present? {:css ".e2e-luo-vastaajatunnuksia"}))
  (w/click {:css ".e2e-luo-vastaajatunnuksia"}))

(defn valitse-vastaajatunnuksen-rahoitusmuoto [rahoitusmuoto]
  (w/select-by-text ".e2e-vastaajatunnuksen-rahoitusmuoto" rahoitusmuoto))

(defn valitse-vastaajatunnuksen-tutkinto [tutkinto]
  (syota-kenttaan "vastaajatunnus.tutkinto" tutkinto)
  (odota-angular-pyyntoa)
  (w/click {:css "ul.dropdown-menu li.active"}))

(defn lisaa-vastaajatunnukset []
  (w/click (w/find-element-under {:css ".e2e-vastaustunnusten-luonti-dialogi"} {:css ".e2e-direktiivit-tallenna"}))
  (odota-angular-pyyntoa))

(defn ensimmaisen-vastaajatunnuksen-url []
  (w/text (w/find-element {:css ".e2e-vastaajatunnus-url"})))
