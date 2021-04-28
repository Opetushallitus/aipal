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
            [oph.korma.common :refer [joda-date->sql-date]]
            [aipal.arkisto.tutkinto :as tutkinto-arkisto]
            [aipal.arkisto.opintoala :as opintoala-arkisto]
            [aipal.arkisto.koulutusala :as koulutusala-arkisto]
            [aipal.arkisto.koulutustoimija :as koulutustoimija-arkisto]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.rest-api.i18n :as i18n]
            [aipal.toimiala.raportti.taustakysymykset :refer :all]))

(defn ^:private hae-valtakunnalliset-kysymykset []
  (->> (sql/select taulut/kysymys
         (sql/join :inner :kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysymys.kysymysryhmaid))
         (sql/where {:kysymysryhma.valtakunnallinen true
                     :kysymys.vastaustyyppi [not= "luku"]})
         (sql/order :kysymysryhma.kysymysryhmaid :ASC)
         (sql/order :kysymys.jarjestys :ASC)
         (sql/fields :kysymys.jarjestys
                     :kysymys.luotuaika
                     :kysymys.kysymysid
                     :kysymys.kysymys_fi
                     :kysymys.kysymys_sv
                     :kysymys.kysymys_en
                     :kysymys.kysymysryhmaid
                     :kysymys.eos_vastaus_sallittu
                     :kysymys.vastaustyyppi))
    (map yhdista-taustakysymysten-kysymykset)
    (sort-by :jarjestys)))

(defn hae-valtakunnalliset-kysymysryhmat [taustakysymysryhmaid]
  (yhdista-valtakunnalliset-taustakysymysryhmat
    (sql/select :kysymysryhma
      (sql/join :inner [:kysymysryhma_taustakysymysryhma_view :kr_tkr]
                (and (= :kr_tkr.kysymysryhmaid :kysymysryhma.kysymysryhmaid)
                     (= :kr_tkr.taustakysymysryhmaid taustakysymysryhmaid)))
      (sql/where {:kysymysryhma.valtakunnallinen true})
      (sql/order :kysymysryhma.kysymysryhmaid :ASC)
      (sql/fields :kysymysryhmaid
                  :nimi_fi
                  :nimi_sv
                  :nimi_en))))

(defn generoi-joinit [query ehdot]
  (reduce (fn [query {:keys [id arvot]}]
            (sql/where query (sql/sqlfn :exists (sql/subselect [:vastaus :v1]
                                                               (sql/where {:v1.vastaajaid :vastaus.vastaajaid
                                                                           :v1.kysymysid [in [id]]
                                                                           :v1.numerovalinta [in arvot]})))))
          query
          ehdot))

(defn ->int [arvo]
  (if (integer? arvo) arvo (Integer/parseInt arvo)))

(defn konvertoi-ehdot [ehdot]
  (for [[kysymysid valinnat] ehdot
        :let [monivalinnat (:monivalinnat valinnat)
              arvot (keys (filter second monivalinnat))]
        :when (seq arvot)]
    {:id (->int kysymysid) :arvot (map ->int arvot)}))

(defn ^:private raportti-query [rajaukset taustakysymysryhmaid alkupvm loppupvm koulutustoimijat oppilaitokset koulutusalatunnus opintoalatunnus tutkintotunnus tutkintotyyppi suorituskieli]
  (->
    (sql/select* [:vastaus :vastaus])
   (sql/join :inner :vastaaja (= :vastaaja.vastaajaid :vastaus.vastaajaid))
    (cond->
      (or tutkintotunnus opintoalatunnus koulutusalatunnus tutkintotyyppi suorituskieli oppilaitokset) (sql/join :inner :vastaajatunnus
                                                                                                                 (and (= :vastaajatunnus.vastaajatunnusid :vastaaja.vastaajatunnusid)
                                                                                                                      (or (nil? tutkintotunnus) (= :vastaajatunnus.tutkintotunnus tutkintotunnus))))
      (or opintoalatunnus koulutusalatunnus tutkintotyyppi) (sql/join :inner :tutkinto
                                                                    (and (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus)
                                                                         (or (nil? opintoalatunnus) (= :tutkinto.opintoala opintoalatunnus))))
      koulutusalatunnus (sql/join :inner :opintoala {:opintoala.opintoalatunnus :tutkinto.opintoala
                                                     :opintoala.koulutusala koulutusalatunnus})
      tutkintotyyppi (sql/where {:tutkinto.tutkintotyyppi tutkintotyyppi})
      koulutustoimijat (->
                         (sql/join :inner :kyselykerta (= :kyselykerta.kyselykertaid :vastaaja.kyselykertaid))
                         (sql/join :inner :kysely (= :kysely.kyselyid :kyselykerta.kyselyid))
                         (sql/where {:kysely.koulutustoimija [in koulutustoimijat]}))
      oppilaitokset (sql/where {:vastaajatunnus.valmistavan_koulutuksen_oppilaitos [in oppilaitokset]})
      suorituskieli (sql/where {:vastaajatunnus.suorituskieli suorituskieli}))
    (generoi-joinit (konvertoi-ehdot rajaukset))
    (sql/where (or (nil? alkupvm) (>= :vastaaja.vastausaika alkupvm)))
    (sql/where (or (nil? loppupvm) (<= :vastaaja.vastausaika loppupvm)))
    (sql/fields :vastaus.kysymysid
                [(sql/sqlfn array_agg :vastaus.vastaajaid) :vastaajat]
                [(sql/sqlfn avg :vastaus.numerovalinta) :keskiarvo]
                [(sql/sqlfn stddev_samp :vastaus.numerovalinta) :keskihajonta]
                [(sql/sqlfn array_agg :vastaus.vaihtoehto) :vaihtoehdot]
                [(sql/sqlfn jakauma :vastaus.numerovalinta) :jakauma]
                [(sql/sqlfn count (sql/raw "case when vastaus.en_osaa_sanoa then 1 end")) :en_osaa_sanoa])
    (sql/group :vastaus.kysymysid)
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

(defn rajaa-vastaajatunnukset-tutkintotyypille [query tutkintotyyppi]
  (sql/join query :inner [:tutkinto :tt_tutkinto] {:tt_tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus
                                                   :tt_tutkinto.tutkintotyyppi tutkintotyyppi}))

(defn rajaa-kyselykerrat-koulutustoimijoihin [query koulutustoimijat]
  (-> query
    (sql/join :inner :kysely (= :kysely.kyselyid :kyselykerta.kyselyid))
    (sql/where {:kysely.koulutustoimija [in koulutustoimijat]})))

(defn rajaa-aikavalille
  [query [alkupvm-sarake loppupvm-sarake] [alkupvm loppupvm]]
  (-> query
    (sql/where (and
                 (or (nil? loppupvm) {alkupvm-sarake nil} (<= alkupvm-sarake loppupvm))
                 (or (nil? alkupvm) {loppupvm-sarake nil} (<= alkupvm loppupvm-sarake))))))

(defn rajaa-vastaajatunnukset-ajalle [query alkupvm loppupvm]
  (rajaa-aikavalille query [:vastaajatunnus.voimassa_alkupvm :vastaajatunnus.voimassa_loppupvm] [alkupvm loppupvm]))

(defn hae-vastaajien-maksimimaara-kysymysryhmalle
  [kysymysryhmaid alkupvm loppupvm koulutustoimijat koulutusalatunnus opintoalatunnus tutkintotunnus tutkintotyyppi suorituskieli]
  (->
    (sql/select* :vastaajatunnus)
    (sql/join :inner :kyselykerta (= :kyselykerta.kyselykertaid :vastaajatunnus.kyselykertaid))
    (sql/join :inner :kysely_kysymysryhma (= :kysely_kysymysryhma.kyselyid :kyselykerta.kyselyid))
    (sql/join :inner :kysymysryhma (= :kysymysryhma.kysymysryhmaid :kysely_kysymysryhma.kysymysryhmaid))
    (cond->
      tutkintotunnus (sql/where {:vastaajatunnus.tutkintotunnus tutkintotunnus})
      opintoalatunnus (rajaa-vastaajatunnukset-opintoalalle opintoalatunnus)
      koulutusalatunnus (rajaa-vastaajatunnukset-koulutusalalle koulutusalatunnus)
      koulutustoimijat (rajaa-kyselykerrat-koulutustoimijoihin koulutustoimijat)
      tutkintotyyppi (rajaa-vastaajatunnukset-tutkintotyypille tutkintotyyppi)
      suorituskieli (sql/where {:vastaajatunnus.suorituskieli suorituskieli}))
    (sql/where {:kysymysryhma.kysymysryhmaid [in [kysymysryhmaid]]})
    (rajaa-vastaajatunnukset-ajalle alkupvm loppupvm)
    (sql/aggregate (sum :vastaajatunnus.kohteiden_lkm) :vastaajia)
    sql/exec
    first
    :vastaajia))

(defn liita-vastaajien-maksimimaarat
  [kysymysryhmat alkupvm loppupvm koulutustoimijat koulutusalatunnus opintoalatunnus tutkintotunnus tutkintotyyppi suorituskieli]
  (for [kysymysryhma kysymysryhmat]
    (assoc kysymysryhma
           :vastaajien_maksimimaara
           (hae-vastaajien-maksimimaara-kysymysryhmalle
             (:kysymysryhmaid kysymysryhma)
             alkupvm loppupvm koulutustoimijat koulutusalatunnus opintoalatunnus tutkintotunnus tutkintotyyppi suorituskieli))))

(defn ^:private nimet [juttu]
  (select-keys juttu [:nimi_fi :nimi_sv :nimi_en]))

(defn ^:private tutkintorakenne-otsikko [parametrit]
  (case (:tutkintorakennetaso parametrit)
     "tutkinto" (when-let [tutkintotunnus (first (:tutkinnot parametrit))]
                  (nimet (tutkinto-arkisto/hae tutkintotunnus)))
     "opintoala" (when-let [opintoalatunnus (first (:opintoalat parametrit))]
                   (nimet (opintoala-arkisto/hae opintoalatunnus)))
     "koulutusala" (when-let [koulutusalatunnus (first (:koulutusalat parametrit))]
                     (nimet (koulutusala-arkisto/hae koulutusalatunnus)))))

(defn raportin-otsikko [parametrit]
  (let [tekstit-fi (i18n/hae-tekstit "fi")
        tekstit-sv (i18n/hae-tekstit "sv")
        tekstit-en (i18n/hae-tekstit "en")]
    (case (:tyyppi parametrit)
      "vertailu" (tutkintorakenne-otsikko parametrit)
      "kehitys" (merge {:nimi_fi (get-in tekstit-fi [:raportit :kaikki_tutkinnot])
                        :nimi_sv (get-in tekstit-sv [:raportit :kaikki_tutkinnot])
                        :nimi_en (get-in tekstit-en [:raportit :kaikki_tutkinnot])}
                       (tutkintorakenne-otsikko parametrit))
      "koulutustoimijat" (let [ytunnus (first (:koulutustoimijat parametrit))]
                           (nimet (koulutustoimija-arkisto/hae ytunnus)))
      "kysely" (tutkintorakenne-otsikko parametrit)
      "valtakunnallinen" {:nimi_fi (get-in tekstit-fi [:yleiset :valtakunnallinen])
                          :nimi_sv (get-in tekstit-sv [:yleiset :valtakunnallinen])
                          :nimi_en (get-in tekstit-en [:yleiset :valtakunnallinen])})))

(defn paivita-nakymat []
  (sql/exec-raw "REFRESH MATERIALIZED VIEW kysymysryhma_taustakysymysryhma_view;"))

(defn muodosta [parametrit]
  (let [alkupvm (joda-date->sql-date (parse-iso-date (:vertailujakso_alkupvm parametrit)))
        loppupvm (joda-date->sql-date (parse-iso-date (:vertailujakso_loppupvm parametrit)))
        rajaukset (:kysymykset parametrit)
        tutkintotunnus (when (= "tutkinto" (:tutkintorakennetaso parametrit)) (first (:tutkinnot parametrit)))
        opintoalatunnus (when (= "opintoala" (:tutkintorakennetaso parametrit)) (first (:opintoalat parametrit)))
        koulutusalatunnus (when (= "koulutusala" (:tutkintorakennetaso parametrit)) (first (:koulutusalat parametrit)))
        koulutustoimijat (not-empty (:koulutustoimijat parametrit))
        oppilaitokset (not-empty (:oppilaitokset parametrit))
        tutkintotyyppi (:tutkintotyyppi parametrit)
        suorituskieli (:suorituskieli parametrit)
        taustakysymysryhmaid (Integer/parseInt (:taustakysymysryhmaid parametrit))
        kysymysryhmat (liita-vastaajien-maksimimaarat
                        (hae-valtakunnalliset-kysymysryhmat taustakysymysryhmaid)
                        alkupvm loppupvm koulutustoimijat koulutusalatunnus opintoalatunnus tutkintotunnus tutkintotyyppi suorituskieli)
        kysymykset (hae-valtakunnalliset-kysymykset)
       ;; Suurin osa ajasta kuluu raportti-queryssa
        data (raportti-query rajaukset taustakysymysryhmaid alkupvm loppupvm koulutustoimijat oppilaitokset koulutusalatunnus opintoalatunnus tutkintotunnus tutkintotyyppi suorituskieli)
        raportti (raportointi/muodosta-raportti kysymysryhmat kysymykset data)]
   (merge
     (raportin-otsikko parametrit)
     {:luontipvm (time/now)
      :raportti (map raportointi/laske-kysymysryhman-vastaajat raportti)
      :parametrit parametrit
      :vastaajien_lukumaara (count (reduce clojure.set/union (map :vastaajat raportti)))
      :vastaajien_maksimimaara (hae-vastaajien-maksimimaara-kysymysryhmalle
                                 taustakysymysryhmaid
                                 alkupvm loppupvm koulutustoimijat koulutusalatunnus opintoalatunnus tutkintotunnus tutkintotyyppi suorituskieli)})))
