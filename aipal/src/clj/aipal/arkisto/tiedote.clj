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

(ns aipal.arkisto.tiedote
  (:require [korma.core :as sql]
            [oph.korma.korma :refer [select-unique-or-nil]]
            [aipal.auditlog :as auditlog]
            [aipal.integraatio.sql.korma :as taulut]))

(defn hae
  "Hakee tiedotteen."
  []
  (when-let [tiedote (select-unique-or-nil taulut/tiedote)]
    {:fi (:teksti_fi tiedote)
     :sv (:teksti_sv tiedote)}))

(defn poista-ja-lisaa!
  "Poistaa vanhan tiedotteen ja lisää uuden."
  [tiedote]
  (auditlog/tiedote-operaatio! :lisays)
  (sql/delete taulut/tiedote)
  (sql/insert taulut/tiedote
    (sql/values {:teksti_fi (:fi tiedote)
                 :teksti_sv (:sv tiedote)})))

(defn poista!
  "Poistaa tiedotteen."
  []
  (auditlog/tiedote-operaatio! :poisto)
  (sql/delete taulut/tiedote))
