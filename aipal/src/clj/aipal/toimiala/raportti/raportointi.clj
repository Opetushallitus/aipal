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
            [oph.common.util.http-util :refer [parse-iso-date]]
            [oph.common.util.util :refer [map-by]]))

(defn ^:private hae-monivalintavaihtoehdot [kysymysid]
  (->
    (sql/select* :monivalintavaihtoehto)
    (sql/fields :jarjestys :teksti_fi :teksti_sv)
    (sql/where {:kysymysid kysymysid})
    sql/exec))

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
  (let [jakauma (merge {1 0, 2 0, 3 0, 4 0, 5 0, :eos 0} jakauma)
        yhteensa (reduce + (vals jakauma))
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
  (let [jakauma (merge {:kylla 0, :ei 0, :eos 0} jakauma)
        yhteensa (+ (:kylla jakauma) (:ei jakauma) (:eos jakauma))]
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

(defn suodata-eos-vastaukset [kysymys]
  (if (:eos_vastaus_sallittu kysymys)
    kysymys
    (update-in kysymys [:jakauma] butlast))) ;; EOS-vastaus on aina jakauman viimeinen

(def asteikkotyyppi? #{"asteikko" "likert_asteikko" "arvosana"})

(defn jakauman-keskiarvo-ja-keskihajonta [jakauma]
  (let [maara (reduce + 0 (vals jakauma))
        keskiarvo (/ (reduce (fn [summa [arvo maara]]
                               (+ summa (* arvo maara)))
                             0
                             jakauma)
                     maara)
        hajonnat (reduce (fn [summa [arvo maara]]
                           (+ summa
                              (* maara
                                 (Math/pow (- arvo keskiarvo) 2))))
                         0
                         jakauma)
        jakaja (dec maara)]
    {:keskiarvo keskiarvo
     :keskihajonta (if (pos? jakaja)
                     (Math/sqrt (/ hajonnat jakaja))
                     0)}))

(defn kasittele-eos [kysymys]
  (if (:eos_vastaus_sallittu kysymys)
    (assoc-in kysymys [:vastaukset :eos] (:eos kysymys))
    kysymys))

(defn kasittele-asteikkokysymys [kysymys]
  (let [kysymys (kasittele-eos kysymys)]
    (assoc kysymys :jakauma (muodosta-asteikko-jakauman-esitys (:vastaukset kysymys)))))

(defn kasittele-monivalintakysymys [kysymys]
  (let [kysymys (kasittele-eos kysymys)]
    (assoc kysymys :jakauma (muodosta-monivalinta-jakauman-esitys
                              (muodosta-monivalintavaihtoehdot kysymys)
                              (:vastaukset kysymys)))))

(defn liita-kylla-jatkovastaukset
  [kysymys]
  (when (:kylla_kysymys kysymys)
    {:kysymys_fi (:kylla_teksti_fi kysymys)
     :kysymys_sv (:kylla_teksti_sv kysymys)
     :jakauma (butlast (muodosta-asteikko-jakauman-esitys (get-in kysymys [:jatkovastaukset :kylla]))) ;; EOS-vastaus on jakauman viimeinen eikä sitä käytetä jatkovastauksissa
     :vastaustyyppi (:kylla_vastaustyyppi kysymys)}))

(defn liita-ei-jatkovastaukset
  [kysymys]
  (when (:ei_kysymys kysymys)
    (let [ei-vastaukset (get-in kysymys [:jatkovastaukset :ei])]
      {:kysymys_fi (:ei_teksti_fi kysymys)
       :kysymys_sv (:ei_teksti_sv kysymys)
       :vapaatekstivastaukset (for [v ei-vastaukset] {:teksti v})
       :vastaustyyppi "vapaateksti"})))

(defn liita-jatkovastaukset
  [kysymys]
  (when (:jatkokysymysid kysymys)
    {:kylla (liita-kylla-jatkovastaukset kysymys)
     :ei (liita-ei-jatkovastaukset kysymys)}))

(defn kasittele-kyllaei-kysymys [kysymys]
  (let [kysymys (kasittele-eos kysymys)]
    (assoc kysymys :jakauma (muodosta-kylla-ei-jakauman-esitys (:vastaukset kysymys))
                   :jatkovastaukset (liita-jatkovastaukset kysymys))))

(defn kasittele-vapaatekstikysymys [kysymys]
  (assoc kysymys :vapaatekstivastaukset
         (for [v (:vastaukset kysymys)] {:teksti v})))

(defn kasittele-kysymys [kysymys]
  (let [vastaajia (count (:vastaajat kysymys))
        keskiarvo-ja-hajonta (when (asteikkotyyppi? (:vastaustyyppi kysymys))
                               (jakauman-keskiarvo-ja-keskihajonta (:vastaukset kysymys)))
        kysymys (case (:vastaustyyppi kysymys)
                  "arvosana" (kasittele-asteikkokysymys kysymys)
                  "asteikko" (kasittele-asteikkokysymys kysymys)
                  "kylla_ei_valinta" (kasittele-kyllaei-kysymys kysymys)
                  "likert_asteikko" (kasittele-asteikkokysymys kysymys)
                  "monivalinta" (kasittele-monivalintakysymys kysymys)
                  "vapaateksti" (kasittele-vapaatekstikysymys kysymys))]
    (-> kysymys
      (update-in [:jarjestys] str)
      (assoc :vastaajien_lukumaara vastaajia)
      (merge keskiarvo-ja-hajonta)
      suodata-eos-vastaukset)))

(defn kasittele-asteikkovastaus [tulokset kysymys vastaus]
  (if-let [numerovalinta (:numerovalinta vastaus)]
    (update-in tulokset [kysymys :vastaukset numerovalinta] (fnil inc 0))
    (update-in tulokset [kysymys :eos] (fnil inc 0))))

(defn kasittele-kyllaei-vastaus [tulokset kysymys vastaus]
  (let [vaihtoehto (keyword (:vaihtoehto vastaus))
        kylla-jatko (:kylla_asteikko vastaus)
        ei-jatko (:ei_vastausteksti vastaus)]
    (cond-> tulokset
      vaihtoehto (update-in [kysymys :vastaukset vaihtoehto] (fnil inc 0))
      (not vaihtoehto) (update-in [kysymys :eos] (fnil inc 0))
      kylla-jatko (update-in [kysymys :jatkovastaukset :kylla kylla-jatko] (fnil inc 0))
      ei-jatko (update-in [kysymys :jatkovastaukset :ei] conj ei-jatko))))

(defn kasittele-vapaatekstivastaus [tulokset kysymys vastaus]
  (update-in tulokset [kysymys :vastaukset] conj (:vapaateksti vastaus)))

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

(defn muodosta-raportti [kysymysryhmat kysymykset vastaukset]
  (let [id->kysymys (map-by :kysymysid kysymykset)
        kasittele-vastaus (fn [tulokset vastaus]
                            (let [kysymys (id->kysymys (:kysymysid vastaus))
                                  tulokset (case (:vastaustyyppi kysymys)
                                             "arvosana" (kasittele-asteikkovastaus tulokset kysymys vastaus)
                                             "asteikko" (kasittele-asteikkovastaus tulokset kysymys vastaus)
                                             "kylla_ei_valinta" (kasittele-kyllaei-vastaus tulokset kysymys vastaus)
                                             "likert_asteikko" (kasittele-asteikkovastaus tulokset kysymys vastaus)
                                             "monivalinta" (kasittele-asteikkovastaus tulokset kysymys vastaus)
                                             "vapaateksti" (kasittele-vapaatekstivastaus tulokset kysymys vastaus))]
                              (update-in tulokset [kysymys :vastaajat] (fnil conj #{}) (:vastaajaid vastaus))))
        tulokset (for [[kysymys tulos] (reduce kasittele-vastaus {} vastaukset)]
                   (merge kysymys tulos))
        kysymysryhmien-kysymykset (->> tulokset
                                    (map kasittele-kysymys)
                                    (sort-by :jarjestys)
                                    (group-by :kysymysryhmaid))]
    (for [kysymysryhma kysymysryhmat
          :let [kysymykset (kysymysryhmien-kysymykset (:kysymysryhmaid kysymysryhma))
                vastaajia (count (reduce clojure.set/union #{} (map :vastaajat kysymykset)))]]
      (assoc kysymysryhma :vastaajien_lukumaara vastaajia
                          :kysymykset (map valitse-kysymyksen-kentat kysymykset)))))

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
                                 [(get-in tekstit [:raportit :liian_vahan_vastaajia]) (:vastaajien_lukumaara raportti)]])
      :delimiter \;)))

(defn ei-riittavasti-vastaajia
  [raportti asetukset]
  (let [vaaditut-vastaajat (:raportointi-minimivastaajat asetukset)]
    (if (>= (:vastaajien_lukumaara raportti) vaaditut-vastaajat)
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
