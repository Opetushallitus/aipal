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

(ns aipal.infra.eraajo
  "Säännöllisin väliajoin suoritettavat toiminnot."
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.schedule.daily-interval :as s]
            [clojurewerkz.quartzite.schedule.cron :as cron]
            [clojure.tools.logging :as log]
            aipal.infra.eraajo.kayttajat
            aipal.infra.eraajo.organisaatiot
            aipal.infra.eraajo.koulutustoimijoiden-tutkinnot
            aipal.infra.eraajo.raportointi
            aipal.infra.eraajo.tutkinnot)
  (:import aipal.infra.eraajo.kayttajat.PaivitaKayttajatLdapistaJob
           aipal.infra.eraajo.organisaatiot.PaivitaOrganisaatiotJob
           aipal.infra.eraajo.koulutustoimijoiden_tutkinnot.PaivitaKoulutustoimijoidenTutkinnotJob
           aipal.infra.eraajo.raportointi.PaivitaNakymatJob
           aipal.infra.eraajo.tutkinnot.PaivitaTutkinnotJob))

(defn ajastus [asetukset tyyppi]
  (cron/schedule
    (cron/cron-schedule (get-in asetukset [:ajastus tyyppi]))))

(defn ^:integration-api kaynnista-ajastimet! [kayttooikeuspalvelu asetukset]
  (log/info "Käynnistetään ajastetut eräajot")
  (qs/initialize)
  (log/info "Poistetaan vanhat jobit ennen uudelleenkäynnistystä")
  (qs/clear!)
  (qs/start)
  (log/info "Eräajomoottori käynnistetty")
  (let [ldap-job (j/build
                   (j/of-type PaivitaKayttajatLdapistaJob)
                   (j/with-identity "paivita-kayttajat-ldapista")
                   (j/using-job-data {"kayttooikeuspalvelu" kayttooikeuspalvelu}))
        ldap-trigger-daily (t/build
                             (t/with-identity "daily4")
                             (t/start-now)
                             (t/with-schedule (ajastus asetukset :kayttooikeuspalvelu)))
        org-job (j/build
                  (j/of-type PaivitaOrganisaatiotJob)
                  (j/with-identity "paivita-organisaatiot")
                  (j/using-job-data {"asetukset" (:organisaatiopalvelu asetukset)}))
        org-trigger-daily (t/build
                            (t/with-identity "daily3")
                            (t/start-now)
                            (t/with-schedule (ajastus asetukset :organisaatiopalvelu)))
        koul-job (j/build
                   (j/of-type PaivitaKoulutustoimijoidenTutkinnotJob)
                   (j/with-identity "paivita-koulutustoimijoiden-tutkinnot"))
        koul-trigger-daily (t/build
                             (t/with-identity "daily5")
                             (t/start-now)
                             (t/with-schedule (ajastus asetukset :koulutustoimijoiden-tutkinnot)))
        raportointi-job (j/build
                          (j/of-type PaivitaNakymatJob)
                          (j/with-identity "paivita-raportoinnin-nakymat"))
        raportointi-trigger (t/build
                              (t/with-identity "raportointi")
                              (t/start-now)
                              (t/with-schedule (ajastus asetukset :raportointi)))
        tutkinnot-job (j/build
                        (j/of-type PaivitaTutkinnotJob)
                        (j/with-identity "paivita-tutkinnot"))
        tutkinnot-trigger (t/build
                            (t/with-identity "tutkinnot")
                            (t/start-now)
                            (t/with-schedule (ajastus asetukset :tutkinnot)))]
    (qs/schedule ldap-job ldap-trigger-daily)
    (qs/schedule org-job org-trigger-daily)
    (qs/schedule koul-job koul-trigger-daily)
    (qs/schedule raportointi-job raportointi-trigger)
    (qs/schedule tutkinnot-job tutkinnot-trigger)))
