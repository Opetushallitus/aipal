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

(ns aipal.arkisto.kyselypohja
  (:require [oph.korma.common :refer [select-unique select-unique-or-nil]]
            [aipal.infra.kayttaja :refer [yllapitaja? *kayttaja*]]
            [aipal.auditlog :as auditlog]
            [arvo.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(defn hae-kyselypohjat
  ([organisaatio vain-voimassaolevat]
   (db/hae-kyselypohjat {:koulutustoimija organisaatio :voimassa vain-voimassaolevat :valtakunnallinen true}))
  ([organisaatio]
   (hae-kyselypohjat organisaatio false)))

(defn hae-kyselypohja [kyselypohjaid]
  (db/hae-kyselypohja {:kyselypohjaid kyselypohjaid}))

(def muokattavat-kentat [:nimi_fi :nimi_sv :nimi_en :selite_fi :selite_sv :selite_en :voimassa_alkupvm :voimassa_loppupvm :valtakunnallinen])

(defn tallenna-kyselypohjan-kysymysryhmat! [kyselypohjaid kysymysryhmat]
  (auditlog/kyselypohja-muokkaus! kyselypohjaid)
  (db/poista-kyselypohjan-kysymysryhmat! {:kyselypohjaid kyselypohjaid})
  (doseq [[index kysymysryhma] (map-indexed vector kysymysryhmat)]
    (db/tallenna-kyselypohjan-kysymysryhma! {:kyselypohjaid kyselypohjaid
                                             :kysymysryhmaid (:kysymysryhmaid kysymysryhma)
                                             :luotu_kayttaja (:oid *kayttaja*)
                                             :muutettu_kayttaja (:oid *kayttaja*)
                                             :jarjestys index})))

(defn tallenna-kyselypohja! [kyselypohjaid kyselypohja]
  (auditlog/kyselypohja-muokkaus! kyselypohjaid)
  (tallenna-kyselypohjan-kysymysryhmat! kyselypohjaid (:kysymysryhmat kyselypohja))
  (db/tallenna-kyselypohja! (assoc (select-keys kyselypohja muokattavat-kentat) :muutettu_kayttaja (:oid *kayttaja*)))
  kyselypohja)

(defn luo-kyselypohja!
  [kyselypohja]
  (let [luotu-kyselypohja (db/luo-kyselypohja!
                           (merge (select-keys kyselypohja (conj muokattavat-kentat :koulutustoimija))
                                  {:luotu_kayttaja (:oid *kayttaja*) :muutettu_kayttaja (:oid *kayttaja*)}))
        _ (log/info "Luotu kyselypohja: "luotu-kyselypohja)]
    (auditlog/kyselypohja-luonti! (:kyselypohjaid luotu-kyselypohja) (:nimi_fi kyselypohja))
    (tallenna-kyselypohjan-kysymysryhmat! (:kyselypohjaid luotu-kyselypohja) (:kysymysryhmat kyselypohja))
    luotu-kyselypohja))
(defn lisaa-kyselypohja! [kyselypohja]
  (db/luo-kyselypohja!
    (merge (select-keys kyselypohja muokattavat-kentat)
           {:luotu_kayttaja (:oid *kayttaja*)
            :muutettu_kayttaja (:oid *kayttaja*)
            :koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*)})))

(defn ^:private aseta-kyselypohjan-tila! [kyselypohjaid tila]
  (db/aseta-kyselypohjan-tila! {:kyselypohjaid kyselypohjaid :tila tila :muutettu_kayttaja (:oid *kayttaja*)})
  (hae-kyselypohja kyselypohjaid))

(defn julkaise-kyselypohja! [kyselypohjaid]
  (auditlog/kyselypohja-muokkaus! kyselypohjaid :julkaistu)
  (aseta-kyselypohjan-tila! kyselypohjaid "julkaistu"))

(defn palauta-kyselypohja-luonnokseksi! [kyselypohjaid]
  (auditlog/kyselypohja-muokkaus! kyselypohjaid :luonnos)
  (aseta-kyselypohjan-tila! kyselypohjaid "luonnos"))

(defn sulje-kyselypohja! [kyselypohjaid]
  (auditlog/kyselypohja-muokkaus! kyselypohjaid :suljettu)
  (aseta-kyselypohjan-tila! kyselypohjaid "suljettu"))

(defn poista-kyselypohja! [kyselypohjaid]
  (auditlog/kyselypohja-poisto! kyselypohjaid)
  (jdbc/with-db-transaction [tx *db*]
    (db/poista-kyselypohjan-kysymysryhmat! tx {:kyselypohjaid kyselypohjaid})
    (db/poista-kyselypohja! tx {:kyselypohjaid kyselypohjaid})))
