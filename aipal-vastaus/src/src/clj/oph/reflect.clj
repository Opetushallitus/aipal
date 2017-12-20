;; Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

(ns oph.reflect
  (:require  [clojure.test :refer [function?]]))

; Funktio lainattu Tatu Tarvaiselta
(defn- arityt
  "Palauttaa funktion eri arityt. Esim. #{0 1} jos funktio tukee nollan ja yhden parametrin arityjä."
  [f]
  (->> f class .getDeclaredMethods
       (map #(-> % .getParameterTypes alength))
       (into #{})))

(defn no-args?
  "Returns true if f is a var of a function with no arguments. False otherwise."
  [f]
  (cond
    (function? f)
      (= #{0} (arityt f))
    (and
      (seq? f)
      (= 'fn* (first f)))
       (empty? (second f))
    (var? f)
      (= '([]) (:arglists (meta f)))
    :else false))