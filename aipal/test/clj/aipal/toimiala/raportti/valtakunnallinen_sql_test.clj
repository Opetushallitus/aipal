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

(ns aipal.toimiala.raportti.valtakunnallinen-sql-test
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [korma.core :as sql]
            [clj-time.coerce :as time-coerce]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [aipal.sql.test-util :refer :all]
            [aipal.toimiala.raportti.valtakunnallinen :refer :all]))

(use-fixtures :each tietokanta-fixture)

(defn date-in-sql [date]
  (if date (str " date '" date "' ") " null::date "))

(defn parse-to-sql-date [date]
  (when date
    (time-coerce/to-sql-date (time-format/parse (time-format/formatters :date) date))))

(deftest ^:integraatio rajaa-aikavalille-testi
  (are [alkupvm loppupvm vastaajatunnus odotettu-vastaajatunnusten-maara]
       (=
         (let [voimassa_alkupvm (date-in-sql (:voimassa_alkupvm vastaajatunnus))
               voimassa_loppupvm (date-in-sql (:voimassa_loppupvm vastaajatunnus))]
           (->
             (sql/select* [(sql/raw (str "(select "
                                         voimassa_alkupvm " as voimassa_alkupvm, "
                                         voimassa_loppupvm " as voimassa_loppupvm) ")) :vastaajatunnus])
             (rajaa-aikavalille [:vastaajatunnus.voimassa_alkupvm :vastaajatunnus.voimassa_loppupvm]
                                [(parse-to-sql-date alkupvm) (parse-to-sql-date loppupvm)])
             sql/exec
             count))
         odotettu-vastaajatunnusten-maara)

       "2013-01-01" "2013-12-31" {:voimassa_alkupvm "2012-10-01" :voimassa_loppupvm "2012-12-31"} 0
       "2013-01-01" "2013-12-31" {:voimassa_alkupvm "2012-11-01" :voimassa_loppupvm "2013-01-01"} 1
       "2013-01-01" "2013-12-31" {:voimassa_alkupvm "2013-01-01" :voimassa_loppupvm "2013-03-01"} 1
       "2013-01-01" "2013-12-31" {:voimassa_alkupvm "2013-11-01" :voimassa_loppupvm "2014-12-31"} 1
       "2013-01-01" "2013-12-31" {:voimassa_alkupvm "2013-12-31" :voimassa_loppupvm "2014-03-01"} 1
       "2013-01-01" "2013-12-31" {:voimassa_alkupvm "2014-01-01" :voimassa_loppupvm "2014-03-01"} 0

       nil "2013-12-31" {:voimassa_alkupvm "2012-10-01" :voimassa_loppupvm "2012-12-31"} 1
       "2013-01-01" nil {:voimassa_alkupvm "2014-01-01" :voimassa_loppupvm "2014-03-01"} 1
       nil nil {:voimassa_alkupvm "2014-01-01" :voimassa_loppupvm "2014-03-01"} 1

       "2013-01-01" "2013-12-31" {:voimassa_alkupvm "2012-10-01" :voimassa_loppupvm nil} 1
       "2013-01-01" "2013-12-31" {:voimassa_alkupvm "2014-01-01" :voimassa_loppupvm nil} 0
       nil "2013-12-31" {:voimassa_alkupvm "2012-10-01" :voimassa_loppupvm nil} 1
       "2013-01-01" nil {:voimassa_alkupvm "2012-10-01" :voimassa_loppupvm nil} 1
       nil nil {:voimassa_alkupvm "2012-10-01" :voimassa_loppupvm nil} 1))
