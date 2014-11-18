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
            [aipal.integraatio.sql.korma :as taulut]))

(defn hae-kysymysryhmat
  ([organisaatio vain-voimassaolevat]
    (-> (sql/select* taulut/kysymysryhma)
      (sql/join :inner :kysymysryhma_organisaatio_view (= :kysymysryhma_organisaatio_view.kysymysryhmaid :kysymysryhmaid))
      (sql/where (or {:kysymysryhma_organisaatio_view.koulutustoimija organisaatio}
                     (and {:kysymysryhma_organisaatio_view.valtakunnallinen true}
                          {:kysymysryhma.lisattavissa true})))
      (cond->
        vain-voimassaolevat (sql/where {:kysymysryhma.lisattavissa true}))
      (sql/fields :kysymysryhma.kysymysryhmaid :kysymysryhma.nimi_fi :kysymysryhma.nimi_sv
                  :kysymysryhma.selite_fi :kysymysryhma.selite_sv :kysymysryhma.valtakunnallinen
                  :kysymysryhma.lisattavissa :kysymysryhma.tila
                  [(sql/subselect taulut/kysymys
                     (sql/aggregate (count :*) :lkm)
                     (sql/where {:kysymys.kysymysryhmaid :kysymysryhma.kysymysryhmaid})) :kysymyksien_lkm])
      (sql/order :muutettuaika :desc)
      sql/exec))
  ([organisaatio]
    (hae-kysymysryhmat organisaatio false)))

(defn lisaa-kysymysryhma! [k]
  (sql/insert taulut/kysymysryhma
    (sql/values k)))

(defn lisaa-kysymys! [k]
  (sql/insert taulut/kysymys
    (sql/values k)))

(defn lisaa-jatkokysymys! [k]
  (sql/insert :jatkokysymys
    (sql/values k)))

(defn lisaa-monivalintavaihtoehto! [v]
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
                :kysymys.poistettava :kysymys.pakollinen :kysymys.vastaustyyppi
                :kysymys.max_vastaus :kysymys.monivalinta_max
                :jatkokysymys.kylla_teksti_fi :jatkokysymys.kylla_teksti_sv
                :jatkokysymys.ei_teksti_fi :jatkokysymys.ei_teksti_sv
                [:jatkokysymys.max_vastaus :jatkokysymys_max_vastaus])
    (sql/order :kysymys.jarjestys)))

(defn taydenna-monivalintakysymys
  [monivalintakysymys]
  (let [kysymysid (:kysymysid monivalintakysymys)]
    (assoc monivalintakysymys :monivalintavaihtoehdot (hae-monivalintakysymyksen-vaihtoehdot kysymysid))))

(def kylla-jatkokysymykset-kentat
  [:kylla_teksti_fi
   :kylla_teksti_sv])

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
      (if hae-kysymykset
        (-> kysymysryhma
          taydenna-kysymysryhma
          taydenna-kysymysryhman-kysymykset)
        kysymysryhma))))

(def kysymysryhma-esikatselulle-select
  (->
    (sql/select* taulut/kysymysryhma)
    (sql/fields :kysymysryhmaid :nimi_fi :nimi_sv)
    (sql/with taulut/kysymys
      (sql/fields :kysymysid :kysymys_fi :kysymys_sv :poistettava :pakollinen :vastaustyyppi :monivalinta_max
                  :jatkokysymys.jatkokysymysid :jatkokysymys.kylla_teksti_fi :jatkokysymys.kylla_teksti_sv :jatkokysymys.ei_teksti_fi :jatkokysymys.ei_teksti_sv)
      (sql/join :left :jatkokysymys (= :kysymys.jatkokysymysid :jatkokysymys.jatkokysymysid))
      (sql/order :kysymys.jarjestys))))

(defn vaihda-kysymysavain [kysymysryhma]
  (clojure.set/rename-keys kysymysryhma {:kysymys :kysymykset}))

(defn hae-esikatselulle
  [kysymysryhmaid]
  (->
    kysymysryhma-esikatselulle-select
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
  (-> kysymysryhma-esikatselulle-select
    (sql/join :inner taulut/kysymysryhma-kyselypohja (= :kysymysryhma_kyselypohja.kysymysryhmaid :kysymysryhma.kysymysryhmaid))
    (sql/fields :kysymysryhma_kyselypohja.kyselypohjaid :kysymysryhma_kyselypohja.jarjestys)
    (sql/where {:kysymysryhma_kyselypohja.kyselypohjaid kyselypohjaid
                :kysymysryhma.lisattavissa true})
    (sql/order :kysymysryhma_kyselypohja.jarjestys)
    sql/exec
    (->> (map (comp taydenna-kysymysryhman-monivalintakysymykset vaihda-kysymysavain)))))

(defn hae-organisaatiotieto
  [kysymysryhmaid]
  (first
    (sql/select :kysymysryhma_organisaatio_view
      (sql/fields :koulutustoimija :valtakunnallinen)
      (sql/where {:kysymysryhmaid kysymysryhmaid}))))

(defn paivita!
  [kysymysryhma]
  (->
    (sql/update* taulut/kysymysryhma)
    (sql/set-fields (select-keys kysymysryhma [:nimi_fi :nimi_sv :selite_fi :selite_sv :valtakunnallinen :koulutustoimija :taustakysymykset]))
    (sql/where {:kysymysryhmaid (:kysymysryhmaid kysymysryhma)})
    (sql/update)))

(defn poista-kysymys!
  [kysymysid]
  (sql/delete
    taulut/kysymys
    (sql/where {:kysymysid kysymysid})))

(defn poista-kysymyksen-monivalintavaihtoehdot!
  [kysymysid]
  (sql/delete
    :monivalintavaihtoehto
    (sql/where {:kysymysid kysymysid})))

(defn poista-jatkokysymys!
  [jatkokysymysid]
  (sql/delete
    taulut/jatkokysymys
    (sql/where {:jatkokysymysid jatkokysymysid})))

(defn julkaise!
  [kysymysryhmaid]
  (sql/update taulut/kysymysryhma
    (sql/set-fields {:tila "julkaistu"})
    (sql/where {:kysymysryhmaid kysymysryhmaid})))

(defn laske-kysymykset
  [kysymysryhmaid]
  (->
    (sql/select taulut/kysymys
      (sql/aggregate (count :*) :lkm)
      (sql/where {:kysymysryhmaid kysymysryhmaid}))
    first
    :lkm))
