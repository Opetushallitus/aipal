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
  (:require [korma.core :as sql]
            [aipal.arkisto.kysely-util :as kysely-util]
            [aipal.arkisto.kyselykerta :as kyselykerta]
            [aipal.infra.kayttaja :refer [ntm-vastuukayttaja? yllapitaja? *kayttaja*]]
            [aipal.integraatio.sql.korma :as taulut]
            [oph.common.util.util :refer [max-date]]
            [clojure.tools.logging :as log]
            [arvo.db.core :as db]
            [oph.korma.common :refer [select-unique-or-nil select-unique unique-or-nil]]
            [aipal.auditlog :as auditlog]))

(def ^:private kysely-poistettavissa-query
  (->
    (sql/select* taulut/kysely)
    (sql/fields [(sql/raw (str "NOT EXISTS (SELECT 1"
                               " FROM (vastaaja JOIN vastaajatunnus ON vastaajatunnus.vastaajatunnusid = vastaaja.vastaajatunnusid) JOIN kyselykerta ON kyselykerta.kyselykertaid = vastaajatunnus.kyselykertaid"
                               " WHERE (kyselykerta.kyselyid = kysely.kyselyid))"
                               " AND tila IN ('luonnos', 'suljettu')")) :poistettavissa])))

(defn hae-kyselyt [koulutustoimija]
  (db/hae-kyselyt {:koulutustoimija koulutustoimija}))

;; käytetään samaan kun korman with yhden suhde moneen tapauksessa, mutta päästään kahdella sql haulla korman n+1:n sijaan
(defn ^:private yhdista-kyselykerrat-kyselyihin [kyselyt kyselykerrat]
  (let [kyselyid->kyselykerrat (group-by :kyselyid kyselykerrat)]
    (for [kysely kyselyt
          :let [kyselyn-kyselykerrat (kyselyid->kyselykerrat (:kyselyid kysely))]]
      (assoc kysely :kyselykerrat kyselyn-kyselykerrat
                    :vastaajia (reduce + (map :vastaajia kyselyn-kyselykerrat))
                    :vastaajatunnuksia (reduce + (map :vastaajatunnuksia kyselyn-kyselykerrat))
                    :viimeisin_vastaus (reduce max-date nil (map :viimeisin_vastaus kyselyn-kyselykerrat))))))

(defn hae-kaikki
  [koulutustoimija]
  (let [kyselyt (hae-kyselyt koulutustoimija)
        kyselykerrat (kyselykerta/hae-kaikki koulutustoimija)]
    (yhdista-kyselykerrat-kyselyihin kyselyt kyselykerrat)))

(defn hae [kyselyid]
  (db/hae-kysely {:kyselyid kyselyid}))

(defn hae-kyselytyypit []
  (db/hae-kyselytyypit))

(defn lisaa!
  "Lisää uuden kyselyn"
  [tiedot]
  (let [kysely (sql/insert taulut/kysely
                 (sql/values (merge tiedot {:luotu_kayttaja (:oid *kayttaja*)
                                            :muutettu_kayttaja (:oid *kayttaja*)})))]
    (auditlog/kysely-luonti! (:nimi_fi kysely) (:kyselyid kysely))
    kysely))

(defn muokkaa-kyselya! [kyselydata]
  (auditlog/kysely-muokkaus! (:kyselyid kyselydata))
  (let [paivitettavat-kentat (if (= "julkaistu" (:tila kyselydata))
                               [:selite_fi :selite_sv :selite_en :uudelleenohjaus_url]
                               [:nimi_fi :nimi_sv :nimi_en :selite_fi :selite_sv :selite_en :voimassa_alkupvm :voimassa_loppupvm :tila :uudelleenohjaus_url :sivutettu :tyyppi])]
    (log/info paivitettavat-kentat)
    (->
     (sql/update* taulut/kysely)
     (sql/set-fields (assoc (select-keys kyselydata paivitettavat-kentat)
                            :muutettu_kayttaja (:oid *kayttaja*)))
     (sql/where {:kyselyid (:kyselyid kyselydata)})
     (sql/update))))


(defn julkaise-kysely! [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid :julkaistu)
  (db/muuta-kyselyn-tila! {:kyselyid kyselyid :tila "julkaistu" :kayttaja (:oid *kayttaja*)})
  ;; haetaan kysely, jotta saadaan myös kaytettavissa tieto mukaan paluuarvona
  (hae kyselyid))

(defn palauta-luonnokseksi! [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid :luonnos)
  (db/muuta-kyselyn-tila! {:kyselyid kyselyid :tila "luonnos" :kayttaja (:oid *kayttaja*)})
  (hae kyselyid))

(defn sulje-kysely! [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid :suljettu)
  (db/muuta-kyselyn-tila! {:kyselyid kyselyid :tila "suljettu" :kayttaja (:oid *kayttaja*)})
  (hae kyselyid))

(defn poista-kysely! [kyselyid]
  (let [vastaajatunnusids (sql/select taulut/vastaajatunnus
                                      (sql/fields :vastaajatunnusid)
                                      (sql/where {:kyselykertaid [in (sql/subselect taulut/kyselykerta (sql/fields :kyselykertaid) (sql/where {:kyselyid kyselyid}))]}))]
    (auditlog/kysely-poisto! kyselyid)
    (sql/delete taulut/vastaajatunnus
      (sql/where {:kyselykertaid [in vastaajatunnusids]}))
    (sql/delete taulut/kyselykerta
      (sql/where {:kyselyid kyselyid}))
    (sql/delete taulut/kysely_kysymysryhma
      (sql/where {:kyselyid kyselyid}))
    (sql/delete taulut/kysely_kysymys
      (sql/where {:kyselyid kyselyid}))
    (sql/delete taulut/kysely
      (sql/where {:kyselyid kyselyid}))))

(defn laske-kysymysryhmat [kyselyid]
  (->
    (sql/select taulut/kysely_kysymysryhma
      (sql/aggregate (count :*) :lkm)
      (sql/where {:kysely_kysymysryhma.kyselyid kyselyid}))
    first
    :lkm))

(defn laske-kyselykerrat [kyselyid]
  (->
    (sql/select taulut/kyselykerta
      (sql/aggregate (count :*) :lkm)
      (sql/where {:kyselyid kyselyid}))
    first
    :lkm))

(defn poistettava-kysymys? [kysymysid]
  (->
    (sql/select* taulut/kysymys)
    (sql/fields :poistettava)
    (sql/where {:kysymysid kysymysid})
    sql/exec
    first
    :poistettava))

(defn hae-kysymysten-poistettavuus
  [kysymysryhmaid]
  (sql/select taulut/kysymys
    (sql/fields :kysymysid :poistettava)
    (sql/where {:kysymysryhmaid kysymysryhmaid})))

(defn poista-kysymykset!
  [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid)
  (sql/delete taulut/kysely_kysymys
    (sql/where {:kyselyid kyselyid})))

(defn poista-kysymysryhmat!
  [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid)
  (sql/delete taulut/kysely_kysymysryhma
    (sql/where {:kyselyid kyselyid})))

(defn lisaa-kysymysryhma!
  [kyselyid ryhma]
  (auditlog/kysely-muokkaus! kyselyid)
  (sql/insert taulut/kysely_kysymysryhma
    (sql/values {:kyselyid kyselyid
                 :kysymysryhmaid (:kysymysryhmaid ryhma)
                 :jarjestys (:jarjestys ryhma)
                 :luotu_kayttaja (:oid *kayttaja*)
                 :muutettu_kayttaja (:oid *kayttaja*)})))

(defn lisaa-kysymys!
  [kyselyid kysymysid]
  (auditlog/kysely-muokkaus! kyselyid)
  (sql/insert taulut/kysely_kysymys
    (sql/values {:kyselyid kyselyid
                 :kysymysid kysymysid
                 :luotu_kayttaja (:oid *kayttaja*)
                 :muutettu_kayttaja (:oid *kayttaja*)})))

(defn hae-kyselyn-taustakysymysryhmaid
  [kyselyid]
  (->
    (sql/select taulut/kysely_kysymysryhma
      (sql/join taulut/kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysymysryhmaid))
      (sql/where {:kysymysryhma.valtakunnallinen true
                  :kysymysryhma.taustakysymykset true
                  :kysely_kysymysryhma.kyselyid kyselyid})
      (sql/fields :kysymysryhmaid))
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
    (seq (sql/select taulut/kysely
           (sql/where {:koulutustoimija (:koulutustoimija kysely)})
           (sql/where (or (when (:nimi_fi kysely)
                            {:nimi_fi (:nimi_fi kysely)})
                          (when (:nimi_sv kysely)
                            {:nimi_sv (:nimi_sv kysely)})
                          (when (:nimi_en kysely)
                            {:nimi_en (:nimi_en kysely)})))
           (sql/where {:kyselyid [not= (:kyselyid kysely)]})))))

(defn kysely-poistettavissa? [kyselyid]
  (->
    kysely-poistettavissa-query
    (sql/where {:kyselyid kyselyid})
    sql/exec
    first
    :poistettavissa))

(defn get-kyselyn-pakolliset-kysymysryhmaidt
  "Hakee kyselyn kaikki valtakunnalliset ja taustakysymykset. Näiden muokkausta ei sallita julkaistussa kyselyssä."
  [kyselyid]
  (seq
   (sql/select taulut/kysely_kysymysryhma
               (sql/join taulut/kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysymysryhmaid))
               (sql/where  (or {:kyselyid kyselyid
                                :kysymysryhma.taustakysymykset true}
                               {:kyselyid kyselyid
                                :kysymysryhma.valtakunnallinen true}))
               (sql/fields :kysymysryhmaid))))
