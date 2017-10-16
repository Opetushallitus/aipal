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

(ns aipal.rest-api.vastaajatunnus
  (:require [compojure.api.core :refer [defroutes DELETE GET POST PUT]]
            [schema.core :as s]
            [aipal.arkisto.vastaajatunnus :as vastaajatunnus]
            aipal.compojure-util
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [clojure.tools.logging :as log]
            [oph.common.util.http-util :refer [parse-iso-date response-or-404]]
            [oph.common.util.util :refer [paivita-arvot]]))

(defn ui->vastaajatunnus [vastaajatunnus kyselykertaid]
  (let [tunnusten-lkm (if (:henkilokohtainen vastaajatunnus) (:vastaajien_lkm vastaajatunnus) 1)
        vastaajien-lkm (if (:henkilokohtainen vastaajatunnus) 1 (:vastaajien_lkm vastaajatunnus))
        valmistavan-koulutuksen-jarjestaja (get-in vastaajatunnus [:koulutuksen_jarjestaja :ytunnus])
        tutkintotunnus (get-in vastaajatunnus [:tutkinto :tutkintotunnus])
        valmistavan-koulutuksen-oppilaitos (get-in vastaajatunnus [:koulutuksen_jarjestaja_oppilaitos :oppilaitoskoodi])
        valmistavan-koulutuksen-toimipaikka (get-in vastaajatunnus [:koulutuksen_toimipaikka :toimipaikkakoodi])
        kunta (get-in vastaajatunnus [:koulutuksen_toimipaikka :kunta])
        rahoitusmuotoid (or(:rahoitusmuotoid vastaajatunnus) 5)]
    {:tunnusten-lkm tunnusten-lkm
     :kyselykertaid kyselykertaid
     :vastaajien_lkm vastaajien-lkm
     :tutkinto tutkintotunnus
     :kieli (:suorituskieli vastaajatunnus)
     :kunta kunta
     :rahoitusmuotoid rahoitusmuotoid
     :koulutusmuoto (:koulutusmuoto vastaajatunnus)
     :valmistavan_koulutuksen_jarjestaja valmistavan-koulutuksen-jarjestaja
     :valmistavan_koulutuksen_oppilaitos valmistavan-koulutuksen-oppilaitos
     :toimipaikka valmistavan-koulutuksen-toimipaikka
     :voimassa_alkupvm (:voimassa_alkupvm vastaajatunnus)
     :voimassa_loppupvm (:voimassa_loppupvm vastaajatunnus)
     :haun_numero (:haun_numero vastaajatunnus)
     :henkilonumero (:henkilonumero vastaajatunnus)}))

(defroutes reitit
  (POST "/:kyselykertaid" []
    :path-params [kyselykertaid :- s/Int]
    :body [vastaajatunnus s/Any]
    :kayttooikeus [:vastaajatunnus-luonti kyselykertaid]
    (let [vastaajatunnus (-> vastaajatunnus
                           (ui->vastaajatunnus kyselykertaid)
                           (paivita-arvot [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date))]
      (response-or-404 (vastaajatunnus/lisaa! vastaajatunnus))))

  (POST "/:kyselykertaid/tunnus/:vastaajatunnusid/lukitse" []
    :path-params [kyselykertaid :- s/Int
                  vastaajatunnusid :- s/Int]
    :body-params [lukitse :- Boolean]
    :kayttooikeus [:vastaajatunnus-tilamuutos kyselykertaid]
    (response-or-404 (vastaajatunnus/aseta-lukittu! kyselykertaid vastaajatunnusid lukitse)))

  (POST "/:kyselykertaid/tunnus/:vastaajatunnusid/muokkaa-lukumaaraa" []
    :path-params [kyselykertaid :- s/Int
                  vastaajatunnusid :- s/Int]
    :body-params [lukumaara :- s/Int]
    :kayttooikeus [:vastaajatunnus-muokkaus kyselykertaid]
    (let [vastaajatunnus (vastaajatunnus/hae kyselykertaid vastaajatunnusid)
          vastaajat (vastaajatunnus/laske-vastaajat vastaajatunnusid)]
      (when-not (:muokattavissa vastaajatunnus)
        (throw (IllegalArgumentException. "Vastaajatunnus ei ole enää muokattavissa")))
      (if (and (pos? lukumaara) (>= lukumaara vastaajat))
        (response-or-404 (vastaajatunnus/muokkaa-lukumaaraa kyselykertaid vastaajatunnusid lukumaara))
        {:status 403})))

  (DELETE "/:kyselykertaid/tunnus/:vastaajatunnusid" []
    :path-params [kyselykertaid :- s/Int
                  vastaajatunnusid :- s/Int]
    :kayttooikeus [:vastaajatunnus-poisto kyselykertaid]
    (let [vastaajat (vastaajatunnus/laske-vastaajat vastaajatunnusid)]
      (if (zero? vastaajat)
        (do
          (vastaajatunnus/poista! kyselykertaid vastaajatunnusid)
          {:status 204})
        {:status 403})))

  (GET "/:kyselykertaid" []
    :path-params [kyselykertaid :- s/Int]
    :kayttooikeus :vastaajatunnus
    (response-or-404 (vastaajatunnus/hae-kyselykerralla kyselykertaid)))

  (GET "/:kyselykertaid/tutkinto" []
    :path-params [kyselykertaid :- s/Int]
    :kayttooikeus :vastaajatunnus
    (if-let [tutkinto (vastaajatunnus/hae-viimeisin-tutkinto kyselykertaid (:aktiivinen-koulutustoimija *kayttaja*))]
      (response-or-404 tutkinto)
      {:status 200})))