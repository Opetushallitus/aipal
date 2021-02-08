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
  (:require [oph.korma.common :refer [unique]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [arvo.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]))

(defn hae-koulutustoimijan-kyselykerrat [koulutustoimija]
  (db/hae-koulutustoimijan-kyselykerrat {:koulutustoimija koulutustoimija}))

(defn poistettavissa? [id]
  (= 0 (:vastaajia (db/laske-kyselykerran-vastaajat {:kyselykertaid id}))))

(defn kysely-julkaistu? [kyselyid]
  (boolean (= "julkaistu" (:tila (db/hae-kysely {:kyselyid kyselyid})))))

(defn muokattavissa? [kyselykertaid]
  (let [kyselykerta (db/hae-kyselykerta {:kyselykertaid kyselykertaid})]
    (kysely-julkaistu? (:kyselyid kyselykerta))))

(defn lisaa! [kyselyid kyselykerta-data]
  (when (kysely-julkaistu? kyselyid)
    (let [kyselykertaid (db/luo-kyselykerta! (merge kyselykerta-data
                                                    {:kyselyid kyselyid
                                                     :kayttaja (:oid *kayttaja*)
                                                     :automaattinen nil
                                                     :metatiedot nil}))]
      (first kyselykertaid))))

(defn hae-automaatti-kyselykerta [koulutustoimija kyselytyyppi tarkenne]
  (db/hae-automaatti-kyselykerta (merge
                                   {:koulutustoimija koulutustoimija :kyselytyyppi kyselytyyppi}
                                   (when tarkenne {:tarkenne tarkenne}))))

(defn hae-rekrykysely [oppilaitos vuosi]
  (first (db/hae-rekry-kyselykerta {:oppilaitoskoodi oppilaitos :vuosi vuosi})))


(defn hae-yksi [kyselykertaid]
  (db/hae-kyselykerta {:kyselykertaid kyselykertaid}))

(defn paivita!
  [kyselykertaid kyselykertadata]
  (when (muokattavissa? kyselykertaid)
    (db/paivita-kyselykerta! (merge {:kayttaja (:oid *kayttaja*)})
                             (select-keys kyselykertadata [:nimi :voimassa_alkupvm :voimassa_loppupvm :lukittu]))
    (assoc kyselykertadata :kyselykertaid kyselykertaid)))

(defn aseta-lukittu!
  [kyselykertaid lukitse]
  (db/set-kyselykerta-lukittu! {:kyselykertaid kyselykertaid :lukittu lukitse :kayttaja (:oid *kayttaja*)})
  (hae-yksi kyselykertaid))

(defn poista! [id]
  {:pre [(poistettavissa? id)]}
  (jdbc/with-db-transaction [tx *db*]
    (db/poista-kyselykerran-tunnukset! {:kyselykertaid id})
    (db/poista-kyselykerta! {:kyselykertaid id})))

(defn hae-kyselykerran-oppilaitokset [kyselykertaid]
  (let [oppilaitokset (db/hae-kyselykerran-oppilaitokset {:kyselykertaid kyselykertaid})]
    (when oppilaitokset
      {:oppilaitokset oppilaitokset})))

(defn hae-kyselyn-oppilaitokset [kyselyid]
  (let [oppilaitokset (db/hae-kyselyn-oppilaitokset {:kyselyid kyselyid})]
    (when oppilaitokset
      {:oppilaitokset oppilaitokset})))

(defn samanniminen-kyselykerta? [kyselyid nimi]
  (boolean (seq (db/samanniminen-kyselykerta? {:kyselyid kyselyid :nimi nimi}))))
