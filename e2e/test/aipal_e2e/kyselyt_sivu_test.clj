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

(ns aipal-e2e.kyselyt-sivu-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clj-webdriver.taxi :as w]
            [aipal-e2e.util :refer :all]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.sivu.kysely :as kysely-sivu]
            [aipal-e2e.sivu.kyselyt :as kyselyt-sivu]
            [aipal-e2e.sivu.kysymysryhma :as kysymysryhma-sivu]
            [aipal-e2e.sivu.kysymysryhmat :as kysymysryhmat-sivu]
            [clj-time.core :as time]))

(defn kyselyt []
  (w/find-elements (-> *ng*
                     (.repeater "kysely in suodatettu"))))

(defn kyselyn-nimi [kysely-elementti]
  (w/text
    (w/find-element-under kysely-elementti
                          {:css ".e2e-kysely-nimi"})))

(defn kysely-linkki [kysely-elementti]
  (w/find-element-under kysely-elementti
                        {:css ".panel-heading"}))

(defn kyselykerran-nimi [kyselykerta-elementti]
  (w/find-element-under kyselykerta-elementti {:css ".e2e-kyselykerta-nimi"}))

(defn avaa-kysely [kysely-elementti]
  (odota-kunnes (w/present? (kysely-linkki kysely-elementti)))
  (let [kysely-auki (-> kysely-elementti (w/attribute :class) (.contains "panel-open"))]
    (when (not kysely-auki)
      (w/click (kysely-linkki kysely-elementti)))))

(defn ^:private kyselykerrat-kyselylle [kysely-elementti]
  (w/present? (w/find-element-under kysely-elementti {:css ".e2e-kyselykerrat"}))
  (let [kyselykerrat (w/find-elements-under kysely-elementti
                                            {:css ".e2e-kyselykerrat"})]
    (->> kyselykerrat
        (map kyselykerran-nimi)
        (map w/text))))

(defn uusi-kyselykerta-kyselylle [kysely-elementti]
  (odota-kunnes (w/present? (w/find-element-under kysely-elementti {:css ".e2e-uusi-kyselykerta"})))
  (w/find-element-under kysely-elementti {:css ".e2e-uusi-kyselykerta"}))

(defn uusi-kysely []
  (w/find-element {:css ".e2e-luo-uusi-kysely"}))

(deftest kyselyt-sivu-test
  (with-webdriver
    (with-data {;ei lisätä vaan olemassa olevaan! :koulutustoimija [{:ytunnus "2345678-0"}]
                :rooli_organisaatio [{:organisaatio "2345678-0"
                                      :rooli "OPL-VASTUUKAYTTAJA"
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}]}
      ;; luo kysely
      (kyselyt-sivu/avaa-sivu)
      (testing
        "Luo uusi kysely ohjaa kyselyn luontiin"
        (w/click (uusi-kysely))
        (w/wait-until #(re-matches #".*/#/kyselyt/kysely/uusi" (w/current-url)) 10000))

      ;; kysymysryhman luonti
      (kysymysryhmat-sivu/avaa-sivu)
      (kysymysryhmat-sivu/luo-uusi)

      (kysymysryhma-sivu/aseta-kysymysryhman-nimi-suomeksi "Uusi kysymysryhmä")
      (kysymysryhma-sivu/luo-uusi-kysymys)
      (kysymysryhma-sivu/aseta-kysymys-suomeksi "Uusi kysymys")
      (kysymysryhma-sivu/lisaa-kysymys)
      (kysymysryhma-sivu/tallenna-kysymysryhma)

      (kysymysryhmat-sivu/julkaise)
      (kysymysryhmat-sivu/vahvista-julkaisu)

      ;; kyselyn luonti
      (kyselyt-sivu/avaa-sivu)
      (kyselyt-sivu/luo-uusi-kysely)
      (kysely-sivu/aseta-kyselyn-nimi-suomeksi "Kysely 1")
      (kysely-sivu/aseta-kyselyn-voimassaolon-alku)
      (kysely-sivu/lisaa-kysymysryhma)
      (kysely-sivu/valitse-kysymysryhma "Uusi kysymysryhmä")
      (kysely-sivu/lisaa-valittu-kysymysryhma)
      (kysely-sivu/tallenna-kysely)

      (kyselyt-sivu/luo-uusi-kysely)
      (kysely-sivu/aseta-kyselyn-nimi-suomeksi "Kysely 2")
      (kysely-sivu/aseta-kyselyn-voimassaolon-alku)
      (kysely-sivu/lisaa-kysymysryhma)
      (kysely-sivu/valitse-kysymysryhma "Uusi kysymysryhmä")
      (kysely-sivu/lisaa-valittu-kysymysryhma)
      (kysely-sivu/tallenna-kysely)

      (testing
        "Kyselyn julkaisu onnistuu toiselle kyselylle"
        (let [kysely (nth (kyselyt) 0)]
          (kyselyt-sivu/avaa-kysely "Kysely 2")
          (kyselyt-sivu/julkaise-kysely)
          (kyselyt-sivu/vahvista-kyselyn-julkaisu))
        (let [kysely (nth (kyselyt) 0)]
          (is (= (kyselyn-nimi kysely) "Kysely 2")))
      )
      (testing
        "Kyselyn julkaisu onnistuu ensimmäiselle kyselylle"
        (let [kysely (nth (kyselyt) 1)]
          (kyselyt-sivu/avaa-kysely "Kysely 1")
          (kyselyt-sivu/julkaise-kysely)
          (kyselyt-sivu/vahvista-kyselyn-julkaisu))
        (let [kysely (nth (kyselyt) 1)]
          (is (= (kyselyn-nimi kysely) "Kysely 1"))))

      ;; kyselykerran luonti
      (kyselyt-sivu/avaa-sivu)
      (testing
        "Kyselykerran luonti onnistuu ensimmäiselle kyselylle"
        (let [kysely (nth (kyselyt) 1)]
          (kyselyt-sivu/avaa-kysely "Kysely 1")
          (w/click (uusi-kyselykerta-kyselylle kysely))
          (odota-kunnes (w/displayed? (str "input[ng-model=\"kyselykerta.nimi\"]"))) ; ajastusongelman kierto
          (syota-kenttaan "kyselykerta.nimi" "Kyselykerta 1-1")
          (tallenna)
          (kyselyt-sivu/avaa-sivu))
        (let [kysely (nth (kyselyt) 1)]
          (is (= (kyselykerrat-kyselylle kysely) ["Kyselykerta: Kyselykerta 1-1"]))))
      (testing
        "Toisen kyselykerran luonti onnistuu ensimmäiselle kyselylle"
        (let [kysely (nth (kyselyt) 1)]
          (kyselyt-sivu/avaa-kysely "Kysely 1")
          (w/click (uusi-kyselykerta-kyselylle kysely))
          (odota-kunnes (w/displayed? (str "input[ng-model=\"kyselykerta.nimi\"]"))) ; ajastusongelman kierto
          (syota-kenttaan "kyselykerta.nimi" "Kyselykerta 1-2")
          (tallenna)
          (kyselyt-sivu/avaa-sivu))
        (let [kysely (nth (kyselyt) 1)]
          (is (= (kyselykerrat-kyselylle kysely) ["Kyselykerta: Kyselykerta 1-1" "Kyselykerta: Kyselykerta 1-2"]))))
      (testing
        "Kyselykerran luonti onnistuu toiselle kyselylle"
        (let [kysely (nth (kyselyt) 0)]
          (kyselyt-sivu/avaa-kysely "Kysely 2")
          (w/click (uusi-kyselykerta-kyselylle kysely))
          (odota-kunnes (w/displayed? (str "input[ng-model=\"kyselykerta.nimi\"]"))) ; ajastusongelman kierto
          (syota-kenttaan "kyselykerta.nimi" "Kyselykerta 2-3")
          (tallenna)
          (kyselyt-sivu/avaa-sivu))
        (let [kysely (nth (kyselyt) 0)]
          (is (= (kyselykerrat-kyselylle kysely) ["Kyselykerta: Kyselykerta 2-3"]))))

      ;; kyselyt sivu
      (kyselyt-sivu/avaa-sivu)
      (testing
        "Ensimmäisellä kyselyllä on kaksi kyselykertaa"
        (let [kysely (nth (kyselyt) 1)]
          (kyselyt-sivu/avaa-kysely "Kysely 1")
          (is (= (kyselyn-nimi kysely) "Kysely 1"))
          (is (= (kyselykerrat-kyselylle kysely) ["Kyselykerta: Kyselykerta 1-1" "Kyselykerta: Kyselykerta 1-2"]))))
      (testing
        "Toisella kyselyllä on yksi kyselykerta"
        (let [kysely (nth (kyselyt) 0)]
          (kyselyt-sivu/avaa-kysely "Kysely 2")
          (is (= (kyselyn-nimi kysely) "Kysely 2"))
          (is (= (kyselykerrat-kyselylle kysely) ["Kyselykerta: Kyselykerta 2-3"]))))

      ;; kyselykerran poisto
      (kyselyt-sivu/avaa-sivu)
      (kyselyt-sivu/avaa-kysely "Kysely 2")
      (kyselyt-sivu/poista-kyselykerta)
      (kyselyt-sivu/vahvista-kyselykerran-poisto)

      (kyselyt-sivu/avaa-kysely "Kysely 1")
      (kyselyt-sivu/poista-kyselykerta)
      (kyselyt-sivu/vahvista-kyselykerran-poisto)
      ; 2x poista kyselykerta
      (kyselyt-sivu/poista-kyselykerta)
      (kyselyt-sivu/vahvista-kyselykerran-poisto)
      ; tässä välissä kyselyt-sivu on syytä ladata tavalla tai toisella uudestaan,
      ; jotta painikkeet päivittyy!
      (kysymysryhmat-sivu/avaa-sivu)

      ;; kyselyn poisto
      (kyselyt-sivu/avaa-sivu)
      (odota-kunnes (w/present? {:xpath "//*[@id=\"content\"]"}))

      (kyselyt-sivu/avaa-kysely "Kysely 1")
      (kyselyt-sivu/palauta-luonnokseksi "Kysely 1")
      (kyselyt-sivu/poista-kysely)
      (kyselyt-sivu/vahvista-kyselyn-poisto)

      (kyselyt-sivu/avaa-kysely "Kysely 2")
      (kyselyt-sivu/palauta-luonnokseksi "Kysely 2")
      (kyselyt-sivu/poista-kysely)
      (kyselyt-sivu/vahvista-kyselyn-poisto)

      ;; kysymysryhman poisto
      (kysymysryhmat-sivu/avaa-sivu)
      (kysymysryhmat-sivu/palauta)
      (kysymysryhmat-sivu/vahvista-palautus)
      (kysymysryhmat-sivu/poista)
      (kysymysryhmat-sivu/vahvista-poisto))))
