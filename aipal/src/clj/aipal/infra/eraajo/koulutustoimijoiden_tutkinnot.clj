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

(ns aipal.infra.eraajo.koulutustoimijoiden-tutkinnot
  (:require [clojure.tools.logging :as log]
            [aipal.integraatio.oiva :as oiva]
            [arvo.db.core :refer [*db*] :as db]
            [cheshire.core :as json]
            [aipal.infra.kayttaja.vakiot :refer [integraatio-uid]]
            [clojure.java.jdbc :as jdbc]))

(defn paivita-koulutustoimijoiden-tutkinnot! []
  (let [koulutustoimijoiden-tutkinnot (oiva/hae-koulutustoimijoiden-tutkinnot)]
    (log/info "Aloitetaan koulutustoimijoiden tutkintojen päivitys Oivasta")
    (jdbc/with-db-transaction [tx *db*]
      (doseq [koulutustoimija koulutustoimijoiden-tutkinnot
              tutkinto (:koulutukset koulutustoimija)]
        (db/lisaa-koulutustoimijan-tutkinto! tx {:ytunnus (:jarjestajaYtunnus koulutustoimija)
                                                 :tutkintotunnus tutkinto
                                                 :alkupvm (:alkupvm koulutustoimija)
                                                 :loppupvm (:loppupvm koulutustoimija)
                                                 :laaja_oppisopimuskoulutus (= "1" (:laajaOppisopimuskoulutus koulutustoimija))})))

    (log/info "Koulutustoimijoiden tutkintojen päivitys Oivasta valmis")))

;; Cloverage ei tykkää `defrecord`eja generoivista makroista, joten hoidetaan
;; `defjob`:n homma käsin.
(defrecord PaivitaKoulutustoimijoidenTutkinnotJob []
   org.quartz.Job
   (execute [this ctx]
     (try
       (paivita-koulutustoimijoiden-tutkinnot!)
       (catch Exception e
         (log/error "Koulutustoimijoiden tutkintojen päivitys Oivasta epäonnistui"
           (map str (.getStackTrace e)))))))
