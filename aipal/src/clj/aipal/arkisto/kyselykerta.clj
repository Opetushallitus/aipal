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
            [aipal.auditlog :as auditlog]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [arvo.db.core :refer [*db*] :as db]))

(defn hae-kaikki
  "Hae kaikki koulutustoimijan kyselykerrat"
  [koulutustoimija]
  (sql/exec-raw [(str "WITH vastaajat AS (SELECT kyselykerta.kyselykertaid,
                          count(vastaaja.vastaajaid) AS vastaajia,
                          count(vastaaja.vastaajaid) filter (where vastaajatunnus_kaytettavissa.kaytettavissa) AS aktiivisia_vastaajia,
                          max(vastaaja.luotuaika) AS viimeisin_vastaus
                   FROM kyselykerta
                   INNER JOIN kysely_organisaatio_view ON kyselykerta.kyselyid = kysely_organisaatio_view.kyselyid
                   LEFT JOIN vastaajatunnus ON (vastaajatunnus.kyselykertaid = kyselykerta.kyselykertaid)
                   LEFT JOIN vastaajatunnus_kaytettavissa ON (vastaajatunnus.vastaajatunnusid = vastaajatunnus_kaytettavissa.vastaajatunnusid)
                   LEFT JOIN vastaaja ON (vastaaja.vastaajatunnusid = vastaajatunnus.vastaajatunnusid)
                   WHERE (kysely_organisaatio_view.koulutustoimija = ?)
                   GROUP BY kyselykerta.kyselykertaid)
SELECT kyselykerta.kyselyid, kyselykerta.kyselykertaid, kyselykerta.nimi, kyselykerta.voimassa_alkupvm, kyselykerta.voimassa_loppupvm,
       kyselykerta.lukittu, kyselykerta.luotuaika, kyselykerta_kaytettavissa.kaytettavissa,
       vastaajat.vastaajia = 0 AS poistettavissa,
       coalesce(sum(vastaajatunnus.vastaajien_lkm) filter (where vastaajatunnus_kaytettavissa.kaytettavissa), 0) AS aktiivisia_vastaajatunnuksia,
       vastaajat.aktiivisia_vastaajia,
       coalesce(sum(vastaajatunnus.vastaajien_lkm), 0) AS vastaajatunnuksia,
       vastaajat.vastaajia,
       vastaajat.viimeisin_vastaus
FROM (((((kyselykerta
     INNER JOIN kyselykerta_kaytettavissa ON (kyselykerta.kyselykertaid = kyselykerta_kaytettavissa.kyselykertaid))
     INNER JOIN kysely ON (kysely.kyselyid = kyselykerta.kyselyid))
     INNER JOIN kysely_organisaatio_view ON (kysely_organisaatio_view.kyselyid = kysely.kyselyid))
     INNER JOIN vastaajat ON (vastaajat.kyselykertaid = kyselykerta.kyselykertaid)
     LEFT JOIN vastaajatunnus ON (vastaajatunnus.kyselykertaid = kyselykerta.kyselykertaid))
     LEFT JOIN vastaajatunnus_kaytettavissa ON (vastaajatunnus.vastaajatunnusid = vastaajatunnus_kaytettavissa.vastaajatunnusid))
WHERE " (kysely-util/rajaa-kayttajalle-sallittuihin-kyselyihin-sql) "
GROUP BY kyselykerta.kyselyid, kyselykerta.kyselykertaid, kyselykerta.nimi, kyselykerta.voimassa_alkupvm, kyselykerta.voimassa_loppupvm, kyselykerta.lukittu, kyselykerta.luotuaika,
         kyselykerta_kaytettavissa.kaytettavissa, vastaajat.vastaajia, vastaajat.aktiivisia_vastaajia, vastaajat.viimeisin_vastaus
ORDER BY kyselykerta.kyselykertaid ASC")
                 [koulutustoimija koulutustoimija]]
                :results))

(defn poistettavissa? [id]
  (empty?
    (sql/select taulut/kyselykerta
      (sql/join :inner :vastaaja (= :vastaaja.kyselykertaid :kyselykerta.kyselykertaid))
      (sql/where {:kyselykerta.kyselykertaid id}))))

(defn kysely-julkaistu? [kyselyid]
  (boolean (= "julkaistu" (:tila (db/hae-kysely {:kyselyid kyselyid})))))

(defn muokattavissa? [kyselykertaid]
  (let [kyselykerta (db/hae-kyselykerta {:kyselykertaid kyselykertaid})]
    (kysely-julkaistu? (:kyselyid kyselykerta))))

(defn lisaa!
  [kyselyid kyselykerta-data]
  (when kysely-julkaistu?
    (let [kyselykerta (sql/insert taulut/kyselykerta
                        (sql/values
                          (assoc
                            (merge (select-keys kyselykerta-data [:nimi :voimassa_alkupvm :voimassa_loppupvm :lukittu])
                                   {:luotu_kayttaja (:oid *kayttaja*) :muutettu_kayttaja (:oid *kayttaja*)})
                            :kyselyid kyselyid)))]
      (auditlog/kyselykerta-luonti! (:kyselykertaid kyselykerta) kyselyid (:nimi kyselykerta-data))
      kyselykerta)))

;avop.fi
(defn hae-nimella-ja-oppilaitoksella
  "Hae kyselykerta nimella ja oppilaitoksella"
  [kyselykertanimi oppilaitosid]
  (first (sql/select taulut/kyselykerta
                     (sql/join :inner :kysely (= :kysely.kyselyid :kyselykerta.kyselyid))
                     (sql/join :inner :oppilaitos (= :oppilaitos.koulutustoimija :kysely.koulutustoimija))
                     (sql/fields :kyselykerta.kyselykertaid)
                     (sql/where {:oppilaitos.oppilaitoskoodi oppilaitosid :kyselykerta.nimi kyselykertanimi :kyselykerta.lukittu false}))))

(defn hae-rekrykysely [oppilaitos vuosi]
  (first (db/hae-rekry-kyselykerta {:oppilaitoskoodi oppilaitos :vuosi vuosi})))

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
  (when (muokattavissa? kyselykertaid)
    (sql/update taulut/kyselykerta
      (sql/set-fields (assoc (select-keys kyselykertadata [:nimi :voimassa_alkupvm :voimassa_loppupvm :lukittu])
                             :muutettu_kayttaja (:oid *kayttaja*)))
      (sql/where {:kyselykertaid kyselykertaid}))
    (assoc kyselykertadata :kyselykertaid kyselykertaid)))

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
    (sql/set-fields {:lukittu lukitse :muutettu_kayttaja (:oid *kayttaja*)})
    (sql/where {:kyselykertaid kyselykertaid}))
  (hae-yksi kyselykertaid))

(defn poista! [id]
  {:pre [(poistettavissa? id)]}
  (auditlog/kyselykerta-poisto! id)
  (let [vastaajatunnukset (sql/select taulut/vastaajatunnus
                            (sql/fields :vastaajatunnusid)
                            (sql/where {:kyselykertaid id}))]
    (sql/delete taulut/vastaajatunnus_tiedot
                (sql/where {:vastaajatunnus_id [in (map :vastaajatunnusid vastaajatunnukset)]}))
    (sql/delete taulut/vastaajatunnus
                (sql/where {:kyselykertaid id}))
    (sql/delete taulut/kyselykerta
                (sql/where {:kyselykertaid id}))))

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

(defn samanniminen-kyselykerta?
  "Palauttaa true jos samalla koulutustoimijalla on jo samanniminen kyselykerta."
  [kyselykerta]
  (boolean
    (seq (sql/select taulut/kyselykerta
           (sql/join :inner taulut/kysely (= :kyselykerta.kyselyid :kysely.kyselyid))
           (sql/where {:kysely.koulutustoimija (sql/subselect taulut/kysely
                                                 (sql/fields :koulutustoimija)
                                                 (sql/where {:kyselyid (:kyselyid kyselykerta)}))})
           (sql/where {:nimi (:nimi kyselykerta)})
           (sql/where {:kyselykertaid [not= (:kyselykertaid kyselykerta)]})))))
