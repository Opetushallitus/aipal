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

(defn tunnus-voimassaolo [tyyppi alkupvm loppupvm]
  (let [alkupvm (when alkupvm (f/parse (f/formatters :date) alkupvm))
        loppupvm (when loppupvm (f/parse (f/formatters :date) loppupvm))
        voimassa_alkupvm (or alkupvm (time/today))]
    {:voimassa_alkupvm voimassa_alkupvm
     :voimassa_loppupvm (or loppupvm (time/plus voimassa_alkupvm
                                                (case tyyppi
                                                  :amispalaute amispalaute-voimassaolo
                                                  :tyoelamapalaute amispalaute-voimassaolo
                                                  palaute-voimassaolo)))}))

(def automaattitunnus-defaults
  {:tunnusten-lkm 1
   :kohteiden_lkm 1})

(defn automaatti-vastaajatunnus [tyyppi tunnus]
  (merge automaattitunnus-defaults
         (tunnus-voimassaolo tyyppi (:voimassa_alkupvm tunnus) (:voimassa_loppupvm tunnus))
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
                                :toimipiste nil
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
                                :voimassa_alkupvm alkupvm
                                :koulutustoimija koulutustoimija
                                :kieli (:tutkinnon_suorituskieli data)
                                :toimipiste (:toimipistekoodi (db/hae-oidilla {:taulu "toimipiste" :oid (:toimipiste_oid data)}))
                                :valmistavan_koulutuksen_oppilaitos (:oppilaitoskoodi (db/hae-oidilla {:taulu "oppilaitos" :oid (:oppilaitos_oid data)}))
                                :tutkinto (:tutkintotunnus data)
                                :osaamisala (:osaamisala data)
                                :hankintakoulutuksen_toteuttaja (:ytunnus (db/hae-oidilla {:taulu "koulutustoimija":oid (:hankintakoulutuksen_toteuttaja data)}))
                                :tarkenne (:kyselyn_tyyppi data)
                                :metatiedot (:metatiedot data)})))

(defn tyoelamapalaute-tunnus [data]
  (let [koulutustoimija (:ytunnus (db/hae-oidilla {:taulu "koulutustoimija" :oid (:koulutustoimija_oid data)}))
        kyselykertaid (:kyselykertaid (kyselykerta/hae-automaatti-kyselykerta koulutustoimija "tyoelamapalaute" (:kyselyn_tyyppi data)))]
    (automaatti-vastaajatunnus :tyoelamapalaute
                               {:kyselykertaid kyselykertaid
                                :koulutustoimija koulutustoimija
                                :toimipiste (:toimipistekoodi (db/hae-oidilla {:taulu "toimipiste" :oid (:toimipiste_oid data)}))
                                :valmistavan_koulutuksen_oppilaitos (:oppilaitoskoodi (db/hae-oidilla {:taulu "oppilaitos" :oid (:oppilaitos_oid data)}))
                                :tutkinto (:tutkintotunnus data)
                                :kieli "fi"
                                :voimassa_alkupvm (:vastaamisajan_alkupvm data)
                                :voimassa_loppupvm (:vastaamisajan_loppupvm data)
                                ;Työelämäpalaute
                                :tyonantaja (:tyonantaja data)
                                :tyopaikka (:tyopaikka data)
                                :tutkinnon_osa (:tutkinnon_osa data)
                                :paikallinen_tutkinnon_osa (:paikallinen_tutkinnon_osa data)
                                :tyopaikkajakson_alkupvm (:tyopaikkajakson_alkupvm data)
                                :tyopaikkajakson_loppupvm (:tyopaikkajakson_loppupvm data)
                                :sopimustyyppi (:sopimustyyppi data)
                                :osaamisala (:osaamisala data)
                                :tutkintonimike (:tutkintonimike data)
                                :metatiedot (:metatiedot data)
                                :tyopaikkajakson_kesto (:tyopaikkajakson_kesto data)
                                :osa_aikaisuus (:osa_aikaisuus data)})))

(defn nippu [data]
  (let [koulutustoimija (:ytunnus (db/hae-oidilla {:taulu "koulutustoimija" :oid (:koulutustoimija_oid data)}))
        kyselyid (:kyselyid (kyselykerta/hae-automaatti-kyselykerta koulutustoimija "tyoelamapalaute" nil))
        taustatiedot {:tutkinto (:tutkintotunnus data)
                      :tyonantaja (:tyonantaja data)
                      :tyopaikka (:tyopaikka data)}]
    (merge data {:kyselyid kyselyid :taustatiedot taustatiedot :koulutustoimija koulutustoimija}
                (tunnus-voimassaolo :tyoelamapalaute (:voimassa_alkupvm data) (:voimassa_loppupvm data)))))

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

(defn validoi-taustatieto [nippu taustatieto tunnukset]
  (if (every? #(= (get-in nippu taustatieto) (get-in % taustatieto)) tunnukset)
    {:valid true}
    {:valid false :error (str "inconsistent info: " taustatieto)}))

(defn validoi-nippu [nippu tunnukset]
  (cons (if (= (count tunnukset) (count (:tunnukset nippu)))
          {:valid true} {:valid false :error "invalid-tunnukset"})
    ((juxt
       #(validoi-taustatieto nippu [:koulutustoimija] %)
       #(validoi-taustatieto nippu [:taustatiedot :tutkinto] %))
     tunnukset)))

(defn niputa-tunnukset! [data]
  (let [nippu (nippu data)
        tunnukset (db/hae-niputettavat-tunnukset data)
        validation-result (validoi-nippu nippu tunnukset)]
    (if (every? :valid validation-result)
      (vastaajatunnus/niputa-tunnukset nippu)
      {:errors (->> validation-result
                    (filter #(not (:valid %)))
                    (map :error))})))

(defn poista-nippu [tunniste]
  (let [tunnukset (db/hae-nipun-tunnukset {:tunniste tunniste})]
    (if (not-any? :vastattu tunnukset)
      (vastaajatunnus/poista-nippu tunniste)
      {:error "Nipussa on jo vastauksia"})))

(defn paivita-metatiedot [tunnus paivitettavat-metatiedot]
  (let [paivitettava-vastaajatunnus {:metatiedot paivitettavat-metatiedot
                                     :tunnus tunnus
                                     :kayttaja aipal.infra.kayttaja.vakiot/integraatio-uid}]
    (db/paivita-metatiedot! paivitettava-vastaajatunnus)))

(defn paivita-nipun-metatiedot [tunniste metatiedot]
  (db/paivita-nipun-metatiedot! {:tunniste tunniste :metatiedot metatiedot}))