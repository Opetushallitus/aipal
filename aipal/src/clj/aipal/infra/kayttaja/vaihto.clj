;; Käyttäjän vaihtoon liittyvä koodi on riippuvuussyklien välttämiseksi omassa
;; nimiavaruudessaan, koska se käyttää arkistoja, jotka puolestaan riippuvat
;; nimiavaruudesta aipal.infra.kayttaja.
(ns aipal.infra.kayttaja.vaihto
  (:require [clojure.tools.logging :as log]
            [korma.core :as sql]
            [aipal.asetukset :refer [asetukset]]
            [oph.common.util.util :refer [map-by some-value]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vakiot :refer [jarjestelma-oid integraatio-uid]]
            [aipal.toimiala.kayttajaroolit :refer [ldap-ryhma->rooli roolijarjestys]]
            [aipal.arkisto.kayttaja :as kayttaja-arkisto]
            [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
            [aipal.arkisto.koulutustoimija :as koulutustoimija-arkisto]
            [aipal.infra.kayttaja.sql :refer [with-sql-kayttaja]]
            [arvo.db.core :refer [*db*] :as db]
            [aipal.integraatio.kayttooikeuspalvelu :as kayttooikeuspalvelu]))

(defn kayttajan-nimi [k]
  (str (:etunimi k) " " (:sukunimi k)))

(declare hae-kayttaja-kayttoikeuspalvelusta)

(defn with-kayttaja* [uid impersonoitu-oid rooli f]
  (log/debug "Yritetään autentikoida käyttäjä" uid)
  (if-let [k (db/hae-voimassaoleva-kayttaja {:uid uid :voimassaolo (:kayttooikeus-tarkistusvali @asetukset)})]
    (let [aktiivinen-oid (or impersonoitu-oid (:oid k))
          aktiiviset-roolit (kayttajaoikeus-arkisto/hae-roolit aktiivinen-oid)
          aktiivinen-rooli (or (when rooli (some-value #(= rooli (:rooli_organisaatio_id %)) aktiiviset-roolit))
                               (first (sort-by (comp roolijarjestys :rooli) aktiiviset-roolit)))
          aktiivinen-koulutustoimija (:organisaatio aktiivinen-rooli)
          oppilaitostyypit  (distinct (remove nil? (map :oppilaitostyyppi aktiiviset-roolit)))
          ik (when impersonoitu-oid
               (kayttaja-arkisto/hae impersonoitu-oid))]
      (binding [*kayttaja*
                (assoc k
                       :aktiivinen-oid aktiivinen-oid
                       :aktiiviset-roolit aktiiviset-roolit
                       :aktiivinen-rooli aktiivinen-rooli
                       :aktiivinen-koulutustoimija aktiivinen-koulutustoimija
                       :oppilaitostyypit oppilaitostyypit
                       :nimi (kayttajan-nimi k)
                       :impersonoidun-kayttajan-nimi (if ik (kayttajan-nimi ik) ""))]
        (log/info "Käyttäjä autentikoitu:" (pr-str *kayttaja*))
        (with-sql-kayttaja (:oid k)
          (f))))
    (if (:voimassa (hae-kayttaja-kayttoikeuspalvelusta uid))
      (recur uid impersonoitu-oid rooli f)
      (throw (IllegalStateException. (str "Ei voimassaolevaa käyttäjää " uid))))))

(defmacro with-kayttaja [uid impersonoitu-oid rooli & body]
  `(with-kayttaja* ~uid ~impersonoitu-oid ~rooli (fn [] ~@body)))

(defn hae-kayttaja-kayttoikeuspalvelusta [uid]
  (log/info "Yritetään hakea Käyttöoikeuspalvelusta käyttäjä" uid)
  (with-kayttaja integraatio-uid nil nil
    (let [oid->ytunnus (into {} (map (juxt :oid :ytunnus) (db/hae-oid->ytunnus)))
          kayttaja (kayttooikeuspalvelu/kayttaja uid ldap-ryhma->rooli oid->ytunnus)]
      (if (:voimassa kayttaja)
        (kayttajaoikeus-arkisto/paivita-kayttaja! kayttaja)
        (db/passivoi-kayttaja! {:uid uid}))
      kayttaja)))
