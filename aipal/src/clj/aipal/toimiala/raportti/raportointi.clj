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
            [clj-time.core :as time]
            [arvo.util :refer [parse-int]]
            [oph.common.util.http-util :refer [parse-iso-date]]
            [oph.common.util.util :refer [map-by]]
            [aipal.toimiala.raportti.util :refer [muuta-kaikki-stringeiksi]]
            [clojure.tools.logging :as log]))

(defn ^:private hae-monivalintavaihtoehdot [kysymysid]
  (->
    (sql/select* :monivalintavaihtoehto)
    (sql/fields :jarjestys :teksti_fi :teksti_sv :teksti_en)
    (sql/where {:kysymysid kysymysid})
    sql/exec))

(defn ^:private laske-osuus
  [lukumaara yhteensa]
  (if (> yhteensa 0)
    (/ (int lukumaara) (int yhteensa))
    0))

(defn prosentteina
  [osuus]
  (Math/round (double (* osuus 100))))

(defn muodosta-asteikko-jakauman-esitys
  [jakauma vaihtoehtoja & {:keys [alku] :or {alku 1}}]
  (let [vaihtoehdot (range alku (+ vaihtoehtoja alku))
        jakauma (merge (into {:eos 0} (for [x vaihtoehdot] {x 0})) jakauma)
        yhteensa (reduce + (vals jakauma))
        tiedot-vaihtoehdolle (fn [avain lukumaara]
                               {:vaihtoehto-avain avain
                                :lukumaara lukumaara
                                :osuus (prosentteina
                                         (laske-osuus (or (int lukumaara) 0) (int yhteensa)))})]
    (conj (vec (for [x vaihtoehdot] (tiedot-vaihtoehdolle (str x) (jakauma x))))
          (tiedot-vaihtoehdolle "eos" (jakauma :eos)))))

(defn muodosta-kylla-ei-jakauman-esitys
  [jakauma]
  (let [jakauma (merge {:kylla 0 :ei 0 :eos 0} jakauma)
        yhteensa (+ (or (:kylla jakauma) 0) (or (:ei jakauma) 0) (or (:eos jakauma) 0))]
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
          :let [lukumaara (or (get jakauma (:jarjestys vaihtoehto)) 0)
                osuus (laske-osuus (int lukumaara) (int yhteensa))]]
      {:vaihtoehto_fi (:teksti_fi vaihtoehto)
       :vaihtoehto_sv (:teksti_sv vaihtoehto)
       :vaihtoehto_en (:teksti_en vaihtoehto)
       :lukumaara lukumaara
       :osuus (prosentteina osuus)
       :jarjestys (:jarjestys vaihtoehto)})))

(defn suodata-eos-vastaukset [kysymys]
  (if (:eos_vastaus_sallittu kysymys)
    kysymys
    (update-in kysymys [:jakauma] butlast))) ;; EOS-vastaus on aina jakauman viimeinen

(def asteikkotyyppi? #{"likert_asteikko" "arvosana" "arvosana6" "arvosana7" "arvosana4_ja_eos" "asteikko5_1" "arvosana6_ja_eos" "nps"})

(defn muotoile-jakauma [jakauma]
  (when jakauma
    (zipmap (range) jakauma)))

(defn muotoile-kyllaei-jakauma [vastaukset]
  (when vastaukset
    (frequencies (keep keyword vastaukset))))

(defn kasittele-asteikkokysymys [kysymys vastaukset vaihtoehtoja & {:keys [alku] :or {alku 1}}]
  (let [jakauma (muotoile-jakauma (:jakauma vastaukset))]
    (assoc kysymys :jakauma (muodosta-asteikko-jakauman-esitys
                              (if (:eos_vastaus_sallittu kysymys)
                                (assoc jakauma :eos (or (:en_osaa_sanoa vastaukset) 0))
                                jakauma)
                              vaihtoehtoja
                              :alku alku))))

(defn jakaumat-summa [jakauma ehto]
  (->> jakauma
       (filter #(some? (parse-int (:vaihtoehto-avain %))))
       (filter #(ehto (parse-int (:vaihtoehto-avain %))))
       (map :lukumaara)
       (reduce +)))

(defn laske-nps-luku [jakauma]
  (when (not-empty jakauma)
    (let [yhteensä (int (jakaumat-summa jakauma #(some? %)))
          arvostelijat (int (jakaumat-summa jakauma #(< % 7)))
          suosittelijat (int (jakaumat-summa jakauma #(> % 8)))]
      (when (> yhteensä 0)
        (Math/round (float (* 100 (- (/ suosittelijat yhteensä) (/ arvostelijat yhteensä)))))))))

(defn kasittele-npskysymys [kysymys vastaukset]
  (let [npskysymys (kasittele-asteikkokysymys kysymys vastaukset 11 :alku 0)]
    (assoc npskysymys :nps_luku (laske-nps-luku (:jakauma npskysymys)))))

(defn kasittele-monivalintakysymys [kysymys vastaukset]
  (let [jakauma (muotoile-jakauma (:jakauma vastaukset))]
    (assoc kysymys :jakauma (muodosta-monivalinta-jakauman-esitys
                              (muodosta-monivalintavaihtoehdot kysymys)
                              (if (:eos_vastaus_sallittu kysymys)
                                (assoc jakauma :eos (or (:en_osaa_sanoa vastaukset) 0))
                                jakauma)))))

(defn kasittele-kyllaei-kysymys [kysymys vastaukset]
  (let [jakauma (muotoile-kyllaei-jakauma (:vaihtoehdot vastaukset))]
    (assoc kysymys :jakauma (muodosta-kylla-ei-jakauman-esitys (if (:eos_vastaus_sallittu kysymys)
                                                                 (assoc jakauma :eos (:en_osaa_sanoa vastaukset))
                                                                 jakauma)))))

(defn kasittele-vapaatekstikysymys [kysymys vastaukset]
  (let [vapaatekstit (:vapaatekstit vastaukset)]
    (assoc kysymys :vapaatekstivastaukset
           (when vapaatekstit
             (for [v vapaatekstit
                   :when v]
               {:teksti v})))))

(defn kasittele-kysymys [kysymys vastaukset]
  (let [vastaajat (set (when-let [vastaajat (:vastaajat vastaukset)]
                         vastaajat))
        vastaajia (count vastaajat)
        keskiarvo-ja-hajonta (when (asteikkotyyppi? (:vastaustyyppi kysymys))
                               (select-keys vastaukset [:keskiarvo :keskihajonta]))
        kysymys (case (:vastaustyyppi kysymys)
                  "arvosana" (kasittele-asteikkokysymys kysymys vastaukset 5)
                  "arvosana6" (kasittele-asteikkokysymys kysymys vastaukset 6)
                  "arvosana7" (kasittele-asteikkokysymys kysymys vastaukset 7)
                  "nps" (kasittele-npskysymys kysymys vastaukset)
                  "kylla_ei_valinta" (kasittele-kyllaei-kysymys kysymys vastaukset)
                  "likert_asteikko" (kasittele-asteikkokysymys kysymys vastaukset 5)
                  "monivalinta" (kasittele-monivalintakysymys kysymys vastaukset)
                  "vapaateksti" (kasittele-vapaatekstikysymys kysymys vastaukset)
                  "arvosana4_ja_eos" (kasittele-asteikkokysymys kysymys vastaukset 5)
                  "asteikko5_1" (kasittele-asteikkokysymys kysymys vastaukset 5)
                  "arvosana6_ja_eos" (kasittele-asteikkokysymys kysymys vastaukset 7 :alku 0)
                  "luku" (kasittele-asteikkokysymys kysymys vastaukset 5))
        nps-luku (when (= "nps" (:vastaustyyppi kysymys))
                   (laske-nps-luku (:jakauma kysymys)))]
    (-> kysymys
        (assoc :vastaajien_lukumaara vastaajia
               :vastaajat vastaajat
               :nps_luku nps-luku)
        (merge keskiarvo-ja-hajonta)
        suodata-eos-vastaukset)))

(defn valitse-kysymyksen-kentat
  [kysymys]
  (select-keys kysymys [:kysymysid
                        :kysymys_fi
                        :kysymys_sv
                        :kysymys_en
                        :jakauma
                        :vapaatekstivastaukset
                        :vastaajien_lukumaara
                        :keskiarvo
                        :keskihajonta
                        :nps_luku
                        :jatkovastaukset
                        :vastaustyyppi
                        :eos_vastaus_sallittu
                        :jarjestys]))

(defn laske-kysymysryhman-vastaajat [kysymysryhma]
  (let [vastaajia (count (:vastaajat kysymysryhma))]
    (-> kysymysryhma
      (assoc :vastaajien_lukumaara vastaajia)
      (dissoc :vastaajat))))

(defn muodosta-raportti [kysymysryhmat kysymykset vastaukset]
  (let [id->vastaukset (map-by :kysymysid vastaukset)
        kysymykset (for [kysymys (filter #(not= "valiotsikko" (:vastaustyyppi %)) kysymykset)
                         :let [kysymyksen-vastaukset (id->vastaukset (:kysymysid kysymys))]]
                     (kasittele-kysymys kysymys kysymyksen-vastaukset))
        kysymysryhmien-kysymykset (group-by :kysymysryhmaid kysymykset)]
    (for [kysymysryhma kysymysryhmat
          :let [kysymykset (sort-by :jarjestys (kysymysryhmien-kysymykset (:kysymysryhmaid kysymysryhma)))
                vastaajat (reduce clojure.set/union (map :vastaajat kysymykset))]]
      (assoc kysymysryhma :kysymykset (map valitse-kysymyksen-kentat kysymykset)
                          :vastaajat vastaajat))))

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
                                 [(get-in tekstit [:raportit :liian_vahan_vastaajia]) (:vastaajien_lukumaara raportti)]])
      :delimiter \;)))

(defonce sallitut-tyypit-ilman-vaadittuja-vastaajia ["tyoelamapalaute"])

(defn ei-riittavasti-vastaajia
  [raportti asetukset kysely-tyyppi]
  (let [nayta-kaikki (some #(= % kysely-tyyppi) sallitut-tyypit-ilman-vaadittuja-vastaajia)
         vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)]
    (if (or nayta-kaikki (>= (:vastaajien_lukumaara raportti) vaaditut-vastaajat))
      raportti
      (assoc (dissoc raportti :raportti) :virhe "ei-riittavasti-vastaajia"))))

(defn valtakunnallinen-raportti-vertailujakso [vertailujakso_alkupvm vertailujakso_loppupvm]
  (let [vertailupvm (->
                      (or (parse-iso-date vertailujakso_loppupvm) (time/today))
                      (time/minus (time/years 1))
                      (time/plus (time/days 1)))]
    {:vertailujakso_alkupvm (when-let [alkupvm (parse-iso-date vertailujakso_alkupvm)]
                              (if (<= (.compareTo alkupvm vertailupvm) 0)
                                vertailujakso_alkupvm
                                (.toString vertailupvm)))
     :vertailujakso_loppupvm vertailujakso_loppupvm}))
