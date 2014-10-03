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

(ns aipal.arkisto.kyselypohja
  (:import java.sql.Date)
  (:require [korma.core :as sql]))

(defn hae-kyselypohjat
  [organisaatio]
  (sql/select :kyselypohja
    (sql/join :inner :kyselypohja_organisaatio_view {:kyselypohja_organisaatio_view.kyselypohjaid :kyselypohja.kyselypohjaid})
    (sql/fields :kyselypohja.kyselypohjaid :kyselypohja.nimi_fi :kyselypohja.nimi_sv
                [(sql/raw (str "((voimassa_alkupvm IS NULL OR voimassa_alkupvm <= current_date) AND "
                               "(voimassa_loppupvm IS NULL OR voimassa_loppupvm >= current_date) AND "
                               "poistettu is null)")) :voimassa])
    (sql/where (or {:kyselypohja_organisaatio_view.koulutustoimija organisaatio}
                   {:kyselypohja_organisaatio_view.valtakunnallinen true}))
    (sql/order :muutettuaika :desc)))
