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
            [aipal.toimiala.kayttajaoikeudet :refer [yllapitaja? kysymysryhma-luku?]]
            [aipal.rest-api.kyselykerta :refer [paivita-arvot]]
            [aipal.rest-api.kysymysryhma :refer [lisaa-jarjestys]]
            [oph.common.util.http-util :refer [json-response parse-iso-date]]
            [oph.common.util.util :refer [map-by]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(defn lisaa-kysymysryhma!
  [kyselyid kysymysryhma]
  (let [kayttajan-kysymykset (map-by :kysymysid (:kysymys kysymysryhma))]
    (doseq [kysymys (arkisto/hae-kysymysten-poistettavuus (:kysymysryhmaid kysymysryhma))
            :let [kysymysid (:kysymysid kysymys)
                  kayttajan-kysymys (get kayttajan-kysymykset kysymysid)]
            :when (not (and (:poistettu kayttajan-kysymys)
                            (:poistettava kysymys)))]
      ;; Assertiin pitäisi törmätä vain jos käyttäjä on muokannut poistetuksi kysymyksen jota ei saisi poistaa
      (assert (not (:poistettu kayttajan-kysymys)))
      (arkisto/lisaa-kysymys! kyselyid kysymysid)))
  (arkisto/lisaa-kysymysryhma! kyselyid kysymysryhma))

(defn paivita-kysely!
  [kysely]
  (arkisto/poista-kysymysryhmat! (:kyselyid kysely))
  (arkisto/poista-kysymykset! (:kyselyid kysely))
  (doseq [kysymysryhma (lisaa-jarjestys (:kysymysryhmat kysely))]
    (assert (kysymysryhma-luku? (:kysymysryhmaid kysymysryhma)))
    (lisaa-kysymysryhma! (:kyselyid kysely) kysymysryhma))
  (arkisto/muokkaa-kyselya kysely))

(c/defroutes reitit
  (cu/defapi :kysely nil :get "/" []
    (json-response (arkisto/hae-kaikki (:aktiivinen-koulutustoimija *kayttaja*))))

  (cu/defapi :kysely-luonti nil :post "/" [& kysely]
    (let [kysely (paivita-arvot kysely
                                [:voimassa_alkupvm :voimassa_loppupvm]
                                parse-iso-date)]
      (json-response
        (let [{:keys [kyselyid]}
              (arkisto/lisaa! (-> kysely
                                (select-keys [:nimi_fi :nimi_sv :selite_fi :selite_sv :voimassa_alkupvm :voimassa_loppupvm :tila])
                                (assoc :koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*))))]
          (paivita-kysely! (assoc kysely :kyselyid kyselyid))))))

  (cu/defapi :kysely-muokkaus kyselyid :post "/:kyselyid" [kyselyid & kysely]
    (json-response
      (paivita-kysely! (paivita-arvot (assoc kysely :kyselyid (Integer/parseInt kyselyid))
                                      [:voimassa_alkupvm :voimassa_loppupvm]
                                      parse-iso-date))))

  (cu/defapi :kysely-luku kyselyid :get "/:kyselyid" [kyselyid]
    (json-response (when-let [kysely (arkisto/hae (Integer/parseInt kyselyid))]
                     (assoc kysely :kysymysryhmat (arkisto/hae-kysymysryhmat (Integer/parseInt kyselyid))))))

  (cu/defapi :kysely-luku kyselyid :get "/:kyselyid/kysymysryhmat" [kyselyid]
    (json-response (arkisto/hae-kysymysryhmat (Integer/parseInt kyselyid))))

  (cu/defapi :kysely-tilamuutos kyselyid :put "/julkaise/:kyselyid" [kyselyid]
    (let [kyselyid (Integer/parseInt kyselyid)]
      (if (> (arkisto/laske-kysymysryhmat kyselyid) 0)
        (json-response (arkisto/julkaise-kysely kyselyid))
        {:status 403})))

  (cu/defapi :kysely-tilamuutos kyselyid :put "/sulje/:kyselyid" [kyselyid]
    (json-response (arkisto/sulje-kysely (Integer/parseInt kyselyid))))

  (cu/defapi :kysely-tilamuutos kyselyid :put "/palauta/:kyselyid" [kyselyid]
    (json-response (arkisto/julkaise-kysely (Integer/parseInt kyselyid)))))
