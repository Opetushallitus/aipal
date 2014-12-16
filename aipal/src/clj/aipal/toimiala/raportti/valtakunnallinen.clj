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
            [oph.korma.korma :refer [joda-date->sql-date]]
            [aipal.arkisto.tutkinto :as tutkinto-arkisto]
            [aipal.arkisto.opintoala :as opintoala-arkisto]
            [aipal.arkisto.koulutusala :as koulutusala-arkisto]
            [aipal.arkisto.koulutustoimija :as koulutustoimija-arkisto]))

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

(defn ^:private hae-vastaukset [rajaukset alkupvm loppupvm koulutustoimijat koulutusalatunnus opintoalatunnus tutkintotunnus]
  (->
    (sql/select* :vastaus)
    (sql/join :inner :kysymys (= :vastaus.kysymysid :kysymys.kysymysid))
    (sql/join :inner :kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysymys.kysymysryhmaid))
    (cond->
      (or tutkintotunnus opintoalatunnus koulutusalatunnus koulutustoimijat) (sql/join :inner :vastaaja (= :vastaaja.vastaajaid :vastaus.vastaajaid))
      (or tutkintotunnus opintoalatunnus koulutusalatunnus) (sql/join :inner :vastaajatunnus (and
                                                        (= :vastaajatunnus.vastaajatunnusid :vastaaja.vastaajatunnusid)
                                                        (or (nil? tutkintotunnus) (= :vastaajatunnus.tutkintotunnus tutkintotunnus))))
      (or opintoalatunnus koulutusalatunnus) (sql/join :inner :tutkinto (and
                                                   (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus)
                                                   (or (nil? opintoalatunnus) (= :tutkinto.opintoala opintoalatunnus))))
      koulutusalatunnus (sql/join :inner :opintoala {:opintoala.opintoalatunnus :tutkinto.opintoala
                                                     :opintoala.koulutusala koulutusalatunnus})
      koulutustoimijat (->
                         (sql/join :inner :kyselykerta (= :kyselykerta.kyselykertaid :vastaaja.kyselykertaid))
                         (sql/join :inner :kysely_organisaatio_view (= :kysely_organisaatio_view.kyselyid :kyselykerta.kyselyid))
                         (sql/where {:kysely_organisaatio_view.koulutustoimija [in koulutustoimijat]})))
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

(defn rajaa-vastaajatunnukset-opintoalalle [query opintoalatunnus]
  (-> query
    (sql/join :inner :tutkinto (and
                                 (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus)
                                 (= :tutkinto.opintoala opintoalatunnus)))))

(defn rajaa-vastaajatunnukset-koulutusalalle [query koulutusalatunnus]
  (-> query
    (sql/join :inner :tutkinto (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus))
    (sql/join :inner :opintoala {:opintoala.opintoalatunnus :tutkinto.opintoala
                                 :opintoala.koulutusala koulutusalatunnus})))

(defn rajaa-kyselykerrat-koulutustoimijoihin [query koulutustoimijat]
  (-> query
    (sql/join :inner :kysely_organisaatio_view (= :kysely_organisaatio_view.kyselyid :kyselykerta.kyselyid))
    (sql/where {:kysely_organisaatio_view.koulutustoimija [in koulutustoimijat]})))

(defn hae-vastaajien-maksimimaara [koulutustoimijat koulutusalatunnus opintoalatunnus tutkintotunnus]
  (->
    (sql/select* :vastaajatunnus)
    (sql/join :inner :kyselykerta (= :kyselykerta.kyselykertaid :vastaajatunnus.kyselykertaid))
    (sql/join :inner :kysely_kysymysryhma (= :kysely_kysymysryhma.kyselyid :kyselykerta.kyselyid))
    (sql/join :inner :kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysely_kysymysryhma.kysymysryhmaid))
    (cond->
      tutkintotunnus (sql/where {:vastaajatunnus.tutkintotunnus tutkintotunnus})
      opintoalatunnus (rajaa-vastaajatunnukset-opintoalalle opintoalatunnus)
      koulutusalatunnus (rajaa-vastaajatunnukset-koulutusalalle koulutusalatunnus)
      koulutustoimijat (rajaa-kyselykerrat-koulutustoimijoihin koulutustoimijat))
    (sql/where {:kysymysryhma.valtakunnallinen true})
    (sql/fields :vastaajatunnus.vastaajatunnusid :vastaajatunnus.vastaajien_lkm)
    (sql/group :vastaajatunnus.vastaajatunnusid :vastaajatunnus.vastaajien_lkm)
    sql/exec
    (->>
      (map :vastaajien_lkm)
      (reduce +))))

(defn ^:private nimet [juttu]
  (select-keys juttu [:nimi_fi :nimi_sv]))

(defn ^:private vertailutyyppi-otsikko [parametrit]
  (case (:vertailutyyppi parametrit)
    "tutkinto" (let [tutkintotunnus (first (:tutkinnot parametrit))]
                 (nimet (tutkinto-arkisto/hae tutkintotunnus)))
    "opintoala" (let [opintoalatunnus (first (:opintoalat parametrit))]
                  (nimet (opintoala-arkisto/hae opintoalatunnus)))
    "koulutusala" (let [koulutusalatunnus (first (:koulutusalat parametrit))]
                    (nimet (koulutusala-arkisto/hae koulutusalatunnus)))))

(defn ^:private raportin-otsikko [parametrit]
  (case (:tyyppi parametrit)
    "vertailu" (vertailutyyppi-otsikko parametrit)
    "kehitys" (vertailutyyppi-otsikko parametrit)
    "koulutustoimijat" (let [ytunnus (first (:koulutustoimijat parametrit))]
                         (nimet (koulutustoimija-arkisto/hae ytunnus)))))

(defn muodosta [parametrit]
  (let [alkupvm (joda-date->sql-date (parse-iso-date (:vertailujakso_alkupvm parametrit)))
        loppupvm (joda-date->sql-date (parse-iso-date (:vertailujakso_loppupvm parametrit)))
        rajaukset (:kysymykset parametrit)
        tutkintotunnus (when (= "tutkinto" (:vertailutyyppi parametrit)) (first (:tutkinnot parametrit)))
        opintoalatunnus (when (= "opintoala" (:vertailutyyppi parametrit)) (first (:opintoalat parametrit)))
        koulutusalatunnus (when (= "koulutusala" (:vertailutyyppi parametrit)) (first (:koulutusalat parametrit)))
        koulutustoimijat (not-empty (:koulutustoimijat parametrit))
        kysymysryhmat (hae-valtakunnalliset-kysymysryhmat)
        kysymykset (hae-valtakunnalliset-kysymykset)
        vastaukset (hae-vastaukset rajaukset alkupvm loppupvm koulutustoimijat koulutusalatunnus opintoalatunnus tutkintotunnus)]
    (merge
      (raportin-otsikko parametrit)
      {:luontipvm (time/today)
       :raportti  (raportointi/muodosta-raportti-vastauksista kysymysryhmat kysymykset vastaukset)
       :vastaajien-lkm (count (group-by :vastaajaid vastaukset))
       :vastaajien_maksimimaara (hae-vastaajien-maksimimaara koulutustoimijat koulutusalatunnus opintoalatunnus tutkintotunnus)})))
