(ns aipal.infra.eraajo.automaattikyselyt
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [clj-time.core :as time]
            [arvo.db.core :refer [*db*] :as db])
  (:import (org.quartz Job)))

(defn get-rahoituskauden-alku [kyselytyyppi]
  (if (= "amispalaute" kyselytyyppi)
;    Amispalaute rahoituskausi vaihtuu 1.7.
    (time/local-date (time/year (time/today)) 7 1)
;    Muuten oletetaan vuodenvaihde
    (time/local-date (+ 1 (time/year (time/today))) 1 1)))

(defn get-rahtoituskauden-loppu [kyselytyyppi]
  (.minusDays (get-rahoituskauden-alku kyselytyyppi) 1))

(defn luo-kysely! [koulutustoimija kuvaus tx]
  (let [kyselyid (:kyselyid (first (db/luo-kysely! tx (merge kuvaus {:tila "julkaistu" :koulutustoimija (:ytunnus koulutustoimija)
                                                                     :kayttaja "JARJESTELMA" :tyyppi (:kyselytyyppi kuvaus)
                                                                     :kategoria {:automatisointi_tunniste (:tunniste kuvaus)}}))))]
    (db/liita-kyselyn-kyselypohja! tx {:kyselyid kyselyid :kyselypohjaid (:kyselypohjaid kuvaus) :kayttaja "JARJESTELMA"})
    (db/liita-kyselyn-kysymykset! tx {:kyselyid kyselyid :kayttaja "JARJESTELMA"})
    (db/paata-kyselykerrat! tx {:tyyppi (:kyselytyyppi kuvaus) :koulutustoimija (:ytunnus koulutustoimija) :paattymis_pvm (get-rahtoituskauden-loppu (:kyselytyyppi kuvaus))})
    (db/luo-kyselykerta! tx {:kyselyid kyselyid :nimi (:kyselykerta_nimi kuvaus) :kayttaja "JARJESTELMA"
                             :automaattinen (format "[%s,]" (get-rahoituskauden-alku (:kyselytyyppi kuvaus))) :kategoria (:kyselykerta_kategoria kuvaus) :voimassa_alkupvm (:voimassa_alkupvm kuvaus)})
    kyselyid))

(defn luo-kyselyt! [kuvaus tx]
  (let [koulutustoimijat (db/hae-automaattikysely-koulutustoimijat tx kuvaus)
        _ (log/info "Luodaan automaattikyselyt ("(:tunniste kuvaus)")" (count koulutustoimijat) "koulutustoimijalle")]
    (doseq[k koulutustoimijat]
      (luo-kysely! k kuvaus tx))))

(defn luo-automaattikyselyt! []
  (let [kuvaukset (db/hae-automaattikysely-data)]
    (jdbc/with-db-transaction [tx *db*]
      (doseq [kuvaus kuvaukset]
        (luo-kyselyt! kuvaus tx)))))

(defrecord LuoAutomaattikyselytJob []
  Job
  (execute [this ctx]
    (try
      (do (luo-automaattikyselyt!)
          (log/info "Automaattikyselyiden luonti valmis"))
      (catch Exception e
        (log/error e "Automaattikyselyiden luonti epäonnistui")))))
