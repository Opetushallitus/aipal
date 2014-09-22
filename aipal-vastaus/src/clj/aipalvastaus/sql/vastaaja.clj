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

(ns aipalvastaus.sql.vastaaja
  (:require [korma.core :as sql]))

(defn validoi-vastaajatunnus
  [vastaajatunnus]
  (->
    (sql/select* :vastaajatunnus)
    (sql/fields :lukittu)
    (sql/where {:tunnus vastaajatunnus})
    sql/exec
    first
    :lukittu
    false?))

(defn luo-vastaaja!
  [vastaustunnus]
  (->
    (sql/exec-raw [(str "INSERT INTO vastaaja(kyselykertaid, vastaajatunnusid)"
                        " SELECT kyselykertaid, vastaajatunnusid"
                        " FROM vastaajatunnus"
                        " WHERE tunnus = ?"
                        " RETURNING vastaajaid") [vastaustunnus]] :results)
    first
    :vastaajaid))

(defn paivata-vastaaja! [vastaajaid]
  (->
    (sql/update* :vastaaja)
    (sql/set-fields {:vastannut true})
    (sql/where {:vastaajaid vastaajaid})
    sql/exec))

(defn validoi-vastaajaid
  [vastaustunnus vastaajaid]
  (->
    (sql/select* :vastaaja)
    (sql/fields :vastannut)
    (sql/join :vastaajatunnus (= :vastaajatunnus.vastaajatunnusid :vastaajatunnusid))
    (sql/where {:vastaajaid vastaajaid
                :vastaajatunnus.tunnus vastaustunnus})
    sql/exec
    first
    :vastannut
    (= false)))
