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

(ns aipal-e2e.data-util
  (:require [aipal-e2e.tietokanta.data]))

(def ^:private entity-tiedot {})

(defn ^:private luo
  [entityt luo-fn]
  (doseq [entity entityt]
    (luo-fn entity)))

(defn ^:private poista
  [entityt poista-fn]
  (doseq [entity entityt]
    (poista-fn entity)))

(def ^:private taulut
  [])

(defn ^:private taydenna-data
  [data]
  (into {}
        (for [[taulu entityt] data
              :let [default (get-in entity-tiedot [taulu :default])]]
          {taulu (map merge default entityt)})))

(defn with-data*
  [data body-fn]
  (let [taydennetty-data (taydenna-data data)]
    (doseq [taulu taulut
            :let [data (taydennetty-data taulu)
                  luo-fn (get-in entity-tiedot [taulu :luo-fn])]
            :when data]
      (luo data luo-fn))
    (try
      (body-fn)
      (finally
        (doseq [taulu (reverse taulut)
                  :let [data (taydennetty-data taulu)
                        poista-fn (get-in entity-tiedot [taulu :poista-fn])]
                  :when data]
            (poista data poista-fn))))))

(defmacro with-data
  [data & body]
  `(with-data* ~data (fn [] ~@body)))
