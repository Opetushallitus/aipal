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

(ns aipal.arkisto.kyselykerta
  (:require [korma.core :as sql]
            [aipal.integraatio.sql.korma :as taulut]))

(defn hae-kaikki
  "Hae kaikki kyselykerrat"
  []
  (->
    (sql/select* taulut/kyselykerta)
    (sql/fields :kyselyid :kyselykertaid :nimi_fi :nimi_sv :voimassa_alkupvm :voimassa_loppupvm)
    (sql/order :kyselykerta.kyselykertaid :ASC)

    sql/exec))

(defn lisaa!
  [kyselyid kyselykerta-data]
  (sql/insert taulut/kyselykerta
    (sql/values {:kyselyid kyselyid
                 :nimi_fi (:nimi_fi kyselykerta-data)
                 :voimassa_alkupvm (:voimassa_alkupvm kyselykerta-data)
                 :voimassa_loppupvm (:voimassa_loppupvm kyselykerta-data)})))

(defn hae-yksi
  "Hae kyselykerta tunnuksella"
  [kyselykertaid]
  (->
    (sql/select* taulut/kyselykerta)
    (sql/fields :kyselyid :kyselykertaid :nimi_fi :nimi_sv :voimassa_alkupvm :voimassa_loppupvm)
    (sql/where (= :kyselykertaid kyselykertaid))
    (sql/order :kyselykerta.kyselykertaid :ASC)
    sql/exec
    first))
