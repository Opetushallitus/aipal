;; Käyttäjän vaihtoon liittyvä koodi on riippuvuussyklien välttämiseksi omassa
;; nimiavaruudessaan, koska se käyttää arkistoja, jotka puolestaan riippuvat
;; nimiavaruudesta aipal.infra.kayttaja.
(ns aipal.infra.kayttaja.vaihto
  (:require [clojure.tools.logging :as log]
            [korma.core :as sql]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vakiot :refer [jarjestelma-oid]]
            [aipal.arkisto.kayttaja :as kayttaja-arkisto]
            [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]
            [aipal.infra.kayttaja.sql :refer [with-sql-kayttaja]]))

(defn kayttajan-nimi [k]
  (str (:etunimi k) " " (:sukunimi k)))

(defn with-kayttaja* [uid impersonoitu-oid f]
  (log/debug "Yritetään autentikoida käyttäjä" uid)
  (if-let [k (kayttaja-arkisto/hae-voimassaoleva uid)]
    (let [voimassaoleva-oid (or impersonoitu-oid (:oid k))
          voimassaolevat-roolit (kayttajaoikeus-arkisto/hae-roolit voimassaoleva-oid)
          voimassaoleva-organisaatio (kayttaja-arkisto/hae-organisaatio voimassaoleva-oid)
          ik (when impersonoitu-oid
               (kayttaja-arkisto/hae impersonoitu-oid))]
      (binding [*kayttaja*
                (assoc k
                       :voimassaoleva-oid voimassaoleva-oid
                       :voimassaolevat-roolit voimassaolevat-roolit
                       :voimassaoleva-organisaatio voimassaoleva-organisaatio
                       :nimi (kayttajan-nimi k)
                       :impersonoidun-kayttajan-nimi (if ik (kayttajan-nimi ik) ""))]
        (log/info "Käyttäjä autentikoitu:" (pr-str *kayttaja*))
        (with-sql-kayttaja (:oid k)
          (f))))
    (throw (IllegalStateException. (str "Ei voimassaolevaa käyttäjää " uid)))))

(defmacro with-kayttaja [uid impersonoitu-oid & body]
  `(with-kayttaja* ~uid ~impersonoitu-oid (fn [] ~@body)))
