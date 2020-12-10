(ns aipal.infra.kayttaja
  (:require [clojure.tools.logging :as log]
            clojure.set))

(def ^:dynamic *kayttaja*)

(defn kayttajalla-on-jokin-rooleista? [roolit]
  (let [aktiivinen-rooli (get-in *kayttaja* [:aktiivinen-rooli :rooli])]
    (boolean (some #{aktiivinen-rooli} roolit))))

(defn yllapitaja? []
  (kayttajalla-on-jokin-rooleista?
    #{"YLLAPITAJA"}))

(defn vastuukayttaja? []
  (kayttajalla-on-jokin-rooleista?
    #{"OPL-VASTUUKAYTTAJA"}))

