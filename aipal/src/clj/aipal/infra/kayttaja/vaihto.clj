;; Käyttäjän vaihtoon liittyvä koodi on riippuvuussyklien välttämiseksi omassa
;; nimiavaruudessaan, koska se käyttää arkistoja, jotka puolestaan riippuvat
;; nimiavaruudesta aipal.infra.kayttaja.
(ns aipal.infra.kayttaja.vaihto
  (:require [clojure.tools.logging :as log]
            [korma.core :as sql]
            [aipal.asetukset :refer [asetukset]]
            [oph.common.util.util :refer [map-by]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vakiot :refer [jarjestelma-oid integraatio-uid]]
            [aipal.toimiala.kayttajaroolit :refer [ldap-ryhma->rooli]]
            [aipal.arkisto.kayttaja :as kayttaja-arkisto]
            [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
            [aipal.arkisto.koulutustoimija :as koulutustoimija-arkisto]
            [aipal.infra.kayttaja.sql :refer [with-sql-kayttaja]]
            [aipal.integraatio.kayttooikeuspalvelu :as kayttooikeuspalvelu]))

(defn kayttajan-nimi [k]
  (str (:etunimi k) " " (:sukunimi k)))

(declare hae-kayttaja-ldapista)

(defn with-kayttaja* [uid impersonoitu-oid f]
  (log/debug "Yritetään autentikoida käyttäjä" uid)
  (if-let [k (kayttaja-arkisto/hae-voimassaoleva uid)]
    (let [aktiivinen-oid (or impersonoitu-oid (:oid k))
          aktiiviset-roolit (kayttajaoikeus-arkisto/hae-roolit aktiivinen-oid)
          aktiivinen-koulutustoimija (some :organisaatio aktiiviset-roolit)
          ik (when impersonoitu-oid
               (kayttaja-arkisto/hae impersonoitu-oid))]
      (binding [*kayttaja*
                (assoc k
                       :aktiivinen-oid aktiivinen-oid
                       :aktiiviset-roolit aktiiviset-roolit
                       :aktiivinen-koulutustoimija aktiivinen-koulutustoimija
                       :nimi (kayttajan-nimi k)
                       :impersonoidun-kayttajan-nimi (if ik (kayttajan-nimi ik) ""))]
        (log/info "Käyttäjä autentikoitu:" (pr-str *kayttaja*))
        (with-sql-kayttaja (:oid k)
          (f))))
    (if (hae-kayttaja-ldapista uid)
      (recur uid impersonoitu-oid f)
      (throw (IllegalStateException. (str "Ei voimassaolevaa käyttäjää " uid))))))

(defmacro with-kayttaja [uid impersonoitu-oid & body]
  `(with-kayttaja* ~uid ~impersonoitu-oid (fn [] ~@body)))

(defn hae-kayttaja-ldapista [uid]
  (with-kayttaja integraatio-uid nil
    (let [kop (kayttooikeuspalvelu/tee-kayttooikeuspalvelu (:ldap-auth-server @asetukset))
          oid->ytunnus (map-by :oid (koulutustoimija-arkisto/hae-kaikki-joissa-oid))
          kayttaja (kayttooikeuspalvelu/kayttaja kop uid oid->ytunnus ldap-ryhma->rooli)]
      (when kayttaja
        (kayttajaoikeus-arkisto/paivita-kayttaja! kayttaja)
        (kayttaja-arkisto/hae-voimassaoleva uid)))))
