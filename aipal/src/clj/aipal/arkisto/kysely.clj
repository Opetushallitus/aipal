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

(ns aipal.arkisto.kysely
  (:require [korma.core :as sql])
  (:use [aitu.integraatio.sql.korma]))

(defn hae-kaikki
  "Hae kaikki kyselyt"
  []
  (->
    (sql/select* kysely)
    (sql/fields :kysely.kyselyid :kysely.nimi_fi :kysely.nimi_sv :kysely.voimassa_alkupvm :kysely.voimassa_loppupvm)

    (sql/with kyselykerta
       (sql/fields :kyselykertaid :nimi_fi :nimi_sv :voimassa_alkupvm :voimassa_loppupvm))
    (sql/order :kysely.kyselyid :ASC)

    sql/exec))

(defn hae
  "Hakee kyselyn tiedot pääavaimella"
  [kyselyid]
  (->
    (sql/select* kysely)
    (sql/fields :kysely.kyselyid :kysely.nimi_fi :kysely.nimi_sv :kysely.voimassa_alkupvm :kysely.voimassa_loppupvm :kysely.selite_fi :kysely.selite_sv)
    (sql/where (= :kyselyid kyselyid))

    sql/exec
    first))
