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

(ns aipal.integraatio.kayttooikeuspalvelu
  (:require [clojure.tools.logging :as log]
            [oph.common.util.cas :as cas]
            [cheshire.core :as cheshire]
            [clojure.walk :refer [keywordize-keys]]
            [aipal.asetukset :refer [asetukset]]
            [oph.common.util.util :refer [get-json-from-url]]
            [aipal.toimiala.kayttajaroolit :refer [koulutustoimija-roolit
                                                   oph-roolit]]))
(defn palvelukutsu [palvelu url options]
  (-> (cas/get-with-cas-auth palvelu url options)
      :body
      cheshire/parse-string
      keywordize-keys))

(defn hae-ytunnus [oid oid->ytunnus]
  (let [ytunnus (get oid->ytunnus oid)]
    (if ytunnus
      ytunnus
      (do (log/error "Ei löydetty y-tunnusta oid:lle " oid)
          nil))))

(defn kayttoikeudet [kayttaja ldap-ryhma->rooli oid->ytunnus]
  (for [organisaatio (:organisaatiot kayttaja)]
    (do (println "Organisaatio" organisaatio "ytunnus:" (hae-ytunnus (:organisaatioOid organisaatio) oid->ytunnus))
      (->> (:kayttooikeudet organisaatio)
           (filter #(= (:palvelu %) "AMKPAL"))
           (map :oikeus)
           (map ldap-ryhma->rooli)
           (map #(into {} {:rooli %
                           :organisaatio (get oid->ytunnus (:organisaatioOid organisaatio))
                           :voimassa true}))))))

(defn kayttaja [uid ldap-ryhma->rooli oid->ytunnus]
  (log/info "Haetaan käyttäjän" uid "tiedot Opintopolusta")
  (let [kayttooikeus-url (str (-> @asetukset :kayttooikeuspalvelu :url) "/kayttooikeus/kayttaja")
        oppijanumerorekisteri-url (str (-> @asetukset :oppijanumerorekisteri :url) "/henkilo/")
        kayttaja (first (palvelukutsu :kayttooikeuspalvelu kayttooikeus-url {:query-params {"username" uid}}))
        roolit (->> (kayttoikeudet kayttaja ldap-ryhma->rooli oid->ytunnus)
                    flatten
                    (filter :organisaatio))
        tiedot (when kayttaja (palvelukutsu :oppijanumerorekisteri (str oppijanumerorekisteri-url (:oidHenkilo kayttaja)) {}))]
    {:oid (:oidHenkilo kayttaja)
     :etunimi (or (:kutsumanimi tiedot) (:etunimet tiedot))
     :sukunimi (:sukunimi tiedot)
     :uid uid
     :voimassa ((complement empty?) roolit)
     :roolit roolit}))
