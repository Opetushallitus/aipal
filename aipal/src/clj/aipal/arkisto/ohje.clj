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

(ns aipal.arkisto.ohje
  (:require
    [korma.core :as sql]
    [aipal.auditlog :as auditlog]))

(defn hae
  "Hakee ohjeen id:n perusteella."
  [ohjetunniste]
  (first
    (sql/select :ohje
      (sql/where {:ohjetunniste ohjetunniste}))))

(defn muokkaa-tai-luo-uusi!
  "Muokkaa ohjetta tai luo uuden jos tunnisteelle ei löydy ohjetta"
  [uusi-ohje]
  (auditlog/ohje-paivitys! (:ohjetunniste uusi-ohje))
  (if-let [olemassa-oleva (hae (:ohjetunniste uusi-ohje))]
    (sql/update :ohje
      (sql/set-fields uusi-ohje)
      (sql/where {:ohjetunniste (:ohjetunniste uusi-ohje)}))
    (sql/insert :ohje
      (sql/values uusi-ohje))))
