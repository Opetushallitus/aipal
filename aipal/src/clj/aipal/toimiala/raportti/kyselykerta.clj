(ns aipal.toimiala.raportti.kyselykerta)

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

(ns aipal.toimiala.raportti.kyselykerta
  (:require [korma.core :as sql]))

(defn ^:private hae-kysymykset []
  (->
    (sql/select* :kyselykerta)
    (sql/fields :kyselykerta.kyselykertaid)
    (sql/where {:kyselykertaid 1})

    (sql/join :inner {:table :kysely}
             (= :kyselykerta.kyselyid
                :kysely.kyselyid))

    (sql/join :inner {:table :kysely_kysymysryhma}
             (= :kysely.kyselyid
                :kysely_kysymysryhma.kyselyid))
    (sql/order :kysely_kysymysryhma.jarjestys :ASC)

    (sql/join :inner {:table :kysymysryhma}
             (= :kysely_kysymysryhma.kysymysryhmaid
                :kysymysryhma.kysymysryhmaid))
    (sql/fields :kysymysryhma.kysymysryhmaid)

    (sql/join :inner {:table :kysymys}
             (= :kysymysryhma.kysymysryhmaid
                :kysymys.kysymysryhmaid))
    (sql/fields :kysymys.kysymysid :kysymys.kysymys_fi)
    (sql/order :kysymys.jarjestys :ASC)

    sql/exec))

(defn ^:private hae-vastaukset []
  (->
    (sql/select* :kyselykerta)
    (sql/fields :kyselykerta.kyselykertaid)
    (sql/where {:kyselykertaid 1})

    (sql/join :inner {:table :vastaustunnus}
             (= :kyselykerta.kyselykertaid
                :vastaustunnus.kyselykertaid))
    (sql/fields :vastaustunnus.vastaustunnusid)

    (sql/join :inner {:table :vastaus}
              (= :vastaustunnus.vastaustunnusid
                 :vastaus.vastaustunnusid))
    (sql/fields :vastaus.vastausid
                :vastaus.kysymysid
                :vastaus.vaihtoehto)

    sql/exec))

(defn ^:private kysymyksen-vastaukset
  [kysymys vastaukset]
  (filter (fn [vastaus] (= (:kysymysid vastaus) (:kysymysid kysymys)))
          vastaukset))

(defn ^:private jaottele-vaihtoehdot
  [vastaukset]
  (reduce (fn [jakauma vastaus] (update-in jakauma [(keyword (:vaihtoehto vastaus))] inc))
          {:kylla 0 :ei 0}
          vastaukset))

(defn ^:private muodosta-jakauman-esitys
  [jakauma]
  [{:vaihtoehto "kyllä"
    :lukumaara (:kylla jakauma)}
   {:vaihtoehto "ei"
    :lukumaara (:ei jakauma)}])

(defn ^:private lisaa-kysymykseen-jakauma
  [kysymys vastaukset]
  (assoc kysymys :jakauma
         (muodosta-jakauman-esitys
           (jaottele-vaihtoehdot (kysymyksen-vastaukset kysymys vastaukset)))))

(defn ^:private laske-vaihtoehtojen-jakauma
  [kysymykset vastaukset]
  (map #(lisaa-kysymykseen-jakauma % vastaukset) kysymykset))

(defn ^:private suodata-raportin-kentat
  [raportti]
  (map #(select-keys % [:kysymys_fi :jakauma]) raportti))

(defn muodosta-raportti []
  (suodata-raportin-kentat
    (laske-vaihtoehtojen-jakauma (hae-kysymykset) (hae-vastaukset))))
