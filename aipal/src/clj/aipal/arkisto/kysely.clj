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

(ns aipal.arkisto.kysely
  (:require [aipal.arkisto.kyselykerta :as kyselykerta]
            [aipal.infra.kayttaja :refer [yllapitaja? *kayttaja*]]
            [oph.common.util.util :refer [max-date]]
            [clojure.tools.logging :as log]
            [arvo.db.core :refer [*db*] :as db]
            [aipal.auditlog :as auditlog]
            [clojure.java.jdbc :as jdbc]
            [arvo.util :refer [add-index]]
            [oph.common.util.util :refer [map-by]])
  (:import (java.security MessageDigest)))

(def kysely-kentat [:nimi_fi :nimi_sv :nimi_en :selite_fi :selite_sv :selite_en :kyselypohjaid :metatiedot :uudelleenohjaus_url :tyyppi])

(def kysely-defaults (zipmap kysely-kentat (repeat nil)))

(defn random-hash []
  (let [algorithm (MessageDigest/getInstance "MD5")
        raw (.digest algorithm (.getBytes (str(rand))))]
    (format "%032x" (BigInteger. 1 raw))))

(defn hae-kyselyt [koulutustoimija]
  (db/hae-kyselyt {:koulutustoimija koulutustoimija}))

(defn ^:private yhdista-kyselykerrat-kyselyihin [kyselyt kyselykerrat]
  (let [kyselyid->kyselykerrat (group-by :kyselyid kyselykerrat)]
    (for [kysely kyselyt
          :let [kyselyn-kyselykerrat (kyselyid->kyselykerrat (:kyselyid kysely))]]
      (assoc kysely :kyselykerrat kyselyn-kyselykerrat
                    :vastaajia (reduce + (map :vastaajia kyselyn-kyselykerrat))
                    :vastaajatunnuksia (reduce + (map :vastaajatunnuksia kyselyn-kyselykerrat))
                    :viimeisin_vastaus (reduce max-date nil (map :viimeisin_vastaus kyselyn-kyselykerrat))))))

(defn hae-kysymysten-poistettavuus  [kysymysryhmaid]
  (map #(select-keys % [:kysymysid :poistettava]) (db/hae-kysymysryhman-kysymykset {:kysymysryhmaid kysymysryhmaid})))

(defn hae-kaikki
  [koulutustoimija]
  (let [kyselyt (hae-kyselyt koulutustoimija)
        kyselykerrat (kyselykerta/hae-koulutustoimijan-kyselykerrat koulutustoimija)]
    (yhdista-kyselykerrat-kyselyihin kyselyt kyselykerrat)))

(defn hae [kyselyid]
  (db/hae-kysely {:kyselyid kyselyid}))

(defn hae-kyselytyypit []
  (let [kyselytyypit (db/hae-kyselytyypit)
        kayttajan-kyselytyypit (-> *kayttaja* :aktiivinen-rooli :kyselytyypit)]
    (filter #(some #{(:id %)} kayttajan-kyselytyypit) kyselytyypit)))

(defn lisaa-kysymysryhma! [tx kyselyid kysymysryhma]
  (db/lisaa-kyselyn-kysymysryhma! tx (merge kysymysryhma {:kyselyid kyselyid :kayttaja (:oid *kayttaja*)}))
  (let [kayttajan-kysymykset (map-by :kysymysid (:kysymykset kysymysryhma))]
    (doseq [kysymys (hae-kysymysten-poistettavuus (:kysymysryhmaid kysymysryhma))]
      (let [kysymysid (:kysymysid kysymys)
            kayttajan-kysymys (get kayttajan-kysymykset kysymysid)
            lisattavissa (not (and (:poistettu kayttajan-kysymys)
                                   (:poistettava kysymys)))]
        (assert (not (:poistettu kayttajan-kysymys)))
        (when lisattavissa
          (db/lisaa-kysymys-kyselyyn! tx {:kyselyid kyselyid :kysymysid kysymysid :kayttaja (:oid *kayttaja*)}))))))


(defn format-kysely [kyselydata]
  (let [metatiedot (merge (:metatiedot kyselydata) {:esikatselu_tunniste (random-hash)})]
    (merge kysely-defaults kyselydata {:metatiedot metatiedot :kayttaja (:oid *kayttaja*)})))

(defn lisaa! [kyselydata]
  (jdbc/with-db-transaction [tx *db*]
    (let [kyselyid (first (db/luo-kysely! tx (format-kysely kyselydata)))
          kyselyid (:kyselyid kyselyid)]
      (doseq [ryhma (add-index :jarjestys (:kysymysryhmat kyselydata))]
        (lisaa-kysymysryhma! tx kyselyid ryhma))
      (assoc kyselydata :kyselyid (:kyselyid kyselyid)))))

(defn paivita-kysely! [kyselydata]
  (let [kyselyid (:kyselyid kyselydata)
        current-data (hae kyselyid)
        updated-data (if (= "julkaistu" (:tila current-data))
                       (select-keys kyselydata [:selite_fi :selite_sv :selite_en :uudelleenohjaus_url])
                       kyselydata)
        new-data (merge current-data updated-data {:kayttaja (:oid *kayttaja*)})]
    (jdbc/with-db-transaction [tx *db*]
      (db/muokkaa-kyselya! new-data)
      ;TODO smarter way to update questions/groups
      (db/poista-kyselyn-kysymykset! tx {:kyselyid kyselyid})
      (db/poista-kyselyn-kysymysryhmat! tx {:kyselyid kyselyid})
      (doseq [kysymysryhma (add-index :jarjestys (:kysymysryhmat kyselydata))]
        (lisaa-kysymysryhma! tx kyselyid kysymysryhma)))))


(defn julkaise-kysely! [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid :julkaistu)
  (db/muuta-kyselyn-tila! {:kyselyid kyselyid :tila "julkaistu" :kayttaja (:oid *kayttaja*)})
  ;; haetaan kysely, jotta saadaan myös kaytettavissa tieto mukaan paluuarvona
  (-> (hae kyselyid)
      (assoc :sijainti "julkaistu")))

(defn palauta-luonnokseksi! [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid :luonnos)
  (db/muuta-kyselyn-tila! {:kyselyid kyselyid :tila "luonnos" :kayttaja (:oid *kayttaja*)})
  (-> (hae kyselyid)
      (assoc :sijainti "luonnos")))

(defn sulje-kysely! [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid :suljettu)
  (db/muuta-kyselyn-tila! {:kyselyid kyselyid :tila "suljettu" :kayttaja (:oid *kayttaja*)})
  (-> (hae kyselyid)
      (assoc :sijainti "suljettu")))

(defn poista-kysely! [kyselyid]
  (jdbc/with-db-transaction [tx *db*]
    (db/poista-kyselyn-kysymykset! tx {:kyselyid kyselyid})
    (db/poista-kyselyn-kysymysryhmat! tx {:kyselyid kyselyid})
    (db/poista-kyselyn-kyselykerrat! tx {:kyselyid kyselyid})
    (db/poista-kysely! tx {:kyselyid kyselyid})))

(defn laske-kysymysryhmat [kyselyid]
  (count (db/hae-kyselyn-kysymysryhmat {:kyselyid kyselyid})))

(defn laske-kyselykerrat [kyselyid]
  (->
    (db/laske-kyselyn-kyselykerrat {:kyselyid kyselyid})
    :lkm))

(defn poista-kysymykset! [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid)
  (db/poista-kyselyn-kysymykset! {:kyselyid kyselyid}))

(defn poista-kysymysryhmat! [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid)
  (db/poista-kyselyn-kysymysryhmat! {:kyselyid kyselyid}))

(defn hae-kyselyn-taustakysymysryhmaid [kyselyid]
  (-> (db/hae-kyselyn-taustakysymysryhmaid {:kyselyid kyselyid})
      first
      :kysymysryhmaid))

(defn aseta-jatkokysymyksen-jarjestys [kysymys kysymykset]
  (if (:jatkokysymys kysymys)
    (let [parent-q (first(filter #(= (:kysymysid %) (:jatkokysymys_kysymysid kysymys)) kysymykset))]
      (assoc kysymys :jarjestys (+ (:jarjestys parent-q) 0.5)))
    kysymys))

(defn aseta-jatkokysymysten-jarjestys [kysymykset]
  (map #(aseta-jatkokysymyksen-jarjestys % kysymykset) kysymykset))

(defn hae-kysymysryhman-kysymykset [kysymysryhma]
  (->> kysymysryhma
       db/hae-kysymysryhman-kysymykset
       aseta-jatkokysymysten-jarjestys
       (sort-by :jarjestys)))

(defn hae-kyselyn-kysymykset [kyselyid]
  (->> (db/hae-kyselyn-kysymysryhmat {:kyselyid kyselyid})
       (map hae-kysymysryhman-kysymykset)))

(defn samanniminen-kysely?
  "Palauttaa true jos samalla koulutustoimijalla on jo samanniminen kysely."
   [kysely]
  (boolean
    (db/samanniminen-kysely? (merge kysely-defaults kysely))))

(defn kysely-poistettavissa? [kyselyid]
  (-> (hae kyselyid)
      :poistettavissa))

(defn get-kyselyn-pakolliset-kysymysryhmaidt
  "Hakee kyselyn kaikki valtakunnalliset ja taustakysymykset. Näiden muokkausta ei sallita julkaistussa kyselyssä."
  [kyselyid]
  (db/hae-kyselyn-pakolliset-kysymysryhmat {:kyselyid kyselyid}))