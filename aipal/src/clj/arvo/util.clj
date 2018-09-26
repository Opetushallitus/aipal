(ns arvo.util)

(defn in? [coll elem]
  (some #(= elem %) coll))
