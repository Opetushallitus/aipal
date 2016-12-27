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

(ns aipal.infra.eraajo.kayttajat
  (:require [clojurewerkz.quartzite.conversion :as qc]
            [clojure.tools.logging :as log]
            [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
            [aipal.arkisto.koulutustoimija :as koulutustoimija-arkisto]
            [aipal.integraatio.kayttooikeuspalvelu :as kop]
            [aipal.toimiala.kayttajaroolit :refer [ldap-ryhma->rooli]]
            [oph.common.util.util :refer [map-by]]
            [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
            [aipal.infra.kayttaja.vakiot :refer [integraatio-uid]]))

(defn paivita-kayttajat-ldapista [kayttooikeuspalvelu]
  (with-kayttaja integraatio-uid nil nil
    (let [oid->ytunnus (map-by :oid (koulutustoimija-arkisto/hae-kaikki-joissa-oid))]
      (log/info "Päivitetään käyttäjät ja käyttäjien roolit käyttöoikeuspalvelun LDAP:sta")
      (kayttajaoikeus-arkisto/paivita-kaikki!
        (apply concat
               (for [[ldap-ryhma rooli] ldap-ryhma->rooli]
                 (kop/kayttajat kayttooikeuspalvelu ldap-ryhma rooli oid->ytunnus))))
      (log/info "Käyttäjien ja käyttäjien roolien päivitys valmis."))))

;; Cloverage ei tykkää `defrecord`eja generoivista makroista, joten hoidetaan
;; `defjob`:n homma käsin.
(defrecord PaivitaKayttajatLdapistaJob []
   org.quartz.Job
   (execute [this ctx]
     (try
       (let [{kop "kayttooikeuspalvelu"} (qc/from-job-data ctx)]
         (paivita-kayttajat-ldapista kop))
       (catch Exception e (log/error "Käyttäjien päivitys LDAPista epäonnistui" (map str (.getStackTrace e)))))))
