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
            [aipal.toimiala.kayttajaroolit :refer [kayttajaroolit
                                                   organisaatio-roolit
                                                   oph-roolit
                                                   ldap-roolit]]))

(def oph-organisaatio {:ytunnus "0829731-2"})

(defn ryhma-cn-filter [ldap-rooli]
  {:filter (str "cn=APP_AIPAL_" (val ldap-rooli) "_*")})

(def ryhma-base "ou=Groups,dc=opintopolku,dc=fi")

(defn kayttajat [kayttooikeuspalvelu ldap-rooli oid->ytunnus]
  (with-open [yhteys (kayttooikeuspalvelu)]
    (let [cn-filter (ryhma-cn-filter ldap-rooli)]
      (if-let [ryhmat (ldap/search yhteys ryhma-base cn-filter)]
        (doall
          (for [ryhma ryhmat
                :let [rooliavain (key ldap-rooli)
                      organisaatio-oid (last (s/split (:cn ryhma) #"_"))
                      ;; OPH:n oid ei ole välttämättä sama joka ympäristössä, joten organisaatio kytketään roolin perusteella
                      organisaatio (or (oid->ytunnus organisaatio-oid)
                                       (when (rooliavain oph-roolit)
                                         oph-organisaatio))
                      rooli (rooliavain kayttajaroolit)
                      kayttaja-dnt (:uniqueMember ryhma)
                      ;; Jos ryhmällä on vain yksi uniqueMember-attribuutti, clj-ldap
                      ;; palauttaa arvon (stringin) eikä vektoria arvoista.
                      kayttaja-dnt (if (string? kayttaja-dnt)
                                     [kayttaja-dnt]
                                     kayttaja-dnt)]
                :when (and rooli
                           (or organisaatio
                              (not (rooliavain organisaatio-roolit))))
                kayttaja-dn kayttaja-dnt
                :let [kayttaja (ldap/get yhteys kayttaja-dn)
                      _ (assert kayttaja)
                      etunimi (first (s/split (:cn kayttaja) #" "))
                      sukunimi (:sn kayttaja)]]
            {:oid (:employeeNumber kayttaja)
             :uid (:uid kayttaja)
             :etunimi etunimi
             :sukunimi (or sukunimi "")
             :voimassa true
             :rooli rooli
             :organisaatio (:ytunnus organisaatio)}))
        (log/warn "Roolin" ldap-rooli "ryhmää ei löytynyt, ei lueta roolin käyttäjiä")))))

(defn tee-kayttooikeuspalvelu [ldap-auth-server-asetukset]
  (fn []
    (let [{:keys [host port user password]} ldap-auth-server-asetukset
          asetukset (merge {:host (str host ":" port)}
                           (when user {:bind-dn user})
                           (when password {:password password}))]
      (ldap/connect asetukset))))
