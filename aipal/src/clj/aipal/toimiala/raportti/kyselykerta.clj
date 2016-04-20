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

(ns aipal.toimiala.raportti.kyselykerta
  (:require [clj-time.core :as time]
            [korma.core :as sql]
            [aipal.integraatio.sql.korma :refer [kyselykerta]]
            [aipal.toimiala.raportti.kyselyraportointi :as kyselyraportointi]
            [aipal.toimiala.raportti.raportointi :as raportointi]))

(defn ^:private hae-kyselykerta [kyselykertaid]
  (->
    (sql/select* kyselykerta)
    (sql/join :inner :kysely
              (= :kysely.kyselyid :kyselykerta.kyselyid))
    (sql/join :inner :kysely_organisaatio_view
              (= :kysely_organisaatio_view.kyselyid :kyselykerta.kyselyid))
    (sql/join :inner :koulutustoimija
              (= :koulutustoimija.ytunnus :kysely_organisaatio_view.koulutustoimija))
    (sql/fields :kyselykerta.kyselykertaid [:kyselykerta.nimi :kyselykerta] :kyselykerta.voimassa_alkupvm :kyselykerta.voimassa_loppupvm
                [:kysely.nimi_fi :kysely_fi] [:kysely.nimi_sv :kysely_sv] [:kysely.nimi_en :kysely_en]
                [:koulutustoimija.nimi_fi :koulutustoimija_fi] [:koulutustoimija.nimi_sv :koulutustoimija_sv] [:koulutustoimija.nimi_en :koulutustoimija_en])
    (sql/where {:kyselykertaid kyselykertaid})

    sql/exec
    first))

(defn muodosta-yhteenveto [kyselykertaid parametrit]
  (when-let [kyselykerta (hae-kyselykerta kyselykertaid)]
    (let [koulutustoimijatiedot (kyselyraportointi/hae-vastaajatunnusten-tiedot-koulutustoimijoittain (merge kyselykerta parametrit))]
      (assoc kyselykerta :koulutustoimijat koulutustoimijatiedot))))

(defn muodosta-raportti [kyselykertaid parametrit]
  (when-let [kyselykerta (hae-kyselykerta kyselykertaid)]
    (let [parametrit (merge kyselykerta parametrit)
          koulutustoimijatiedot (kyselyraportointi/hae-vastaajatunnusten-tiedot-koulutustoimijoittain parametrit)]
      {:luontipvm (time/now)
       :vastaajien_maksimimaara (kyselyraportointi/hae-vastaajien-maksimimaara parametrit)
       :vastaajien_lukumaara (kyselyraportointi/laske-vastaajat-yhteensa koulutustoimijatiedot)
       :raportti (map raportointi/laske-kysymysryhman-vastaajat (kyselyraportointi/muodosta-raportti parametrit))
       :nimi (:kyselykerta kyselykerta)})))
