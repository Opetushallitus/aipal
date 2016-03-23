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

(ns aipal.rest-api.avopvastaajatunnus
  (:require [compojure.api.core :refer [defroutes POST GET]]
            [schema.core :as s]
            [buddy.auth.backends.token :refer (jws-backend)]
            [buddy.auth.middleware :refer (wrap-authentication)]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus]
            [aipal.arkisto.oppilaitos :as oppilaitos]
            [aipal.arkisto.tutkinto :as tutkinto]
            [aipal.arkisto.koulutustoimija :as koulutustoimija]
            [aipal.arkisto.toimipaikka :as toimipaikka]
            [aipal.arkisto.kysely :as kysely]
            [aipal.arkisto.kyselykerta :as kyselykerta]
            [clojure.tools.logging :as log]
            [cheshire.core :as cheshire]
            [clj-time.core :as time]
            aipal.compojure-util
            [oph.common.util.http-util :refer [response-or-404 response-validation-error]]))


(defn alkupvm [] (time/today))
(defn loppupvm [] (time/plus (alkupvm) (time/months 6)))
;;TODO: To move it to vault
(def secret "secret")
(def auth-backend (jws-backend {:secret secret}))

(defn avop->arvo-map
  [{:keys [oppilaitos koulutus kunta kieli koulutusmuoto opiskeluoikeustyyppi laajuus kyselykerran_nimi]}]
  (let [
        ;;kunta <- no need
        ;;opiskeluoikeustyyppi <- no need
        ;;laajuus <- no need
        ent_oppilaitos (oppilaitos/hae oppilaitos)
        ent_koulutustoimija (koulutustoimija/hae-kentat (ent_oppilaitos :koulutustoimija))
        ent_tutkinto (tutkinto/hae-kentat koulutus)
        kyselykerta-id (kyselykerta/hae-nimella-ja-oppilaitoksella kyselykerran_nimi oppilaitos)]
    {
     :voimassa_alkupvm (alkupvm)
     :voimassa_loppupvm (loppupvm)
     :suorituskieli kieli
     :vastaajien_lkm 1
     :rahoitusmuotoid 5
     :henkilokohtainen true
     :koulutksen_jarjestaja_oppilaitos (cheshire.core/generate-string ent_oppilaitos)
     :koulutksen_jarjestaja  (cheshire.core/generate-string ent_koulutustoimija)
     :tutkinto (cheshire.core/generate-string ent_tutkinto)
     :koulutusmuoto koulutusmuoto
     :kyselykertaid kyselykerta-id
     }))


(defroutes reitit
  (wrap-authentication (POST "/" []
    :body [avopdata s/Any]
    ;:header-params [authorization :- String]
   (try
      ;(log/info (format "%s" (avop->arvo-map avopdata)))
      (let [vastaajatunnus (avop->arvo-map avopdata)]
        (response-or-404 (vastaajatunnus/lisaa! vastaajatunnus)))
      (catch java.lang.AssertionError e1 
        (response-validation-error "Mandatory fields are missing or not found")
      )
      (catch Exception e2
         (response-validation-error (format "Unexpected error: %s" (.getMessage e2)))
      )
    )) auth-backend)
