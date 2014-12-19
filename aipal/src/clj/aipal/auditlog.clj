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

(defn kysely-poisto!
  [kyselyid]
  (kirjoita! :kysely :poisto {:kyselyid kyselyid}))

(defn kysely-luonti!
  [kyselyid nimi_fi]
  (kirjoita! :kysely :lisays {:kyselyid kyselyid
                              :nimi_fi nimi_fi}))

(defn kyselypohja-muokkaus!
  ([kyselypohjaid]
    (kirjoita! :kyselypohja :paivitys {:kyselypohjaid kyselypohjaid}))
  ([kyselypohjaid tilamuutos]
    (kirjoita! :kyselypohja :paivitys {:kyselypohjaid kyselypohjaid
                                       :tila tilamuutos})))

(defn kyselypohja-luonti!
  [nimi]
  (kirjoita! :kyselypohja :lisays {:nimi nimi}))

(defn kyselypohja-poisto!
  [kyselypohjaid]
  (kirjoita! :kyselypohja :poisto {:kyselypohjaid kyselypohjaid}))

(defn kysymysryhma-muokkaus!
  ([kysymysryhmaid]
    (kirjoita! :kysymysryhma :paivitys {:kysymysryhmaid kysymysryhmaid}))
  ([kysymysryhmaid tilamuutos]
    (kirjoita! :kysymysryhma :paivitys {:kysymysryhmaid kysymysryhmaid
                                        :tila tilamuutos})))

(defn kysymysryhma-luonti!
  [kysymysryhmaid nimi]
  (kirjoita! :kysymysryhma :lisays {:kysymysryhmaid kysymysryhmaid
                                    :nimi nimi}))

(defn kysymysryhma-poisto!
  [kysymysryhmaid]
  (kirjoita! :kysymysryhma :poisto {:kysymysryhmaid kysymysryhmaid}))

(defn kysymys-poisto!
  [kysymysid]
  (kirjoita! :kysymys :poisto {:kysymysid kysymysid}))

(defn kysymys-monivalinnat-poisto!
  [kysymysid]
  (kirjoita! :kysymys :poisto {:kysymysid kysymysid
                               :monivalinnat true}))

(defn kysymys-monivalinnat-luonti!
  [kysymysid]
  (kirjoita! :kysymys :lisays {:kysymysid kysymysid
                               :monivalinnat true}))

(defn jatkokysymys-poisto!
  [jatkokysymysid]
  (kirjoita! :jatkokysymys :poisto {:jatkokysymysid jatkokysymysid}))

(defn jatkokysymys-luonti!
  [jatkokysymysid]
  (kirjoita! :jatkokysymys :lisays {:jatkokysymysid jatkokysymysid}))

(defn kysymys-muokkaus!
  [kysymysid]
  (kirjoita! :kysymys :paivitys {:kysymysid kysymysid}))

(defn kysymys-luonti!
  [kysymysryhmaid kysymysid]
  (kirjoita! :kysymys :lisays {:kysymysid kysymysid
                               :kysymysryhmaid kysymysryhmaid}))

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
  ([kyselykertaid]
    (kirjoita! :vastaajatunnus :lisays {:kyselykertaid kyselykertaid}))
  ([vastaajatunnus kyselykertaid]
    (kirjoita! :vastaajatunnus :lisays {:kyselykertaid kyselykertaid
                                        :tunnus vastaajatunnus})))

(defn vastaajatunnus-muokkaus!
  [vastaajatunnusid kyselykertaid lukittu-tila]
  (kirjoita! :vastaajatunnus :paivitys {:kyselykertaid kyselykertaid
                                        :vastaajatunnusid vastaajatunnusid
                                        :lukittu lukittu-tila}))

(defn tiedote-operaatio!
  [operaatio]
  {:pre [(contains? operaatiot operaatio)]}
  (kirjoita! :tiedote operaatio))
