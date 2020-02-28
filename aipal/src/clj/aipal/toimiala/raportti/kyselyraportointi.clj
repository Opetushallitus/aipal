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

(ns aipal.toimiala.raportti.kyselyraportointi
  (:require [clj-time.core :as time]
            [korma.core :as sql]
            [aipal.toimiala.raportti.taustakysymykset :refer :all]
            [aipal.toimiala.raportti.raportointi :as raportointi]
            [aipal.integraatio.sql.korma :as taulut]
            [oph.common.util.util :refer [paivita-arvot poista-tyhjat]]
            [oph.korma.common :refer [joda-date->sql-date]]
            [oph.common.util.http-util :refer [parse-iso-date]]))

(defn yhdista-ja-jarjesta-tutkinnot
  [tutkinnot]
  (->>
    (let [tutkinnot (group-by #(select-keys % [:tutkintotunnus :nimi_fi :nimi_sv :nimi_en]) tutkinnot)]
      (for [[tutkinto lukumaarat] tutkinnot]
        (assoc tutkinto :vastaajien_lukumaara (reduce + 0 (map :kohteiden_lkm lukumaarat)))))
    (sort-by :tutkintotunnus)))

(defn koulutustoimijat-hierarkiaksi
  [vastaajatunnus-tiedot parametrit]
  (let [koulutustoimijat (group-by #(select-keys % [:ytunnus :koulutustoimija_fi :koulutustoimija_sv :koulutustoimija_en]) vastaajatunnus-tiedot)]
    (for [[koulutustoimija tutkinnot] koulutustoimijat]
      (-> (select-keys parametrit [:koulutustoimija_fi :koulutustoimija_sv :koulutustoimija_en])
          (assoc :tutkinnot (yhdista-ja-jarjesta-tutkinnot tutkinnot)
                 :vastaajien_lukumaara (reduce + 0 (map :kohteiden_lkm tutkinnot)))))))

(defn rajaa-vastaajatunnukset-tutkintoihin
  [query tutkinnot]
  (if (= tutkinnot [nil])
    (sql/where query {:vastaajatunnus.tutkintotunnus nil})
    (sql/where query {:vastaajatunnus.tutkintotunnus [in tutkinnot]})))

(defn yhteiset-rajaukset
  [query {:keys [tutkinnot jarjestavat_oppilaitokset kyselyid kyselykertaid suorituskieli]}]
  (cond-> query
    tutkinnot (sql/where {:vastaajatunnus.tutkintotunnus [in tutkinnot]})
    jarjestavat_oppilaitokset (sql/where {:vastaajatunnus.valmistavan_koulutuksen_oppilaitos [in jarjestavat_oppilaitokset]})
    kyselyid (sql/where {:kyselykerta.kyselyid kyselyid})
    ; vastaajatunnus.kyselykertaid tai kyselykerta.kyselykertaid
    kyselykertaid (sql/where {:kyselykertaid kyselykertaid})
    suorituskieli (sql/where {:vastaajatunnus.suorituskieli suorituskieli})))

(defn hae-vastaajatunnusten-tiedot-koulutustoimijoittain
  [{:keys [tutkinnot kyselyid vertailujakso_alkupvm vertailujakso_loppupvm] :as parametrit}]
  (->
    (sql/select* :vastaajatunnus)
    (cond->
      tutkinnot (rajaa-vastaajatunnukset-tutkintoihin tutkinnot)
      kyselyid (sql/join :inner :kyselykerta (= :kyselykerta.kyselykertaid :vastaajatunnus.kyselykertaid))
      vertailujakso_alkupvm (sql/where (or (= :vastaajatunnus.voimassa_loppupvm nil)
                                           (>= :vastaajatunnus.voimassa_loppupvm vertailujakso_alkupvm)))
      vertailujakso_loppupvm (sql/where (or (= :vastaajatunnus.voimassa_alkupvm nil)
                                            (<= :vastaajatunnus.voimassa_alkupvm vertailujakso_loppupvm))))
    (yhteiset-rajaukset parametrit)
    (sql/join :left :tutkinto
              (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus))
    (sql/fields :tutkinto.tutkintotunnus :tutkinto.nimi_fi :tutkinto.nimi_sv :tutkinto.nimi_en
                [(sql/subselect :vastaaja
                                (sql/aggregate (count :*) :kohteiden_lkm)
                                (sql/where (or (nil? vertailujakso_alkupvm)
                                               (>= :vastaaja.luotuaika vertailujakso_alkupvm)))
                                (sql/where (or (nil? vertailujakso_loppupvm)
                                               (<= :vastaaja.luotuaika vertailujakso_loppupvm)))
                                (sql/where {:vastaaja.vastaajatunnusid :vastaajatunnus.vastaajatunnusid})) :kohteiden_lkm])
    sql/exec
    (koulutustoimijat-hierarkiaksi parametrit)))

(defn laske-vastaajat-yhteensa
  [koulutustoimijat]
  (reduce + 0 (map :vastaajien_lukumaara koulutustoimijat)))

(defn hae-vastaajien-maksimimaara [{:keys [tutkinnot vertailujakso_alkupvm vertailujakso_loppupvm] :as parametrit}]
  (->
    (sql/select* :kyselykerta)
    (sql/join :inner :vastaajatunnus
              (= :kyselykerta.kyselykertaid :vastaajatunnus.kyselykertaid))
    (sql/aggregate (sum :vastaajatunnus.kohteiden_lkm) :vastaajien_maksimimaara)
    (cond->
      tutkinnot (rajaa-vastaajatunnukset-tutkintoihin tutkinnot)
      vertailujakso_alkupvm (sql/where (or (= :vastaajatunnus.voimassa_loppupvm nil)
                                           (>= :vastaajatunnus.voimassa_loppupvm vertailujakso_alkupvm)))
      vertailujakso_loppupvm (sql/where (or (= :vastaajatunnus.voimassa_alkupvm nil)
                                            (<= :vastaajatunnus.voimassa_alkupvm vertailujakso_loppupvm))))
    (yhteiset-rajaukset parametrit)
    sql/exec
    first
    :vastaajien_maksimimaara))

(defn liita-vastaajien-maksimimaarat
  [kysymysryhmat parametrit]
  (let [vastaajien-maksimimaara (hae-vastaajien-maksimimaara parametrit)]
    (for [kysymysryhma kysymysryhmat]
      (assoc kysymysryhma :vastaajien_maksimimaara vastaajien-maksimimaara))))

(defn ^:private hae-kysymykset [{:keys [kyselykertaid kyselyid]}]
  (->
    (sql/select* taulut/kysely)
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
    (cond->
      kyselykertaid (->
                      (sql/join :inner :kyselykerta
                                (= :kyselykerta.kyselyid :kysely.kyselyid))
                      (sql/where {:kyselykerta.kyselykertaid kyselykertaid}))
      kyselyid (sql/where {:kysely.kyselyid kyselyid}))
    (sql/order :kysely_kysymysryhma.jarjestys :ASC)
    (sql/order :kysymys.jarjestys :ASC)
    (sql/fields :kysymys.kysymysryhmaid
                :kysymys.luotuaika
                :kysymys.kysymysid
                :kysymys.kysymys_fi
                :kysymys.kysymys_sv
                :kysymys.kysymys_en
                :kysymys.vastaustyyppi
                :kysymys.eos_vastaus_sallittu
                :kysymys.jarjestys
                :jatkokysymys.jatkokysymysid
                :jatkokysymys.kylla_kysymys
                :jatkokysymys.kylla_teksti_fi
                :jatkokysymys.kylla_teksti_sv
                :jatkokysymys.kylla_teksti_en
                :jatkokysymys.kylla_vastaustyyppi
                :jatkokysymys.ei_kysymys
                :jatkokysymys.ei_teksti_fi
                :jatkokysymys.ei_teksti_sv
                :jatkokysymys.ei_teksti_en)
    sql/exec
    (->>
      (map yhdista-taustakysymysten-kysymykset)
      (sort-by :jarjestys))))

(defn hae-kysymysryhmat [{:keys [kyselykertaid kyselyid]}]
  (->
    (sql/select* :kysely)
    (sql/join :inner :kysely_kysymysryhma
              (= :kysely.kyselyid
                 :kysely_kysymysryhma.kyselyid))
    (sql/join :inner :kysymysryhma
              (= :kysely_kysymysryhma.kysymysryhmaid
                 :kysymysryhma.kysymysryhmaid))
    (cond->
      kyselykertaid (->
                      (sql/join :inner :kyselykerta
                                (= :kyselykerta.kyselyid :kysely.kyselyid))
                      (sql/where {:kyselykerta.kyselykertaid kyselykertaid}))
      kyselyid (sql/where {:kyselyid kyselyid}))
    (sql/order :kysely_kysymysryhma.jarjestys :ASC)
    (sql/fields :kysymysryhma.kysymysryhmaid
                :kysymysryhma.nimi_fi
                :kysymysryhma.nimi_sv
                :kysymysryhma.nimi_en)
    sql/exec
    yhdista-valtakunnalliset-taustakysymysryhmat))

(defn ^:private hae-vastaukset [{:keys [tutkinnot suorituskieli
                                        jarjestavat_oppilaitokset vertailujakso_alkupvm vertailujakso_loppupvm]
                                 :as parametrit}]
  (->
    (sql/select* :kyselykerta)
    (sql/join :inner :vastaaja
              (= :kyselykerta.kyselykertaid
                 :vastaaja.kyselykertaid))
    (sql/join :inner :vastaus
              (= :vastaaja.vastaajaid
                 :vastaus.vastaajaid))
    (sql/join :left :jatkovastaus
              (= :jatkovastaus.jatkovastausid
                 :vastaus.jatkovastausid))
    (cond->
      (or tutkinnot
          suorituskieli
          jarjestavat_oppilaitokset) (sql/join :inner :vastaajatunnus
                                               (= :vastaajatunnus.vastaajatunnusid :vastaaja.vastaajatunnusid))
      tutkinnot (rajaa-vastaajatunnukset-tutkintoihin tutkinnot)
      vertailujakso_alkupvm (sql/where (>= :vastaus.vastausaika vertailujakso_alkupvm))
      vertailujakso_loppupvm (sql/where (<= :vastaus.vastausaika vertailujakso_loppupvm)))
    (yhteiset-rajaukset parametrit)
    (sql/fields :vastaus.kysymysid
                [(sql/sqlfn array_agg :vastaus.vastaajaid) :vastaajat]
                [(sql/sqlfn avg :vastaus.numerovalinta) :keskiarvo]
                [(sql/sqlfn stddev_samp :vastaus.numerovalinta) :keskihajonta]
                [(sql/sqlfn array_agg :vastaus.vaihtoehto) :vaihtoehdot]
                [(sql/sqlfn jakauma :vastaus.numerovalinta) :jakauma]
                [(sql/sqlfn array_agg :vastaus.vapaateksti) :vapaatekstit]
                [(sql/sqlfn count (sql/raw "case when vastaus.en_osaa_sanoa then 1 end")) :en_osaa_sanoa]
                [(sql/sqlfn avg :jatkovastaus.kylla_asteikko) :jatkovastaus_keskiarvo]
                [(sql/sqlfn stddev_samp :jatkovastaus.kylla_asteikko) :keskihajonta]
                [(sql/sqlfn jakauma :jatkovastaus.kylla_asteikko) :jatkovastaus_jakauma]
                [(sql/sqlfn array_agg :jatkovastaus.ei_vastausteksti) :jatkovastaus_vapaatekstit])
    (sql/group :vastaus.kysymysid)
    sql/exec))

(defn muodosta-raportti [parametrit]
  {:pre [(or (:kyselyid parametrit)
             (:kyselykertaid parametrit))]}
  (raportointi/muodosta-raportti (liita-vastaajien-maksimimaarat
                                   (hae-kysymysryhmat parametrit) parametrit)
                                 (hae-kysymykset parametrit)
                                 (hae-vastaukset parametrit)))

(defn paivita-parametrit [parametrit]
  (-> parametrit
    poista-tyhjat
    (cond-> (:ei_tutkintoa parametrit) (assoc :tutkinnot [nil]))
    (paivita-arvot [:vertailujakso_alkupvm :vertailujakso_loppupvm] (comp joda-date->sql-date parse-iso-date))))
