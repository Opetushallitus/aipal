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

(ns aipal.toimiala.raportti.kyselykerta
  (:require [clj-time.core :as time]
            [korma.core :as sql]
            [aipal.integraatio.sql.korma :refer [kyselykerta]]
            [aipal.toimiala.raportti.raportointi :as raportointi]))

(defn ^:private hae-kyselykerta [kyselykertaid]
  (->
    (sql/select* kyselykerta)
    (sql/join :inner :kysely_organisaatio_view
              (= :kysely_organisaatio_view.kyselyid :kyselykerta.kyselyid))
    (sql/join :inner :koulutustoimija
              (= :koulutustoimija.ytunnus :kysely_organisaatio_view.koulutustoimija))
    (sql/fields :kyselykerta.kyselyid :kyselykerta.kyselykertaid :kyselykerta.nimi :kyselykerta.voimassa_alkupvm :kyselykerta.voimassa_loppupvm
                [:koulutustoimija.nimi_fi :koulutustoimija_fi] [:koulutustoimija.nimi_sv :koulutustoimija_sv])
    (sql/where {:kyselykertaid kyselykertaid})

    sql/exec
    first))

(defn hae-vastaajatunnusten-tutkinnot [kyselykertaid]
  (sql/select :vastaajatunnus
    (sql/join :left :tutkinto
              (= :tutkinto.tutkintotunnus :vastaajatunnus.tutkintotunnus))
    (sql/join :left :vastaaja
              (= :vastaaja.vastaajatunnusid :vastaajatunnus.vastaajatunnusid))
    (sql/aggregate (count :vastaaja.vastaajaid) :vastaajien_lkm)
    (sql/fields :tutkinto.tutkintotunnus :tutkinto.nimi_fi :tutkinto.nimi_sv)
    (sql/where {:kyselykertaid kyselykertaid})
    (sql/group :tutkinto.tutkintotunnus :tutkinto.nimi_fi :tutkinto.nimi_sv)
    (sql/order :tutkinto.tutkintotunnus)))

(defn hae-vastaajien-maksimimaara [kyselykertaid]
  (->
    (sql/select* :vastaajatunnus)
    (sql/aggregate (sum :vastaajien_lkm) :vastaajien_maksimimaara)
    (sql/where {:kyselykertaid kyselykertaid})
    sql/exec
    first
    :vastaajien_maksimimaara))

(defn ^:private hae-kysymykset [kyselykertaid]
  (sql/select :kyselykerta
    (sql/join :inner :kysely
             (= :kyselykerta.kyselyid
                :kysely.kyselyid))
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
    (sql/where {:kyselykertaid kyselykertaid})
    (sql/order :kysely_kysymysryhma.jarjestys :ASC)
    (sql/order :kysymys.jarjestys :ASC)
    (sql/fields :kyselykerta.kyselykertaid
                :kysymys.kysymysryhmaid
                :kysymys.kysymysid
                :kysymys.kysymys_fi
                :kysymys.kysymys_sv
                :kysymys.vastaustyyppi
                :kysymys.eos_vastaus_sallittu
                :jatkokysymys.jatkokysymysid
                :jatkokysymys.kylla_kysymys
                :jatkokysymys.kylla_teksti_fi
                :jatkokysymys.kylla_teksti_sv
                :jatkokysymys.kylla_vastaustyyppi
                :jatkokysymys.ei_kysymys
                :jatkokysymys.ei_teksti_fi
                :jatkokysymys.ei_teksti_sv)))

(defn hae-kysymysryhmat [kyselykertaid]
  (sql/select :kyselykerta
    (sql/join :inner :kysely
             (= :kyselykerta.kyselyid
                :kysely.kyselyid))
    (sql/join :inner :kysely_kysymysryhma
             (= :kysely.kyselyid
                :kysely_kysymysryhma.kyselyid))
    (sql/join :inner :kysymysryhma
              (= :kysely_kysymysryhma.kysymysryhmaid
                 :kysymysryhma.kysymysryhmaid))
    (sql/where {:kyselykertaid kyselykertaid})
    (sql/order :kysely_kysymysryhma.jarjestys :ASC)
    (sql/fields :kysymysryhma.kysymysryhmaid
                :kysymysryhma.nimi_fi
                :kysymysryhma.nimi_sv)))

(defn ^:private hae-vastaukset [kyselykertaid]
  (sql/select :kyselykerta
    (sql/join :inner :vastaaja
              (= :kyselykerta.kyselykertaid
                 :vastaaja.kyselykertaid))
    (sql/join :inner :vastaus
              (= :vastaaja.vastaajaid
                 :vastaus.vastaajaid))
    (sql/join :left :jatkovastaus
              (= :jatkovastaus.jatkovastausid
                 :vastaus.jatkovastausid))
    (sql/where {:kyselykertaid kyselykertaid})
    (sql/fields :kyselykerta.kyselykertaid
                :vastaaja.vastaajaid
                :vastaus.vastausid
                :vastaus.kysymysid
                :vastaus.numerovalinta
                :vastaus.vaihtoehto
                :vastaus.vapaateksti
                :vastaus.en_osaa_sanoa
                :jatkovastaus.jatkovastausid
                :jatkovastaus.jatkokysymysid
                :jatkovastaus.kylla_asteikko
                :jatkovastaus.ei_vastausteksti)))

(defn ^:private muodosta-raportti-kyselykerrasta [kyselykertaid]
  (raportointi/muodosta-raportti-vastauksista (hae-kysymysryhmat kyselykertaid)
                                              (hae-kysymykset kyselykertaid)
                                              (hae-vastaukset kyselykertaid)))

(defn muodosta-raportti-perustiedot [kyselykertaid]
  (when-let [kyselykerta (hae-kyselykerta kyselykertaid)]
    {:kyselykerta (assoc kyselykerta :tutkinnot (hae-vastaajatunnusten-tutkinnot kyselykertaid))
     :luontipvm (time/today)
     :vastaajien_maksimimaara (hae-vastaajien-maksimimaara kyselykertaid)}))

(defn muodosta-raportti [kyselykertaid]
  (let [perustiedot (muodosta-raportti-perustiedot kyselykertaid)]
    (when perustiedot
      (assoc perustiedot :raportti (muodosta-raportti-kyselykerrasta kyselykertaid)))))

(defn laske-vastaajat [kyselykertaid]
  (-> (sql/select :vastaaja
        (sql/aggregate (count :*) :vastaajia)
        (sql/where {:kyselykertaid kyselykertaid}))
      first
      :vastaajia))
