;; Käyttäjän vaihtoon liittyvä koodi on riippuvuussyklien välttämiseksi omassa
;; nimiavaruudessaan, koska se käyttää arkistoja, jotka puolestaan riippuvat
;; nimiavaruudesta aipal.infra.kayttaja.
(ns aipal.infra.kayttaja.vaihto
  (:require [clojure.tools.logging :as log]
            [aipal.asetukset :refer [asetukset]]
            [oph.common.util.util :refer [map-by some-value]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vakiot :refer [jarjestelma-oid integraatio-uid]]
            [aipal.toimiala.kayttajaroolit :refer [ldap-ryhma->rooli roolijarjestys]]
            [aipal.arkisto.kayttaja :as kayttaja-arkisto]
            [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
            [aipal.infra.kayttaja.sql :refer [with-sql-kayttaja]]
            [arvo.db.core :refer [*db*] :as db]
            [aipal.integraatio.kayttooikeuspalvelu :as kayttooikeuspalvelu]))

(defn kayttajan-nimi [k]
  (str (:etunimi k) " " (:sukunimi k)))

(declare hae-kayttaja-kayttoikeuspalvelusta)

(defn autentikoi-kayttaja [k impersonoitu-oid rooli f]
  (let [aktiivinen-oid (or impersonoitu-oid (:oid k))
        aktiivinen-rooli (or (when rooli (some-value #(= rooli (:rooli_organisaatio_id %)) (:roolit k)))
                             (first (sort-by (comp roolijarjestys :rooli) (:roolit k))))
        aktiivinen-koulutustoimija (:organisaatio aktiivinen-rooli)
        ik (when impersonoitu-oid
             (kayttaja-arkisto/hae impersonoitu-oid))]
    (binding [*kayttaja*
              (assoc k
                :aktiivinen-oid aktiivinen-oid
                :aktiiviset-roolit (:roolit k)
                :aktiivinen-rooli aktiivinen-rooli
                :aktiivinen-koulutustoimija aktiivinen-koulutustoimija
                :nimi (kayttajan-nimi k)
                :impersonoidun-kayttajan-nimi (if ik (kayttajan-nimi ik) ""))]
      (log/info "Käyttäjä autentikoitu:" (pr-str *kayttaja*))
      (with-sql-kayttaja (:oid k)
                       (f)))))

(defn with-kayttaja* [uid impersonoitu-oid rooli f]
  (log/debug "Yritetään autentikoida käyttäjä" uid)
  (if-let [k (db/hae-voimassaoleva-kayttaja {:uid uid :voimassaolo (:kayttooikeus-tarkistusvali @asetukset)})]
    (let [aktiivinen-oid (or impersonoitu-oid (:oid k))
          roolit (db/hae-voimassaolevat-roolit {:kayttajaOid aktiivinen-oid})]
      (autentikoi-kayttaja (assoc k :roolit roolit) impersonoitu-oid rooli f))
    (autentikoi-kayttaja (hae-kayttaja-kayttoikeuspalvelusta uid impersonoitu-oid) impersonoitu-oid rooli f)))

(defmacro with-kayttaja [uid impersonoitu-oid rooli & body]
  `(with-kayttaja* ~uid ~impersonoitu-oid ~rooli (fn [] ~@body)))

(defn hae-kayttaja-kayttoikeuspalvelusta [uid impersonoitu-oid]
  (log/info "Yritetään hakea Käyttöoikeuspalvelusta käyttäjä" uid)
  (with-kayttaja integraatio-uid nil nil
    (let [oid->ytunnus (into {} (map (juxt :oid :ytunnus) (db/hae-oid->ytunnus)))
          kayttaja (kayttooikeuspalvelu/kayttaja uid ldap-ryhma->rooli oid->ytunnus)]
      (if (:voimassa kayttaja)
        (kayttajaoikeus-arkisto/paivita-kayttaja! kayttaja impersonoitu-oid)
        (do (db/passivoi-kayttaja! {:uid uid})
            (throw (IllegalStateException. (str "Ei voimassaolevaa käyttäjää " uid))))))))
