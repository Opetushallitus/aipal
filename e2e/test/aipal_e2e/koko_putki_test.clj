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

(ns aipal-e2e.koko-putki-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clj-webdriver.taxi :as w]
            [aipal-e2e.util :refer :all]
            [aipal-e2e.data-util :refer :all]
            [aipal-e2e.sivu.kysely :as kysely-sivu]
            [aipal-e2e.sivu.kyselykerta :as kyselykerta-sivu]
            [aipal-e2e.sivu.kyselyt :as kyselyt-sivu]
            [aipal-e2e.sivu.kysymysryhma :as kysymysryhma-sivu]
            [aipal-e2e.sivu.kysymysryhmat :as kysymysryhmat-sivu]
            [aipal-e2e.sivu.kyselykertaraportti :as kyselykertaraportti-sivu]
            [aipalvastaus-e2e.sivu.vastaus :as vastaus-sivu]
            [aipalvastaus-e2e.util :as aipalvastaus]))

(defn vastaa-kyselyyn! [vastaajatunnus-url]
  (vastaus-sivu/avaa-sivu vastaajatunnus-url "UUSI KYSELY")
  (vastaus-sivu/valitse-ensimmaisen-kysymyksen-ensimmainen-vaihtoehto)
  (vastaus-sivu/tallenna-vastaukset)
  (is (vastaus-sivu/vastaaminen-onnistui?)))

(deftest koko-putki-test
  (with-webdriver
    (with-data {:koulutustoimija [{:ytunnus "0000000-0"}] ; ei käytetä
                :rooli_organisaatio [{:organisaatio "9876543-2" ; sama kuin CI-koneella
                                      :rooli "YLLAPITAJA" ; ylläpitäjä, jotta koulutustoimijan valinta tunnusten luonnissa
                                      :kayttaja "OID.AIPAL-E2E"
                                      :voimassa true}]
                :koulutusala [{:koulutusalatunnus "1"}] ; ei käytetä
                :opintoala [{:opintoalatunnus "123" ; ei käytetä
                             :koulutusala "1"}]
                :tutkinto [{:tutkintotunnus "123456" ; ei käytetä
                            :nimi_fi "Tutkinto"
                            :opintoala "123"}]}
      ;; luo kysymysryhma
      (kysymysryhmat-sivu/avaa-sivu)
      ;(let [kysymysryhma-on-jo (-> (w/find-elements {:xpath "//*[@id=\"content\"]/div/div[3]/table/tbody/tr[2]/td"}) (w/text) (.contains "Uusi kysymysryhmä"))]
      ;  (when (not kysymysryhma-on-jo)
          (kysymysryhmat-sivu/luo-uusi)

          (kysymysryhma-sivu/aseta-kysymysryhman-nimi-suomeksi "Uusi kysymysryhmä")
          (kysymysryhma-sivu/luo-uusi-kysymys)
          (kysymysryhma-sivu/aseta-kysymys-suomeksi "Uusi kysymys")
          (kysymysryhma-sivu/lisaa-kysymys)
          (kysymysryhma-sivu/tallenna-kysymysryhma)

          ;; julkaise kysymysryhmä
          (kysymysryhmat-sivu/julkaise)
          (kysymysryhmat-sivu/vahvista-julkaisu)
      ;))

      ;; luo kysely
      (kyselyt-sivu/avaa-sivu)
      (kyselyt-sivu/luo-uusi-kysely)

      (kysely-sivu/aseta-kyselyn-nimi-suomeksi "Uusi kysely")
      ; ARVO
      (kysely-sivu/aseta-kyselyn-voimassaolon-alku)

      (kysely-sivu/lisaa-kysymysryhma)
      (kysely-sivu/valitse-kysymysryhma "Uusi kysymysryhmä")
      (kysely-sivu/lisaa-valittu-kysymysryhma)
      (kysely-sivu/tallenna-kysely)

      ;; julkaise kysely
      (kyselyt-sivu/avaa-ensimmainen-kysely)
      (kyselyt-sivu/julkaise-kysely)
      (kyselyt-sivu/vahvista-kyselyn-julkaisu)

      ;; luo kyselykerta
      (kyselyt-sivu/luo-uusi-kyselykerta)

      (kyselykerta-sivu/aseta-kyselykerran-nimi "Uusi kyselykerta")
      (kyselykerta-sivu/tallenna-kyselykerta)

      ;väliaikainen testi toistoille
      ;(kyselykerta-sivu/avaa-vastaajatunnukset)

      ;; luo vastaajatunnuksia
      (kyselykerta-sivu/luo-vastaajatunnuksia)
      (kyselykerta-sivu/valitse-vastaajatunnusten-maara "5")
      ; ARVO \
      (kyselykerta-sivu/valitse-vastaajatunnusten-tutkinto "Sairaanhoitaja")
      ;(kyselykerta-sivu/valitse-vastaajatunnusten-koulutuksen_jarjestaja "Ruikonperä")
      ; ARVO /
      (kyselykerta-sivu/lisaa-vastaajatunnukset)

      ;; vastaa kyselyyn
      (let [vastaajatunnus-url (kyselykerta-sivu/ensimmaisen-vastaajatunnuksen-url)]
        (try
          (dotimes [_ 5]
            (vastaa-kyselyyn! vastaajatunnus-url))
          (kyselyt-sivu/avaa-sivu)
          (kyselyt-sivu/nayta-raportti)
          (is (= (kyselykertaraportti-sivu/ensimmaisen-kysymyksen-ensimmaisen-vaihtoehdon-vastausten-lukumaara)
                 "5"))
          (finally
            (aipalvastaus/poista-vastaajat-ja-vastaukset-vastaustunnukselta! vastaajatunnus-url)))
      )

    ;; siivoa putki
    ;; siivoa
    (kyselyt-sivu/avaa-sivu)
    (kyselyt-sivu/avaa-kysely "Uusi kysely")
    (kyselyt-sivu/poista-kyselykerta)
    (kyselyt-sivu/vahvista-kyselykerran-poisto)
    ; lataa uudestaan, niin päivittyy painikkeet...
    (kysymysryhmat-sivu/avaa-sivu)
    (kyselyt-sivu/avaa-sivu)
    (odota-kunnes (w/present? {:xpath "//*[@id=\"content\"]"}))
    (kyselyt-sivu/avaa-kysely "Uusi kysely")
    (kyselyt-sivu/palauta-luonnokseksi "Uusi kysely")
    (kyselyt-sivu/poista-kysely)
    (kyselyt-sivu/vahvista-kyselyn-poisto)

    (kysymysryhmat-sivu/avaa-sivu)
    (kysymysryhmat-sivu/palauta)
    (kysymysryhmat-sivu/vahvista-palautus)
    (kysymysryhmat-sivu/poista)
    (kysymysryhmat-sivu/vahvista-poisto)

  )))
