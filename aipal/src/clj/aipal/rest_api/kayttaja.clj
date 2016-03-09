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

(ns aipal.rest-api.kayttaja
  (:require [compojure.api.core :refer [defroutes GET POST]]
            [schema.core :as s]
            [aipal.arkisto.kayttaja :as arkisto]
            [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
            aipal.compojure-util
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [oph.common.util.http-util :refer [response-or-404]]))

(defroutes reitit
  (POST "/impersonoi" [:as {session :session}, oid]
    :kayttooikeus :impersonointi
    {:status 200
     :session (assoc session :impersonoitu-oid oid)})

  (POST "/lopeta-impersonointi" {session :session}
    :kayttooikeus :impersonointi-lopetus
    {:status 200
     :session (dissoc session :impersonoitu-oid)})

  (POST "/rooli" {{rooli :rooli_organisaatio_id} :params
                  session :session}
    :kayttooikeus :roolin-valinta
    {:status 200
     :session (assoc session :rooli rooli)})
  (GET "/impersonoitava" [termi]
    :kayttooikeus :impersonointi
    :query-params [termi :- s/Str]
    (response-or-404 (arkisto/hae-impersonoitava-termilla termi)))
  (GET "/" []
    :kayttooikeus :omat_tiedot
    (let [oikeudet (kayttajaoikeus-arkisto/hae-oikeudet (:aktiivinen-oid *kayttaja*))]
      (response-or-404 (assoc oikeudet :impersonoitu_kayttaja (:impersonoidun-kayttajan-nimi *kayttaja*)
                                       :aktiivinen_rooli (:aktiivinen-rooli *kayttaja*)))))
  (GET "/:oid" []
    :path-params [oid :- s/Str]
    :kayttooikeus [:kayttajan_tiedot oid]
    true))
