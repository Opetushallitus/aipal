;; Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.toimiala.raportti.yhdistaminen
  (:require [clojure.tools.logging :as log]))

(defn yhdistä-kentästä [kenttä datat]
  {kenttä (map kenttä datat)})

(defn yhdistä-kaikki-kentät [datat]
  (reduce (fn [result key] (merge result (yhdistä-kentästä key datat)))
          {}
          (keys (first datat))))

(defn yhdistä-vektorit [vektorit]
  (when (not-every? nil? vektorit)
    (if (not-any? empty? vektorit)
      (apply map vector
             (replace {nil (repeat nil)} vektorit))
;      Luodaan tyhjät parit omille kysymyksille joita ei ole valtakunnallisessa vertailuraportissa
      (map vector (first vektorit) (map (constantly {}) (first vektorit))))))


(defn yhdistä-samat [xs]
  (let [xs (remove nil? xs)]
    (if-not (or (empty? xs) (apply = xs)) (log/info "Mismatching values in yhdistä-samat:" xs)))
  (first xs))

(defn päivitä-polusta [[k & ks] päivitä rakenne]
  (let [päivitä-seuraavat (if ks
                            (partial päivitä-polusta ks päivitä)
                            päivitä)]
    (if (sequential? rakenne)
      (map päivitä-seuraavat rakenne)
      (update-in rakenne [k] päivitä-seuraavat))))

(defn päivitä-kentät [kentät päivitä rakenne]
  (reduce (fn [rakenne kenttä] (update-in rakenne [kenttä] päivitä))
          rakenne
          kentät))

(defn käsittele-vapaatekstivastaukset [kysymys]
  (if (not-every? nil? (:vapaatekstivastaukset kysymys))
    (->> kysymys
      (päivitä-polusta [:vapaatekstivastaukset] yhdistä-vektorit)
      (päivitä-polusta [:vapaatekstivastaukset :*] yhdistä-kaikki-kentät))
    (assoc kysymys :vapaatekstivastaukset nil)))

(defn nimet-yhteen-listaan [data]
  (let [zipped (map vector (:nimi_fi data) (:nimi_sv data) (:nimi_en data))
        nimet (for [z zipped] {:nimi_fi (first z)
                               :nimi_sv (second z)
                               :nimi_en (nth z 2)})]
    (->
      data
      (assoc :nimet nimet)
      (dissoc :nimi_fi :nimi_sv :nimi_en))))

(defn poista-vapaatekstikysymykset [kysymykset]
  (filter #(not= (:vastaustyyppi %) "vapaateksti") kysymykset))

(defn poista-vapaatekstit [r]
  (let [kysymysryhmat (get-in r [:raportti])]
    (assoc r :raportti (map #(update % :kysymykset poista-vapaatekstikysymykset) kysymysryhmat))))

(defn yhdista-raportit [raportit valtakunnallinen]
  (->> (if valtakunnallinen (map poista-vapaatekstit raportit) raportit)
    yhdistä-kaikki-kentät
    (päivitä-polusta [:raportti] yhdistä-vektorit)
    (päivitä-polusta [:raportti :*] yhdistä-kaikki-kentät)
    (päivitä-polusta [:raportti :* :kysymykset] yhdistä-vektorit)
    (päivitä-polusta [:raportti :* :kysymykset :*] yhdistä-kaikki-kentät)
    (päivitä-polusta [:raportti :* :kysymykset :* :jakauma] yhdistä-vektorit)
    (päivitä-polusta [:raportti :* :kysymykset :* :jakauma :*] yhdistä-kaikki-kentät)
    (päivitä-polusta [:raportti :* :kysymykset :*] käsittele-vapaatekstivastaukset)
    (päivitä-polusta [:raportti :* :kysymykset :* :jakauma :*] (partial päivitä-kentät [:jarjestys :vaihtoehto_fi :vaihtoehto_sv :vaihtoehto_en :vaihtoehto-avain] yhdistä-samat))
    (päivitä-polusta [:raportti :* :kysymykset :*] (partial päivitä-kentät [:jarjestys :eos_vastaus_sallittu :kysymysid :kysymys_fi :kysymys_sv :kysymys_en :vastaustyyppi] yhdistä-samat))
    (päivitä-polusta [:raportti :*] (partial päivitä-kentät [:kysymysryhmaid :nimi_fi :nimi_sv :nimi_en] yhdistä-samat))
    nimet-yhteen-listaan
    (päivitä-kentät [:luontipvm] first)))

