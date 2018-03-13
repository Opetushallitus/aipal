;; Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
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

(ns arvo.rest-api.avopvastaajatunnus
  (:import [java.sql.BatchUpdateException])
  (:require [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus]
            [aipal.arkisto.oppilaitos :as oppilaitos]
            [aipal.arkisto.tutkinto :as tutkinto]
            [aipal.arkisto.koulutustoimija :as koulutustoimija]
            [aipal.arkisto.kyselykerta :as kyselykerta]
            [clojure.tools.logging :as log]

            [buddy.auth.backends.token :refer (jws-backend)]
            [buddy.auth.middleware :refer (wrap-authentication)]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [cheshire.core :as cheshire]
            [clj-time.core :as time]
            aipal.compojure-util
            [oph.common.util.http-util :refer [parse-iso-date]]))


(defn on-response [message]
    {:status 200
      :headers {"Content-Type" "application/json"}
      :body {:tunnus message}})


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

(defn get-shared-secret [asetukset] 
  (get-in asetukset [:avopfi-shared-secret]))
   
(defn auth-backend [asetukset] (jws-backend {:secret (get-shared-secret asetukset) :token-name "Bearer"}))

(defn alkupvm [] (time/today))
(defn loppupvm [] (time/plus (alkupvm) (time/months 6)))

(defn auth-mw [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      (on-403 request))))


(defn automaatti-vastaajatunnus []
  {:tunnusten-lkm 1
   :vastaajien_lkm 1
   :voimassa_alkupvm (alkupvm)
   :voimassa_loppupvm (loppupvm)
   :rahoitusmuotoid 5
   :toimipaikka nil})


(defn avop->arvo-map
  [{:keys [oppilaitos koulutus kunta kieli koulutusmuoto kyselykerran_nimi]}]
  (let [ent_oppilaitos (oppilaitos/hae oppilaitos)
        ent_koulutustoimija (koulutustoimija/hae-kentat (ent_oppilaitos :koulutustoimija))
        ent_tutkinto (tutkinto/hae koulutus)
        kyselykerta-id (kyselykerta/hae-nimella-ja-oppilaitoksella kyselykerran_nimi oppilaitos)]
    {:tunnusten-lkm 1
     :vastaajien_lkm 1
     :voimassa_alkupvm (alkupvm)
     :voimassa_loppupvm (loppupvm)
     :kieli kieli
     :rahoitusmuotoid 5
     :toimipaikka nil
     :valmistavan_koulutuksen_jarjestaja (if (nil? ent_koulutustoimija) nil (get-in ent_koulutustoimija [:ytunnus])) 
     :valmistavan_koulutuksen_oppilaitos (get-in ent_oppilaitos [:oppilaitoskoodi])
     :tutkinto (ent_tutkinto :tutkintotunnus)
     :kunta kunta
     :koulutusmuoto koulutusmuoto
     :kyselykertaid (kyselykerta-id :kyselykertaid)}))


(defn rekry-vastaajatunnus
  [{:keys [henkilonumero oppilaitos vuosi]}]
  (let [ent_oppilaitos (oppilaitos/hae oppilaitos)
        ent_koulutustoimija (koulutustoimija/hae-kentat (ent_oppilaitos :koulutustoimija))
        kyselykerta-id (kyselykerta/hae-rekrykysely oppilaitos vuosi)]
    (merge (automaatti-vastaajatunnus)
           {:kyselykertaid (kyselykerta-id :kyselykertaid)
            :henkilonumero henkilonumero
            :valmistavan_koulutuksen_jarjestaja (if (nil? ent_koulutustoimija) nil (get-in ent_koulutustoimija [:ytunnus]))
            :valmistavan_koulutuksen_oppilaitos (get-in ent_oppilaitos [:oppilaitoskoodi])})))

(defroutes vastaajatunnus-routes
  (POST "/" []
    :body [avopdata s/Any]
    :middleware [arvo.rest-api.avopvastaajatunnus/auth-mw]
    :header-params [authorization :- String]
    (try
      (let [vastaajatunnus (avop->arvo-map avopdata)]
        (on-response (get-in (first (vastaajatunnus/lisaa-avopfi! vastaajatunnus)) [:tunnus])))
      (catch java.lang.AssertionError e1
        (log/error e1 "Mandatory fields missing")
        (on-validation-error (format "Mandatory fields are missing or not found")))
      (catch Exception e2
        (log/error e2 "Unexpected error")
        (on-validation-error (format "Unexpected error: %s" (.getMessage e2))))))
  (POST "/rekry" []
    :body [rekrydata s/Any]
    :middleware [arvo.rest-api.avopvastaajatunnus/auth-mw]
    :header-params [authorization :- String]
    (try
      (let [vastaajatunnus (rekry-vastaajatunnus rekrydata)]
        (on-response (get-in (first (vastaajatunnus/lisaa-avopfi! vastaajatunnus)) [:tunnus])))
      (catch java.lang.AssertionError e1
        (log/error e1 "Mandatory fields missing")
        (on-validation-error (format "Mandatory fields are missing or not found")))
      (catch Exception e2
        (log/error e2 "Unexpected error")
        (on-validation-error (format "Unexpected error: %s" (.getMessage e2)))))))

;Hae kyselykerta metatietojen perusteella (vanhoissa vielä nimellä mutta tuki valmiiksi?)
;requestiin mukaan erillisinä avaimina taustatiedot ja kyselykerta-ohajaustiedot?
;REKRY = organisaatio + kyselytyyppi + kyselykerran vuositieto? (nimeä uudelleen uraseurannan käyttämä?
;MUUT = organisaatio + kyselytyyppi + vuosi + meta (avop-amk, avop-yamk jne vai pitäisikö päättely olla täällä päässä?)
  ; Automaatti-merkintä + UI?

(defn reitit [asetukset]
  (wrap-authentication vastaajatunnus-routes (auth-backend asetukset)))
