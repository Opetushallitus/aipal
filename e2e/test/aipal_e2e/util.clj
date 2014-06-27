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

(ns aipal-e2e.util
  (:require [aitu-e2e.util :refer [avaa]]))

(defn aipal-url [polku]
  (str (or (System/getenv "AIPAL_URL")
           "http://192.168.50.1:8082")
       polku))

(defn avaa-aipal
  [polku]
  (aitu-e2e.util/avaa aipal-url polku))
