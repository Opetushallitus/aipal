;; Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.arkisto.kysely-util
  (:require [korma.core :as sql]
            [aipal.infra.kayttaja :refer [yllapitaja?]]))

(defn rajaa-kayttajalle-sallittuihin-kyselyihin-sql []
  (let [koulutustoimijan-oma "(kysely_organisaatio_view.koulutustoimija = ?)"]
    (cond
      (yllapitaja?)         koulutustoimijan-oma
      :else                 (str "(" koulutustoimijan-oma ")"))))
