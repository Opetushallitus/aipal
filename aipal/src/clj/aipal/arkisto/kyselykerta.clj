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
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.auditlog :as auditlog]))

(defn hae-kaikki
  "Hae kaikki koulutustoimijan kyselykerrat"
  ([koulutustoimija]
      (sql/select taulut/kyselykerta
        (sql/modifier "distinct")
        (sql/join :inner taulut/kysely (= :kysely.kyselyid :kyselykerta.kyselyid))
        (sql/join :inner :kysely_organisaatio_view (= :kysely_organisaatio_view.kyselyid :kysely.kyselyid))
        (sql/join :left :vastaaja (= :vastaaja.kyselykertaid :kyselykerta.kyselykertaid))
        (sql/fields :kyselykerta.kyselyid :kyselykerta.kyselykertaid :kyselykerta.nimi
                    :kyselykerta.voimassa_alkupvm :kyselykerta.voimassa_loppupvm
                    :kyselykerta.lukittu :kyselykerta.luotuaika
                    [(sql/raw "vastaaja.vastaajaid is null") :poistettavissa])
        (cond-> (not (nil? koulutustoimija))
          (sql/where {:kysely_organisaatio_view.koulutustoimija koulutustoimija}))
        (sql/order :kyselykerta.kyselykertaid :ASC)))
  ([] (hae-kaikki nil)))

(defn poistettavissa? [id]
  (empty?
    (sql/select taulut/kyselykerta
      (sql/join :inner :vastaaja (= :vastaaja.kyselykertaid :kyselykerta.kyselykertaid))
      (sql/where {:kyselykerta.kyselykertaid id}))))

(defn lisaa!
  [kyselyid kyselykerta-data]
  (let [kyselykerta (sql/insert taulut/kyselykerta
                      (sql/values
                        (assoc
                          (select-keys kyselykerta-data [:nimi :voimassa_alkupvm :voimassa_loppupvm :lukittu])
                          :kyselyid kyselyid)))]
    (auditlog/kyselykerta-luonti! kyselyid (:nimi kyselykerta-data))
    kyselykerta))

(defn hae-yksi
  "Hae kyselykerta tunnuksella"
  [kyselykertaid]
  (->
    (sql/select* taulut/kyselykerta)
    (sql/fields :kyselyid :kyselykertaid :nimi :voimassa_alkupvm :voimassa_loppupvm :lukittu)
    (sql/where (= :kyselykertaid kyselykertaid))
    sql/exec
    first))

(defn paivita!
  [kyselykertaid kyselykertadata]
  (auditlog/kyselykerta-muokkaus! kyselykertaid)
  (sql/update taulut/kyselykerta
    (sql/set-fields (select-keys kyselykertadata [:nimi :voimassa_alkupvm :voimassa_loppupvm :lukittu]))
    (sql/where {:kyselykertaid kyselykertaid})))

(defn kyselykertaid->kyselyid
  [kyselykertaid]
  (let [result (sql/select taulut/kyselykerta
    (sql/fields :kyselyid)
    (sql/where {:kyselykertaid kyselykertaid}))]
    (-> result
        first
        :kyselyid)))

(defn lukitse!
  [kyselykertaid]
  (auditlog/kyselykerta-muokkaus! kyselykertaid :lukittu)
  (sql/update taulut/kyselykerta
    (sql/set-fields {:lukittu true})
    (sql/where {:kyselykertaid kyselykertaid})))

(defn avaa!
  [kyselykertaid]
  (auditlog/kyselykerta-muokkaus! kyselykertaid :avattu)
  (sql/update taulut/kyselykerta
    (sql/set-fields {:lukittu false})
    (sql/where {:kyselykertaid kyselykertaid})))

(defn poista! [id]
  {:pre [(poistettavissa? id)]}
  (auditlog/kyselykerta-poisto! id)
  (sql/delete taulut/vastaajatunnus
    (sql/where {:kyselykertaid id}))
  (sql/delete taulut/kyselykerta
    (sql/where {:kyselykertaid id})))
