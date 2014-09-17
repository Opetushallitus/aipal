;; Käyttäjän vaihtoon liittyvä koodi on riippuvuussyklien välttämiseksi omassa
;; nimiavaruudessaan, koska se käyttää käyttäjäarkistoa, joka puolestaan riippuu
;; nimiavaruudesta aipal.infra.kayttaja.
(ns aipal.infra.kayttaja.vaihto
  (:require [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.arkisto.kayttaja :as arkisto]))

(defn with-kayttaja* [uid impersonoitu-oid f]
  ;; Poolista ei saa yhteyttä ilman että *kayttaja* on sidottu, joten tehdään
  ;; käyttäjän tietojen haku käyttäjänä JARJESTELMA.
  (if-let [k (binding [*kayttaja* {:oid "JARJESTELMA"}]
               (arkisto/hae-voimassaoleva uid))]
    (binding [*kayttaja* (assoc k
                                :voimassaoleva-oid (or impersonoitu-oid (:oid k))
                                :nimi (str (:etunimi k) " " (:sukunimi k)))]
      (f))
    (throw (IllegalStateException. (str "Ei voimassaolevaa käyttäjää " uid)))))

(defmacro with-kayttaja [uid impersonoitu-oid & body]
  `(with-kayttaja* ~uid ~impersonoitu-oid (fn [] ~@body)))
