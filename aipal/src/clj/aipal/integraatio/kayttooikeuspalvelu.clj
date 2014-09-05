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
                                                   organisaatio-roolit]]))

(defn aipal-ryhma-cn-filter [rooli]
  {:filter (str "cn=APP_AIPAL_" rooli "*")})

(def roolin-ryhma-cn-filter
  {(:paakayttaja kayttajaroolit) (aipal-ryhma-cn-filter "CRUD")
   (:oppilaitos-paakayttaja kayttajaroolit) (aipal-ryhma-cn-filter "OPLPAAKAYTTAJA")
   (:oppilaitos-vastuukayttaja kayttajaroolit) (aipal-ryhma-cn-filter "OPLVASTUUKAYTTAJA")
   (:oppilaitos-kayttaja kayttajaroolit) (aipal-ryhma-cn-filter "OPLKAYTTAJA")
   (:oph-katselija kayttajaroolit) (aipal-ryhma-cn-filter "OPHKATSELIJA")
   (:oppilaitos-katselija kayttajaroolit) (aipal-ryhma-cn-filter "OPLKATSELIJA")
   (:toimikuntakatselija kayttajaroolit) (aipal-ryhma-cn-filter "TTKKATSELIJA")
   (:katselija kayttajaroolit) (aipal-ryhma-cn-filter "KATSELIJA")})

(def ryhma-base "ou=Groups,dc=opintopolku,dc=fi")

(defn kayttajat [kayttooikeuspalvelu rooli oid->ytunnus]
  {:pre [(contains? roolin-ryhma-cn-filter rooli)]}
  (with-open [yhteys (kayttooikeuspalvelu)]
    (let [cn-filter (roolin-ryhma-cn-filter rooli)]
      (if-let [ryhmat (ldap/search yhteys ryhma-base cn-filter)]
        (doall
          (for [ryhma ryhmat
                :let [organisaatio-oid (last (s/split (:cn ryhma) #"_"))
                      organisaatio (oid->ytunnus organisaatio-oid)
                      kayttaja-dnt (:uniqueMember ryhma)
                      ;; Jos ryhmällä on vain yksi uniqueMember-attribuutti, clj-ldap
                      ;; palauttaa arvon (stringin) eikä vektoria arvoista.
                      kayttaja-dnt (if (string? kayttaja-dnt)
                                     [kayttaja-dnt]
                                     kayttaja-dnt)]
                :when [(or organisaatio
                           (not (contains? (set (vals organisaatio-roolit)) rooli)))]
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
        (log/warn "Roolin" rooli "ryhmää ei löytynyt, ei lueta roolin käyttäjiä")))))

(defn tee-kayttooikeuspalvelu [ldap-auth-server-asetukset]
  (fn []
    (let [{:keys [host port user password]} ldap-auth-server-asetukset
          asetukset (merge {:host (str host ":" port)}
                           (when user {:bind-dn user})
                           (when password {:password password}))]
      (ldap/connect asetukset))))
