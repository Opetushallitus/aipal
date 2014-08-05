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

(ns aipal.sql.test-data-util
  (:require [aipal.arkisto.vastaajatunnus]
    [aipal.arkisto.kysely]
    [aipal.arkisto.kyselykerta]
    [clj-time.core :as time]
    [clj-time.core :as ctime]
    [korma.core :as sql]
    [oph.korma.korma :refer [joda-datetime->sql-timestamp]]
  ))

(def  default-tutkinto 
  {:tutkintotunnus "123456"
   :nimi_fi "Autoalan perustutkinto"})

(def default-koulutustoimija
  {:ytunnus "1234567-8"
   :nimi_fi "Pörsänmäen opistokeskittymä"})

(defn lisaa-tutkinto! 
  ([tutkinto]
    (let [t (merge default-tutkinto tutkinto)]
      (sql/exec-raw [(str "insert into tutkinto("
                     "tutkintotunnus,"
                     "nimi_fi " 
                     ")values("
                     "?,"
                     "? " 
                     ")")
                    (map t [:tutkintotunnus :nimi_fi])])
      t
      ))
  ([] (lisaa-tutkinto! default-tutkinto)))

(defn lisaa-koulutustoimija! 
  ([koulutustoimija]
    (let [t (merge default-koulutustoimija koulutustoimija)]
      (sql/exec-raw [(str "insert into koulutustoimija("
                     "ytunnus,"
                     "nimi_fi " 
                     ")values("
                     "?,"
                     "? " 
                     ")")
                    (map t [:ytunnus :nimi_fi])])
      t
      ))
  ([] lisaa-koulutustoimija! default-koulutustoimija))

(defn lisaa-kysely!
  []
  (let [koulutustoimija (lisaa-koulutustoimija! default-koulutustoimija)]
    (aipal.arkisto.kysely/lisaa! {:nimi_fi "oletuskysely, testi"
                                  :koulutustoimija (:ytunnus koulutustoimija)
                                  })))

(defn lisaa-kyselykerta!
  []
  (let [kysely (lisaa-kysely!)]
    (aipal.arkisto.kyselykerta/lisaa! (:kyselyid kysely) {:kyselyid (:kyselyid kysely)
                                                          :nimi_fi "oletuskyselykerta, testi"
                                                          :voimassa_alkupvm (joda-datetime->sql-timestamp (ctime/now))
                                                          :voimassa_loppupvm (joda-datetime->sql-timestamp (ctime/now))
                                                          })))
