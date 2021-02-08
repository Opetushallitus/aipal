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
            [aipal.infra.kayttaja :refer [vastuukayttaja? yllapitaja?]]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.auditlog :as auditlog]
            [clojure.tools.logging :as log]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.toimiala.raportti.taustakysymykset :refer :all]
            [arvo.db.core :refer [*db*] :as db]
            [aipal.arkisto.kysely :refer [hae-kysymysryhman-kysymykset]]))

(defn ^:private rajaa-kayttajalle-sallittuihin-kysymysryhmiin [query organisaatio]
  (let [koulutustoimijan-oma {:kysymysryhma_organisaatio_view.koulutustoimija organisaatio}
        valtakunnallinen     {:kysymysryhma_organisaatio_view.valtakunnallinen true}
        lisattavissa         {:kysymysryhma.tila "julkaistu"}]
    (cond
      (yllapitaja?)         (-> query
                              (sql/where (or koulutustoimijan-oma
                                             valtakunnallinen)))
      :else                 (-> query
                              (sql/where (and (or koulutustoimijan-oma
                                                  (and valtakunnallinen
                                                       lisattavissa))))))))

(defn hae-kysymysryhmat
  ([organisaatio vain-voimassaolevat]
   (-> (sql/select* taulut/kysymysryhma)
     (sql/join :inner :kysymysryhma_organisaatio_view (= :kysymysryhma_organisaatio_view.kysymysryhmaid :kysymysryhmaid))
     (rajaa-kayttajalle-sallittuihin-kysymysryhmiin organisaatio)
     (cond->
       vain-voimassaolevat (sql/where {:kysymysryhma.tila "julkaistu"}))
     (sql/fields :kysymysryhma.kysymysryhmaid :kysymysryhma.nimi_fi :kysymysryhma.nimi_sv :kysymysryhma.nimi_en
                 :kysymysryhma.selite_fi :kysymysryhma.selite_sv :kysymysryhma.selite_en :kysymysryhma.valtakunnallinen :kysymysryhma.taustakysymykset
                 :kysymysryhma.tila :kysymysryhma.kuvaus_fi :kysymysryhma.kuvaus_sv :kysymysryhma.kuvaus_en :kysymysryhma.metatiedot
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

(defn ^:private rajaa-kayttajalle-sallittuihin-taustakysymysryhmiin [query]
  query)

(defn hae-taustakysymysryhmat
  []
  (-> (sql/select* taulut/kysymysryhma)
    (sql/where (and {:taustakysymykset true
                     :valtakunnallinen true}))
    rajaa-kayttajalle-sallittuihin-taustakysymysryhmiin
    (sql/fields :kysymysryhmaid :nimi_fi :nimi_sv :nimi_en)
    (sql/order :muutettuaika :desc)
    sql/exec
    yhdista-valtakunnalliset-taustakysymysryhmat))

(def tyhja-kysymysryhma { :taustakysymykset false
                          :valtakunnallinen false
                          :nimi_fi nil
                          :nimi_sv nil
                          :selite_fi nil
                          :selite_sv nil
                          :koulutustoimija nil
                          :oppilaitos nil
                          :luotuaika nil
                          :muutettuaika nil
                          :tila "luonnos"
                          :kuvaus_fi nil
                          :kuvaus_sv nil
                          :nimi_en nil
                          :selite_en nil
                          :kuvaus_en nil})

(defn lisaa-kysymysryhma! [kysymysryhma-data]
  (let [kysymysryhma  (merge tyhja-kysymysryhma (assoc kysymysryhma-data :kayttaja (:oid *kayttaja*)))
        kysymysryhma-id (db/lisaa-kysymysryhma! kysymysryhma)]
    (auditlog/kysymysryhma-luonti! (:kysymysryhmaid kysymysryhma-id) (:nimi_fi (:nimi_fi kysymysryhma)))
    (first kysymysryhma-id)))

(def tyhja-kysymys { :kysymysryhmaid nil
                     :pakollinen nil
                     :eos_vastaus_sallittu nil
                     :poistettava nil
                     :vastaustyyppi nil
                     :kysymys_fi nil
                     :kysymys_sv nil
                     :kysymys_en nil
                     :selite_fi nil
                     :selite_sv nil
                     :selite_en nil
                     :max_vastaus nil
                     :monivalinta_max nil
                     :jatkokysymys false
                     :rajoite nil
                     :jarjestys nil
                     :metatiedot {}})


(defn lisaa-kysymys! [kysymys-data kysymysryhmaid]
  (let [kysymys (assoc (merge tyhja-kysymys kysymys-data) :kayttaja (:oid *kayttaja*))
         kysymysid (db/lisaa-kysymys! kysymys)]
    (auditlog/kysymys-luonti! (:kysymysryhmaid kysymysryhmaid) (:kysymysid kysymysid))
    kysymysid))

(defn liita-jatkokysymys! [kysymysid jatkokysymysid vastaus]
  (db/liita-jatkokysymys! {:kysymysid kysymysid :jatkokysymysid jatkokysymysid :vastaus vastaus}))


(defn lisaa-monivalintavaihtoehto! [v]
  (auditlog/kysymys-monivalinnat-luonti! (:kysymysid v))
  (sql/insert :monivalintavaihtoehto
    (sql/values v)))

(defn hae-monivalintakysymyksen-vaihtoehdot
  [kysymysid]
  (->
    (sql/select* :monivalintavaihtoehto)
    (sql/fields :jarjestys :teksti_fi :teksti_sv :teksti_en)
    (sql/order :jarjestys)
    (sql/where {:kysymysid kysymysid})
    (sql/exec)))

(def kysymysryhma-select
  (->
    (sql/select* taulut/kysymysryhma)
    (sql/fields :kysymysryhmaid :nimi_fi :nimi_sv :nimi_en
                :selite_fi :selite_sv :selite_en
                :kuvaus_fi :kuvaus_sv :kuvaus_en
                :taustakysymykset :valtakunnallinen :tila)))

(defn taydenna-monivalintakysymys
  [monivalintakysymys]
  (let [kysymysid (:kysymysid monivalintakysymys)]
    (assoc monivalintakysymys :monivalintavaihtoehdot (hae-monivalintakysymyksen-vaihtoehdot kysymysid))))

(def kylla-jatkokysymyksen-kentat
  [:kylla_teksti_fi
   :kylla_teksti_sv
   :kylla_teksti_en
   :kylla_vastaustyyppi])

(def ei-jatkokysymyksen-kentat
  [:ei_teksti_fi
   :ei_teksti_sv
   :ei_teksti_en
   :jatkokysymys_max_vastaus])

(def jatkokysymyksen-kentat
  (into kylla-jatkokysymyksen-kentat ei-jatkokysymyksen-kentat))

(defn onko-jokin-kentista-annettu?
  [m kentat]
  (not-every? nil? (vals (select-keys m kentat))))

(defn kylla-jatkokysymys?
  [kysymys]
  (onko-jokin-kentista-annettu? kysymys [:kylla_teksti_fi :kylla_teksti_sv :kylla_teksti_en]))

(defn ei-jatkokysymys?
  [kysymys]
  (onko-jokin-kentista-annettu? kysymys [:ei_teksti_fi :ei_teksti_sv :ei_teksti_en]))

(defn jatkokysymys?
  [kysymys]
  (or (kylla-jatkokysymys? kysymys) (ei-jatkokysymys? kysymys)))

(defn poista-nil-kentat
  [m]
  (into {} (remove (comp nil? second) m)))

(defn erottele-jatkokysymys
  [kysymys]
  (-> kysymys
    (select-keys jatkokysymyksen-kentat)
    poista-nil-kentat
    (clojure.set/rename-keys {:jatkokysymys_max_vastaus :max_vastaus})
    (assoc :kylla_jatkokysymys (kylla-jatkokysymys? kysymys))
    (assoc :ei_jatkokysymys (ei-jatkokysymys? kysymys))))

(defn taydenna-jatkokysymys
  [kysymys]
  (let [jatkokysymys (erottele-jatkokysymys kysymys)]
    (-> kysymys
        (assoc :jatkokysymys jatkokysymys)
        (as-> kysymys (apply dissoc kysymys jatkokysymyksen-kentat)))))

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

(def kysymysryhma-fields [:kysymysryhmaid :tila :nimi_fi :nimi_sv :nimi_en :selite_fi :selite_sv :selite_en :kuvaus_fi :kuvaus_sv :kuvaus_en :taustakysymykset :valtakunnallinen])

(defn liita-jatkokysymykset [jatkokysymykset-map kysymys]
  (if-let [jatkokysymykset (get jatkokysymykset-map (:kysymysid kysymys))]
      (reduce #(assoc-in %1 [:jatkokysymykset (:jatkokysymys_vastaus %2)] %2) kysymys jatkokysymykset)
    kysymys))

(defn taydenna-kysymysryhma [kysymysryhma]
  (let [kysymykset (db/hae-kysymysryhman-kysymykset {:kysymysryhmaid (:kysymysryhmaid kysymysryhma)})
        jatkokysymykset (group-by :jatkokysymys_kysymysid (filter :jatkokysymys kysymykset))
        kys (map #(liita-jatkokysymykset jatkokysymykset %) (remove :jatkokysymys kysymykset))]
    (assoc kysymysryhma :kysymykset (sort-by :jarjestys kys))))

(defn hae
  ([kysymysryhmaid]
   (hae kysymysryhmaid true))
  ([kysymysryhmaid hae-kysymykset]
   (let [kysymysryhma (db/hae-kysymysryhma {:kysymysryhmaid kysymysryhmaid})]
     (if hae-kysymykset
       (-> kysymysryhma
         (select-keys kysymysryhma-fields)
         taydenna-kysymysryhma
         taydenna-kysymysryhman-kysymykset)
       kysymysryhma))))

(defn kysymysryhma-tilassa? [kysymysryhmaid & tilat]
  (boolean (some #{(:tila (hae kysymysryhmaid false))} (into #{} tilat))))

(defn luonnos? [kysymysryhmaid]
  (kysymysryhma-tilassa? kysymysryhmaid "luonnos"))

(defn julkaistu? [kysymysryhmaid]
  (kysymysryhma-tilassa? kysymysryhmaid "julkaistu"))

(defn julkaistavissa? [kysymysryhmaid]
  (kysymysryhma-tilassa? kysymysryhmaid "luonnos" "suljettu"))

(defn hae-taustakysymysryhma
  [kysymysryhmaid]
  ;(if (= kysymysryhmaid suorittamisvaihe-id)
  ;  (let [hakeutumisvaihe (hae hakeutumisvaihe-id)
  ;        suorittamisvaihe (hae suorittamisvaihe-id)
  ;        kysymykset (->> (mapcat :kysymykset [suorittamisvaihe hakeutumisvaihe])
  ;                     (remove (comp valtakunnalliset-duplikaattikysymykset :kysymysid))
  ;                     ;(map lisaa-selite-taustakysymykseen)
  ;                     ;(map aseta-kysymyksen-jarjestys)
  ;                     (sort-by :jarjestys))]
  ;    (assoc suorittamisvaihe
  ;           :nimi_fi "Näyttötutkintojen taustakysymykset"
  ;           :nimi_sv "Bakgrundsfrågor gällande fristående examina"
  ;           :kysymykset kysymykset))
  (hae kysymysryhmaid))

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
     (sql/fields :kysymysryhmaid :nimi_fi :nimi_sv :nimi_en :kuvaus_fi :kuvaus_sv :kuvaus_en :tila :valtakunnallinen :taustakysymykset :metatiedot)
     (sql/with taulut/kysymys
       (sql/fields :kysymysid :kysymys_fi :kysymys_sv :kysymys_en :poistettava :pakollinen :vastaustyyppi :monivalinta_max :eos_vastaus_sallittu :jatkokysymys :jarjestys :kysymysryhmaid :max_vastaus
                   [:kysymys_jatkokysymys.kysymysid :jatkokysymys_kysymysid]
                   [:kysymys_jatkokysymys.vastaus :jatkokysymys_vastaus])
       (cond->
         kyselyid (->
                    (sql/fields [(sql/raw "kysely_kysymys.kysymysid is null") :poistettu])
                    (sql/join :left :kysely_kysymys (and (= :kysely_kysymys.kysymysid :kysymysid)
                                                         (= :kysely_kysymys.kyselyid kyselyid)))))
       (sql/join :left :kysymys_jatkokysymys (= :kysymys.kysymysid :kysymys_jatkokysymys.jatkokysymysid))
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
                :kysymysryhma.tila "julkaistu"})
    (sql/order :kysymysryhma_kyselypohja.jarjestys)
    sql/exec
    (->> (map (comp taydenna-kysymysryhman-monivalintakysymykset vaihda-kysymysavain)))))

(defn hae-kyselypohjaan-kuuluvat [kyselypohjaid]
  (let [kysymysryhmat (db/hae-kyselypohjan-kysymysryhmat {:kyselypohjaid kyselypohjaid})
        kysymysryhmaidt (map :kysymysryhmaid kysymysryhmat)]
    (map hae kysymysryhmaidt)))

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
    (sql/set-fields (assoc (select-keys kysymysryhma [:nimi_fi :nimi_sv :nimi_en :selite_fi :selite_sv :selite_en :kuvaus_fi :kuvaus_sv :kuvaus_en
                                                      :valtakunnallinen :koulutustoimija :taustakysymykset])
                      :muutettu_kayttaja (:oid *kayttaja*)))
    (sql/where {:kysymysryhmaid (:kysymysryhmaid kysymysryhma)})
    (sql/update)))

(defn poista!
  [kysymysryhmaid]
  (auditlog/kysymysryhma-poisto! kysymysryhmaid)
  (sql/delete taulut/kysymysryhma
    (sql/where {:kysymysryhmaid kysymysryhmaid})))

(defn poista-kysymyksen-monivalintavaihtoehdot!
  [kysymysid]
  (auditlog/kysymys-monivalinnat-poisto! kysymysid)
  (db/poista-monivalintavaihtoehdot! {:kysymysidt kysymysid}))

(defn poista-jatkokysymys! [kysymysid]
  (db/poista-jatkokysymykset! {:kysymysidt kysymysid}))

(defn poista-kysymys! [kysymys]
  (when (= "monivalinta" (:vastaustyyppi kysymys))
    (poista-kysymyksen-monivalintavaihtoehdot! (:kysymysid kysymys)))
  (poista-jatkokysymys! (:kysymysid kysymys))
  (db/poista-kysymykset! {:kysymysidt (:kysymysid kysymys)})
  (:kysymysid kysymys))

(defn poista-kysymysryhman-kysymykset! [kysymysryhmaid]
  (let [kysymykset (db/hae-kysymysryhman-kysymykset {:kysymysryhmaid kysymysryhmaid})]
    (doseq [kysymys kysymykset]
      (poista-kysymys! kysymys))))

(defn ^:private aseta-tila!
  [kysymysryhmaid tila]
  (sql/update taulut/kysymysryhma
    (sql/set-fields {:tila tila :muutettu_kayttaja (:oid *kayttaja*)})
    (sql/where {:kysymysryhmaid kysymysryhmaid}))
  (hae kysymysryhmaid))

(defn julkaise!
  [kysymysryhmaid]
  (when (julkaistavissa? kysymysryhmaid)
    (auditlog/kysymysryhma-muokkaus! kysymysryhmaid :julkaistu)
    (aseta-tila! kysymysryhmaid "julkaistu")))

(defn sulje!
  [kysymysryhmaid]
  (auditlog/kysymysryhma-muokkaus! kysymysryhmaid :suljettu)
  (aseta-tila! kysymysryhmaid "suljettu"))

(defn palauta-luonnokseksi!
  [kysymysryhmaid]
  (when (julkaistu? kysymysryhmaid))
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

(defn hae-asteikot [koulutustoimija]
  (db/hae-asteikot {:koulutustoimija koulutustoimija}))

(defn tallenna-asteikko [asteikko]
  (do (db/tallenna-asteikko asteikko)
      asteikko))
