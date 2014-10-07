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
            [aipal.arkisto.kysely :as arkisto]
            [aipal.toimiala.kayttajaoikeudet :refer [yllapitaja?]]
            [aipal.rest-api.kyselykerta :refer [paivita-arvot]]
            [aipal.rest-api.kysymysryhma :refer [jarjesta-alkiot]]
            [oph.common.util.http-util :refer [json-response parse-iso-date]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(defn lisaa-kysymysryhma!
  [kyselyid kysymysryhma]
  (doseq [kysymys (:kysymys kysymysryhma)
          :when (not (:poistettu kysymys))]
    (arkisto/lisaa-kysymys! kyselyid (:kysymysid kysymys)))
  (arkisto/lisaa-kysymysryhma! kyselyid kysymysryhma))

(defn paivita-kysely!
  [kysely]
  (arkisto/poista-kysymysryhmat! (:kyselyid kysely))
  (arkisto/poista-kysymykset! (:kyselyid kysely))
  (doseq [kysymysryhma (jarjesta-alkiot (:kysymysryhmat kysely))]
    (lisaa-kysymysryhma! (:kyselyid kysely) kysymysryhma))
  (arkisto/muokkaa-kyselya kysely))

(c/defroutes reitit
  (cu/defapi :kysely nil :get "/" []
    (json-response (if (yllapitaja?)
                     (arkisto/hae-kaikki)
                     (arkisto/hae-kaikki (:voimassaoleva-oid *kayttaja*)))))

  (cu/defapi :kysely-luonti nil :post "/" []
    (json-response (arkisto/lisaa! {:nimi_fi "Uusi kysely"
                                   :koulutustoimija (:voimassaoleva-organisaatio *kayttaja*)})))

  (cu/defapi :kysely-luku kyselyid :get "/:kyselyid" [kyselyid]
    (json-response (let [kysely (arkisto/hae (Integer/parseInt kyselyid))]
                     (assoc kysely :kysymysryhmat (arkisto/hae-kysymysryhmat (Integer/parseInt kyselyid))))))

  (cu/defapi :kysely-muokkaus kyselyid :post "/:kyselyid" [kyselyid & kysely]
    (json-response
      (paivita-kysely! (paivita-arvot (assoc kysely :kyselyid (Integer/parseInt kyselyid)) [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date))))

  (cu/defapi :kysely-muokkaus kyselyid :post "/:kyselyid/lisaa-kyselypohja/:kyselypohjaid" [kyselyid kyselypohjaid]
    (json-response (arkisto/lisaa-kyselypohja (Integer/parseInt kyselyid) (Integer/parseInt kyselypohjaid))))

  (cu/defapi :kysely-luku kyselyid :get "/:kyselyid/kysymysryhmat" [kyselyid]
    (json-response (arkisto/hae-kysymysryhmat (Integer/parseInt kyselyid))))

  (cu/defapi :kysely-muokkaus kyselyid :delete "/:kyselyid/poista-kysymys/:kysymysid" [kyselyid kysymysid]
    (json-response
      (arkisto/poista-kysymys (Integer/parseInt kyselyid) (Integer/parseInt kysymysid))))

  (cu/defapi :kysely-muokkaus kyselyid :post "/:kyselyid/palauta-kysymys/:kysymysid" [kyselyid kysymysid]
    (json-response
      (arkisto/palauta-kysymys (Integer/parseInt kyselyid) (Integer/parseInt kysymysid))))

  (cu/defapi :kysely-muokkaus kyselyid :post "/:kyselyid" [kyselyid & kysely]
    (json-response
      (arkisto/muokkaa-kyselya (paivita-arvot (assoc kysely :kyselyid (Integer/parseInt kyselyid)) [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)))))
