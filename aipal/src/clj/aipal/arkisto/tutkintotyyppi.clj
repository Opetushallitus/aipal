;; Copyright (c) 2016 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.arkisto.tutkintotyyppi
  (:require [arvo.db.core :refer [*db*] :as db]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(defn hae-kaikki []
  (db/hae-tutkintotyypit))

(defn hae-kayttajalle []
  (db/hae-kayttajan-tutkintotyypit {:koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*)}))

(defn ^:integration-api lisaa! [tutkintotyyppi]
  (db/lisaa-tutkintotyyppi! tutkintotyyppi))

(defn ^:integration-api paivita! [tutkintotyyppi]
  (db/paivita-tutkintotyyppi! tutkintotyyppi))