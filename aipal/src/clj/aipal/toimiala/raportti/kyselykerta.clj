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

(defn ^:private hae-kysymykset [kyselykertaid]
  (->
    (sql/select* :kyselykerta)
    (sql/fields :kyselykerta.kyselykertaid)
    (sql/where {:kyselykertaid kyselykertaid})

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
    (sql/fields :kysymys.kysymysid
                :kysymys.kysymys_fi
                :kysymys.vastaustyyppi)
    (sql/order :kysymys.jarjestys :ASC)

    ;; otetaan mukaan vain kyselyyn kuuluvat kysymykset
    (sql/join :inner {:table :kysely_kysymys}
              (and (= :kysely.kyselyid
                      :kysely_kysymys.kyselyid)
                   (= :kysymys.kysymysid
                      :kysely_kysymys.kysymysid)))

    sql/exec))

(defn ^:private hae-vastaukset [kyselykertaid]
  (->
    (sql/select* :kyselykerta)
    (sql/fields :kyselykerta.kyselykertaid)
    (sql/where {:kyselykertaid kyselykertaid})

    (sql/join :inner {:table :vastaaja}
             (= :kyselykerta.kyselykertaid
                :vastaaja.kyselykertaid))
    (sql/fields :vastaaja.vastaajaid)

    (sql/join :inner {:table :vastaus}
              (= :vastaaja.vastaajaid
                 :vastaus.vastaajaid))
    (sql/fields :vastaus.vastausid
                :vastaus.kysymysid
                :vastaus.numerovalinta
                :vastaus.vaihtoehto
                :vastaus.vapaateksti)

    sql/exec))

(defn ^:private hae-monivalintavaihtoehdot [kysymysid]
  (->
    (sql/select* :monivalintavaihtoehto)
    (sql/fields :jarjestys :teksti_fi :teksti_sv)
    (sql/where {:kysymysid kysymysid})

    sql/exec))

(defn ^:private kysymyksen-vastaukset
  [kysymys vastaukset]
  (filter (fn [vastaus] (= (:kysymysid vastaus) (:kysymysid kysymys)))
          vastaukset))

(defn jaottele-asteikko
  [vastaukset]
  (merge {1 0 2 0 3 0 4 0 5 0}
         (frequencies (map :numerovalinta vastaukset))))

(defn jaottele-monivalinta
  [vastaukset]
  (frequencies (map :numerovalinta vastaukset)))

(defn jaottele-vaihtoehdot
  [vastaukset]
  (reduce (fn [jakauma vastaus] (update-in jakauma [(keyword (:vaihtoehto vastaus))]
                                           (fn [n] (if (number? n)
                                                     (inc n)
                                                     1))))
          {:kylla 0 :ei 0}
          vastaukset))

(defn ^:private muodosta-asteikko-jakauman-esitys
  [jakauma]
  [{:vaihtoehto "En / ei lainkaan"
    :lukumaara (jakauma 1)}
   {:vaihtoehto "Hieman"
    :lukumaara (jakauma 2)}
   {:vaihtoehto "Jonkin verran"
    :lukumaara (jakauma 3)}
   {:vaihtoehto "Melko paljon"
    :lukumaara (jakauma 4)}
   {:vaihtoehto "Erittäin paljon"
    :lukumaara (jakauma 5)}])

(defn ^:private muodosta-kylla-ei-jakauman-esitys
  [jakauma]
  [{:vaihtoehto "kyllä"
    :lukumaara (:kylla jakauma)}
   {:vaihtoehto "ei"
    :lukumaara (:ei jakauma)}])

(defn ^:private muodosta-monivalintavaihtoehdot
  [kysymys]
  (->>
    (hae-monivalintavaihtoehdot (:kysymysid kysymys))
    (sort-by :jarjestys)))

(defn muodosta-monivalinta-jakauman-esitys
  [vaihtoehdot jakauma]
  (map (fn [vaihtoehto] {:vaihtoehto (:teksti_fi vaihtoehto)
                         :lukumaara (or (jakauma (:jarjestys vaihtoehto))
                                        0)})
       vaihtoehdot))

(defn ^:private lisaa-asteikon-jakauma
  [kysymys vastaukset]
  (assoc kysymys :jakauma
         (muodosta-asteikko-jakauman-esitys
           (jaottele-asteikko vastaukset))))

(defn ^:private lisaa-monivalinnan-jakauma
  [kysymys vastaukset]
  (assoc kysymys :jakauma
         (muodosta-monivalinta-jakauman-esitys
           (muodosta-monivalintavaihtoehdot kysymys)
           (jaottele-monivalinta vastaukset))))

(defn ^:private lisaa-vaihtoehtojen-jakauma
  [kysymys vastaukset]
  (assoc kysymys :jakauma
         (muodosta-kylla-ei-jakauman-esitys
           (jaottele-vaihtoehdot vastaukset))))

(defn ^:private lisaa-vastausten-vapaateksti
  [kysymys vastaukset]
  (assoc kysymys :vastaukset
         (map (fn [v] {:teksti (:vapaateksti v)}) vastaukset)))

(defn kysymyksen-kasittelija
  [kysymys]
  (cond
    (= (:vastaustyyppi kysymys) "asteikko") lisaa-asteikon-jakauma
    (= (:vastaustyyppi kysymys) "kylla_ei_valinta") lisaa-vaihtoehtojen-jakauma
    (= (:vastaustyyppi kysymys) "monivalinta") lisaa-monivalinnan-jakauma
    (= (:vastaustyyppi kysymys) "vapaateksti") lisaa-vastausten-vapaateksti
    :else (fn [kysymys vastaukset] kysymys)))

(defn ^:private muodosta-raportti-vastauksista
  [kysymykset vastaukset]
  (map (fn [kysymys]
         ((kysymyksen-kasittelija kysymys) kysymys
                                           (kysymyksen-vastaukset kysymys vastaukset)))
       kysymykset))

(defn ^:private suodata-raportin-kentat
  [raportti]
  (map #(select-keys % [:kysymys_fi :jakauma :vastaukset :vastaustyyppi])
       raportti))

(defn muodosta-raportti [kyselykertaid]
  (suodata-raportin-kentat
    (muodosta-raportti-vastauksista (hae-kysymykset kyselykertaid) (hae-vastaukset kyselykertaid))))
