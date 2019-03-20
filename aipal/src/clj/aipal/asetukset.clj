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

(ns aipal.asetukset
  (:require
    [clojure.java.io :as io]
    [schema.core :as s]
    [clj-time.local :as time-local]
    [oph.common.infra.asetukset :refer [lue-asetukset]])
  (:import (java.util Properties)))

(def asetukset (promise))

(def ^:private Palvelu {:url s/Str
                        :user s/Str
                        :password s/Str})

(def Asetukset
  {:server {:port s/Int
            :base-url s/Str}
   :db {:host s/Str
        :port s/Int
        :name s/Str
        :user s/Str
        :password s/Str
        :migration-user s/Str
        :migration-password s/Str
        :maximum-pool-size s/Int
        :minimum-pool-size s/Int}
   :cas-auth-server {:url s/Str
                     :unsafe-https Boolean
                     :enabled Boolean}
   :vastaus-base-url s/Str
   :oiva Palvelu
   :kyselyynohjaus Palvelu
   :avopfi-shared-secret s/Str
   :organisaatiopalvelu {:url s/Str}
   :koodistopalvelu {:url s/Str}
   :eraajo Boolean
   :kayttooikeuspalvelu Palvelu
   :oppijanumerorekisteri Palvelu
   :development-mode Boolean
   :ominaisuus {s/Keyword Boolean}
   :raportointi-minimivastaajat s/Int
   :logback {:properties-file s/Str}
   :ajastus {:organisaatiopalvelu s/Str
             :kayttooikeuspalvelu s/Str
             :koulutustoimijoiden-tutkinnot s/Str
             :raportointi s/Str
             :tutkinnot s/Str}
   :kayttooikeus-tarkistusvali s/Str
   :api-page-length s/Int
   (s/optional-key :basic-auth) {:tunnus s/Str
                                 :salasana s/Str}})

(def oletusasetukset
  {:server {:port 8082
            :base-url "http://localhost:8082"}
   :db {:host "127.0.0.1"
        :port 5432
        :name "arvo_db"
        :user "aipal_user"
        :password "aipal"
        :migration-user "aipal_adm"
        :migration-password "aipal-adm"
        :maximum-pool-size 15
        :minimum-pool-size 3}
   :cas-auth-server {:url "https://virkailija.testiopintopolku.fi/cas"
                     :unsafe-https true
                     :enabled false}
   :vastaus-base-url "http://127.0.0.1:8083"
   :avopfi-shared-secret "secret"
   :organisaatiopalvelu {:url "https://virkailija.opintopolku.fi/organisaatio-service/rest/organisaatio/"}
   :koodistopalvelu {:url "https://virkailija.opintopolku.fi/koodisto-service/rest/json/"}
   :eraajo true
   :oiva {:url "http://oiva.minedu.fi/api/export/koulutusluvat"
          :user "tunnus"
          :password "salasana"}
   :kyselyynohjaus {:url "http://localhost:3000/api/export/v1/opiskeluoikeudet"
                    :user "vipunen"
                    :password "salasana"}
   :development-mode false ; oletusarvoisesti ei olla kehitysmoodissa. Pitää erikseen kääntää päälle jos tarvitsee kehitysmoodia.
   :ominaisuus {:koulutustoimijan_valtakunnalliset_raportit false}
   :kayttooikeuspalvelu {:url "https://testi.virkailija.opintopolku.fi/kayttooikeus-service"
                         :user "tunnus"
                         :password "salasana"}
   :oppijanumerorekisteri {:url "https://testi.virkailija.opintopolku.fi/oppijanumerorekisteri-service"
                           :user "tunnus"
                           :password "salasana"}
   :basic-auth {:tunnus "testi" :salasana "kissa13"}
   :raportointi-minimivastaajat 5
   :kayttooikeus-tarkistusvali "6000d"
   :logback {:properties-file "resources/logback.xml"}
   :ajastus {:organisaatiopalvelu "0 25 15 ? * * *"
             :kayttooikeuspalvelu "0 0 4 * * ?"
             :koulutustoimijoiden-tutkinnot "0 40 17 ? * * *"
             :raportointi "0 30 5 * * ?"
             :tutkinnot "0 0 2 * * ?"}
   :api-page-length 50000})

(def common-audit-log-asetukset {:boot-time        (time-local/local-now)
                                 :hostname         "localhost"
                                 :service-name     "aipal"
                                 :application-type "virkailija"})

(def build-id (delay (if-let [resource (io/resource "build-id.txt")]
                       (.trim (slurp resource :encoding "UTF-8"))
                       "dev")))


(def project-version
  (delay
    (-> (doto (Properties.)
          (.load (-> "META-INF/maven/aipal/aipal/pom.properties"
                     (io/resource)
                     (io/reader))))
        (.get "version"))))


(defn kehitysmoodi?
  [asetukset]
  (true? (:development-mode asetukset)))

(defn hae-asetukset
  ([alkuasetukset] (lue-asetukset alkuasetukset Asetukset "aipal.properties"))
  ([] (hae-asetukset oletusasetukset)))

(defn service-path [base-url]
  (let [path (drop 3 (clojure.string/split base-url #"/"))]
    (str "/" (clojure.string/join "/" path))))
