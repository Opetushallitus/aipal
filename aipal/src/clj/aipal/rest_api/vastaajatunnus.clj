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
  {:kyselykertaid kyselykertaid
   :tunnusten-lkm (if (:henkilokohtainen vastaajatunnus) (:vastaajien_lkm vastaajatunnus) 1)
   :vastaajien_lkm (if (:henkilokohtainen vastaajatunnus) 1 (:vastaajien_lkm vastaajatunnus))
   :tutkinto (get-in vastaajatunnus [:tutkinto :tutkintotunnus])
   :kieli (:suorituskieli vastaajatunnus)
   :kunta (get-in vastaajatunnus [:koulutuksen_toimipaikka :kunta])
   :koulutusmuoto (:koulutusmuoto vastaajatunnus)
   :valmistavan_koulutuksen_jarjestaja (get-in vastaajatunnus [:koulutuksen_jarjestaja :ytunnus])
   :valmistavan_koulutuksen_oppilaitos (get-in vastaajatunnus [:koulutuksen_jarjestaja_oppilaitos :oppilaitoskoodi])
   :toimipaikka (get-in vastaajatunnus [:koulutuksen_toimipaikka :toimipaikkakoodi])
   :voimassa_alkupvm (:voimassa_alkupvm vastaajatunnus)
   :voimassa_loppupvm (:voimassa_loppupvm vastaajatunnus)
   :haun_numero (:haun_numero vastaajatunnus)
   :henkilonumero (:henkilonumero vastaajatunnus)
   :hankintakoulutuksen_toteuttaja (get-in vastaajatunnus [:hankintakoulutuksen_toteuttaja :ytunnus])
   :tutkintomuoto (:tutkintomuoto vastaajatunnus)})

(defroutes reitit
  (POST "/:kyselykertaid" []
    :path-params [kyselykertaid :- s/Int]
    :body [vastaajatunnus s/Any]
    :kayttooikeus [:vastaajatunnus {:kyselykertaid kyselykertaid}]
    (let [vastaajatunnus (-> vastaajatunnus
                           (ui->vastaajatunnus kyselykertaid)
                           (paivita-arvot [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date))]
      (response-or-404 (vastaajatunnus/lisaa! vastaajatunnus))))

  (POST "/:kyselykertaid/tunnus/:vastaajatunnusid/lukitse" []
    :path-params [kyselykertaid :- s/Int
                  vastaajatunnusid :- s/Int]
    :body-params [lukitse :- Boolean]
    :kayttooikeus [:vastaajatunnus {:kyselykertaid kyselykertaid}]
    (response-or-404 (vastaajatunnus/aseta-lukittu! kyselykertaid vastaajatunnusid lukitse)))

  (POST "/:kyselykertaid/tunnus/:vastaajatunnusid/muokkaa-lukumaaraa" []
    :path-params [kyselykertaid :- s/Int
                  vastaajatunnusid :- s/Int]
    :body-params [lukumaara :- s/Int]
    :kayttooikeus [:vastaajatunnus {:kyselykertaid kyselykertaid}]
    (let [vastaajatunnus (vastaajatunnus/hae kyselykertaid vastaajatunnusid)
          vastaajat (vastaajatunnus/laske-vastaajat vastaajatunnusid)]
      (when-not (:muokattavissa vastaajatunnus)
        (throw (IllegalArgumentException. "Vastaajatunnus ei ole enää muokattavissa")))
      (if (and (pos? lukumaara) (>= lukumaara vastaajat))
        (response-or-404 (vastaajatunnus/muokkaa-lukumaaraa! kyselykertaid vastaajatunnusid lukumaara))
        {:status 403})))

  (DELETE "/:kyselykertaid/tunnus/:vastaajatunnusid" []
    :path-params [kyselykertaid :- s/Int
                  vastaajatunnusid :- s/Int]
    :kayttooikeus [:vastaajatunnus {:kyselykertaid kyselykertaid}]
    (let [vastaajat (vastaajatunnus/laske-vastaajat vastaajatunnusid)]
      (if (and (zero? vastaajat) (vastaajatunnus/tunnus-poistettavissa? kyselykertaid vastaajatunnusid))
        (do
          (vastaajatunnus/poista! kyselykertaid vastaajatunnusid)
          {:status 204})
        {:status 403})))

  (GET "/:kyselykertaid" []
    :path-params [kyselykertaid :- s/Int]
    :query-params [{omat :- s/Bool false}]
    :kayttooikeus :katselu
    (response-or-404 (vastaajatunnus/hae-kyselykerralla kyselykertaid omat)))

  (GET "/:kyselykertaid/tutkinto" []
    :path-params [kyselykertaid :- s/Int]
    :kayttooikeus :katselu
    (if-let [tutkinto (vastaajatunnus/hae-viimeisin-tutkinto kyselykertaid (:aktiivinen-koulutustoimija *kayttaja*))]
      (response-or-404 tutkinto)
      {:status 200})))