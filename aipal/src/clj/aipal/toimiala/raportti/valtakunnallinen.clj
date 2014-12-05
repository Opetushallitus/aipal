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

(ns aipal.toimiala.raportti.valtakunnallinen
  (:require [korma.core :as sql]
            [aipal.toimiala.raportti.raportointi :as raportointi]
            [clj-time.core :as time]
            [oph.common.util.http-util :refer [parse-iso-date]]
            [oph.korma.korma :refer [joda-date->sql-date]]))

(defn ^:private hae-valtakunnalliset-kysymykset []
  (sql/select :kysymys
    (sql/join :inner :kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysymys.kysymysryhmaid))
    (sql/where {:kysymysryhma.valtakunnallinen true})
    (sql/order :kysymysryhma.kysymysryhmaid :ASC)
    (sql/order :kysymys.jarjestys :ASC)
    (sql/fields :kysymys.kysymysid
                :kysymys.kysymys_fi
                :kysymys.kysymys_sv
                :kysymys.kysymysryhmaid
                :kysymys.vastaustyyppi)))

(defn hae-valtakunnalliset-kysymysryhmat []
  (sql/select
    :kysymysryhma
    (sql/where {:kysymysryhma.valtakunnallinen true})
    (sql/order :kysymysryhma.kysymysryhmaid :ASC)
    (sql/fields :kysymysryhmaid
                :nimi_fi
                :nimi_sv)))

(defn generoi-joinit [query ehdot]
  (reduce (fn [query {:keys [id arvot]}]
            (sql/where query (sql/sqlfn :exists (sql/subselect [:vastaus :v1]
                                                               (sql/where {:v1.vastaajaid :vastaus.vastaajaid
                                                                           :v1.kysymysid id
                                                                           :v1.numerovalinta [in arvot]})))))
          query
          ehdot))

(defn ->int [arvo]
  (if (integer? arvo) arvo (Integer/parseInt arvo)))

(defn konvertoi-ehdot [ehdot]
  (for [[kysymysid valinnat] ehdot
        :let [monivalinnat (:monivalinnat valinnat)
              arvot (keys (filter #(second %) monivalinnat))]
        :when (seq arvot)]
    {:id (->int kysymysid) :arvot (map #(->int %) arvot)}))

(defn ^:private hae-vastaukset [rajaukset alkupvm loppupvm]
  (->
    (sql/select* :vastaus)
    (sql/join :inner :kysymys (= :vastaus.kysymysid :kysymys.kysymysid))
    (sql/join :inner :kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysymys.kysymysryhmaid))
    (generoi-joinit (konvertoi-ehdot rajaukset))
    (sql/where {:kysymysryhma.valtakunnallinen true})
    (sql/where (or (nil? alkupvm) (>= :vastaus.vastausaika alkupvm)))
    (sql/where (or (nil? loppupvm) (<= :vastaus.vastausaika loppupvm)))
    (sql/fields :vastaus.vastaajaid
                :vastaus.kysymysid
                :vastaus.numerovalinta
                :vastaus.vaihtoehto
                :vastaus.vapaateksti)
    sql/exec))

(defn muodosta [parametrit]
  (let [alkupvm (joda-date->sql-date (parse-iso-date (:vertailujakso_alkupvm parametrit)))
        loppupvm (joda-date->sql-date (parse-iso-date (:vertailujakso_loppupvm parametrit)))
        rajaukset (:kysymykset parametrit)
        kysymysryhmat (hae-valtakunnalliset-kysymysryhmat)
        kysymykset (hae-valtakunnalliset-kysymykset)
        vastaukset (hae-vastaukset rajaukset alkupvm loppupvm)]
    {:luontipvm (time/today)
     :raportti  (raportointi/muodosta-raportti-vastauksista kysymysryhmat kysymykset vastaukset)
     :vastaajien-lkm (count (group-by :vastaajaid vastaukset))}))
