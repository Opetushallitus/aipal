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
            [oph.korma.korma :refer [select-unique-or-nil]]
            [aipal.toimiala.raportti.kyselyraportointi :as kyselyraportointi]
            [aipal.toimiala.raportti.raportointi :as raportointi]))

(defn ^:private hae-kysely [kyselyid]
  (select-unique-or-nil :kysely
    (sql/join :inner :kysely_organisaatio_view
              (= :kysely_organisaatio_view.kyselyid :kysely.kyselyid))
    (sql/join :inner :koulutustoimija
              (= :koulutustoimija.ytunnus :kysely_organisaatio_view.koulutustoimija))
    (sql/fields :kysely.kyselyid [:kysely.nimi_fi :kysely_fi] [:kysely.nimi_sv :kysely_sv] :kysely.voimassa_alkupvm :kysely.voimassa_loppupvm
                [:koulutustoimija.nimi_fi :koulutustoimija_fi] [:koulutustoimija.nimi_sv :koulutustoimija_sv])
    (sql/where {:kyselyid kyselyid})))

(defn muodosta-raportti [kyselyid]
  (when-let [kysely (hae-kysely kyselyid)]
    (let [koulutustoimijatiedot (kyselyraportointi/hae-vastaajatunnusten-tiedot-koulutustoimijoittain kysely)
          vastaajien-lkm (reduce + (map :vastaajat_yhteensa koulutustoimijatiedot))]
      {:yhteenveto (assoc kysely :koulutustoimijat koulutustoimijatiedot)
       :luontipvm (time/today)
       :vastaajien_maksimimaara (kyselyraportointi/hae-vastaajien-maksimimaara kysely)
       :vastaajien-lkm vastaajien-lkm
       :raportti (kyselyraportointi/muodosta-raportti kysely)})))
