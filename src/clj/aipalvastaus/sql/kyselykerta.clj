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

(ns aipalvastaus.sql.kyselykerta
  (:require [korma.core :as sql]
            [aipalvastaus.sql.korma :refer :all]))

(defn hae-kysymysryhmat [kyselyid]
  (sql/select :kysymysryhma
    (sql/join :kysely_kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysely_kysymysryhma.kysymysryhmaid))
    (sql/fields :kysymysryhma.kysymysryhmaid
                :kysymysryhma.nimi_fi
                :kysymysryhma.nimi_sv
                :kysymysryhma.taustakysymykset
                :kysymysryhma.valtakunnallinen)
    (sql/where {:kysely_kysymysryhma.kyselyid kyselyid})))

(defn hae-kysymysryhmien-kysymykset [kyselyid]
  (sql/select :kysymys
    (sql/join :kysymysryhma (= :kysymys.kysymysryhmaid :kysymysryhma.kysymysryhmaid))
    (sql/join :kysely_kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysely_kysymysryhma.kysymysryhmaid))
    (sql/fields :kysymys.kysymysryhmaid
                :kysymys.kysymysid
                :kysymys.vastaustyyppi
                :kysymys.kysymys_fi
                :kysymys.kysymys_sv)
    (sql/where {:kysely_kysymysryhma.kyselyid kyselyid})))

(defn ^:private filteroi-kysymysryhman-kysymykset [kysymykset kysymysryhmaid]
  (filter #(= kysymysryhmaid (:kysymysryhmaid %)) kysymykset))

(defn ^:private joinaa-tietorakenteet [kysymysryhmat kysymykset]
  (for [kysymysryhma kysymysryhmat
        :let [kysymykset (filteroi-kysymysryhman-kysymykset kysymykset (:kysymysryhmaid kysymysryhma))]]
    (assoc kysymysryhma :kysymykset kysymykset)))

(defn hae-kysymysryhmat-ja-kysymykset [kyselyid]
  (let [kysymysryhmat (hae-kysymysryhmat kyselyid)
        kysymykset (hae-kysymysryhmien-kysymykset kyselyid)]
    (joinaa-tietorakenteet kysymysryhmat kysymykset)))

(defn hae
  "Hakee kyselykerran tiedot pääavaimella"
  [kyselyid]
  {:kysymysryhmat (hae-kysymysryhmat-ja-kysymykset kyselyid)})
