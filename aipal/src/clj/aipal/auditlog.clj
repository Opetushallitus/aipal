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

(ns aipal.auditlog
  "Audit lokituksen abstrahoiva rajapinta.
  Aipalin osalta halutaan lokittaa päivitysoperaatiot."
  (:require
    [aipal.infra.kayttaja :as ka]
    [clojure.tools.logging :as log]
    [oph.log :as aipallog]))

(def operaatiot {:poisto "poisto"
                 :lisays "lisäys"
                 :paivitys "päivitys"})

(defn ^:private kirjoita!
  ([tieto operaatio tiedot-map]
  {:pre [(bound? #'ka/*kayttaja*),
         (contains? operaatiot operaatio)
         (keyword? tieto)
         (map? tiedot-map)]}
  (let [uid (:uid ka/*kayttaja*)
        msg (str "uid: " uid " oper: " (operaatio operaatiot) " kohde: " (name tieto) " meta: (" tiedot-map ")")]
    (binding [aipallog/*lisaa-uid-ja-request-id?* false]
      (log/info msg))))
  ([tieto operaatio]
    (kirjoita! tieto operaatio {})))

(defn ohje-paivitys!
  [ohjetunniste]
  (kirjoita! :ohje :paivitys
    {:ohjetunniste ohjetunniste}))

(defn kysely-muokkaus!
  ([kyselyid]
    (kirjoita! :kysely :paivitys {:kyselyid kyselyid}))
  ([kyselyid tilamuutos]
    (kirjoita! :kysely :paivitys {:kyselyid kyselyid
                                  :tila tilamuutos})))

(defn kysely-luonti!
  [kyselyid nimi_fi]
  (kirjoita! :kysely :lisays {:kyselyid kyselyid
                              :nimi_fi nimi_fi}))

(defn kyselypohja-muokkaus!
  [kyselypohjaid]
  (kirjoita! :kyselypohja :paivitys {:kyselypohjaid kyselypohjaid}))

(defn kysymysryhma-muokkaus!
  [kysymysryhmaid]
  (kirjoita! :kysymysryhma :paivitys {:kysymysryhmaid kysymysryhmaid}))

(defn kysymys-muokkaus!
  [kysymysid]
  (kirjoita! :kysymys :paivitys {:kysymysid kysymysid}))

(defn kyselykerta-muokkaus!
  ([kyselykertaid]
    (kirjoita! :kyselykerta :paivitys {:kyselykertaid kyselykertaid}))
  ([kyselykertaid tilamuutos]
    (kirjoita! :kyselykerta :paivitys {:kyselykertaid kyselykertaid
                                       :tila tilamuutos})))

(defn kyselykerta-luonti!
  [kyselyid nimi]
  (kirjoita! :kyselykerta :lisays {:kyselyid kyselyid
                                   :nimi nimi}))

(defn vastaajatunnus-luonti!
  [kyselykertaid]
  (kirjoita! :vastaajatunnus :lisays {:kyselykertaid kyselykertaid}))

(defn vastaajatunnus-lukitus!
  [kyselykertaid]
  (kirjoita! :vastaajatunnus :paivitys {:kyselykertaid kyselykertaid}))


