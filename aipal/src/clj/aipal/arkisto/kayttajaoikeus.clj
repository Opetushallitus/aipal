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

(ns aipal.arkisto.kayttajaoikeus
  (:require [korma.core :as sql]
            [korma.db :as db]
            [aipal.infra.kayttaja-arkisto :as kayttaja-arkisto]
            oph.korma.korma-auth)
  (:use [aipal.integraatio.sql.korma]))

(defn hae-oikeudet
  ([oid]
    (db/transaction
      (let [kayttaja (kayttaja-arkisto/hae oid)
            roolit (sql/select rooli-organisaatio
                     (sql/where {:kayttaja oid
                                 :voimassa true})
                     (sql/fields :rooli :organisaatio))]
        (assoc kayttaja :roolit roolit))))
  ([]
    (let [userid oph.korma.korma-auth/*current-user-uid*]
      (assert (realized? oph.korma.korma-auth/*current-user-oid*) (str "Ongelma sisäänkirjautumisessa. Käyttäjätunnuksella " userid " ei ole käyttöoikeuksia. (uid -> oid epäonnistui)."))
      (hae-oikeudet @oph.korma.korma-auth/*current-user-oid*))))

(defn hae-kyselylla
  [kyselyid kayttaja-oid] 
  (sql/exec-raw [(str "select  ytunnus, oid, organisaatio, rooli, kayttaja, voimassa, kyselyid, oppilaitos, toimipaikka from kysely_omistaja_view "
                   "where kyselyid = ? and kayttaja=?") [kyselyid kayttaja-oid]] 
             :results))
