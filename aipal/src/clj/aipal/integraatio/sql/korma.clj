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

(ns aipal.integraatio.sql.korma
  (:require
    [korma.core :as sql]
    [oph.korma.korma  :refer [defentity]]))

(declare kysymys)

(defentity kyselykerta
  (sql/pk :kyselykertaid))

(defentity kysely
  (sql/pk :kyselyid)
  (sql/has-many kyselykerta {:fk :kyselyid}))

(defentity kysymysryhma
  (sql/pk :kysymysryhmaid)
  (sql/has-many kysymys {:fk :kysymysryhmaid}))

(defentity kysely_kysymysryhma)

(defentity kysely_kysymys)

(defentity kysymys
  (sql/pk :kysymysid)
  (sql/belongs-to kysymysryhma {:fk :kysymysryhmaid}))

(defentity vastaajatunnus
  (sql/pk :vastaajatunnusid))

(defentity rooli-organisaatio
  (sql/table :rooli_organisaatio))

(defentity kayttaja
  (sql/pk :oid))

(defentity tutkinto
  (sql/pk :tutkintotunnus))

(defentity koulutustoimija
  (sql/pk :ytunnus))
