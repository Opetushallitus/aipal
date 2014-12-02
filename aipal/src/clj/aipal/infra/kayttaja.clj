(ns aipal.infra.kayttaja
  (:require [clojure.tools.logging :as log]))

(def ^:dynamic *kayttaja*)

(defn sisaltaa-jonkin-rooleista? [roolit roolirivit]
  (not (empty? (clojure.set/select roolit (set (map :rooli roolirivit))))))

(defn kayttajalla-on-jokin-rooleista? [roolit]
  (sisaltaa-jonkin-rooleista? roolit (:aktiiviset-roolit *kayttaja*)))

(defn yllapitaja? []
  (kayttajalla-on-jokin-rooleista?
    #{"YLLAPITAJA"}))

(defn vastuukayttaja? []
  (kayttajalla-on-jokin-rooleista?
    #{"OPL-VASTUUKAYTTAJA"}))
