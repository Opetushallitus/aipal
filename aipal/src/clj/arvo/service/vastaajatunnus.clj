(ns arvo.service.vastaajatunnus
  (:require [arvo.db.core :refer [*db*] :as db]
            [clojure.tools.logging :as log]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus]
            [clj-time.core :as time]
            [aipal.arkisto.oppilaitos :as oppilaitos]
            [aipal.arkisto.tutkinto :as tutkinto]
            [aipal.arkisto.kyselykerta :as kyselykerta]
            [clj-time.format :as f]))

(def palaute-voimassaolo (time/months 6))
(def amispalaute-voimassaolo (time/days 30))

(defn tunnus-voimassaolo [tyyppi alkupvm]
  {:voimassa_alkupvm (or alkupvm (time/today))
   :voimassa_loppupvm (time/plus (or alkupvm (time/today))
                                 (case tyyppi
                                   :amispalaute amispalaute-voimassaolo
                                   :tyoelamapalaute amispalaute-voimassaolo
                                   palaute-voimassaolo))})

(def automaattitunnus-defaults
  {:tunnusten-lkm 1
   :kohteiden_lkm 1})

(defn automaatti-vastaajatunnus [tyyppi tunnus]
  (merge automaattitunnus-defaults
         (tunnus-voimassaolo tyyppi (:voimassa_alkupvm tunnus))
         tunnus))

(defn palaute-tunnus
  [{:keys [oppilaitoskoodi koulutus kunta kieli koulutusmuoto kyselytyyppi tarkenne]}]
  (let [ent_oppilaitos (oppilaitos/hae oppilaitoskoodi)
        ent_tutkinto (tutkinto/hae koulutus)
        _ (log/info "Haetaan automaattikyselykerta:" (:koulutustoimija ent_oppilaitos) kyselytyyppi tarkenne)
        kyselykerta-id (kyselykerta/hae-automaatti-kyselykerta (:koulutustoimija ent_oppilaitos) kyselytyyppi tarkenne)
        _ (log/info "Automaattikyselykerta: " kyselykerta-id)]
    (automaatti-vastaajatunnus :palaute
                               {:kieli kieli
                                :toimipaikka nil
                                :valmistavan_koulutuksen_oppilaitos (get-in ent_oppilaitos [:oppilaitoskoodi])
                                :tutkinto (ent_tutkinto :tutkintotunnus)
                                :kunta kunta
                                :koulutusmuoto koulutusmuoto
                                :kyselykertaid (:kyselykertaid kyselykerta-id)})))


(defn rekry-tunnus [tunnus]
  (let [{henkilonumero :henkilonumero oppilaitos :oppilaitos vuosi :vuosi} tunnus
        ent_oppilaitos (oppilaitos/hae oppilaitos)
        kyselykerta-id (kyselykerta/hae-rekrykysely oppilaitos vuosi)]
    (automaatti-vastaajatunnus :rekry
                               {:kyselykertaid (:kyselykertaid kyselykerta-id)
                                :henkilonumero henkilonumero
                                :valmistavan_koulutuksen_oppilaitos (get-in ent_oppilaitos [:oppilaitoskoodi])
                                :kieli "fi"
                                :tutkinto nil})))

(defn amispalaute-tunnus [data]
  (let [koulutustoimija (:ytunnus (db/hae-oidilla {:taulu "koulutustoimija" :oid (:koulutustoimija_oid data)}))
        kyselykertaid (:kyselykertaid (kyselykerta/hae-automaatti-kyselykerta koulutustoimija "amispalaute" (:kyselyn_tyyppi data)))
        alkupvm (:vastaamisajan_alkupvm data)]
    (automaatti-vastaajatunnus :amispalaute
                               {:kyselykertaid kyselykertaid
                                :voimassa_alkupvm (when alkupvm (f/parse (f/formatters :date) alkupvm))
                                :koulutustoimija koulutustoimija
                                :kieli (:tutkinnon_suorituskieli data)
                                :toimipaikka (:toimipaikkakoodi (db/hae-oidilla {:taulu "toimipaikka" :oid (:toimipiste_oid data)}))
                                :valmistavan_koulutuksen_oppilaitos (:oppilaitoskoodi (db/hae-oidilla {:taulu "oppilaitos" :oid (:oppilaitos_oid data)}))
                                :tutkinto (:tutkintotunnus data)
                                :hankintakoulutuksen_toteuttaja (:ytunnus (db/hae-oidilla {:taulu "koulutustoimija":oid (:hankintakoulutuksen_toteuttaja data)}))
                                :tarkenne (:kyselyn_tyyppi data)
                                :metatiedot (:metatiedot data)})))

(defn tyoelamapalaute-tunnus [data]
  (let [koulutustoimija (:ytunnus (db/hae-oidilla {:taulu "koulutustoimija" :oid (:koulutustoimija_oid data)}))
        kyselykertaid (:kyselykertaid (kyselykerta/hae-automaatti-kyselykerta koulutustoimija "tyoelamapalaute" (:kyselyn_tyyppi data)))]
    (automaatti-vastaajatunnus :tyoelamapalaute
                               {:kyselykertaid kyselykertaid
                                :koulutustoimija koulutustoimija
                                :toimipaikka (:toimipaikkakoodi (db/hae-oidilla {:taulu "toimipaikka" :oid (:toimipiste_oid data)}))
                                :valmistavan_koulutuksen_oppilaitos (:oppilaitoskoodi (db/hae-oidilla {:taulu "oppilaitos" :oid (:oppilaitos_oid data)}))
                                :tutkinto (:tutkintotunnus data)
                                :kieli "fi"
                                ;Työelämäpalaute
                                :tyonantaja (:tyonantaja data)
                                :tutkinnon_osa (:tutkinnon_osa data)
                                :tyopaikkajakson_alkupvm (:tyopaikkajakson_alkupvm data)
                                :tyopaikkajakson_loppupvm (:tyopaikkajakson_loppupvm data)
                                :sopimustyyppi (:sopimustyyppi data)
                                :osaamisala (:osaamisala data)
                                :tutkintonimike (:tutkintonimike data)})))

(defn nippu [data]
  (let [koulutustoimija (:ytunnus (db/hae-oidilla {:taulu "koulutustoimija" :oid (:koulutustoimija_oid data)}))
        oppilaitos (db/hae-oidilla {:taulu "oppilaitos" :oid (:oppilaitos_oid data)})
        kyselyid (:kyselyid (kyselykerta/hae-automaatti-kyselykerta koulutustoimija "tyoelamapalaute" nil))
        alkupvm (f/parse (f/formatters :date) (:voimassa_alkupvm data))
        taustatiedot {:oppilaitos (:oppilaitoskoodi oppilaitos)
                      :tutkinto (:tutkintotunnus data)
                      :tutkinnon_osa (:tutkinnon_osa data)}]
    (merge data {:kyselyid kyselyid :taustatiedot taustatiedot}
                (tunnus-voimassaolo :tyoelamapalaute alkupvm))))

(defn lisaa-tyoelamapalaute-tunnus! [data]
  (log/info "Luodaan työelämäpalautteen tunnus, id:" (:request_id data))
  (let [tunnus (tyoelamapalaute-tunnus data)]
    (vastaajatunnus/lisaa-automaattitunnus! tunnus)))

(defn lisaa-amispalaute-tunnus! [data]
  (log/info "Luodaan automaattitunnus, request-id:" (:request_id data))
  (let [tunnus (amispalaute-tunnus data)]
    (when (:kyselykertaid tunnus ) (vastaajatunnus/lisaa-amispalaute-automatisointi! tunnus))
    (vastaajatunnus/lisaa-automaattitunnus! tunnus)))

(defn lisaa-kyselyynohjaus! [data]
  (let [tunnus (palaute-tunnus data)]
    (vastaajatunnus/lisaa-automaattitunnus! tunnus)))

(defn lisaa-rekry-tunnus! [data]
  (let [tunnus (rekry-tunnus data)]
    (vastaajatunnus/lisaa-automaattitunnus! tunnus)))

(defn niputa-tunnukset! [data]
  (let [nippu (nippu data)]
    (vastaajatunnus/niputa-tunnukset nippu)
    nippu))