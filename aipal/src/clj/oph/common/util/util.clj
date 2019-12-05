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

(ns oph.common.util.util
  "Yleisiä apufunktioita."
  (:require [cheshire.core :as cheshire]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [clojure.string :as string]
            [clj-http.client :as http]
            [clojure.set :refer [union]]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.tools.logging :as log]))

(defn oletus-header [options]
  (assoc-in options [:headers "Caller-Id"] "1.2.246.562.10.2013112012294919827487.arvo"))

;; http://clojuredocs.org/clojure_contrib/clojure.contrib.map-utils/deep-merge-with
(defn deep-merge-with
  "Like merge-with, but merges maps recursively, applying the given fn
  only when there's a non-map at a particular level.

  (deepmerge + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
               {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
  [f & maps]
  (apply
    (fn m [& maps]
      (if (every? map? maps)
        (apply merge-with m maps)
        (apply f maps)))
    maps))

(defn last-arg [& args]
  (last args))

(def deep-merge (partial deep-merge-with last-arg))

(defn pisteavaimet->puu [m]
  (reduce #(let [[k v] %2
                 polku (map keyword (.split (name k) "\\."))]
             (assoc-in %1 polku v))
          {}
          m))

(defn get-in-list
  "Like get-in, but also inspects each item of a list"
  ([m ks]
   (get-in-list m ks nil))
  ([m ks not-found]
   (loop [sentinel (Object.)
          m m
          ks (seq ks)]
     (if ks
       (let [m (get m (first ks) sentinel)
             ks (next ks)]
         (cond
           (identical? sentinel m) not-found
           (map? m) (recur sentinel m ks)
           (coll? m) (flatten (for [val m] (get-in-list val ks not-found)))
           :else (recur sentinel m ks)))
       m))))

(defn update-vals
  [f c]
  (reduce #(assoc %1 %2 (f (%1 %2)))
          c (vec (keys c))))

(defn deep-update-vals
  [f c]
  (reduce #(let [val (%1 %2)]
             (assoc %1 %2
                    (if (map? val)
                      (deep-update-vals f val)
                      (f val))))
          c (vec (keys c))))

(defn max-date
  ([a] a)
  ([a b]
   (if (< 0 (compare a b))
     a
     b))
  ([a b & more]
   (reduce max-date (max-date a b) more)))

(defn min-date
  ([a] a)
  ([a b]
   (if (> 0 (compare a b))
     a
     b))
  ([a b & more]
   (reduce min-date (min-date a b) more)))

(defn paths
  "palauttaa joukon jossa on kaikki polut mapin sisään. Ts. rekursiivinen mapin rakenteen kuvaus"
  ([m]
   (set (paths m [])))
  ([m ks]
   (apply concat
          (for [[k v] m
                :let [path (conj ks k)]]
            (if (map? v)
              (conj (paths v path) path)
              [path])))))

(defn parse-ymd
  [ymd]
  (time-format/parse-local-date (time-format/formatters :year-month-day) ymd))

(defn get-json-from-url
  ([url]
   (get-json-from-url url {}))
  ([url options]
   (->
    (http/get url (oletus-header options))
    :body
    cheshire/parse-string
    keywordize-keys)))

(defn post-json-from-url
  ([url]
   (get-json-from-url url {}))
  ([url options]
   (->
    (http/post url (oletus-header options))
    :body
    cheshire/parse-string
    keywordize-keys)))

(defn uusin-muokkausaika
  "Palauttaa uusimman muokkausajan annetuista arvoista.
   Polut ovat get-in-tyylisiä avainpolkuja, jotka kertovat mistä muokkausajat haetaan."
  [arvot & polut]
  (let [muokkausajat (flatten
                       (for [arvo arvot
                             polku polut]
                         (get-in-list arvo polku)))]
    (if (empty? arvot)
      (time/date-time 1970 1 1 0 0 1)
      (reduce max-date muokkausajat))))

(defn uusin-muokkausaika-tai-nykyhetki
  [arvot & polut]
  (or (uusin-muokkausaika arvot polut) (time/now)))

(defn sisaltaako-kentat?
  "Predikaatti joka palauttaa true, jos annettujen kenttien sisältö sisältää annetun termin. Kirjainkoolla ei ole väliä.
 Kenttien sisältö konkatenoidaan yhteen välilyönnillä erotettuna."
  [entity kentat termi]
  (let [termi (string/lower-case termi)
        kenttien-sisallot (for [kentta kentat] (entity kentta))
        sisalto (string/lower-case (string/join " " kenttien-sisallot))]
    (.contains sisalto termi)))

(defn some-value [pred coll]
  (first (filter pred coll)))

(defn some-value-with [f expected coll]
  (some-value #(= (f %) expected) coll))

(defn map-by
  "Kuten group-by, mutta jättää vain viimeisen täsmäävän alkion"
  [f coll]
  (into {} (for [item coll
                 :let [k (f item)]
                 :when (not (nil? k))]
             [k item])))

(defn map-values
  "Applies f to each value in m and returns the resulting map"
   [f m]
  (into (empty m) (for [[k v] m]
                    [k (f v)])))

(defn retrying* [expected-throwable attempts f]
  {:pre [(isa? expected-throwable Throwable)
         (pos? attempts)]}
  (if (= attempts 1)
    (f)
    (try
      (f)
      (catch Throwable t
        (if (instance? expected-throwable t)
          (let [attempts-left (dec attempts)]
            (log/warn t "Operaatio epäonnistui, yritetään uudelleen vielä"
                      (if (= attempts-left 1)
                        "kerran"
                        (str attempts-left " kertaa")))
            (when (instance? java.sql.BatchUpdateException t)
              (log/warn (.getNextException t) "getNextException:"))
            (retrying* expected-throwable attempts-left f))
          (throw t))))))

(defmacro retrying [expected-throwable attempts & body]
  `(retrying* ~expected-throwable ~attempts (fn [] ~@body)))

(def time-forever (time/local-date 2199 1 1))

(defn pvm-menneisyydessa?
  [pvm]
  {:pre [(not (nil? pvm))]}
  (let [nytpvm (time/today)]
    (time/after? nytpvm pvm)))

(defn pvm-tulevaisuudessa?
  [pvm]
  {:pre [(not (nil? pvm))]}
  (let [nytpvm (time/today)]
    (time/before? nytpvm pvm)))

(def pvm-mennyt-tai-tanaan?
  (complement pvm-tulevaisuudessa?))

(def pvm-tuleva-tai-tanaan?
  (complement pvm-menneisyydessa?))

(defn merkitse-voimassaolevat
  [entity avain f]
  (update-in entity [avain]
    #(vec
       (for [arvo %]
         (assoc arvo :voimassa (f entity arvo))))))

(defn paivita-arvot [m avaimet f]
  (reduce #(update-in % [%2] f) m avaimet))

(defn poista-tyhjat [m]
  (into {} (filter (comp #(or (instance? Boolean %) (number? %) (not-empty %)) val) m)))

(defn keyword-syntax?
  "Keywordit muutetaan samalla syntaksilla kun wrap-keyword-params:
   https://github.com/ring-clojure/ring/blob/1.3.1/ring-core/src/ring/middleware/keyword_params.clj"
  [s]
  (when (string? s)
    (re-matches #"[A-Za-z*+!_?-][A-Za-z0-9*+!_?-]*" s)))

(defn muunna-avainsanoiksi
  "Päivitetty versio clojure.walk/keywordize-keys funktiosta, koska alkuperäinen muuntaa myös numerolla alkavan symbolin avainsanaksi."
  [m]
  (let [f (fn [[k v]] (if (keyword-syntax? k) [(keyword k) v] [k v]))]
    (clojure.walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn ->vector [item]
  (when item
    (if (vector? item)
      item
      [item])))

(defn update-in-if-exists [m [k & ks] f & args]
  (if (and (associative? m) (contains? m k))
    (if ks
      (assoc m k (apply update-in-if-exists (get m k) ks f args))
      (assoc m k (apply f (get m k) args)))
    m))

(defn select-and-rename-keys
  "Poimii mapista annetut avaimet. Jos avain on muotoa [:a :b], vaihtaa samalla avaimen nimen :a -> :b."
  [map keys]
  (loop [ret {} keys (seq keys)]
    (if keys
      (let [key (first keys)
            [from to] (if (coll? key)
                        key
                        [key key])
            entry (. clojure.lang.RT (find map from))]
        (recur
          (if entry
            (conj ret [to (val entry)])
            ret)
          (next keys)))
      ret)))

(defn remove-nil-vals [m]
  (into {} (remove (comp nil? val) m)))

(defn diff-maps
  "Palauttaa kahden mapin erot muodossa {avain [uusi-arvo vanha-arvo]} tai avaimen arvona nil jos muutoksia ei ole."
  [new-map old-map]
  (into {} (for [k (union (set (keys old-map))
                          (set (keys new-map)))
                 :let [old-v (get old-map k)
                       new-v (get new-map k)]]
             [k (when (not= old-v new-v)
                  [new-v old-v])])))

(defn muutos
  "Palauttaa kahden mapin eroavaisuudet"
  [old-map new-map]
  (into {} (for [[k [new-v old-v]] (remove-nil-vals (diff-maps new-map old-map))]
             [k [old-v new-v]])))
