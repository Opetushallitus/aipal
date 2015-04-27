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

(ns aipal.sql.test-util
  (:import java.util.Locale)
  (:require [korma.core :as sql]
            [korma.db :as db]
            [oph.common.infra.i18n :as i18n]
            [infra.test.data :as testdata]
            [aipal.asetukset :refer [hae-asetukset oletusasetukset]]
            [aipal.integraatio.sql.korma :refer [kayttaja]]
            [aipal.toimiala.kayttajaoikeudet :as ko]
            [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
            oph.korma.common))

(def testikayttaja-uid "MAN-O-TEST")
(def testikayttaja-oid "OID.MAN-O-TEST")
(def testi-locale (Locale. "fi"))

(defn luo-testikayttaja!
  []
  (testdata/luo-testikayttaja! testikayttaja-oid testikayttaja-uid))

(defn poista-testikayttaja!
  []
  (testdata/poista-testikayttaja! testikayttaja-oid))

(defn alusta-korma!
  ([asetukset]
    (let [db-asetukset (merge-with #(or %2 %1)
                         (:db asetukset)
                         {:host (System/getenv "AIPAL_DB_HOST")
                          :port (System/getenv "AIPAL_DB_PORT")
                          :name (System/getenv "AIPAL_DB_NAME")
                          :user (System/getenv "AIPAL_DB_USER")
                          :password (System/getenv "AIPAL_DB_PASSWORD")})]
      (oph.korma.common/luo-db db-asetukset)))
    ([]
    (let [dev-asetukset (assoc oletusasetukset :development-mode true)
          asetukset (hae-asetukset dev-asetukset)]
      (alusta-korma! asetukset))))

(defn tietokanta-fixture-oid
  "Annettu käyttäjätunnus sidotaan Kormalle testifunktion ajaksi."
  [f oid uid]
  (let [pool (alusta-korma!)]
    (luo-testikayttaja!) ; eri transaktio kuin loppuosassa!
    ;; testin aikana eri käyttäjä
    (with-kayttaja uid nil nil
      (binding [i18n/*locale* testi-locale]
        ;; avataan transaktio joka on voimassa koko kutsun (f) ajan
        (db/transaction
          (try
            (testdata/luo-dummy-rivit!)
            (f)
            (finally
              (testdata/tyhjenna-testidata! oid)
              (poista-testikayttaja!))))
        (-> pool :pool :datasource .close)))))

(defn tietokanta-fixture [f]
  (tietokanta-fixture-oid f testikayttaja-oid testikayttaja-uid))

(defmacro testidata-poistaen-kayttajana [oid & body]
  `(tietokanta-fixture-oid (fn [] ~@body) ~oid ~oid))

(defn exec-raw-fixture
  "Alustaa korman ennen testiä ja sulkee tietokantayhteydet testin jälkeen."
  [f]
  (let [pool (alusta-korma!)]
    (try
      (f)
      (finally
        (-> pool :pool :datasource .close)))))
