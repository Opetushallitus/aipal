;; Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
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

(ns aipal-e2e.arkisto.sql.korma
  (:require korma.db
            [korma.core :as sql]
            [oph.korma.common :refer [defentity korma-asetukset]]))

(defn datasource
  [db-asetukset]
  (let [korma-pool (:datasource (korma.db/connection-pool (korma.db/postgres (korma-asetukset db-asetukset))))]
    (.setCheckoutTimeout korma-pool 2000)
    (.setTestConnectionOnCheckout korma-pool true)
    {:make-pool? false
     :datasource korma-pool}))

(defn luo-db [db-asetukset]
  (korma.db/default-connection
    (korma.db/create-db (datasource db-asetukset))))

(defentity kayttaja)
(defentity kysely)
(defentity kyselykerta)
(defentity kysymysryhma)
(defentity kysymys)
(defentity kyselypohja)
(defentity kysely_kysymysryhma)
(defentity kysely_kysymys)
(defentity monivalintavaihtoehto)
(defentity rooli_organisaatio)
(defentity vastaajatunnus)
(defentity vastaaja)
(defentity vastaus)
(defentity koulutustoimija)
(defentity koulutustoimija_ja_tutkinto)
