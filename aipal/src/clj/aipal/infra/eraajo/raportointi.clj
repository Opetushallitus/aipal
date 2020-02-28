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

(ns aipal.infra.eraajo.raportointi
  (:require [clojure.tools.logging :as log]
            [aipal.toimiala.raportti.valtakunnallinen :as raportti]
            [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
            [aipal.infra.kayttaja.vakiot :refer [jarjestelma-uid]]))

(defn ^:integration-api paivita-nakymat! []
  (with-kayttaja jarjestelma-uid nil nil
    (log/info "Päivitetään raportoinnin ja Vipusen näkymät")
    (raportti/paivita-nakymat)
    (log/info "Raportoinnin ja Vipusen näkymien päivitys valmis")))

;; Cloverage ei tykkää `defrecord`eja generoivista makroista, joten hoidetaan
;; `defjob`:n homma käsin.
(defrecord PaivitaNakymatJob []
   org.quartz.Job
   (execute [this ctx]
     (try
       (paivita-nakymat!)
       (catch Exception e
         (log/error "Raportoinnin ja Vipusen näkymien päivitys"
                    (map str (.getStackTrace e)))))))
