;; Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
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

(ns aipal-db.koulutustoimijakayttajat
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [clojure-csv.core :as csv]))

;;; vvvvv aipal.arkisto.vastaajatunnus vvvvv

(def sallitut-merkit "ACEFHJKLMNPRTWXY347")

(defn luo-satunnainen-tunnus
  [pituus]
  {:post [(and
            (string? %)
            (= pituus (.length %)))]}
  (apply str (take pituus (repeatedly #(rand-nth sallitut-merkit)))))

(defn luo-tunnuksia
  "Luo keskenään uniikkeja satunnaisia määritellyn pituisia tunnuksia."
  [pituus]
  (distinct
    (repeatedly 10000 #(luo-satunnainen-tunnus pituus))))

;;; ^^^^^ aipal.arkisto.vastaajatunnus ^^^^^

(defn prefix [s n]
  (.substring s 0 (min (.length s) n)))

(defn hae-koulutustoimijat [db-spec]
  (jdbc/query db-spec [(str "SELECT DISTINCT ytunnus, nimi_fi "
                            "FROM kysely_organisaatio_view "
                            "INNER JOIN koulutustoimija ON ytunnus = koulutustoimija ")]))

(def roolit {"OPL-VASTUUKAYTTAJA" "Vastuukäyttäjä"
             "OPL-KAYTTAJA" "Käyttäjä"})

(defn koulutustoimija-rooli-uidit [db-spec]
  (let [koulutustoimija-roolit (for [koulutustoimija (hae-koulutustoimijat db-spec)
                                     rooli roolit]
                                 [koulutustoimija rooli])
        uidit (luo-tunnuksia 6)]
    (assert (>= (count uidit) (count koulutustoimija-roolit)))
    (map conj koulutustoimija-roolit uidit)))

(defn koulutustoimijakayttajat [db-uri]
  (doall
    (let [db-spec {:connection-uri db-uri}]
      (for [[{koulutustoimijan-nimi :nimi_fi
              koulutustoimijan-y-tunnus :ytunnus}
             [rooli roolin-nimi]
             uid]
            (koulutustoimija-rooli-uidit db-spec)
            :let [oid (str koulutustoimijan-y-tunnus "." rooli)
                  etunimi roolin-nimi
                  sukunimi (prefix koulutustoimijan-nimi 99)]]
        [(str "INSERT INTO kayttaja (oid, uid, etunimi, sukunimi, voimassa) "
              "VALUES ('" oid "', '" uid "', '" etunimi "', '" sukunimi "', true);\n"
              "INSERT INTO rooli_organisaatio (organisaatio, rooli, kayttaja, voimassa) "
              "VALUES ('" koulutustoimijan-y-tunnus "', '" rooli "', '" oid "', true);\n")
         [koulutustoimijan-nimi roolin-nimi uid]]))))

(defn kirjoita-koulutustoimijakayttajat [db-uri sql-polku csv-polku]
  (let [kt (koulutustoimijakayttajat db-uri)
        sqlt (map first kt)
        csvt (map second kt)]
    (binding [*out* (io/writer sql-polku)]
      (doall (map print sqlt)))
    (binding [*out* (io/writer csv-polku)]
      (print (csv/write-csv csvt)))))

(comment
  (kirjoita-koulutustoimijakayttajat
    "jdbc:postgresql://127.0.0.1:3456/aipal?user=aipal_adm&password=aipal-adm"
    "/tmp/koulutustoimijakayttajat.sql"
    "/tmp/koulutustoimijakayttajat.csv"))
