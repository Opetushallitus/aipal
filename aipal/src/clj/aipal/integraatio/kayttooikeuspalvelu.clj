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
            [oph.common.util.util :refer [get-json-from-url]]))

(defn palvelukutsu [palvelu url options]
  (-> (cas/get-with-cas-auth palvelu url options)
      :body
      cheshire/parse-string
      keywordize-keys))

(defn postpalvelukutsu [palvelu url options]
  (-> (cas/post-with-cas-auth palvelu url options)
      :body
      cheshire/parse-string
      keywordize-keys))

(defn kayttoikeudet [kayttaja]
  (for [organisaatio (:organisaatiot kayttaja)]
   (let [oikeudet (->> (:kayttooikeudet organisaatio)
                      (filter #(= (:palvelu %) "AMKPAL"))
                      (map #(merge % {:organisaatioOid (:organisaatioOid organisaatio)})))]
     oikeudet)))

(defn kayttaja [uid]
  (log/info "Haetaan käyttäjän" uid "tiedot Opintopolusta")
  (let [kayttooikeus-url (str (-> @asetukset :kayttooikeuspalvelu :url) "/kayttooikeus/kayttaja")
        oppijanumerorekisteri-url (str (-> @asetukset :oppijanumerorekisteri :url) "/henkilo/")
        kayttaja (first (palvelukutsu :kayttooikeuspalvelu kayttooikeus-url {:query-params {"username" uid}}))
        oikeudet (->> (kayttoikeudet kayttaja)
                      flatten)
        tiedot (when kayttaja (palvelukutsu :oppijanumerorekisteri (str oppijanumerorekisteri-url (:oidHenkilo kayttaja)) {}))]
    (log/info "Saatiin käyttäjälle" uid "oikeudet" oikeudet)
    {:oid      (:oidHenkilo kayttaja)
     :etunimi  (or (:kutsumanimi tiedot) (:etunimet tiedot))
     :sukunimi (:sukunimi tiedot)
     :uid      uid
     :voimassa ((complement empty?) oikeudet)
     :oikeudet oikeudet}))

(defn kaikki-oidit [oid]
  (let [oppijanumerorekisteri-url (format "%s/s2s/duplicateHenkilos" (-> @asetukset :oppijanumerorekisteri :url))
        vastaus (postpalvelukutsu :oppijanumerorekisteri oppijanumerorekisteri-url {:body (format "{\"henkiloOids\": [\"%s\"]}" oid) :content-type :json})
        oidit (->> vastaus
                   (mapcat vals)
                   distinct)]
;    Normaali tapaus on, että henkilöllä ei ole duplikaatteja
    (if (empty? oidit)
      (list oid)
      oidit)))
