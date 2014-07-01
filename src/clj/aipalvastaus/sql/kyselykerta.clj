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
  (:require [korma.core :as sql]))

(defn hae-kysymysryhmat [kyselyid]
  (sql/select :kysymysryhma
    (sql/join :kysely_kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysely_kysymysryhma.kysymysryhmaid))
    (sql/fields :kysymysryhma.kysymysryhmaid
                :kysymysryhma.nimi_fi
                :kysymysryhma.nimi_sv)
    (sql/where {:kysely_kysymysryhma.kyselyid kyselyid})
    (sql/order :kysely_kysymysryhma.jarjestys)))

(defn hae-kysymysryhmien-kysymykset [kyselyid]
  (sql/select :kysymys
    (sql/join :kysely_kysymys (= :kysely_kysymys.kysymysid :kysymys.kysymysid))
    (sql/fields :kysymys.kysymysryhmaid
                :kysymys.kysymysid
                :kysymys.vastaustyyppi
                :kysymys.monivalinta_max
                :kysymys.kysymys_fi
                :kysymys.kysymys_sv)
    (sql/where {:kysely_kysymys.kyselyid kyselyid})
    (sql/order :kysymys.jarjestys)))

(defn hae-kysymysten-monivalintavaihtoehdot [kyselyid]
  (sql/select :monivalintavaihtoehto
    (sql/join :kysely_kysymys (= :kysely_kysymys.kysymysid :monivalintavaihtoehto.kysymysid))
    (sql/fields :monivalintavaihtoehto.monivalintavaihtoehtoid
                :monivalintavaihtoehto.kysymysid
                :monivalintavaihtoehto.teksti_fi
                :monivalintavaihtoehto.teksti_sv)
    (sql/where {:kysely_kysymys.kyselyid kyselyid})
    (sql/order :monivalintavaihtoehto.jarjestys)))

(defn hae-kyselyn-tiedot [kyselyid]
  (first
    (sql/select :kysely
      (sql/fields :nimi_fi
                  :nimi_sv
                  :selite_fi
                  :selite_sv)
      (sql/where {:kyselyid kyselyid}))))

(defn ^:private yhdista-monivalintavaihtoehdot-kysymyksiin [kysymykset monivalintavaihtoehdot]
  (let [kysymysid->monivalinnat (group-by :kysymysid monivalintavaihtoehdot)]
    (for [kysymys kysymykset]
      (assoc kysymys :monivalintavaihtoehdot (kysymysid->monivalinnat (:kysymysid kysymys))))))

(defn ^:private yhdista-tietorakenteet [kysymysryhmat kysymykset monivalintavaihtoehdot]
  (let [kysymysryhmaid->kysymykset (group-by :kysymysryhmaid (yhdista-monivalintavaihtoehdot-kysymyksiin kysymykset monivalintavaihtoehdot))]
    (for [kysymysryhma kysymysryhmat]
      (assoc kysymysryhma :kysymykset (kysymysryhmaid->kysymykset (kysymysryhma :kysymysryhmaid))))))

(defn hae-kysymysryhmat-ja-kysymykset [kyselyid]
  (let [kysymysryhmat (hae-kysymysryhmat kyselyid)
        kysymykset (hae-kysymysryhmien-kysymykset kyselyid)
        monivalintavaihtoehdot (hae-kysymysten-monivalintavaihtoehdot kyselyid)]
    (yhdista-tietorakenteet kysymysryhmat kysymykset monivalintavaihtoehdot)))

(defn hae
  "Hakee kyselyn tiedot pääavaimella"
  [kyselyid]
  (merge (hae-kyselyn-tiedot kyselyid)
         {:kysymysryhmat (hae-kysymysryhmat-ja-kysymykset kyselyid)}))
