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
            [aipal-e2e.util :refer :all]))

(defn aseta-kyselykerran-nimi [nimi]
  (syota-kenttaan "kyselykerta.nimi" nimi))

(defn tallenna-kyselykerta []
  (w/click {:css ".e2e-tallenna-kyselykerta"})
  (odota-angular-pyyntoa))

(defn luo-vastaajatunnuksia []
  (odota-kunnes (w/present? {:css ".e2e-luo-vastaajatunnuksia"}))
  (w/click {:css ".e2e-luo-vastaajatunnuksia"})
  (odota-kunnes (w/present? {:css ".modal-dialog"})))

; ARVO - not active, temporarily used when repeating tests
(defn avaa-vastaajatunnukset []
  (w/click {:xpath "/html/body/div[4]/div/div/div[3]/uib-accordion/div/div/div[2]/div/div[2]/div[2]/span[1]/span/a"}))

(defn valitse-vastaajatunnusten-maara [maara]
  (odota-kunnes (w/present? {:css "#vastaajien_maara"}))
  (w/clear "#vastaajien_maara")
  (w/input-text "#vastaajien_maara" maara)
  (w/click ".e2e-vastaajatunnus-henkilokohtainen"))

(defn valitse-vastaajatunnuksen-rahoitusmuoto [rahoitusmuoto]
  (odota-kunnes (w/present? {:css ".e2e-vastaajatunnuksen-rahoitusmuoto"}))
  (w/select-by-text ".e2e-vastaajatunnuksen-rahoitusmuoto" rahoitusmuoto))

; ARVO
(defn valitse-vastaajatunnusten-tutkinto [tutkinto]
  ; valintalista auki
  (odota-kunnes (w/present? {:xpath "/html/body/div[1]/div/div/form/div[1]/div[3]/label/div/div/button[2]"}))
  (w/click {:xpath "/html/body/div[1]/div/div/form/div[1]/div[3]/label/div/div/button[2]"})
  ; syötä haku
  (odota-kunnes (w/present? {:xpath "/html/body/div[1]/div/div/form/div[1]/div[3]/label/div/input[1]"}))
  (w/input-text {:xpath "/html/body/div[1]/div/div/form/div[1]/div[3]/label/div/input[1]"} tutkinto)
  ;valitse listalta (joka on nyt 1:n mittainen)
  (odota-kunnes (w/present? {:xpath "/html/body/div[1]/div/div/form/div[1]/div[3]/label/div/ul/li/div[3]/a/div"}))
  (w/click {:xpath "/html/body/div[1]/div/div/form/div[1]/div[3]/label/div/ul/li/div[3]/a/div"}))

; ARVO
(defn valitse-vastaajatunnusten-koulutuksen_jarjestaja [jarjestaja]
  ; valintalista auki
  (w/click {:xpath "/html/body/div[1]/div/div/form/div[1]/div[5]/label/div/div/button[2]"})
  ; syötä haku
  (odota-kunnes (w/present? {:xpath "/html/body/div[1]/div/div/form/div[1]/div[5]/label/div/input"}))
  (-> (w/find-element {:xpath "/html/body/div[1]/div/div/form/div[1]/div[5]/label/div/input"})
    (w/input-text jarjestaja))
  ; valitse listalta (joka on nyt 1:n mittainen)
  (odota-kunnes (w/present? {:xpath "/html/body/div[1]/div/div/form/div[1]/div[5]/label/div/ul/li/div[3]/a/div"}))
  (w/click {:xpath "/html/body/div[1]/div/div/form/div[1]/div[5]/label/div/ul/li/div[3]/a/div"}))

(defn lisaa-vastaajatunnukset []
  (w/click (w/find-element-under {:css ".e2e-vastaustunnusten-luonti-dialogi"} {:css ".e2e-direktiivit-tallenna"}))
  (odota-angular-pyyntoa))

(defn ensimmaisen-vastaajatunnuksen-url []
  (w/text (w/find-element {:css ".e2e-vastaajatunnus-url"})))
