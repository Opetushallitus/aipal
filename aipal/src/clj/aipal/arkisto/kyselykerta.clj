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
            [aipal.arkisto.kysely-util :as kysely-util]
            [oph.korma.common :refer [unique]]
            [clojure.tools.logging :as log]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.auditlog :as auditlog]))

(defn hae-kaikki
  "Hae kaikki koulutustoimijan kyselykerrat"
  [koulutustoimija]
  (sql/select :kyselykerta
    (sql/modifier "distinct")
    (sql/join :inner :kyselykerta_kaytettavissa {:kyselykerta.kyselykertaid :kyselykerta_kaytettavissa.kyselykertaid})
    (sql/join :inner :kysely {:kysely.kyselyid :kyselykerta.kyselyid})
    (sql/join :inner :kysely_organisaatio_view {:kysely_organisaatio_view.kyselyid :kysely.kyselyid})
    (sql/join :left :vastaajatunnus {:vastaajatunnus.kyselykertaid :kyselykerta.kyselykertaid})
    (sql/join :left :vastaajatunnus_kaytettavissa {:vastaajatunnus.vastaajatunnusid :vastaajatunnus_kaytettavissa.vastaajatunnusid})
    (sql/join :left :vastaaja {:vastaaja.vastaajatunnusid :vastaajatunnus.vastaajatunnusid})
    (sql/fields :kyselykerta.kyselyid :kyselykerta.kyselykertaid :kyselykerta.nimi
                :kyselykerta.voimassa_alkupvm :kyselykerta.voimassa_loppupvm
                :kyselykerta.lukittu :kyselykerta.luotuaika
                :kyselykerta_kaytettavissa.kaytettavissa
                [(sql/raw "count(vastaaja.vastaajaid) = 0") :poistettavissa]
                [(sql/raw "coalesce(sum(vastaajatunnus.vastaajien_lkm) filter (where vastaajatunnus_kaytettavissa.kaytettavissa), 0)") :aktiivisia_vastaajatunnuksia]
                [(sql/raw "count(vastaaja.vastaajaid) filter (where vastaajatunnus_kaytettavissa.kaytettavissa)") :aktiivisia_vastaajia]
                [(sql/raw "coalesce(sum(vastaajatunnus.vastaajien_lkm), 0)") :vastaajatunnuksia]
                [(sql/raw "count(vastaaja.vastaajaid)") :vastaajia]
                [(sql/raw "max(vastaaja.luotuaika)") :viimeisin_vastaus])
    (sql/group :kyselykerta.kyselyid :kyselykerta.kyselykertaid :kyselykerta.nimi
               :kyselykerta.voimassa_alkupvm :kyselykerta.voimassa_loppupvm
               :kyselykerta.lukittu :kyselykerta.luotuaika
               :kyselykerta_kaytettavissa.kaytettavissa)
    (kysely-util/rajaa-kayttajalle-sallittuihin-kyselyihin :kysely.kyselyid koulutustoimija)
    (sql/order :kyselykerta.kyselykertaid :ASC)))

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

;avop.fi
(defn hae-nimella-ja-oppilaitoksella
  "Hae kyselykerta nimella ja oppilaitoksella"
  [kyselykertanimi oppilaitosid]
  (first (sql/select taulut/kyselykerta
                     (sql/join :inner :kysely (= :kysely.kyselyid :kyselykerta.kyselyid))
                     (sql/join :inner :oppilaitos (= :oppilaitos.koulutustoimija :kysely.koulutustoimija))
                     (sql/fields :kyselykerta.kyselykertaid)
                     (sql/where {:oppilaitos.oppilaitoskoodi oppilaitosid :kyselykerta.nimi kyselykertanimi :kyselykerta.lukittu false
                                 }))))
;end avop.fi

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
    (sql/where {:kyselykertaid kyselykertaid}))
  (assoc kyselykertadata :kyselykertaid kyselykertaid))

(defn kyselykertaid->kyselyid
  [kyselykertaid]
  (let [result (sql/select taulut/kyselykerta
                 (sql/fields :kyselyid)
                 (sql/where {:kyselykertaid kyselykertaid}))]
    (-> result
        first
        :kyselyid)))

(defn aseta-lukittu!
  [kyselykertaid lukitse]
  (auditlog/kyselykerta-muokkaus! kyselykertaid (if lukitse :lukittu :avattu))
  (sql/update :kyselykerta
    (sql/set-fields {:lukittu lukitse})
    (sql/where {:kyselykertaid kyselykertaid}))
  (hae-yksi kyselykertaid))

(defn poista! [id]
  {:pre [(poistettavissa? id)]}
  (auditlog/kyselykerta-poisto! id)
  (sql/delete taulut/vastaajatunnus
    (sql/where {:kyselykertaid id}))
  (sql/delete taulut/kyselykerta
    (sql/where {:kyselykertaid id})))

(defn hae-koulutustoimijatiedot
  "Hakee valmistavan koulutuksen järjestäjät"
  [kyselykerta-where]
  (sql/select taulut/kyselykerta
    (sql/modifier "distinct")
    (sql/join :inner taulut/vastaajatunnus (= :vastaajatunnus.kyselykertaid :kyselykerta.kyselykertaid))
    (sql/join :inner taulut/koulutustoimija (= :koulutustoimija.ytunnus :vastaajatunnus.valmistavan_koulutuksen_jarjestaja))
    (sql/fields :koulutustoimija.ytunnus :koulutustoimija.nimi_fi :koulutustoimija.nimi_sv :koulutustoimija.nimi_en)
    (sql/where kyselykerta-where)))

(defn hae-oppilaitostiedot
  "Hakee valmistavan koulutuksen oppilaitokset"
  [kyselykerta-where]
  (sql/select taulut/kyselykerta
    (sql/modifier "distinct")
    (sql/join :inner taulut/vastaajatunnus (= :vastaajatunnus.kyselykertaid :kyselykerta.kyselykertaid))
    (sql/join :inner taulut/oppilaitos (= :oppilaitos.oppilaitoskoodi :vastaajatunnus.valmistavan_koulutuksen_oppilaitos))
    (sql/fields :oppilaitos.oppilaitoskoodi :oppilaitos.nimi_fi :oppilaitos.nimi_sv :oppilaitos.nimi_en)
    (sql/where kyselykerta-where)))

(defn hae-vastaustunnustiedot
  "Hakee vastaustunnuksista tiedot kyselykerta taulun kautta"
  [kyselykerta-where]
  (let [koulutustoimijat (hae-koulutustoimijatiedot kyselykerta-where)
        oppilaitokset (hae-oppilaitostiedot kyselykerta-where)]
    (when (or koulutustoimijat oppilaitokset)
      {:koulutustoimijat koulutustoimijat
       :oppilaitokset oppilaitokset})))

(defn hae-vastaustunnustiedot-kyselykerralta
  "Hakee vastaustunnuksista tiedot kyselykerran pääavaimella"
  [kyselykertaid]
  (hae-vastaustunnustiedot {:kyselykerta.kyselykertaid kyselykertaid}))

(defn hae-vastaustunnustiedot-kyselylta
  "Hakee vastaustunnuksista tiedot kyselyn pääavaimella"
  [kyselyid]
  (hae-vastaustunnustiedot {:kyselykerta.kyselyid kyselyid}))

(defn samanniminen-kyselykerta? [kyselykerta]
  "Palauttaa true jos samalla koulutustoimijalla on jo samanniminen kyselykerta."
  (boolean
    (seq (sql/select taulut/kyselykerta
           (sql/join :inner taulut/kysely (= :kyselykerta.kyselyid :kysely.kyselyid))
           (sql/where {:kysely.koulutustoimija (sql/subselect taulut/kysely
                                                 (sql/fields :koulutustoimija)
                                                 (sql/where {:kyselyid (:kyselyid kyselykerta)}))})
           (sql/where {:nimi (:nimi kyselykerta)})
           (sql/where {:kyselykertaid [not= (:kyselykertaid kyselykerta)]})))))
