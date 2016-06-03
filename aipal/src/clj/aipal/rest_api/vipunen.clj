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

(ns aipal.rest-api.vipunen
  (:require [compojure.api.core :refer [defroutes GET]]
            [cheshire.core :as json]
            [clojure.java.io :refer [make-writer]]
            [ring.util.io :refer [piped-input-stream]]
            [aipal.arkisto.vipunen :as vipunen]
            aipal.compojure-util
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(defroutes reitit
  (GET "/" []
    {:status 200
     :body (piped-input-stream
             #(->>
                (make-writer % {})
                (json/generate-stream (vipunen/hae-kaikki))
                (.flush)))
     :headers {"Content-Type" "application/json"}})
  (GET "/valtakunnallinen" []
    {:status 200
     :body (piped-input-stream
             #(->>
                (make-writer % {})
                (json/generate-stream (vipunen/hae-valtakunnalliset))
                (.flush)))
     :headers {"Content-Type" "application/json"}}))