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

(ns aipal.toimiala.raportti.raportointi
  (:require [korma.core :as sql]))

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
  (merge {1 0 2 0 3 0 4 0 5 0}
         (frequencies (keep :kylla_asteikko vastaukset))))

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
        tiedot-vaihtoehdolle (fn [avain lukumaara]
                               {:vaihtoehto-avain avain
                                :lukumaara lukumaara
                                :osuus (prosentteina
                                         (laske-osuus lukumaara yhteensa))})]
    [(tiedot-vaihtoehdolle "1" (jakauma 1))
     (tiedot-vaihtoehdolle "2" (jakauma 2))
     (tiedot-vaihtoehdolle "3" (jakauma 3))
     (tiedot-vaihtoehdolle "4" (jakauma 4))
     (tiedot-vaihtoehdolle "5" (jakauma 5))]))

(defn muodosta-kylla-ei-jakauman-esitys
  [jakauma]
  (let [yhteensa (+ (:kylla jakauma) (:ei jakauma))]
    [{:vaihtoehto-avain "kylla"
      :lukumaara (:kylla jakauma)
      :osuus (prosentteina
               (laske-osuus (:kylla jakauma) yhteensa))}
     {:vaihtoehto-avain "ei"
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
             {:vaihtoehto_fi (:teksti_fi vaihtoehto)
              :vaihtoehto_sv (:teksti_sv vaihtoehto)
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
    (let [ei-vastaukset (keep :ei_vastausteksti vastaukset)]
      {:kysymys_fi (:ei_teksti_fi kysymys)
       :kysymys_sv (:ei_teksti_sv kysymys)
       :vastaukset (for [v ei-vastaukset] {:teksti v})})))

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
         (for [v vastaukset] {:teksti (:vapaateksti v)})))

(defn kysymyksen-kasittelija
  [kysymys]
  (cond
    (= (:vastaustyyppi kysymys) "asteikko") lisaa-asteikon-jakauma
    (= (:vastaustyyppi kysymys) "kylla_ei_valinta") lisaa-vaihtoehtojen-jakauma
    (= (:vastaustyyppi kysymys) "likert_asteikko") lisaa-asteikon-jakauma
    (= (:vastaustyyppi kysymys) "monivalinta") lisaa-monivalinnan-jakauma
    (= (:vastaustyyppi kysymys) "vapaateksti") lisaa-vastausten-vapaateksti
    :else (fn [kysymys vastaukset] kysymys)))

(defn muodosta-raportti-vastauksista
  [kysymykset vastaukset]
  (map (fn [kysymys]
         ((kysymyksen-kasittelija kysymys) kysymys
          (kysymyksen-vastaukset kysymys vastaukset)))
       kysymykset))

(defn suodata-raportin-kentat
  [raportti]
  (map #(select-keys % [:kysymys_fi :kysymys_sv :jakauma :vastaukset :jatkovastaukset :vastaustyyppi])
       raportti))
