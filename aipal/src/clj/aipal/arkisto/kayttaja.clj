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

(ns aipal.arkisto.kayttaja
  (:require [arvo.db.core :refer [*db*] :as db]
            [oph.common.util.util :refer [sisaltaako-kentat?]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(defn hae [oid]
  (db/hae-kayttaja {:kayttajaOid oid}))

(defn olemassa? [k]
  (boolean (hae (:oid k))))

(defn hae-impersonoitava-termilla [termi]
  (for [kayttaja (db/hae-impersonoitavat-kayttajat)
        :when (sisaltaako-kentat? kayttaja [:etunimi :sukunimi] termi)]
    {:nimi (str (:etunimi kayttaja) " " (:sukunimi kayttaja) " (" (:uid kayttaja) ")")
     :oid (:oid kayttaja)}))
