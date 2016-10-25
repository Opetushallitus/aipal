(ns aipal.toimiala.raportti.util)


(defn numeroiden-piste-pilkuksi
  "Jos merkkijono on numero, niin muutetaan piste pilkuksi"
  [merkkijono]
  (if (re-matches #"[0-9.]+" merkkijono)
    (clojure.string/replace merkkijono #"\." ",")
    merkkijono))

(defn muuta-kaikki-stringeiksi [rivit]
  (clojure.walk/postwalk (fn [x]
                           (if (coll? x)
                             x
                             (numeroiden-piste-pilkuksi (str x))))
                         rivit))
