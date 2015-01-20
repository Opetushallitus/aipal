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
  (:require [clojure.string :as s]
            [clojure.tools.logging :as log]
            [clj-ldap.client :as ldap]
            [aipal.toimiala.kayttajaroolit :refer [koulutustoimija-roolit
                                                   oph-roolit]]))

(def oph-koulutustoimija {:ytunnus "0829731-2"})

(defn ryhma-cn-filter [ldap-ryhma]
  {:filter (str "cn=APP_AIPAL_" ldap-ryhma "_*")})

(defn jasen-filter [jasen-dn]
  {:filter (str "uniqueMember=" jasen-dn)})

(defn kayttaja-dn [uid]
  (str "uid=" uid ",ou=People,dc=opintopolku,dc=fi"))

(def ryhma-base "ou=Groups,dc=opintopolku,dc=fi")

(def kayttaja-base "ou=People,dc=opintopolku,dc=fi")

(defn kayttajat [kayttooikeuspalvelu ldap-ryhma rooli oid->ytunnus]
  (with-open [yhteys (kayttooikeuspalvelu)]
    (let [cn-filter (ryhma-cn-filter ldap-ryhma)]
      (if-let [ryhmat (ldap/search yhteys ryhma-base cn-filter)]
        (doall
          (for [ryhma ryhmat
                :let [koulutustoimija-oid (last (s/split (:cn ryhma) #"_"))
                      ;; OPH:n oid ei ole välttämättä sama joka ympäristössä, joten koulutustoimija kytketään roolin perusteella
                      koulutustoimija (or (oid->ytunnus koulutustoimija-oid)
                                          (when (contains? oph-roolit rooli)
                                            oph-koulutustoimija))
                      kayttaja-dnt (:uniqueMember ryhma)
                      ;; Jos ryhmällä on vain yksi uniqueMember-attribuutti, clj-ldap
                      ;; palauttaa arvon (stringin) eikä vektoria arvoista.
                      kayttaja-dnt (if (string? kayttaja-dnt)
                                     [kayttaja-dnt]
                                     kayttaja-dnt)]
                ;; ei haeta koulutustoimijakäyttäjiä, joiden koulutustoimija ei ole tiedossa
                :when (or koulutustoimija
                          (not (contains? koulutustoimija-roolit rooli)))
                kayttaja-dn kayttaja-dnt
                :let [kayttaja (ldap/get yhteys kayttaja-dn)]
                :when kayttaja
                :let [etunimi (first (s/split (:cn kayttaja) #" "))
                      sukunimi (:sn kayttaja)]]
            {:oid (:employeeNumber kayttaja)
             :uid (:uid kayttaja)
             :etunimi etunimi
             :sukunimi (or sukunimi "")
             :voimassa true
             :rooli rooli
             :organisaatio (:ytunnus koulutustoimija)}))
        (log/warn "Roolin" rooli "ldap-ryhmää" ldap-ryhma "ei löytynyt, ei lueta roolin käyttäjiä")))))

(defn kayttaja [kayttooikeuspalvelu uid oid->ytunnus ryhma->rooli]
  (with-open [yhteys (kayttooikeuspalvelu)]
    (when-let [kayttaja (ldap/get yhteys (kayttaja-dn uid))]
      (let [etunimi (first (s/split (:cn kayttaja) #" "))
            sukunimi (:sn kayttaja)
            ryhmat (ldap/search yhteys ryhma-base (jasen-filter (kayttaja-dn uid)))
            roolit (for [ldap-ryhma ryhmat
                         :let [koulutustoimija-oid (last (s/split (:cn ldap-ryhma) #"_"))
                               ryhma (second (re-matches #"APP_AIPAL_(.*)_[\d.]+" (:cn ldap-ryhma)))
                               rooli (ryhma->rooli ryhma)
                               koulutustoimija (or (oid->ytunnus koulutustoimija-oid)
                                                   (when (contains? oph-roolit rooli)
                                                     oph-koulutustoimija))]
                         :when rooli]
                     {:rooli rooli
                      :organisaatio (:ytunnus koulutustoimija)
                      :voimassa true})]
        (when (seq roolit)
          {:oid (:employeeNumber kayttaja)
           :uid (:uid kayttaja)
           :etunimi etunimi
           :sukunimi (or sukunimi "")
           :voimassa true
           :roolit roolit})))))

(defn tee-kayttooikeuspalvelu [ldap-auth-server-asetukset]
  (fn []
    (let [{:keys [host port user password]} ldap-auth-server-asetukset
          asetukset (merge {:host (str host ":" port)}
                           (when user {:bind-dn user})
                           (when password {:password password}))]
      (ldap/connect asetukset))))
