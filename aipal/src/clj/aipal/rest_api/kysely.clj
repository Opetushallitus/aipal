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

(ns aipal.rest-api.kysely
  (:require [compojure.core :as c]
            [korma.db :as db]
            [schema.core :as schema]
            [aipal.compojure-util :as cu]
            [aipal.arkisto.kayttaja :as kayttaja]
            [aipal.arkisto.kysely :as kysely]
            [aipal.rest-api.kyselykerta :refer [paivita-arvot]]
            [oph.common.util.http-util :refer [json-response parse-iso-date]]
            [oph.korma.korma-auth :refer [*current-user-oid*]]))

(c/defroutes reitit
  (cu/defapi :kysely nil :get "/" []
    (json-response (kysely/hae-kaikki)))

  (cu/defapi :kysely nil :post "/" []
    (json-response (kysely/lisaa! {:nimi_fi "Uusi kysely"
                                   :koulutustoimija (kayttaja/hae-organisaatio @*current-user-oid*)})))

  (cu/defapi :kysely nil :get "/:kyselyid" [kyselyid] 
    (json-response (let [kysely (kysely/hae (Integer/parseInt kyselyid))]
                     (assoc kysely :kysymysryhmat (kysely/hae-kysymysryhmat (Integer/parseInt kyselyid))))))

  (c/POST "/:kyselyid" [kyselyid & kysely]
          (db/transaction
            (json-response
              (kysely/muokkaa-kyselya (paivita-arvot (assoc kysely :kyselyid (Integer/parseInt kyselyid)) [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)))))

  (c/POST "/:kyselyid/lisaa-kyselypohja/:kyselypohjaid" [kyselyid kyselypohjaid]
          (db/transaction
            (json-response (kysely/lisaa-kyselypohja (Integer/parseInt kyselyid) (Integer/parseInt kyselypohjaid)))))

  (c/GET "/:kyselyid/kysymysryhmat" [kyselyid]
         (db/transaction
           (json-response (kysely/hae-kysymysryhmat (Integer/parseInt kyselyid)))))

  (cu/defapi :kysely nil :delete "/:kyselyid/poista-kysymys/:kysymysid" [kyselyid kysymysid]
    (json-response
      (kysely/poista-kysymys (Integer/parseInt kyselyid) (Integer/parseInt kysymysid))))

  (cu/defapi :kysely nil :post "/:kyselyid/palauta-kysymys/:kysymysid" [kyselyid kysymysid]
    (json-response
      (kysely/palauta-kysymys (Integer/parseInt kyselyid) (Integer/parseInt kysymysid))))

  (cu/defapi :kysely nil :post "/:kyselyid" [kyselyid & kysely]
    (json-response
      (kysely/muokkaa-kyselya (paivita-arvot (assoc kysely :kyselyid (Integer/parseInt kyselyid)) [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)))))
