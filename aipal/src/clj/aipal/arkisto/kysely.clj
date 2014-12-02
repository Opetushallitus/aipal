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
            [aipal.arkisto.kyselykerta :as kyselykerta]
            [aipal.arkisto.kysymysryhma :as kysymysryhma]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.auditlog :as auditlog]))

(defn hae-kyselyt
  "Hae koulutustoimijan kyselyt"
  [koulutustoimija]
  (sql/select taulut/kysely
    (sql/join :inner :kysely_organisaatio_view (= :kysely_organisaatio_view.kyselyid :kyselyid))
    (sql/fields :kysely.kyselyid :kysely.nimi_fi :kysely.nimi_sv
                :kysely.voimassa_alkupvm :kysely.voimassa_loppupvm
                :kysely.tila :kysely.kaytettavissa
                [(sql/subselect taulut/kysely_kysymysryhma
                   (sql/aggregate (count :*) :lkm)
                   (sql/where {:kysely_kysymysryhma.kyselyid :kysely.kyselyid})) :kysymysryhmien_lkm])
    (sql/where {:kysely_organisaatio_view.koulutustoimija koulutustoimija})
    (sql/order :luotuaika :desc)))

;; käytetään samaan kun korman with yhden suhde moneen tapauksessa, mutta päästään kahdella sql haulla korman n+1:n sijaan
(defn ^:private yhdista-kyselykerrat-kyselyihin [kyselyt kyselykerrat]
  (let [kyselyid->kyselykerrat (group-by :kyselyid kyselykerrat)]
    (for [kysely kyselyt]
      (assoc kysely :kyselykerrat (kyselyid->kyselykerrat (:kyselyid kysely))))))

(defn hae-kaikki
  [koulutustoimija]
  (let [kyselyt (hae-kyselyt koulutustoimija)
        kyselykerrat (kyselykerta/hae-kaikki koulutustoimija)]
    (yhdista-kyselykerrat-kyselyihin kyselyt kyselykerrat)))

(defn hae
  "Hakee kyselyn tiedot pääavaimella"
  [kyselyid]
  (first
    (sql/select taulut/kysely
      (sql/fields :kysely.kyselyid :kysely.nimi_fi :kysely.nimi_sv
                  :kysely.voimassa_alkupvm :kysely.voimassa_loppupvm
                  :kysely.selite_fi :kysely.selite_sv
                  :kysely.tila :kysely.kaytettavissa)
      (sql/where (= :kyselyid kyselyid)))))

(defn hae-organisaatiotieto
  "Hakee kyselyn luoneen organisaation tiedot"
  [kyselyid]
  (first
    (sql/select
      :kysely_organisaatio_view
      (sql/fields :koulutustoimija)
      (sql/where {:kyselyid kyselyid}))))

(defn lisaa!
  "Lisää uuden kyselyn"
  [tiedot]
  (sql/insert taulut/kysely
    (sql/values tiedot)))

(defn muokkaa-kyselya! [kyselydata]
  (auditlog/kysely-muokkaus! (:kyselyid kyselydata))
  (->
    (sql/update* taulut/kysely)
    (sql/set-fields (select-keys kyselydata [:nimi_fi :nimi_sv :selite_fi :selite_sv :voimassa_alkupvm :voimassa_loppupvm :tila]))
    (sql/where {:kyselyid (:kyselyid kyselydata)})
    (sql/update)))

(defn uudelleennimea-kysymys-kentta
  [kysymysryhmat]
  (map #(clojure.set/rename-keys % {:kysymys :kysymykset}) kysymysryhmat))

(defn julkaise-kysely! [kyselyid]
  (sql/update taulut/kysely
    (sql/set-fields {:tila "julkaistu"})
    (sql/where {:kyselyid kyselyid}))
  ;; haetaan kysely, jotta saadaan myös kaytettavissa tieto mukaan paluuarvona
  (hae kyselyid))

(defn sulje-kysely [kyselyid]
  (sql/update taulut/kysely
    (sql/set-fields {:tila "suljettu"})
    (sql/where {:kyselyid kyselyid}))
  (hae kyselyid))

(defn hae-kysymysryhmat [kyselyid]
  (->
    (sql/select* taulut/kysymysryhma)
    (sql/fields :kysymysryhmaid :nimi_fi :nimi_sv)
    (sql/join taulut/kysely_kysymysryhma (= :kysely_kysymysryhma.kysymysryhmaid :kysymysryhmaid))
    (sql/where {:kysely_kysymysryhma.kyselyid kyselyid})
    (sql/order :kysely_kysymysryhma.jarjestys)
    (sql/with taulut/kysymys
      (sql/fields :kysymysid :kysymys_fi :kysymys_sv :poistettava :pakollinen [(sql/raw "kysely_kysymys.kysymysid is null") :poistettu] :vastaustyyppi :monivalinta_max
                  :jatkokysymys.jatkokysymysid :jatkokysymys.kylla_teksti_fi :jatkokysymys.kylla_teksti_sv :jatkokysymys.ei_teksti_fi :jatkokysymys.ei_teksti_sv)
      (sql/join :left :kysely_kysymys (and (= :kysely_kysymys.kysymysid :kysymysid) (= :kysely_kysymys.kyselyid kyselyid)))
      (sql/join :left :jatkokysymys (= :kysymys.jatkokysymysid :jatkokysymys.jatkokysymysid))
      (sql/order :kysymys.jarjestys))
    sql/exec
    uudelleennimea-kysymys-kentta
    (->> (map kysymysryhma/taydenna-kysymysryhman-monivalintakysymykset))))

(defn laske-kysymysryhmat [kyselyid]
  (->
    (sql/select taulut/kysely_kysymysryhma
      (sql/aggregate (count :*) :lkm)
      (sql/where {:kysely_kysymysryhma.kyselyid kyselyid}))
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
  (sql/delete taulut/kysely_kysymys
    (sql/where {:kyselyid kyselyid})))

(defn poista-kysymysryhmat!
  [kyselyid]
  (sql/delete taulut/kysely_kysymysryhma
    (sql/where {:kyselyid kyselyid})))

(defn lisaa-kysymysryhma!
  [kyselyid ryhma]
  (sql/insert taulut/kysely_kysymysryhma
    (sql/values {:kyselyid kyselyid
                 :kysymysryhmaid (:kysymysryhmaid ryhma)
                 :jarjestys (:jarjestys ryhma)})))

(defn lisaa-kysymys!
  [kyselyid kysymysid]
  (sql/insert taulut/kysely_kysymys
    (sql/values {:kyselyid kyselyid
                 :kysymysid kysymysid})))
