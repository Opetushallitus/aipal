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
            [aipal.integraatio.sql.korma :as taulut :refer [voimassa]]))

(defn hae-kysymysryhmat
  ([organisaatio vain-voimassaolevat]
    (-> (sql/select* taulut/kysymysryhma)
      (sql/join :inner :kysymysryhma_organisaatio_view (= :kysymysryhma_organisaatio_view.kysymysryhmaid :kysymysryhmaid))
      (sql/where (or {:kysymysryhma_organisaatio_view.koulutustoimija organisaatio}
                     {:kysymysryhma_organisaatio_view.valtakunnallinen true}))
      (cond->
        vain-voimassaolevat (voimassa))
      (sql/fields :kysymysryhma.kysymysryhmaid :kysymysryhma.nimi_fi :kysymysryhma.nimi_sv :kysymysryhma.selite_fi :kysymysryhma.selite_sv :kysymysryhma.valtakunnallinen)
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
    (sql/fields :kysymysryhmaid :nimi_fi :nimi_sv :taustakysymykset :valtakunnallinen)))

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
  (apply conj kylla-jatkokysymykset-kentat ei-jatkokysymykset-kentat))

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

(defn valitse-jatkokysymyksen-kentat [jatkokysymys]
  (-> jatkokysymys
    (select-keys jatkokysymykset-kentat)
    poista-nil-kentat
    (clojure.set/rename-keys {:jatkokysymys_max_vastaus :max_vastaus})))

(defn erottele-jatkokysymys
  [kysymys]
  (-> kysymys
    valitse-jatkokysymyksen-kentat
    (assoc :kylla_jatkokysymys (kylla-jatkokysymys? kysymys))
    (assoc :ei_jatkokysymys (ei-jatkokysymys? kysymys))))

(defn taydenna-jatkokysymys
  [kysymys]
  (let [jatkokysymys (erottele-jatkokysymys kysymys)]
    (-> kysymys
      (assoc :jatkokysymys jatkokysymys)
      ((fn [kysymys] (apply dissoc kysymys jatkokysymykset-kentat))))))

(defn taydenna-kysymys
  [kysymys]
  (cond-> kysymys
    (= "monivalinta" (:vastaustyyppi kysymys)) taydenna-monivalintakysymys
    (jatkokysymys? kysymys) taydenna-jatkokysymys))

(defn taydenna-kysymysryhman-kysymykset
  [kysymysryhma]
  (update-in kysymysryhma [:kysymykset] #(map taydenna-kysymys %)))

(defn taydenna-kysymysryhma
  [kysymysryhma]
  (let [kysymysryhmaid (:kysymysryhmaid kysymysryhma)]
    (assoc kysymysryhma :kysymykset
           (-> kysymys-select
             (sql/where {:kysymysryhmaid kysymysryhmaid})
             (sql/exec)))))

(defn hae [kysymysryhmaid]
  (-> kysymysryhma-select
    (sql/where {:kysymysryhmaid kysymysryhmaid})
    sql/exec
    first
    taydenna-kysymysryhma
    taydenna-kysymysryhman-kysymykset))

(defn hae-kyselypohjasta [kyselypohjaid]
  (-> kysymysryhma-select
    (sql/join :inner taulut/kysymysryhma-kyselypohja (= :kysymysryhma_kyselypohja.kysymysryhmaid :kysymysryhma.kysymysryhmaid))
    (sql/fields :kysymysryhma_kyselypohja.kyselypohjaid)
    (sql/where {:kysymysryhma_kyselypohja.kyselypohjaid kyselypohjaid})
    (sql/order :kysymysryhma_kyselypohja.jarjestys)
    sql/exec
    ((fn [kysymysryhmat] (map taydenna-kysymysryhma kysymysryhmat)))))

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
