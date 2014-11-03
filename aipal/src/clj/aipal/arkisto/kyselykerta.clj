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

(ns aipal.arkisto.kyselykerta
  (:require [korma.core :as sql]
            [aipal.integraatio.sql.korma :as taulut]))

(defn hae-kaikki
  "Hae kaikki koulutustoimijan kyselykerrat"
  [koulutustoimija]
  (sql/select taulut/kyselykerta
    (sql/join :inner taulut/kysely (= :kysely.kyselyid :kyselykerta.kyselyid))
    (sql/join :inner :kysely_organisaatio_view (= :kysely_organisaatio_view.kyselyid :kysely.kyselyid))
    (sql/fields :kyselykerta.kyselyid :kyselykerta.kyselykertaid :kyselykerta.nimi_fi :kyselykerta.nimi_sv :kyselykerta.voimassa_alkupvm :kyselykerta.voimassa_loppupvm)
    (sql/where {:kysely_organisaatio_view.koulutustoimija koulutustoimija})
    (sql/order :kyselykerta.kyselykertaid :ASC)))

(defn lisaa!
  [kyselyid kyselykerta-data]
  (sql/insert taulut/kyselykerta
    (sql/values
      (assoc
        (select-keys kyselykerta-data [:nimi_fi :nimi_sv :voimassa_alkupvm :voimassa_loppupvm :lukittu])
        :kyselyid kyselyid))))

(defn hae-yksi
  "Hae kyselykerta tunnuksella"
  [kyselykertaid]
  (->
    (sql/select* taulut/kyselykerta)
    (sql/fields :kyselyid :kyselykertaid :nimi_fi :nimi_sv :voimassa_alkupvm :voimassa_loppupvm)
    (sql/where (= :kyselykertaid kyselykertaid))
    (sql/order :kyselykerta.kyselykertaid :ASC)
    sql/exec
    first))

(defn paivita!
  [kyselykertaid kyselykertadata]
  (sql/update taulut/kyselykerta
    (sql/set-fields (select-keys kyselykertadata [:nimi_fi :nimi_sv :voimassa_alkupvm :voimassa_loppupvm :lukittu]))
    (sql/where {:kyselykertaid kyselykertaid})))

(defn kyselykertaid->kyselyid
  [kyselykertaid]
  (let [result (sql/select taulut/kyselykerta
    (sql/fields :kyselyid)
    (sql/where {:kyselykertaid kyselykertaid}))]
    (-> result
        first
        :kyselyid)))
