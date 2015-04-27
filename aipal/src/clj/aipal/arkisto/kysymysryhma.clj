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

(ns aipal.arkisto.kysymysryhma
  (:require [korma.core :as sql]
            [oph.korma.common :refer [select-unique]]
            [aipal.infra.kayttaja :refer [yllapitaja?]]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.auditlog :as auditlog]
            [aipal.toimiala.raportti.taustakysymykset :refer :all]))

(defn hae-kysymysryhmat
  ([organisaatio vain-voimassaolevat]
    (-> (sql/select* taulut/kysymysryhma)
      (sql/join :inner :kysymysryhma_organisaatio_view (= :kysymysryhma_organisaatio_view.kysymysryhmaid :kysymysryhmaid))
      (sql/where (or {:kysymysryhma_organisaatio_view.koulutustoimija organisaatio}
                     (and {:kysymysryhma_organisaatio_view.valtakunnallinen true}
                          (or {:kysymysryhma.lisattavissa true}
                              (yllapitaja?)))))
      (cond->
        vain-voimassaolevat (sql/where {:kysymysryhma.lisattavissa true}))
      (sql/fields :kysymysryhma.kysymysryhmaid :kysymysryhma.nimi_fi :kysymysryhma.nimi_sv
                  :kysymysryhma.selite_fi :kysymysryhma.selite_sv :kysymysryhma.valtakunnallinen :kysymysryhma.taustakysymykset
                  :kysymysryhma.lisattavissa :kysymysryhma.tila
                  [(sql/subselect taulut/kysymys
                     (sql/aggregate (count :*) :lkm)
                     (sql/where {:kysymys.kysymysryhmaid :kysymysryhma.kysymysryhmaid})) :kysymyksien_lkm]
                  [(sql/sqlfn exists (sql/subselect taulut/kysymysryhma_kyselypohja
                                       (sql/where {:kysymysryhma_kyselypohja.kysymysryhmaid :kysymysryhma.kysymysryhmaid}))) :lisatty_kyselypohjaan]
                  [(sql/sqlfn exists (sql/subselect taulut/kysely_kysymysryhma
                                       (sql/where {:kysely_kysymysryhma.kysymysryhmaid :kysymysryhma.kysymysryhmaid}))) :lisatty_kyselyyn])
      (sql/order :muutettuaika :desc)
      sql/exec))
  ([organisaatio]
    (hae-kysymysryhmat organisaatio false)))

(defn hae-taustakysymysryhmat
  []
  (-> (sql/select* taulut/kysymysryhma)
    (sql/where {:taustakysymykset true
                :valtakunnallinen true})
    (sql/fields :kysymysryhmaid :nimi_fi :nimi_sv)
    (sql/order :muutettuaika :desc)
    sql/exec
    yhdista-valtakunnalliset-taustakysymysryhmat))

(defn lisaa-kysymysryhma! [k]
  (let [kysymysryhma (sql/insert taulut/kysymysryhma
                       (sql/values k))]
    (auditlog/kysymysryhma-luonti! (:kysymysryhmaid kysymysryhma) (:nimi_fi kysymysryhma))
    kysymysryhma))

(defn lisaa-kysymys! [k]
  (let [kysymys  (sql/insert taulut/kysymys
                   (sql/values k))]
    (auditlog/kysymys-luonti! (:kysymysryhmaid kysymys) (:kysymysid kysymys))
    kysymys))

(defn lisaa-jatkokysymys! [k]
  (let [jatkokysymys (sql/insert :jatkokysymys
                       (sql/values k))]
    (auditlog/jatkokysymys-luonti! (:jatkokysymysid jatkokysymys))
    jatkokysymys))

(defn lisaa-monivalintavaihtoehto! [v]
  (auditlog/kysymys-monivalinnat-luonti! (:kysymysid v))
  (sql/insert :monivalintavaihtoehto
    (sql/values v)))

(defn hae-monivalintakysymyksen-vaihtoehdot
  [kysymysid]
  (->
    (sql/select* :monivalintavaihtoehto)
    (sql/fields :jarjestys :teksti_fi :teksti_sv)
    (sql/order :jarjestys)
    (sql/where {:kysymysid kysymysid})
    (sql/exec)))

(def kysymysryhma-select
  (->
    (sql/select* taulut/kysymysryhma)
    (sql/fields :kysymysryhmaid :nimi_fi :nimi_sv
                :selite_fi :selite_sv
                :taustakysymykset :valtakunnallinen :tila)))

(def kysymys-select
  (->
    (sql/select* taulut/kysymys)
    (sql/join :left :jatkokysymys (= :jatkokysymys.jatkokysymysid :kysymys.jatkokysymysid))
    (sql/fields :kysymys.kysymysid :kysymys.kysymys_fi :kysymys.kysymys_sv
                :kysymys.poistettava :kysymys.pakollinen :kysymys.vastaustyyppi :kysymys.eos_vastaus_sallittu
                :kysymys.max_vastaus :kysymys.monivalinta_max :kysymys.jarjestys
                :jatkokysymys.kylla_teksti_fi :jatkokysymys.kylla_teksti_sv
                :jatkokysymys.ei_teksti_fi :jatkokysymys.ei_teksti_sv
                :jatkokysymys.kylla_vastaustyyppi
                [:jatkokysymys.max_vastaus :jatkokysymys_max_vastaus])
    (sql/order :kysymys.jarjestys)))

(defn taydenna-monivalintakysymys
  [monivalintakysymys]
  (let [kysymysid (:kysymysid monivalintakysymys)]
    (assoc monivalintakysymys :monivalintavaihtoehdot (hae-monivalintakysymyksen-vaihtoehdot kysymysid))))

(def kylla-jatkokysymykset-kentat
  [:kylla_teksti_fi
   :kylla_teksti_sv
   :kylla_vastaustyyppi])

(def ei-jatkokysymykset-kentat
  [:ei_teksti_fi
   :ei_teksti_sv
   :jatkokysymys_max_vastaus])

(def jatkokysymykset-kentat
  (into kylla-jatkokysymykset-kentat ei-jatkokysymykset-kentat))

(defn onko-jokin-kentista-annettu?
  [m kentat]
  (not-every? nil? (vals (select-keys m kentat))))

(defn jatkokysymys?
  [kysymys]
  (onko-jokin-kentista-annettu? kysymys jatkokysymykset-kentat))

(defn kylla-jatkokysymys?
  [kysymys]
  (onko-jokin-kentista-annettu? kysymys kylla-jatkokysymykset-kentat))

(defn ei-jatkokysymys?
  [kysymys]
  (onko-jokin-kentista-annettu? kysymys ei-jatkokysymykset-kentat))

(defn poista-nil-kentat
  [m]
  (into {} (remove (comp nil? second) m)))

(defn erottele-jatkokysymys
  [kysymys]
  (-> kysymys
    (select-keys jatkokysymykset-kentat)
    poista-nil-kentat
    (clojure.set/rename-keys {:jatkokysymys_max_vastaus :max_vastaus})
    (assoc :kylla_jatkokysymys (kylla-jatkokysymys? kysymys))
    (assoc :ei_jatkokysymys (ei-jatkokysymys? kysymys))))

(defn taydenna-jatkokysymys
  [kysymys]
  (let [jatkokysymys (erottele-jatkokysymys kysymys)]
    (-> kysymys
      (assoc :jatkokysymys jatkokysymys)
      (as-> kysymys (apply dissoc kysymys jatkokysymykset-kentat)))))

(defn taydenna-kysymys
  [kysymys]
  (cond-> kysymys
    (= "monivalinta" (:vastaustyyppi kysymys)) taydenna-monivalintakysymys
    (jatkokysymys? kysymys) taydenna-jatkokysymys))

(defn taydenna-kysymysryhman-monivalintakysymykset
  [kysymysryhma]
  (let [kysymykset (for [kysymys (:kysymykset kysymysryhma)]
                     (if (= "monivalinta" (:vastaustyyppi kysymys))
                       (taydenna-monivalintakysymys kysymys)
                       kysymys))]
    (assoc kysymysryhma :kysymykset kysymykset)))

(defn taydenna-kysymysryhman-kysymykset
  [kysymysryhma]
  (update-in kysymysryhma [:kysymykset] #(doall (map taydenna-kysymys %))))

(defn taydenna-kysymysryhma
  [kysymysryhma]
  (let [kysymysryhmaid (:kysymysryhmaid kysymysryhma)]
    (assoc kysymysryhma :kysymykset
           (-> kysymys-select
             (sql/where {:kysymysryhmaid kysymysryhmaid})
             (sql/exec)))))

(defn hae
  ([kysymysryhmaid]
    (hae kysymysryhmaid true))
  ([kysymysryhmaid hae-kysymykset]
    (let [kysymysryhma (-> kysymysryhma-select
                         (sql/where {:kysymysryhmaid kysymysryhmaid})
                         sql/exec
                         first)]
      (when kysymysryhma
        (if hae-kysymykset
          (-> kysymysryhma
              taydenna-kysymysryhma
              taydenna-kysymysryhman-kysymykset)
          kysymysryhma)))))

(defn hae-taustakysymysryhma
  [kysymysryhmaid]
  (if (= kysymysryhmaid suorittamisvaihe-id)
    (let [hakeutumisvaihe (hae hakeutumisvaihe-id)
          suorittamisvaihe (hae suorittamisvaihe-id)
          kysymykset (->> (mapcat :kysymykset [suorittamisvaihe hakeutumisvaihe])
                       (remove (comp valtakunnalliset-duplikaattikysymykset :kysymysid))
                       (map lisaa-selite-taustakysymykseen)
                       (map aseta-taustakysymyksen-jarjestys)
                       (sort-by :jarjestys))]
      (assoc suorittamisvaihe
             :nimi_fi "Näyttötutkintojen taustakysymykset"
             :nimi_sv "Bakgrundsfrågor gällande fristående examina"
             :kysymykset kysymykset))
    (hae kysymysryhmaid)))

(defn hae-kysymysryhman-kysymyksien-idt
  [kysymysryhmaid]
  (map :kysymysid
       (sql/select taulut/kysymys
         (sql/where {:kysymysryhmaid kysymysryhmaid})
         (sql/fields :kysymysid))))

(defn kysymysryhma-esikatselulle-select
  ([kyselyid]
    (->
      (sql/select* taulut/kysymysryhma)
      (sql/fields :kysymysryhmaid :nimi_fi :nimi_sv :kuvaus_fi :kuvaus_sv :tila :valtakunnallinen :taustakysymykset)
      (sql/with taulut/kysymys
        (sql/fields :kysymysid :kysymys_fi :kysymys_sv :poistettava :pakollinen :vastaustyyppi :monivalinta_max :eos_vastaus_sallittu
                    :jatkokysymys.jatkokysymysid
                    :jatkokysymys.kylla_kysymys :jatkokysymys.kylla_teksti_fi :jatkokysymys.kylla_teksti_sv
                    :jatkokysymys.ei_kysymys :jatkokysymys.ei_teksti_fi :jatkokysymys.ei_teksti_sv :jatkokysymys.kylla_vastaustyyppi)
        (cond->
          kyselyid (->
                     (sql/fields [(sql/raw "kysely_kysymys.kysymysid is null") :poistettu])
                     (sql/join :left :kysely_kysymys (and (= :kysely_kysymys.kysymysid :kysymysid)
                                                          (= :kysely_kysymys.kyselyid kyselyid)))))
        (sql/join :left :jatkokysymys (= :kysymys.jatkokysymysid :jatkokysymys.jatkokysymysid))
        (sql/order :kysymys.jarjestys))))
  ([] (kysymysryhma-esikatselulle-select nil)))

(defn vaihda-kysymysavain [kysymysryhma]
  (clojure.set/rename-keys kysymysryhma {:kysymys :kysymykset}))

(defn hae-esikatselulle
  [kysymysryhmaid]
  (->
    (kysymysryhma-esikatselulle-select)
    (sql/where {:kysymysryhmaid kysymysryhmaid})
    (sql/join taulut/kysely_kysymysryhma (= :kysely_kysymysryhma.kysymysryhmaid :kysymysryhmaid))
    (sql/order :kysely_kysymysryhma.jarjestys)
    sql/exec
    first
    vaihda-kysymysavain
    taydenna-kysymysryhman-monivalintakysymykset))

(defn hae-kyselypohjasta
  "Hakee kyselypohjan kyselyryhmät, jotka ovat lisättävissä kyselyyn"
  [kyselypohjaid]
  (-> (kysymysryhma-esikatselulle-select)
    (sql/join :inner taulut/kysymysryhma_kyselypohja (= :kysymysryhma_kyselypohja.kysymysryhmaid :kysymysryhma.kysymysryhmaid))
    (sql/fields :kysymysryhma_kyselypohja.kyselypohjaid :kysymysryhma_kyselypohja.jarjestys)
    (sql/where {:kysymysryhma_kyselypohja.kyselypohjaid kyselypohjaid
                :kysymysryhma.lisattavissa true})
    (sql/order :kysymysryhma_kyselypohja.jarjestys)
    sql/exec
    (->> (map (comp taydenna-kysymysryhman-monivalintakysymykset vaihda-kysymysavain)))))

(defn hae-kyselysta
  "Hakee kyselyn kysymysryhmät"
  [kyselyid]
  (-> (kysymysryhma-esikatselulle-select kyselyid)
    (sql/join :inner taulut/kysely_kysymysryhma (= :kysely_kysymysryhma.kysymysryhmaid :kysymysryhmaid))
    (sql/where {:kysely_kysymysryhma.kyselyid kyselyid})
    (sql/order :kysely_kysymysryhma.jarjestys)
    sql/exec
    (->> (map (comp taydenna-kysymysryhman-monivalintakysymykset vaihda-kysymysavain)))))

(defn hae-organisaatiotieto
  [kysymysryhmaid]
  (select-unique :kysymysryhma_organisaatio_view
    (sql/fields :koulutustoimija :valtakunnallinen)
    (sql/where {:kysymysryhmaid kysymysryhmaid})))

(defn paivita!
  [kysymysryhma]
  (auditlog/kysymysryhma-muokkaus! (:kysymysryhmaid kysymysryhma))
  (->
    (sql/update* taulut/kysymysryhma)
    (sql/set-fields (select-keys kysymysryhma [:nimi_fi :nimi_sv :selite_fi :selite_sv :valtakunnallinen :koulutustoimija :taustakysymykset]))
    (sql/where {:kysymysryhmaid (:kysymysryhmaid kysymysryhma)})
    (sql/update)))

(defn poista!
  [kysymysryhmaid]
  (auditlog/kysymysryhma-poisto! kysymysryhmaid)
  (sql/delete taulut/kysymysryhma
    (sql/where {:kysymysryhmaid kysymysryhmaid})))

(defn poista-kysymys!
  [kysymysid]
  (auditlog/kysymys-poisto! kysymysid)
  (sql/delete
    taulut/kysymys
    (sql/where {:kysymysid kysymysid})))

(defn poista-kysymyksen-monivalintavaihtoehdot!
  [kysymysid]
  (auditlog/kysymys-monivalinnat-poisto! kysymysid)
  (sql/delete
    :monivalintavaihtoehto
    (sql/where {:kysymysid kysymysid})))

(defn poista-jatkokysymys!
  [jatkokysymysid]
  (auditlog/jatkokysymys-poisto! jatkokysymysid)
  (sql/delete
    taulut/jatkokysymys
    (sql/where {:jatkokysymysid jatkokysymysid})))

(defn ^:private aseta-tila!
  [kysymysryhmaid tila]
  (sql/update taulut/kysymysryhma
    (sql/set-fields {:tila tila})
    (sql/where {:kysymysryhmaid kysymysryhmaid})))

(defn julkaise!
  [kysymysryhmaid]
  (auditlog/kysymysryhma-muokkaus! kysymysryhmaid :julkaistu)
  (aseta-tila! kysymysryhmaid "julkaistu"))

(defn sulje!
  [kysymysryhmaid]
  (auditlog/kysymysryhma-muokkaus! kysymysryhmaid :suljettu)
  (aseta-tila! kysymysryhmaid "suljettu"))

(defn palauta-luonnokseksi!
  [kysymysryhmaid]
  (auditlog/kysymysryhma-muokkaus! kysymysryhmaid :luonnos)
  (aseta-tila! kysymysryhmaid "luonnos"))

(defn laske-kysymykset
  [kysymysryhmaid]
  (->
    (sql/select taulut/kysymys
      (sql/aggregate (count :*) :lkm)
      (sql/where {:kysymysryhmaid kysymysryhmaid}))
    first
    :lkm))

(defn laske-kyselyt
  [kysymysryhmaid]
  (->
    (sql/select taulut/kysely_kysymysryhma
      (sql/aggregate (count :*) :lkm)
      (sql/where {:kysymysryhmaid kysymysryhmaid}))
    first
    :lkm))

(defn laske-kyselypohjat
  [kysymysryhmaid]
  (->
    (sql/select taulut/kysymysryhma_kyselypohja
      (sql/aggregate (count :*) :lkm)
      (sql/where {:kysymysryhmaid kysymysryhmaid}))
    first
    :lkm))
