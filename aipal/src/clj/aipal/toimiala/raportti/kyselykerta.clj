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
                [:kysely.nimi_fi :kysely_fi] [:kysely.nimi_sv :kysely_sv]
                [:koulutustoimija.nimi_fi :koulutustoimija_fi] [:koulutustoimija.nimi_sv :koulutustoimija_sv])
    (sql/where {:kyselykertaid kyselykertaid})

    sql/exec
    first))

(defn ^:private muodosta-raportti-perustiedot [kyselykertaid]
  (when-let [kyselykerta (hae-kyselykerta kyselykertaid)]
    (let [koulutustoimijat (kyselyraportointi/hae-vastaajatunnusten-tiedot-koulutustoimijoittain kyselykerta)]
      {:yhteenveto (assoc kyselykerta :koulutustoimijat koulutustoimijat
                                      :vastaajat_yhteensa (kyselyraportointi/laske-vastaajat-yhteensa koulutustoimijat))
       :luontipvm (time/today)
       :vastaajien_maksimimaara (kyselyraportointi/hae-vastaajien-maksimimaara kyselykerta)})))

(defn ^:private laske-vastaajat [kyselykertaid]
  (-> (sql/select :vastaaja
                  (sql/aggregate (count :*) :vastaajia)
                  (sql/where {:kyselykertaid kyselykertaid}))
      first
      :vastaajia))

(defn muodosta-raportti [kyselykertaid]
  (let [perustiedot (muodosta-raportti-perustiedot kyselykertaid)]
    (when perustiedot
      (assoc perustiedot :raportti (kyselyraportointi/muodosta-raportti (:yhteenveto perustiedot))
                         :vastaajien-lkm (laske-vastaajat kyselykertaid)))))
