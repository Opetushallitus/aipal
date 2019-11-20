;; Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.toimiala.raportti.kysely
  (:require [clj-time.core :as time]
            [korma.core :as sql]
            [aipal.integraatio.sql.korma :refer [kyselykerta]]
            [oph.korma.common :refer [select-unique-or-nil]]
            [aipal.arkisto.kysely :as arkisto]
            [aipal.arkisto.kysymysryhma :as kysymysryhma-arkisto]
            [aipal.toimiala.raportti.kyselyraportointi :as kyselyraportointi]
            [aipal.toimiala.raportti.raportointi :as raportointi]
            [aipal.toimiala.raportti.valtakunnallinen :as valtakunnallinen-raportti]))

(defn ^:private hae-kysely [kyselyid]
  (select-unique-or-nil :kysely
    (sql/join :inner :kysely_organisaatio_view
              (= :kysely_organisaatio_view.kyselyid :kysely.kyselyid))
    (sql/join :inner :koulutustoimija
              (= :koulutustoimija.ytunnus :kysely_organisaatio_view.koulutustoimija))
    (sql/fields :kysely.kyselyid [:kysely.nimi_fi :kysely_fi] [:kysely.nimi_sv :kysely_sv] [:kysely.nimi_en :kysely_en] :kysely.voimassa_alkupvm :kysely.voimassa_loppupvm
                [:koulutustoimija.nimi_fi :koulutustoimija_fi] [:koulutustoimija.nimi_sv :koulutustoimija_sv] [:koulutustoimija.nimi_en :koulutustoimija_en])
    (sql/where {:kyselyid kyselyid})))

(defn muodosta-yhteenveto [kyselyid parametrit]
  (when-let [kysely (hae-kysely kyselyid)]
    (let [koulutustoimijatiedot (kyselyraportointi/hae-vastaajatunnusten-tiedot-koulutustoimijoittain (merge kysely parametrit))]
      (assoc kysely :koulutustoimijat koulutustoimijatiedot))))

(defn muodosta-valtakunnallinen-vertailuraportti [kyselyid parametrit]
  (when-let [taustakysymysryhmaid (arkisto/hae-kyselyn-taustakysymysryhmaid kyselyid)]
    (let [kysymysidt (kysymysryhma-arkisto/hae-kysymysryhman-kysymyksien-idt taustakysymysryhmaid)
          parametrit (assoc parametrit :taustakysymysryhmaid (str taustakysymysryhmaid)
                                       :tyyppi "vertailu"
                                       :tutkintorakennetaso "tutkinto"
                                       :kysymykset (into {} (for [kysymysid kysymysidt]
                                                              {kysymysid {:monivalinnat {}}})))
          raportti (valtakunnallinen-raportti/muodosta parametrit)]
      (assoc raportti :parametrit parametrit))))

(defn muodosta-raportti [kyselyid parametrit]
  (when-let [kysely (hae-kysely kyselyid)]
    (let [parametrit (merge kysely parametrit)
          koulutustoimijatiedot (kyselyraportointi/hae-vastaajatunnusten-tiedot-koulutustoimijoittain parametrit)
          tutkinto-otsikko (valtakunnallinen-raportti/raportin-otsikko (merge parametrit
                                                                              {:tyyppi "vertailu"
                                                                               :tutkintorakennetaso "tutkinto"}))]
      (merge {:luontipvm (time/now)
              :vastaajien_maksimimaara (kyselyraportointi/hae-vastaajien-maksimimaara parametrit)
              :vastaajien_lukumaara (kyselyraportointi/laske-vastaajat-yhteensa koulutustoimijatiedot)
              :raportti (map raportointi/laske-kysymysryhman-vastaajat (kyselyraportointi/muodosta-raportti parametrit))
              :nimi_fi (:kysely_fi kysely)
              :nimi_sv (:kysely_sv kysely)
              :nimi_en (:kysely_en kysely)}
             tutkinto-otsikko))))
