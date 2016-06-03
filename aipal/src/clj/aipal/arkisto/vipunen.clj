;; Copyright (c) 2016 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.arkisto.vipunen
  (:require [korma.core :as sql]
    [clj-time.core :as time]
    [aipal.integraatio.sql.korma :refer :all]))

(defn hae-kaikki []
  (sql/select vipunen_view))

(defn hae-valtakunnalliset 
  ([]
    (hae-valtakunnalliset nil nil))
  ([alkupvm loppupvm]
    (sql/select vipunen_view
      (sql/where 
        (and 
          {:valtakunnallinen true}
          (>= :vastausaika (or alkupvm (time/local-date 1900 1 1))
          (<= :vastausaika (or loppupvm ((time/plus (time/today-at-midnight) (time/days -1))))))))
        )))
    