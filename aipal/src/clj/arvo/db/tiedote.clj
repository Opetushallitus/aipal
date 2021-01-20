(ns arvo.db.tiedote
  (:require [arvo.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]))

(defn hae []
  (when-let [tiedote (db/hae-tiedote)]
    {:fi (:teksti_fi tiedote)
     :sv (:teksti_sv tiedote)
     :en (:teksti_en tiedote)}))

(defn poista-ja-lisaa! [tiedote]
  (jdbc/with-db-transaction [tx *db*]
    (db/poista-tiedotteet! tx)
    (db/lisaa-tiedote! tx {:teksti_fi (:fi tiedote)
                           :teksti_sv (:sv tiedote)
                           :teksti_en (:en tiedote)})))

(defn poista! []
  (db/poista-tiedotteet!))
