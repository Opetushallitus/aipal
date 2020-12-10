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
    [oph.log :as aipallog]
    [oph.common.infra.common-audit-log :as common-audit-log]))

(def operaatiot {:poisto "poisto"
                 :lisays "lisäys"
                 :paivitys "päivitys"})

;; OPH-1966
(defn ^:private ->common-audit-log-json-entry
  "Logittaa OPH:n projektien yhteiseen audit-logiin"
  [tieto oid tieto-id operaatio tiedot-map]
  {:pre [(bound? #'ka/*kayttaja*)]}
  (let [data  {:operation   operaatio
               :user        {:oid (:oid ka/*kayttaja*)}
               :resource    (name tieto)
               :resourceOid oid
               :id          (str tieto-id)
               :delta       (reduce-kv
                              (fn [result k v] (conj result {:op (operaatio operaatiot) :path (name k) :value v}))
                              []
                              tiedot-map)}]
    (when (:session common-audit-log/*request-meta*)
      (common-audit-log/->audit-log-entry data))))


(defn ^:private kirjoita!
  ([tieto oid tieto-id operaatio]
   (kirjoita! tieto oid tieto-id operaatio {}))
  ([tieto oid tieto-id operaatio tiedot-map]
   {:pre [(contains? operaatiot operaatio)
          (keyword? tieto)
          (map? tiedot-map)]}
   (let [log-entry (->common-audit-log-json-entry tieto oid tieto-id operaatio tiedot-map)]
     (binding [aipallog/*lisaa-uid-ja-request-id?* false]
       (log/info log-entry)))))


(defn ohje-paivitys!
  [ohjetunniste]
  (kirjoita! :ohje nil ohjetunniste :paivitys
    {:ohjetunniste ohjetunniste}))

(defn kysely-muokkaus!
  ([kyselyid]
   (kirjoita! :kysely nil kyselyid :paivitys {:kyselyid kyselyid}))
  ([kyselyid tilamuutos]
   (kirjoita! :kysely nil kyselyid :paivitys {:kyselyid kyselyid
                                              :tila tilamuutos})))

(defn kysely-poisto!
  [kyselyid]
  (kirjoita! :kysely nil kyselyid :poisto {:kyselyid kyselyid}))

(defn kysely-luonti!
  [kyselyid nimi_fi]
  (kirjoita! :kysely nil kyselyid :lisays {:kyselyid kyselyid
                                           :nimi_fi nimi_fi}))

(defn kyselypohja-muokkaus!
  ([kyselypohjaid]
   (kirjoita! :kyselypohja nil kyselypohjaid :paivitys {:kyselypohjaid kyselypohjaid}))
  ([kyselypohjaid tilamuutos]
   (kirjoita! :kyselypohja nil kyselypohjaid :paivitys {:kyselypohjaid kyselypohjaid
                                                        :tila tilamuutos})))

(defn kyselypohja-luonti!
  [kyselypohjaid nimi]
  (kirjoita! :kyselypohja nil kyselypohjaid :lisays {:nimi nimi}))

(defn kyselypohja-poisto!
  [kyselypohjaid]
  (kirjoita! :kyselypohja nil kyselypohjaid :poisto {:kyselypohjaid kyselypohjaid}))

(defn kysymysryhma-muokkaus!
  ([kysymysryhmaid]
   (kirjoita! :kysymysryhma nil kysymysryhmaid :paivitys {:kysymysryhmaid kysymysryhmaid}))
  ([kysymysryhmaid tilamuutos]
   (kirjoita! :kysymysryhma nil kysymysryhmaid :paivitys {:kysymysryhmaid kysymysryhmaid
                                                          :tila tilamuutos})))

(defn kysymysryhma-luonti!
  [kysymysryhmaid nimi]
  (kirjoita! :kysymysryhma nil kysymysryhmaid :lisays {:kysymysryhmaid kysymysryhmaid
                                                       :nimi nimi}))

(defn kysymysryhma-poisto!
  [kysymysryhmaid]
  (kirjoita! :kysymysryhma nil kysymysryhmaid :poisto {:kysymysryhmaid kysymysryhmaid}))

(defn kysymys-poisto!
  [kysymysid]
  (kirjoita! :kysymys nil kysymysid :poisto {:kysymysid kysymysid}))

(defn kysymys-monivalinnat-poisto!
  [kysymysid]
  (kirjoita! :kysymys nil kysymysid :poisto {:kysymysid kysymysid
                                             :monivalinnat true}))

(defn kysymys-monivalinnat-luonti!
  [kysymysid]
  (kirjoita! :kysymys nil kysymysid :lisays {:kysymysid kysymysid
                                             :monivalinnat true}))

(defn kysymys-muokkaus!
  [kysymysid]
  (kirjoita! :kysymys nil kysymysid :paivitys {:kysymysid kysymysid}))

(defn kysymys-luonti!
  [kysymysryhmaid kysymysid]
  (kirjoita! :kysymys nil kysymysid :lisays {:kysymysid kysymysid
                                             :kysymysryhmaid kysymysryhmaid}))

(defn kyselykerta-muokkaus!
  ([kyselykertaid]
   (kirjoita! :kyselykerta nil kyselykertaid :paivitys {:kyselykertaid kyselykertaid}))
  ([kyselykertaid tilamuutos]
   (kirjoita! :kyselykerta nil kyselykertaid :paivitys {:kyselykertaid kyselykertaid
                                                        :tila tilamuutos})))

(defn kyselykerta-luonti!
  [kyselykertaid kyselyid nimi]
  (kirjoita! :kyselykerta nil kyselykertaid :lisays {:kyselyid kyselyid
                                                     :nimi nimi}))

(defn kyselykerta-poisto! [kyselykertaid]
  (kirjoita! :kyselykerta nil kyselykertaid :poisto {:kyselykertaid kyselykertaid}))

(defn vastaajatunnus-luonti!
  ([vastaajatunnusid vastaajatunnus kyselykertaid]
   (kirjoita! :vastaajatunnus nil vastaajatunnusid :lisays {:kyselykertaid kyselykertaid
                                                            :tunnus vastaajatunnus})))

(defn vastaajatunnus-muokkaus!
  [vastaajatunnusid kyselykertaid lukittu-tila]
  (kirjoita! :vastaajatunnus nil vastaajatunnusid :paivitys {:kyselykertaid kyselykertaid
                                                             :vastaajatunnusid vastaajatunnusid
                                                             :lukittu lukittu-tila}))

(defn vastaajatunnus-poisto!
  [vastaajatunnusid kyselykertaid]
  (kirjoita! :vastaajatunnus nil vastaajatunnusid :poisto {:kyselykertaid kyselykertaid
                                                           :vastaajatunnusid vastaajatunnusid}))

(defn tiedote-operaatio!
  [tiedoteid operaatio]
  {:pre [(contains? operaatiot operaatio)]}
  (kirjoita! :tiedote nil tiedoteid operaatio))
