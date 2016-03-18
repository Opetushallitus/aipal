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
  (:require [compojure.api.core :refer [defroutes POST]]
            [schema.core :as s]
            [buddy.auth.backends.token :refer (jws-backend)]
            [buddy.auth.middleware :refer (wrap-authentication)]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus]
            [aipal.arkisto.oppilaitos :as oppilaitos]
            [aipal.arkisto.tutkinto :as tutkinto]
            [aipal.arkisto.koulutustoimija :as koulutustoimija]
            [aipal.arkisto.toimipaikka :as toimipaikka]
            [aipal.arkisto.kysely :as kysely]
            aipal.compojure-util
            [oph.common.util.http-util :refer [response-or-404]]))


(defn alkupvm [] (java.util.Date))

;;TODO: To move it to vault
(def secret "secret")
(def auth-backend (jws-backend {:secret secret}))

(defn avop->arvo-map
  [{:keys [kuntaid kieli koulutusid tyyppi tutkintoid oppilaitosid]}]
  (let [
        oppilaitos (oppilaitos/hae oppilaitosid)
        koulutustoimija (koulutustoimija/hae oppilaitosid) ;;oppilaitos.koulutustoimija)
        toimipaikka (toimipaikka/hae-oppilaitoksen-ja-kunta-toimipaikka oppilaitosid kuntaid)
        tutkinto (tutkinto/hae tutkintoid)
        koulutusmuoto tyyppi
        ;;Wrong but do something for the time being
        kysely (kysely/hae-kyselyt kuntaid)] ;;toimipaikka.koulutustoimija)]
    {
     :henkilokohtainen "true"
     :koulutksen_jarjestaja_oppilaitos oppilaitos
     :koulutksen_jarjestaja  koulutustoimija
     :koulutksen_toimipaikka toimipaikka
     :koulutusmuoto koulutusmuoto
     :rahoitusmuotoid nil
     :suorituskieli kieli
     :tutkinto tutkinto
     :vastaajien_lkm 1
     :voimassa_alkupvm (alkupvm)
     :voimassa_loppupvm (alkupvm)
     :kyselykertaid kysely ;;kysely.kyselyid
     }))


(defroutes reitit
  (wrap-authentication (POST "/" []
    :body [avopdata s/Any]
    (let [vastaajatunnus (avop->arvo-map avopdata)]
      (response-or-404 (vastaajatunnus/lisaa! vastaajatunnus))))) auth-backend)