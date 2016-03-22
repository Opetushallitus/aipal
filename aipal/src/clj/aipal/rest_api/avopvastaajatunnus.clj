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
            [clj-time.core :as time]
            aipal.compojure-util
            [oph.common.util.http-util :refer [response-or-404]]))


(defn alkupvm [] (time/now))
(defn loppupvm [] (time/plus (alkupvm) (time/months 6)))
;;TODO: To move it to vault
(def secret "secret")
(def auth-backend (jws-backend {:secret secret}))

(defn avop->arvo-map
  [{:keys [kuntaid kieli koulutusid tyyppi tutkintoid oppilaitosid kyselykertanimi]}]
  (let [
        oppilaitos (oppilaitos/hae oppilaitosid)
        koulutustoimija (koulutustoimija/hae oppilaitosid)
        ;;toimipaikka (toimipaikka/hae-oppilaitoksen-toimipaikka oppilaitosid)
        ;;toimipaikka nil
        tutkinto (tutkinto/hae tutkintoid)
        koulutusmuoto tyyppi
        kyselykerta-id (kyselykerta/hae-nimella-ja-oppilaitoksella kyselykertanimi oppilaitosid)]
    {
     :henkilokohtainen "true"
     :koulutksen_jarjestaja_oppilaitos oppilaitos
     :koulutksen_jarjestaja  koulutustoimija
     :koulutksen_toimipaikka nil
     :koulutusmuoto koulutusmuoto
     :rahoitusmuotoid 5
     :suorituskieli kieli
     :tutkinto tutkinto
     :vastaajien_lkm 1
     :voimassa_alkupvm (alkupvm)
     :voimassa_loppupvm (loppupvm)
     :kyselykertaid 1
     }))


(defroutes reitit
  (POST "/" []
    :body [avopdata s/Any]
    (log/info (format "%s" (avop->arvo-map avopdata)))
    (let [vastaajatunnus (avop->arvo-map avopdata)]
      (response-or-404 (vastaajatunnus/lisaa! vastaajatunnus)))))
