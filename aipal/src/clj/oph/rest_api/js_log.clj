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

(ns oph.rest_api.js-log
  (:require [compojure.core :as c]
            [aipal.compojure-util :as cu]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))

;; max length of message strings from the client side
(def maxlength 1000)

(defn sanitize
  "replaces linefeeds with blanks and limits the length"
  [s]
  {:pre [clojure.core/string?]}
  (let [ln (min (.length s) maxlength)]
    (-> s
      (str/replace "\n" "!")
      (str/replace "\r" "!")
      (.substring 0 ln))))

(defn logita
  "Tarkoitus on wrapata tämä sopivaan compojure-reittiin"
  [virheenUrl userAgent virheviesti stackTrace cause]
  (log/info (str "\n--- Javascript virhe ---\n"
                 "Virheen url: " (sanitize virheenUrl) "\n"
                 "User agent string: " (sanitize userAgent) "\n"
                 "Virheviesti: " (sanitize virheviesti) "\n"
                 "Stacktrace: " (sanitize stackTrace) "\n"
                 "Aiheuttaja: " (sanitize cause) "\n"
                 "------------------------"))
  {:status 200})
