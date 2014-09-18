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

(ns aipal.infra.eraajo.tutkinnot
  (:require [clojurewerkz.quartzite.jobs :as j
             :refer [defjob]]
            [clojurewerkz.quartzite.conversion :as qc]
            [clojure.tools.logging :as log]
            [oph.korma.korma-auth :refer [integraatiokayttaja]]
            [aipal.integraatio.koodistopalvelu :as org]
            [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]))

(defn paivita-tutkinnot! [asetukset]
  (with-kayttaja integraatiokayttaja
    (org/paivita-tutkinnot! asetukset)))

;; Cloverage ei tykkää `defrecord`eja generoivista makroista, joten hoidetaan
;; `defjob`:n homma käsin.
(defrecord PaivitaTutkinnotJob []
   org.quartz.Job
   (execute [this ctx]
     (let [{asetukset "asetukset"} (qc/from-job-data ctx)]
       (paivita-tutkinnot! asetukset))))
