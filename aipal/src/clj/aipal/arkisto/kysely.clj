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
            [aipal.infra.kayttaja :refer [ntm-vastuukayttaja? yllapitaja?]]
            [aipal.integraatio.sql.korma :as taulut]
            [oph.common.util.util :refer [max-date]]
            [clojure.tools.logging :as log]
            [oph.korma.common :refer [select-unique-or-nil select-unique unique-or-nil]]
            [aipal.auditlog :as auditlog]))

(defn kysely-kentat
  [query]
  (->
    query
    (sql/fields :kysely.kyselyid :kysely.nimi_fi :kysely.nimi_sv :kysely.nimi_en
                :kysely.voimassa_alkupvm :kysely.voimassa_loppupvm
                :kysely.tila :kysely.kaytettavissa :uudelleenohjaus_url :kysely.sivutettu
                [(sql/raw "now() < voimassa_alkupvm") :tulevaisuudessa]
                [(sql/raw "CASE WHEN kysely.tila='luonnos' THEN 'luonnos' WHEN kysely.kaytettavissa OR now() < kysely.voimassa_alkupvm THEN 'julkaistu' ELSE 'suljettu' END") :sijainti])))

(def ^:private kysely-poistettavissa-query
  (->
    (sql/select* taulut/kysely)
    (sql/fields [(sql/raw (str "NOT EXISTS (SELECT 1"
                               " FROM (vastaaja JOIN vastaajatunnus ON vastaajatunnus.vastaajatunnusid = vastaaja.vastaajatunnusid) JOIN kyselykerta ON kyselykerta.kyselykertaid = vastaajatunnus.kyselykertaid"
                               " WHERE (kyselykerta.kyselyid = kysely.kyselyid))"
                               " AND tila IN ('luonnos', 'suljettu')")) :poistettavissa])))

(defn hae-kyselyt
  "Hae koulutustoimijan kyselyt"
  [koulutustoimija]
  (->
    kysely-poistettavissa-query
    (sql/join :inner :kysely_organisaatio_view (= :kysely_organisaatio_view.kyselyid :kyselyid))
    (kysely-util/rajaa-kayttajalle-sallittuihin-kyselyihin :kysely.kyselyid koulutustoimija)
    kysely-kentat
    (sql/fields [(sql/subselect taulut/kysely_kysymysryhma
                   (sql/aggregate (count :*) :lkm)
                   (sql/where {:kysely_kysymysryhma.kyselyid :kysely.kyselyid})) :kysymysryhmien_lkm])
    (sql/order :kyselyid :desc)
    sql/exec))

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

(defn hae
  "Hakee kyselyn tiedot pääavaimella"
  [kyselyid]
  (->
    kysely-poistettavissa-query
    kysely-kentat
    (sql/fields :kysely.selite_fi :kysely.selite_sv :kysely.selite_en)
    (sql/where (= :kyselyid kyselyid))
    sql/exec
    unique-or-nil))

(defn hae-organisaatiotieto
  "Hakee kyselyn luoneen organisaation tiedot"
  [kyselyid]
  (select-unique
    :kysely_organisaatio_view
    (sql/fields :koulutustoimija)
    (sql/where {:kyselyid kyselyid})))

(defn lisaa!
  "Lisää uuden kyselyn"
  [tiedot]
  (let [kysely (sql/insert taulut/kysely
                 (sql/values tiedot))]
    (auditlog/kysely-luonti! (:nimi_fi kysely) (:kyselyid kysely))
    kysely))

(defn muokkaa-kyselya! [kyselydata]
  (auditlog/kysely-muokkaus! (:kyselyid kyselydata))
  (->
    (sql/update* taulut/kysely)
    (sql/set-fields (select-keys kyselydata [:nimi_fi :nimi_sv :nimi_en :selite_fi :selite_sv :selite_en :voimassa_alkupvm :voimassa_loppupvm :tila :uudelleenohjaus_url :sivutettu]))
    (sql/where {:kyselyid (:kyselyid kyselydata)})
    (sql/update)))

(defn julkaise-kysely! [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid :julkaistu)
  (sql/update taulut/kysely
    (sql/set-fields {:tila "julkaistu"})
    (sql/where {:kyselyid kyselyid}))
  ;; haetaan kysely, jotta saadaan myös kaytettavissa tieto mukaan paluuarvona
  (hae kyselyid))

(defn palauta-luonnokseksi! [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid :luonnos)
  (sql/update taulut/kysely
    (sql/set-fields {:tila "luonnos"})
    (sql/where {:kyselyid kyselyid
                :tila "julkaistu"}))
  (hae kyselyid))

(defn sulje-kysely! [kyselyid]
  (auditlog/kysely-muokkaus! kyselyid :suljettu)
  (sql/update taulut/kysely
    (sql/set-fields {:tila "suljettu"})
    (sql/where {:kyselyid kyselyid}))
  (hae kyselyid))

(defn poista-kysely! [kyselyid]
  (auditlog/kysely-poisto! kyselyid)
  (sql/delete taulut/vastaajatunnus
    (sql/where {:kyselykertaid [in (sql/subselect taulut/kyselykerta (sql/fields :kyselykertaid) (sql/where {:kyselyid kyselyid}))]}))
  (sql/delete taulut/kyselykerta
    (sql/where {:kyselyid kyselyid}))
  (sql/delete taulut/kysely_kysymysryhma
    (sql/where {:kyselyid kyselyid}))
  (sql/delete taulut/kysely_kysymys
    (sql/where {:kyselyid kyselyid}))
  (sql/delete taulut/kysely
    (sql/where {:kyselyid kyselyid})))

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
                 :jarjestys (:jarjestys ryhma)})))

(defn lisaa-kysymys!
  [kyselyid kysymysid]
  (auditlog/kysely-muokkaus! kyselyid)
  (sql/insert taulut/kysely_kysymys
    (sql/values {:kyselyid kyselyid
                 :kysymysid kysymysid})))

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

(defn ntm-kysely?
  "Onko NTM-kysely."
   [kyselyid]
  (boolean
   (seq (sql/select taulut/kysely
          (sql/where (and {:kyselyid kyselyid}
                          (kysely-util/kysely-sisaltaa-ntm-kysymysryhman :kysely.kyselyid)))))))

(defn kysely-poistettavissa? [kyselyid]
  (->
    kysely-poistettavissa-query
    (sql/where {:kyselyid kyselyid})
    sql/exec
    first
    :poistettavissa))
