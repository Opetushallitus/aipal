;; Käyttäjän vaihtoon liittyvä koodi on riippuvuussyklien välttämiseksi omassa
;; nimiavaruudessaan, koska se käyttää arkistoja, jotka puolestaan riippuvat
;; nimiavaruudesta aipal.infra.kayttaja.
(ns aipal.infra.kayttaja.vaihto
  (:require [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.arkisto.kayttaja :as kayttaja-arkisto]
            [aipal.arkisto.kayttajaoikeus :as kayttajaoikeus-arkisto]))

(defn with-kayttaja* [uid impersonoitu-oid f]
  ;; Poolista ei saa yhteyttä ilman että *kayttaja* on sidottu, joten tehdään
  ;; käyttäjän tietojen haku käyttäjänä JARJESTELMA.
  (if-let [k (binding [*kayttaja* {:oid "JARJESTELMA"}]
               (kayttaja-arkisto/hae-voimassaoleva uid))]
    (let [voimassaoleva-oid (or impersonoitu-oid (:oid k))
          voimassaolevat-roolit (binding [*kayttaja* {:oid "JARJESTELMA"}]
                                  (kayttajaoikeus-arkisto/hae-roolit voimassaoleva-oid))]
      (binding [*kayttaja*
                (assoc k
                       :voimassaoleva-oid voimassaoleva-oid
                       :voimassaolevat-roolit voimassaolevat-roolit
                       :nimi (str (:etunimi k) " " (:sukunimi k)))]
        (f)))
    (throw (IllegalStateException. (str "Ei voimassaolevaa käyttäjää " uid)))))

(defmacro with-kayttaja [uid impersonoitu-oid & body]
  `(with-kayttaja* ~uid ~impersonoitu-oid (fn [] ~@body)))
