(ns aipal.infra.kayttaja
  (:require [clojure.tools.logging :as log]))

(defn validate-user
  [con uid]
  {:pre [(string? uid)]}
  (log/debug "tarkistetaan käyttäjä " uid)
  (with-open [pstmt (doto
                      (.prepareStatement con "select * from kayttaja where uid = ?")
                      (.setString 1 uid))
              rs (.executeQuery pstmt)]
    (let [valid (.next rs)]
      (when-not valid (throw (IllegalArgumentException. (str "Käyttäjä " uid " puuttuu tietokannasta"))))
      (let [voimassa (.getBoolean rs "voimassa")
            oid (.getString rs "oid")]
        (when-not voimassa (throw (IllegalArgumentException. (str "Käyttäjätunnus " uid " ei ole voimassa."))))
        (log/debug (str "user " uid " ok"))
        oid))))
