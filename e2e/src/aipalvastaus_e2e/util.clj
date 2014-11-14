
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

(ns aipalvastaus-e2e.util
  (:require [aitu-e2e.util :refer [avaa-url]]
            [aipalvastaus-e2e.data-util :as data-util]))

(defn vastaus-url [polku]
  (str (or (System/getenv "VASTAUS_URL")
           "http://localhost:8083")
       polku))

(defn avaa [polku]
  (avaa-url (vastaus-url polku)))

(defn vastaajatunnus-url->tunnus [url]
 (re-find #"[a-zA-Z0-9]*$" url))

(defn poista-vastaajat-ja-vastaukset-vastaustunnukselta! [vastaustunnus-url]
  (data-util/poista-vastaajat-ja-vastaukset-vastaustunnukselta*!
    (vastaajatunnus-url->tunnus vastaustunnus-url)))
