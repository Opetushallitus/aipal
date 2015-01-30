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
            [aipal.toimiala.raportti.raportointi :as raportointi]))

(defn yhdista-ja-jarjesta-tutkinnot
  [tutkinnot]
  (->>
    (let [tutkinnot (group-by #(select-keys % [:tutkintotunnus :nimi_fi :nimi_sv]) tutkinnot)]
      (for [[tutkinto lukumaarat] tutkinnot]
        (assoc tutkinto :vastaajien_lkm (reduce + 0 (map :vastaajien_lkm lukumaarat)))))
    (sort-by :tutkintotunnus)))

(defn koulutustoimijat-hierarkiaksi
  [vastaajatunnus-tiedot parametrit]
  (let [koulutustoimijat (group-by #(select-keys % [:ytunnus :koulutustoimija_fi :koulutustoimija_sv]) vastaajatunnus-tiedot)]
    (for [[koulutustoimija tutkinnot] koulutustoimijat]
      (-> (if (:ytunnus koulutustoimija)
            (dissoc koulutustoimija :ytunnus)
            (select-keys parametrit [:koulutustoimija_fi :koulutustoimija_sv]))
        (assoc :tutkinnot (yhdista-ja-jarjesta-tutkinnot tutkinnot)
               :vastaajat_yhteensa (reduce + 0 (map :vastaajien_lkm tutkinnot)))))))

(defn hae-vastaajatunnusten-tiedot-koulutustoimijoittain
  [parametrit]
  (->
    (sql/select* :vastaajatunnus)
    (cond->
      (:tutkinnot parametrit) (sql/where {:vastaajatunnus.tutkintotunnus [in (:tutkinnot parametrit)]})
      (:vertailujakso_alkupvm parametrit) (sql/where (or (= :vastaajatunnus.voimassa_loppupvm nil)
                                                         (>= :vastaajatunnus.voimassa_loppupvm (:vertailujakso_alkupvm parametrit))))
      (:vertailujakso_loppupvm parametrit) (sql/where (or (= :vastaajatunnus.voimassa_alkupvm nil)
                                                          (<= :vastaajatunnus.voimassa_alkupvm (:vertailujakso_loppupvm parametrit))))
      (:kyselyid parametrit) (->
                               (sql/join :inner :kyselykerta
                                         (= :kyselykerta.kyselykertaid :vastaajatunnus.kyselykertaid))
                               (sql/where {:kyselykerta.kyselyid (:kyselyid parametrit)}))
      (:kyselykertaid parametrit) (sql/where {:kyselykertaid (:kyselykertaid parametrit)}))
    (sql/join :left :koulutustoimija
              (= :koulutustoimija.ytunnus :vastaajatunnus.valmistavan_koulutuksen_jarjestaja))
    (sql/join :left :tutkinto
              (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus))
    (sql/fields :tutkinto.tutkintotunnus :tutkinto.nimi_fi :tutkinto.nimi_sv
                [(sql/subselect :vastaaja
                                (sql/aggregate (count :*) :vastaajien_lkm)
                                (sql/where {:vastaaja.vastaajatunnusid :vastaajatunnus.vastaajatunnusid})) :vastaajien_lkm]
                :koulutustoimija.ytunnus [:koulutustoimija.nimi_fi :koulutustoimija_fi] [:koulutustoimija.nimi_sv :koulutustoimija_sv])
    sql/exec
    (koulutustoimijat-hierarkiaksi parametrit)))

(defn laske-vastaajat-yhteensa
  [koulutustoimijat]
  (reduce + 0 (map :vastaajat_yhteensa koulutustoimijat)))

(defn hae-vastaajien-maksimimaara [parametrit]
  (->
    (sql/select* :kyselykerta)
    (sql/join :inner :vastaajatunnus
              (= :kyselykerta.kyselykertaid :vastaajatunnus.kyselykertaid))
    (sql/aggregate (sum :vastaajatunnus.vastaajien_lkm) :vastaajien_maksimimaara)
    (cond->
      (:tutkinnot parametrit) (sql/where {:vastaajatunnus.tutkintotunnus [in (:tutkinnot parametrit)]})
      (:vertailujakso_alkupvm parametrit) (sql/where (or (= :vastaajatunnus.voimassa_loppupvm nil)
                                                         (>= :vastaajatunnus.voimassa_loppupvm (:vertailujakso_alkupvm parametrit))))
      (:vertailujakso_loppupvm parametrit) (sql/where (or (= :vastaajatunnus.voimassa_alkupvm nil)
                                                          (<= :vastaajatunnus.voimassa_alkupvm (:vertailujakso_loppupvm parametrit))))
      (:kyselykertaid parametrit) (sql/where {:kyselykertaid (:kyselykertaid parametrit)})
      (:kyselyid parametrit) (sql/where {:kyselyid (:kyselyid parametrit)}))
    sql/exec
    first
    :vastaajien_maksimimaara))

(defn liita-vastaajien-maksimimaarat
  [kysymysryhmat parametrit]
  (let [vastaajien-maksimimaara (hae-vastaajien-maksimimaara parametrit)]
    (for [kysymysryhma kysymysryhmat]
      (assoc kysymysryhma :vastaajien_maksimimaara vastaajien-maksimimaara))))

(defn ^:private hae-kysymykset [parametrit]
  (->
    (sql/select* :kysely)
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
      (:kyselykertaid parametrit) (->
                                    (sql/join :inner :kyselykerta
                                              (= :kyselykerta.kyselyid :kysely.kyselyid))
                                    (sql/where {:kyselykerta.kyselykertaid (:kyselykertaid parametrit)}))
      (:kyselyid parametrit) (sql/where {:kysely.kyselyid (:kyselyid parametrit)}))
    (sql/order :kysely_kysymysryhma.jarjestys :ASC)
    (sql/order :kysymys.jarjestys :ASC)
    (sql/fields :kysymys.kysymysryhmaid
                :kysymys.kysymysid
                :kysymys.kysymys_fi
                :kysymys.kysymys_sv
                :kysymys.vastaustyyppi
                :kysymys.eos_vastaus_sallittu
                :kysymys.jarjestys
                :jatkokysymys.jatkokysymysid
                :jatkokysymys.kylla_kysymys
                :jatkokysymys.kylla_teksti_fi
                :jatkokysymys.kylla_teksti_sv
                :jatkokysymys.kylla_vastaustyyppi
                :jatkokysymys.ei_kysymys
                :jatkokysymys.ei_teksti_fi
                :jatkokysymys.ei_teksti_sv)
    sql/exec))

(defn hae-kysymysryhmat [parametrit]
  (->
    (sql/select* :kysely)
    (sql/join :inner :kysely_kysymysryhma
              (= :kysely.kyselyid
                 :kysely_kysymysryhma.kyselyid))
    (sql/join :inner :kysymysryhma
              (= :kysely_kysymysryhma.kysymysryhmaid
                 :kysymysryhma.kysymysryhmaid))
    (cond->
      (:kyselykertaid parametrit) (->
                                    (sql/join :inner :kyselykerta
                                              (= :kyselykerta.kyselyid :kysely.kyselyid))
                                    (sql/where {:kyselykerta.kyselykertaid (:kyselykertaid parametrit)}))
      (:kyselyid parametrit) (sql/where {:kyselyid (:kyselyid parametrit)}))
    (sql/order :kysely_kysymysryhma.jarjestys :ASC)
    (sql/fields :kysymysryhma.kysymysryhmaid
                :kysymysryhma.nimi_fi
                :kysymysryhma.nimi_sv)
    sql/exec))

(defn ^:private hae-vastaukset [parametrit]
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
      (:tutkinnot parametrit) (->
                                (sql/join :inner :vastaajatunnus
                                          (= :vastaajatunnus.vastaajatunnusid :vastaaja.vastaajatunnusid))
                                (sql/where {:vastaajatunnus.tutkintotunnus [in (:tutkinnot parametrit)]}))
      (:vertailujakso_alkupvm parametrit) (sql/where (>= :vastaus.vastausaika (:vertailujakso_alkupvm parametrit)))
      (:vertailujakso_loppupvm parametrit) (sql/where (<= :vastaus.vastausaika (:vertailujakso_loppupvm parametrit)))
      (:kyselykertaid parametrit) (sql/where {:kyselykertaid (:kyselykertaid parametrit)})
      (:kyselyid parametrit) (sql/where {:kyselyid (:kyselyid parametrit)}))
    (sql/fields :vastaaja.vastaajaid
                :vastaus.vastausid
                :vastaus.kysymysid
                :vastaus.numerovalinta
                :vastaus.vaihtoehto
                :vastaus.vapaateksti
                :vastaus.en_osaa_sanoa
                :jatkovastaus.jatkovastausid
                :jatkovastaus.jatkokysymysid
                :jatkovastaus.kylla_asteikko
                :jatkovastaus.ei_vastausteksti)
    sql/exec))

(defn muodosta-raportti [parametrit]
  {:pre [(or :kyselyid parametrit
             :kyselykertaid parametrit)]}
  (raportointi/muodosta-raportti-vastauksista (liita-vastaajien-maksimimaarat
                                                (hae-kysymysryhmat parametrit) parametrit)
                                              (hae-kysymykset parametrit)
                                              (hae-vastaukset parametrit)))
