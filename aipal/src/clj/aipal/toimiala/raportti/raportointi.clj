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
  (:require [korma.core :as sql]
            [clojure-csv.core :refer [write-csv]]
            [aipal.rest-api.i18n :as i18n]
            [clj-time.core :as t]
            [oph.common.util.http-util :refer [parse-iso-date]]))

(defn ^:private hae-monivalintavaihtoehdot [kysymysid]
  (->
    (sql/select* :monivalintavaihtoehto)
    (sql/fields :jarjestys :teksti_fi :teksti_sv)
    (sql/where {:kysymysid kysymysid})
    sql/exec))

(defn aseta-eos [vastaukset kentta]
  (for [vastaus vastaukset]
    (if (:en_osaa_sanoa vastaus)
      (assoc vastaus kentta :eos)
      vastaus)))

(defn ^:private kysymyksen-vastaukset
  [kysymys vastaukset]
  (filter (fn [vastaus] (= (:kysymysid vastaus) (:kysymysid kysymys)))
          vastaukset))

(defn jaottele-asteikko
  [vastaukset]
  (merge {1 0, 2 0, 3 0, 4 0, 5 0, :eos 0}
         (frequencies (map :numerovalinta vastaukset))))

(defn jaottele-jatkokysymys-asteikko
  [vastaukset]
  (merge {1 0, 2 0, 3 0, 4 0, 5 0, :eos 0}
         (frequencies (keep :kylla_asteikko vastaukset))))

(defn jaottele-monivalinta
  [vastaukset]
  (frequencies (map :numerovalinta vastaukset)))

(defn jaottele-vaihtoehdot
  [vastaukset]
  (->> vastaukset
    (map (comp keyword :vaihtoehto))
    frequencies
    (merge {:kylla 0, :ei 0, :eos 0})))

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
                                         (laske-osuus (or lukumaara 0) yhteensa))})]
    [(tiedot-vaihtoehdolle "1" (jakauma 1))
     (tiedot-vaihtoehdolle "2" (jakauma 2))
     (tiedot-vaihtoehdolle "3" (jakauma 3))
     (tiedot-vaihtoehdolle "4" (jakauma 4))
     (tiedot-vaihtoehdolle "5" (jakauma 5))
     (tiedot-vaihtoehdolle "eos" (jakauma :eos))]))

(defn muodosta-kylla-ei-jakauman-esitys
  [jakauma]
  (let [yhteensa (+ (:kylla jakauma) (:ei jakauma) (:eos jakauma))]
    [{:vaihtoehto-avain "kylla"
      :lukumaara (:kylla jakauma)
      :osuus (prosentteina
               (laske-osuus (:kylla jakauma) yhteensa))}
     {:vaihtoehto-avain "ei"
      :lukumaara (:ei jakauma)
      :osuus (prosentteina
               (laske-osuus (:ei jakauma) yhteensa))}
     {:vaihtoehto-avain "eos"
      :lukumaara (:eos jakauma)
      :osuus (prosentteina
               (laske-osuus (:eos jakauma) yhteensa))}]))

(defn ^:private muodosta-monivalintavaihtoehdot
  [kysymys]
  (let [vaihtoehdot (hae-monivalintavaihtoehdot (:kysymysid kysymys))]
    (concat (sort-by :jarjestys vaihtoehdot) [{:jarjestys :eos}])))

(defn muodosta-monivalinta-jakauman-esitys
  [vaihtoehdot jakauma]
  (let [yhteensa (reduce + (vals jakauma))]
    (for [vaihtoehto vaihtoehdot
          :let [lukumaara (or (jakauma (:jarjestys vaihtoehto)) 0)
                osuus (laske-osuus lukumaara yhteensa)]]
      {:vaihtoehto_fi (:teksti_fi vaihtoehto)
       :vaihtoehto_sv (:teksti_sv vaihtoehto)
       :lukumaara lukumaara
       :osuus (prosentteina osuus)
       :jarjestys (:jarjestys vaihtoehto)})))

(defn ^:private lisaa-asteikon-jakauma
  [kysymys vastaukset]
  (let [vastaukset (aseta-eos vastaukset :numerovalinta)]
    (assoc kysymys :jakauma
           (muodosta-asteikko-jakauman-esitys
             (jaottele-asteikko vastaukset)))))

(defn ^:private lisaa-monivalinnan-jakauma
  [kysymys vastaukset]
  (let [vastaukset (aseta-eos vastaukset :numerovalinta)]
    (assoc kysymys :jakauma
           (muodosta-monivalinta-jakauman-esitys
             (muodosta-monivalintavaihtoehdot kysymys)
             (jaottele-monivalinta vastaukset)))))

(defn keraa-kylla-jatkovastaukset
  [kysymys vastaukset]
  (when (:kylla_kysymys kysymys)
    {:kysymys_fi (:kylla_teksti_fi kysymys)
     :kysymys_sv (:kylla_teksti_sv kysymys)
     :jakauma (butlast (muodosta-asteikko-jakauman-esitys (jaottele-jatkokysymys-asteikko vastaukset))) ;; EOS-vastaus on jakauman viimeinen eikä sitä käytetä jatkovastauksissa
     :vastaustyyppi (:kylla_vastaustyyppi kysymys)}))

(defn keraa-ei-jatkovastaukset
  [kysymys vastaukset]
  (when (:ei_kysymys kysymys)
    (let [ei-vastaukset (keep :ei_vastausteksti vastaukset)]
      {:kysymys_fi (:ei_teksti_fi kysymys)
       :kysymys_sv (:ei_teksti_sv kysymys)
       :vapaatekstivastaukset (for [v ei-vastaukset] {:teksti v})
       :vastaustyyppi "vapaateksti"})))

(defn keraa-jatkovastaukset
  [kysymys vastaukset]
  (when (:jatkokysymysid kysymys)
    {:kylla (keraa-kylla-jatkovastaukset kysymys vastaukset)
     :ei (keraa-ei-jatkovastaukset kysymys vastaukset)}))

(defn ^:private lisaa-vaihtoehtojen-jakauma
  [kysymys vastaukset]
  (let [vastaukset (aseta-eos vastaukset :vaihtoehto)]
    (assoc kysymys
           :jakauma
           (muodosta-kylla-ei-jakauman-esitys
             (jaottele-vaihtoehdot vastaukset))
           :jatkovastaukset
           (keraa-jatkovastaukset kysymys vastaukset))))

(defn ^:private lisaa-vastausten-vapaateksti
  [kysymys vastaukset]
  (assoc kysymys :vapaatekstivastaukset
         (for [v vastaukset] {:teksti (:vapaateksti v)})))

(defn kysymyksen-kasittelija
  [kysymys]
  (case (:vastaustyyppi kysymys)
    "arvosana" lisaa-asteikon-jakauma
    "asteikko" lisaa-asteikon-jakauma
    "kylla_ei_valinta" lisaa-vaihtoehtojen-jakauma
    "likert_asteikko" lisaa-asteikon-jakauma
    "monivalinta" lisaa-monivalinnan-jakauma
    "vapaateksti" lisaa-vastausten-vapaateksti))

(defn suodata-eos-vastaukset [kysymys]
  (if (:eos_vastaus_sallittu kysymys)
    kysymys
    (update-in kysymys [:jakauma] butlast))) ;; EOS-vastaus on aina jakauman viimeinen

(defn valitse-kysymyksen-kentat
  [kysymys]
  (select-keys kysymys [:kysymys_fi
                        :kysymys_sv
                        :jakauma
                        :vapaatekstivastaukset
                        :vastaajien_lukumaara
                        :keskiarvo
                        :keskihajonta
                        :jatkovastaukset
                        :vastaustyyppi
                        :eos_vastaus_sallittu
                        :jarjestys]))

(defn vastaajien-lukumaara [vastaukset]
  (->> vastaukset
    (map :vastaajaid)
    distinct
    count))

(defn keskihajonta [arvot]
  (let [lukumaara (count arvot)
        summa (reduce + arvot)
        keskiarvo (/ summa lukumaara)
        hajonnat (map #(Math/pow (- % keskiarvo) 2) arvot)
        jaettava (reduce + hajonnat)
        jakaja (- lukumaara 1)]
    (if (not= jakaja 0)
      (Math/sqrt (/ jaettava jakaja))
      0)))

(defn vastausten-keskiarvo-ja-hajonta [kysymys]
  (let [numerovalinnat (keep :numerovalinta (:vastaukset kysymys))
        vastauksia (count numerovalinnat)]
    (when
      (and
        (some #{(:vastaustyyppi kysymys)} '("asteikko" "likert_asteikko" "arvosana"))
        (not= 0 vastauksia))
      (let [summa (reduce + numerovalinnat)
            keskiarvo (float (/ summa vastauksia))
            keskihajonta (keskihajonta numerovalinnat)]
        {:keskiarvo keskiarvo
         :keskihajonta keskihajonta}))))

(defn kasittele-kysymykset
  [kysymykset]
  (for [kysymys kysymykset
        :let [keskiarvo-ja-hajonta (vastausten-keskiarvo-ja-hajonta kysymys)
              vastaajia (vastaajien-lukumaara (:vastaukset kysymys))
              kasitelty-kysymys ((kysymyksen-kasittelija kysymys) kysymys (:vastaukset kysymys))]]
    (-> kasitelty-kysymys
      (assoc :vastaajien_lukumaara vastaajia)
      (merge keskiarvo-ja-hajonta)
      suodata-eos-vastaukset
      valitse-kysymyksen-kentat)))

(defn kysymysryhmaan-vastanneiden-lukumaara [kysymysryhma]
  (->> (:kysymykset kysymysryhma)
    (mapcat :vastaukset)
    (map :vastaajaid)
    distinct
    count))

(defn kasittele-kysymysryhmat
  [kysymysryhmat]
  (for [kysymysryhma kysymysryhmat
        :let [vastaajia (kysymysryhmaan-vastanneiden-lukumaara kysymysryhma)]]
    (-> kysymysryhma
      (assoc :vastaajien_lukumaara vastaajia)
      (update-in [:kysymykset] kasittele-kysymykset))))

(defn liita-kysymyksiin-vastaukset [kysymykset kysymysten-vastaukset]
  (for [kysymys kysymykset
        :let [vastaukset (get kysymysten-vastaukset (:kysymysid kysymys))]]
    (assoc kysymys :vastaukset vastaukset)))

(defn ryhmittele-kysymykset-ja-vastaukset-kysymysryhmittain [kysymykset vastaukset kysymysryhmat]
  (let [kysymysryhmien-kysymykset (group-by :kysymysryhmaid kysymykset)
        kysymysten-vastaukset (group-by :kysymysid vastaukset)]
    (for [kysymysryhma kysymysryhmat
          :let [kysymykset (get kysymysryhmien-kysymykset (:kysymysryhmaid kysymysryhma))
                kysymykset-ja-vastaukset (liita-kysymyksiin-vastaukset kysymykset kysymysten-vastaukset)]
          :when (seq (mapcat :vastaukset kysymykset-ja-vastaukset))]
      (assoc kysymysryhma :kysymykset kysymykset-ja-vastaukset))))

(defn muodosta-raportti-vastauksista
  [kysymysryhmat kysymykset vastaukset]
  (kasittele-kysymysryhmat (ryhmittele-kysymykset-ja-vastaukset-kysymysryhmittain kysymykset vastaukset kysymysryhmat)))

(defn numeroiden-piste-pilkuksi
  "Jos merkkijono on numero, niin muutetaan piste pilkuksi"
  [merkkijono]
  (if (re-matches #"[0-9.]+" merkkijono)
    (clojure.string/replace merkkijono #"\." ",")
    merkkijono))

(defn muuta-kaikki-stringeiksi [rivit]
  (clojure.walk/postwalk (fn [x]
                           (if (coll? x)
                             x
                             (numeroiden-piste-pilkuksi (str x))))
                         rivit))

(defn lokalisoitu-kentta
  [m kentta kieli]
  (let [avain (keyword (str kentta "_" kieli))
        vaihtoehto-avain (keyword (str kentta "_" (if (= kieli "fi")
                                                    "sv"
                                                    kieli)))]
    (or (avain m) (vaihtoehto-avain m))))

(defn lokalisoi-vaihtoehto-avain
  [tekstit tyyppi avain]
  (get-in tekstit [:kysymys (keyword tyyppi) (keyword avain)]))

(def otsikot [:kysymysryhma
              :kysymys
              :vastaajien_lukumaara
              :vastaajien_maksimimaara
              :keskiarvo
              :keskihajonta
              :vastaukset])

(defn raportti-taulukoksi
  [raportti kieli]
  (let [tekstit (i18n/hae-tekstit kieli)]
    (concat [[(lokalisoitu-kentta raportti "nimi" kieli)]]
            (into [(for [otsikko otsikot]
                     (get-in tekstit [:raportit :csv otsikko]))]
                  (for [kysymysryhma (:raportti raportti)
                        kysymys (:kysymykset kysymysryhma)]
                    (flatten [(lokalisoitu-kentta kysymysryhma "nimi" kieli) (lokalisoitu-kentta kysymys "kysymys" kieli) (:vastaajien_lukumaara kysymys) (:vastaajien_maksimimaara kysymysryhma)
                              (:keskiarvo kysymys) (:keskihajonta kysymys)
                              (for [vaihtoehto (:jakauma kysymys)]
                                [(if (:vaihtoehto-avain vaihtoehto)
                                   (lokalisoi-vaihtoehto-avain tekstit (:vastaustyyppi kysymys) (:vaihtoehto-avain vaihtoehto))
                                   (lokalisoitu-kentta vaihtoehto "vaihtoehto" kieli))
                                 (:lukumaara vaihtoehto)])]))))))

(defn muodosta-csv
  [raportti kieli]
  (write-csv
    (muuta-kaikki-stringeiksi (raportti-taulukoksi raportti kieli))
    :delimiter \;))

(defn muodosta-tyhja-csv
  [raportti kieli]
  (let [tekstit (i18n/hae-tekstit kieli)]
    (write-csv
      (muuta-kaikki-stringeiksi [[(lokalisoitu-kentta raportti "nimi" kieli)]
                                 [(get-in tekstit [:raportit :liian_vahan_vastaajia]) (:vastaajien-lkm raportti)]])
      :delimiter \;)))

(defn ei-riittavasti-vastaajia
  [raportti asetukset]
  (let [vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)]
    (if (>= (:vastaajien-lkm raportti) vaaditut-vastaajat)
      raportti
      (assoc (dissoc raportti :raportti) :virhe "ei-riittavasti-vastaajia"))))

(defn vertailuraportti-vertailujakso [vertailujakso_alkupvm vertailujakso_loppupvm]
  (let [alkupvm (parse-iso-date vertailujakso_alkupvm)
        loppupvm (or (parse-iso-date vertailujakso_loppupvm) (t/today))
        vertailupvm (t/minus loppupvm (t/years 1))]
    (if (and alkupvm (<= (.compareTo alkupvm vertailupvm) 0))
      {:vertailujakso_alkupvm vertailujakso_alkupvm
       :vertailujakso_loppupvm vertailujakso_loppupvm}
      {:vertailujakso_alkupvm (and alkupvm (.toString vertailupvm))
       :vertailujakso_loppupvm vertailujakso_loppupvm})))
