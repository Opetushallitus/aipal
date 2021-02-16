(ns arvo.service.osio-tunnukset
  (:require [arvo.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [aipal.arkisto.vastaajatunnus :refer [get-vastaajatunnukset]]
            [clojure.tools.logging :as log]))


(defn get-tunnus []
  (first (get-vastaajatunnukset 1)))

(defn format-tunnus [kyselykertaid taustatiedot metatiedot]
  {:tunnus (get-tunnus)
   :kyselykertaid kyselykertaid
   :kieli nil
   :tutkinto nil
   :taustatiedot taustatiedot
   :metatiedot metatiedot
   :kohteiden_lkm 1
   :valmistavan_koulutuksen_oppilaitos nil
   :voimassa_alkupvm nil
   :voimassa_loppupvm nil
   :kayttaja "JARJESTELMA"})

(defn luo-tunnukset [tx koulutustoimija ohjaus-kyselykerta alikyselyt]
    (let [_ (log/info "Luodaan tunnukset koulutustoimijalle " koulutustoimija)
          osio-tunnukset (for [osio-kyselykerta alikyselyt]
                           (db/lisaa-vastaajatunnus! tx (format-tunnus osio-kyselykerta {:koulutustoimija koulutustoimija} nil)))]
      (db/lisaa-vastaajatunnus! tx (format-tunnus ohjaus-kyselykerta
                                                  {:koulutustoimija koulutustoimija}
                                                  {:osio_tunnukset (map :vastaajatunnusid (flatten osio-tunnukset))}))))

(defn luo-osio-tunnukset [kyselykertaid osiot]
  (jdbc/with-db-transaction [tx *db*]
    (let [kohteet (map :koulutustoimija (db/hae-ammatilliset-koulutustoimijat))]
      (doseq [koulutustoimija kohteet]
        (luo-tunnukset tx koulutustoimija kyselykertaid osiot))
      "ok")))