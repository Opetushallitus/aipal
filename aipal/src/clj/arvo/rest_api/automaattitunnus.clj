(ns arvo.rest-api.automaattitunnus
  (:import [java.sql.BatchUpdateException])
  (:require [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus]
            [aipal.arkisto.oppilaitos :as oppilaitos]
            [aipal.arkisto.tutkinto :as tutkinto]
            [aipal.arkisto.koulutustoimija :as koulutustoimija]
            [aipal.arkisto.kyselykerta :as kyselykerta]
            [clojure.tools.logging :as log]
            [clj-time.core :as time]
            [arvo.db.core :refer [*db*] :as db]
            [arvo.util :refer [api-response]]
            [aipal.asetukset :refer [asetukset]]
            [clj-time.format :as f]))

(s/defschema Amispalaute-tunnus
  {:vastaamisajan_alkupvm s/Str ;ISO formaatti
   :kyselyn_tyyppi s/Str
   :tutkintotunnus s/Str ;6 merkkiä
   :tutkinnon_suorituskieli s/Str ;fi, sv, en
   :koulutustoimija_oid s/Str ;organisaatio-oid
   :oppilaitos_oid s/Str ;organisaatio-oid
   :toimipiste_oid (s/maybe s/Str) ;organisaatio-oid
   :hankintakoulutuksen_toteuttaja (s/maybe s/Str)
   :request_id s/Str})

(s/defschema Automaattitunnus
  {:oppilaitos s/Str
   :koulutus s/Str
   :kunta s/Str
   :kieli s/Str
   :kyselytyyppi s/Str
   (s/optional-key :tarkenne) s/Str
   (s/optional-key :koulutusmuoto) s/Int})

(s/defschema Tunnus-status
  {})


(defn on-validation-error [message]
    {:status 400
      :headers {"Content-Type" "application/json"}
      :body {:status 400
              :detail message}})

(defn on-403 [request]
  {:status  403
   :headers {"Content-Type" "application/json"}
   :body   {:status 403
            :detail  (str "Access to " (:uri request) " is forbidden")}})

(def palaute-voimassaolo (time/months 6))
(def amispalaute-voimassaolo (time/days 30))

(defn tunnus-voimassaolo [tyyppi alkupvm]
  {:voimassa_alkupvm (or alkupvm (time/today))
   :voimassa_loppupvm (time/plus (or alkupvm (time/today))
                                 (case tyyppi
                                   :amispalaute amispalaute-voimassaolo
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
      :tarkenne (:kyselyn_tyyppi data)})))

(defn handle-error
  ([error request-id]
   (log/error "Virhe vastaajatunnuksen luonnissa: "
              (if request-id (str "request-id " request-id " - ") "")
              (:msg error))
   {:status 404 :body error})
  ([error]
   (handle-error error nil)))

(defn vastauslinkki-response [luotu-tunnus request-id]
  (if (:tunnus luotu-tunnus)
    (api-response {:kysely_linkki (str (:vastaus-base-url @asetukset)"/"(:tunnus luotu-tunnus))
                   :voimassa_loppupvm (f/unparse (f/formatters :date)(:voimassa_loppupvm luotu-tunnus))})
    (handle-error (:error luotu-tunnus) request-id)))

(defn kyselyynohjaus-response [luotu-tunnus]
  (if {:tunnus luotu-tunnus}
    (api-response {:tunnus (:tunnus luotu-tunnus)})
    (handle-error {:error luotu-tunnus})))

(defn lisaa-amispalaute-automatisointi! [tunnus]
  (db/lisaa-automatisointiin! {:koulutustoimija (:koulutustoimija tunnus)
                               :lahde "EHOKS"}))

(defn lisaa-kyselyynohjaus! [tunnus]
  (let [luotu-tunnus (vastaajatunnus/lisaa-automaattitunnus! tunnus)]
    (kyselyynohjaus-response luotu-tunnus)))

(defn lisaa-automaattitunnus! [tunnus request-id]
  (let [luotu-tunnus (vastaajatunnus/lisaa-automaattitunnus! tunnus)]
    (vastauslinkki-response luotu-tunnus request-id)))

(defroutes kyselyynohjaus-v1
  (POST "/" []
    :body [avopdata s/Any]
    (try
      (let [vastaajatunnus (palaute-tunnus avopdata)]
        (lisaa-kyselyynohjaus! vastaajatunnus))
      (catch java.lang.AssertionError e1
        (log/error e1 "Mandatory fields missing")
        (on-validation-error (format "Mandatory fields are missing or not found")))
      (catch Exception e2
        (log/error e2 "Unexpected error")
        (on-validation-error (format "Unexpected error: %s" (.getMessage e2))))))
  (POST "/rekry" []
    :body [rekrydata s/Any]
    (try
      (let [vastaajatunnus (rekry-tunnus rekrydata)]
        (lisaa-kyselyynohjaus! vastaajatunnus))
      (catch java.lang.AssertionError e1
        (log/error e1 "Mandatory fields missing")
        (on-validation-error (format "Mandatory fields are missing or not found")))
      (catch Exception e2
        (log/error e2 "Unexpected error")
        (on-validation-error (format "Unexpected error: %s" (.getMessage e2)))))))

(defroutes ehoks-v1
  (POST "/" []
    :body [data Amispalaute-tunnus]
    :return s/Any
    :summary "Kyselylinkin luominen"
    :description (str "Päivämäärät ovat ISO-formaatin mukaisia. Suorituskieli on fi, sv tai en. Tutkintotunnus
        on opintopolun koulutus koodiston 6 numeroinen koodi.")
    (let [tunnus (amispalaute-tunnus data)]
      (log/info "Luodaan automaattitunnus, request-id:" (:request_id data))
      (when (:kyselykertaid tunnus ) (lisaa-amispalaute-automatisointi! tunnus))
      (lisaa-automaattitunnus! tunnus (:request_id data))))
  (GET "/status/:tunnus" []
    :path-params [tunnus :- s/Str]
    (let [status (db/vastaajatunnus-status {:tunnus tunnus})]
      (api-response (dissoc status :vastaajatunnusid))))
  (DELETE "/:tunnus" []
    :path-params [tunnus :- s/Str]
    (let [status (db/vastaajatunnus-status {:tunnus tunnus})]
      (if-not (:vastattu status)
        (do (db/poista-vastaajatunnus! {:vastaajatunnusid (:vastaajatunnusid status)})
            (api-response "Tunnus poistettu"))
        {:status 404 :body "Tunnuksella on jo vastauksia"}))))

