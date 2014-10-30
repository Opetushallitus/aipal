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
  (:require [clj-time.core :as time]
            [korma.core :as sql]
            [aipal.integraatio.sql.korma :refer [kyselykerta]]))

(defn ^:private hae-kyselykerta [kyselykertaid]
  (->
    (sql/select* kyselykerta)
    (sql/fields :kyselyid :kyselykertaid :nimi_fi :nimi_sv :voimassa_alkupvm :voimassa_loppupvm)
    (sql/where {:kyselykertaid kyselykertaid})

    sql/exec
    first))

(defn ^:private hae-kysymykset [kyselykertaid]
  (sql/select :kyselykerta
    (sql/join :inner :kysely
             (= :kyselykerta.kyselyid
                :kysely.kyselyid))
    (sql/join :inner :kysely_kysymysryhma
             (= :kysely.kyselyid
                :kysely_kysymysryhma.kyselyid))
    ;; otetaan mukaan vain kyselyyn kuuluvat kysymykset
    (sql/join :inner :kysely_kysymys
              (= :kysely.kyselyid
                 :kysely_kysymys.kyselyid))
    (sql/join :inner :kysymys
             (and (= :kysely_kysymysryhma.kysymysryhmaid
                     :kysymys.kysymysryhmaid)
                  (= :kysely_kysymys.kysymysid
                     :kysymys.kysymysid)))
    (sql/join :left :jatkokysymys
              (= :jatkokysymys.jatkokysymysid
                 :kysymys.jatkokysymysid))
    (sql/where {:kyselykertaid kyselykertaid})
    (sql/order :kysely_kysymysryhma.jarjestys :ASC)
    (sql/order :kysymys.jarjestys :ASC)
    (sql/fields :kyselykerta.kyselykertaid
                :kysymys.kysymysryhmaid
                :kysymys.kysymysid
                :kysymys.kysymys_fi
                :kysymys.kysymys_sv
                :kysymys.vastaustyyppi
                :jatkokysymys.jatkokysymysid
                :jatkokysymys.kylla_kysymys
                :jatkokysymys.kylla_teksti_fi
                :jatkokysymys.kylla_teksti_sv
                :jatkokysymys.ei_kysymys
                :jatkokysymys.ei_teksti_fi
                :jatkokysymys.ei_teksti_sv)))

(defn ^:private hae-vastaukset [kyselykertaid]
  (sql/select :kyselykerta
    (sql/join :inner :vastaaja
              (= :kyselykerta.kyselykertaid
                 :vastaaja.kyselykertaid))
    (sql/join :inner :vastaus
              (= :vastaaja.vastaajaid
                 :vastaus.vastaajaid))
    (sql/join :left :jatkovastaus
              (= :jatkovastaus.jatkovastausid
                 :vastaus.jatkovastausid))
    (sql/where {:kyselykertaid kyselykertaid})
    (sql/fields :kyselykerta.kyselykertaid
                :vastaaja.vastaajaid
                :vastaus.vastausid
                :vastaus.kysymysid
                :vastaus.numerovalinta
                :vastaus.vaihtoehto
                :vastaus.vapaateksti
                :jatkovastaus.jatkovastausid
                :jatkovastaus.jatkokysymysid
                :jatkovastaus.kylla_asteikko
                :jatkovastaus.ei_vastausteksti)))

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

(defn jaottele-jatkokysymys-asteikko
  [vastaukset]
  (dissoc (merge {1 0 2 0 3 0 4 0 5 0}
                 (frequencies (map :kylla_asteikko vastaukset)))
          nil))

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

(defn ^:private laske-osuus
  [lukumaara yhteensa]
  (if (> yhteensa 0)
    (/ lukumaara yhteensa)
    0))

(defn prosentteina
  [osuus]
  (Math/round (double (* osuus 100))))

(defn muodosta-asteikko-jakauman-esitys
  [jakauma]
  (let [yhteensa (reduce + (vals jakauma))
        tiedot-vaihtoehdolle (fn [kuvaus lukumaara]
                               {:vaihtoehto kuvaus
                                :lukumaara lukumaara
                                :osuus (prosentteina
                                         (laske-osuus lukumaara yhteensa))})]
    [(tiedot-vaihtoehdolle "Ei / en lainkaan" (jakauma 1))
     (tiedot-vaihtoehdolle "Hieman" (jakauma 2))
     (tiedot-vaihtoehdolle "Jonkin verran" (jakauma 3))
     (tiedot-vaihtoehdolle "Melko paljon" (jakauma 4))
     (tiedot-vaihtoehdolle "Erittäin paljon" (jakauma 5))]))

(defn muodosta-kylla-ei-jakauman-esitys
  [jakauma]
  (let [yhteensa (+ (:kylla jakauma) (:ei jakauma))]
    [{:vaihtoehto "Kyllä"
      :lukumaara (:kylla jakauma)
      :osuus (prosentteina
               (laske-osuus (:kylla jakauma) yhteensa))}
     {:vaihtoehto "Ei"
      :lukumaara (:ei jakauma)
      :osuus (prosentteina
               (laske-osuus (:ei jakauma) yhteensa))}]))

(defn ^:private muodosta-monivalintavaihtoehdot
  [kysymys]
  (->>
    (hae-monivalintavaihtoehdot (:kysymysid kysymys))
    (sort-by :jarjestys)))

(defn muodosta-monivalinta-jakauman-esitys
  [vaihtoehdot jakauma]
  (let [yhteensa (reduce + (vals jakauma))]
    (map (fn [vaihtoehto]
           (let [lukumaara (or (jakauma (:jarjestys vaihtoehto))
                               0)
                 osuus (laske-osuus lukumaara yhteensa)]
             {:vaihtoehto (:teksti_fi vaihtoehto)
              :lukumaara lukumaara
              :osuus (prosentteina osuus)}))
         vaihtoehdot)))

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

(defn keraa-kylla-jatkovastaukset
  [kysymys vastaukset]
  (when (:kylla_kysymys kysymys)
    {:kysymys_fi (:kylla_teksti_fi kysymys)
     :kysymys_sv (:kylla_teksti_sv kysymys)
     :jakauma (muodosta-asteikko-jakauman-esitys (jaottele-jatkokysymys-asteikko vastaukset))}))

(defn keraa-ei-jatkovastaukset
  [kysymys vastaukset]
  (when (:ei_kysymys kysymys)
    (let [ei-vastaukset (filter identity (map :ei_vastausteksti vastaukset))]
      {:kysymys_fi (:ei_teksti_fi kysymys)
       :kysymys_sv (:ei_teksti_sv kysymys)
       :vastaukset (map (fn [v] {:teksti v}) ei-vastaukset)})))

(defn keraa-jatkovastaukset
  [kysymys vastaukset]
  (when (:jatkokysymysid kysymys)
    {:kylla (keraa-kylla-jatkovastaukset kysymys vastaukset)
     :ei (keraa-ei-jatkovastaukset kysymys vastaukset)}))

(defn ^:private lisaa-vaihtoehtojen-jakauma
  [kysymys vastaukset]
  (assoc kysymys
         :jakauma
         (muodosta-kylla-ei-jakauman-esitys
           (jaottele-vaihtoehdot vastaukset))
         :jatkovastaukset
         (keraa-jatkovastaukset kysymys vastaukset)))

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

(defn ^:private muodosta-raportti-kyselykerrasta [kyselykertaid]
  (suodata-raportin-kentat
    (muodosta-raportti-vastauksista (hae-kysymykset kyselykertaid) (hae-vastaukset kyselykertaid))))

(defn muodosta-raportti [kyselykertaid]
  {:kyselykerta (hae-kyselykerta kyselykertaid)
   :luontipvm (time/today)
   :raportti (muodosta-raportti-kyselykerrasta kyselykertaid)})
