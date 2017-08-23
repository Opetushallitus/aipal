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

(ns aipal.auditlog-test
   (:require
     [clojure.test :refer :all]
     [clj-time.local :as time-local]
     [oph.log-util :as log-util]
     [oph.common.infra.asetukset :refer [konfiguroi-lokitus]]
     [oph.common.infra.common-audit-log :as common-audit-log]
     [oph.common.infra.common-audit-log-test :as common-audit-log-test]
     [aipal.infra.kayttaja :as kayttaja]
     [aipal.asetukset :refer [oletusasetukset]]
     [aipal.auditlog :as auditlog]))

(defn ^:private log-through-with-mock-user
  [f]
  (binding [kayttaja/*kayttaja* (promise)
            common-audit-log/*request-meta* common-audit-log-test/test-request-meta]
    (deliver kayttaja/*kayttaja* {:oid "T-X-oid"})
    (konfiguroi-lokitus oletusasetukset)  ;; Tarpeen, jotta käytetty loglevel (info) enabloituu
    (common-audit-log/konfiguroi-common-audit-lokitus
      (common-audit-log-test/test-environment-meta "aipal"))
    (log-util/log-through f)))

(deftest test-kyselypohja-luonti
  (testing "logittaa oikein kyselypohjan luonnin"
    (let [msg (second (first (log-through-with-mock-user
                               #(auditlog/kyselypohja-luonti! 123 "Kyselypohjan-nimi"))))]
      (is (and
            (.contains msg
              "{\"operation\":\"lisäys\",\"type\":\"log\",\"hostname\":\"host\",\"applicationType\":\"virkailija\",\"delta\":[{\"op\":\"lisäys\",\"path\":\"nimi\",\"value\":\"Kyselypohjan-nimi\"}],")
            (.contains msg
              "\"logSeq\":")  ;; ;; ei tarkasteta arvoa, sillä se on vaihtuva
            (.contains msg
              "\"bootTime\":\"1980-09-20T01:02:03.123\",")
            (.contains msg
              "\"timestamp\":")  ;; ei tarkasteta arvoa, sillä se on vaihtuva
            (.contains msg
              "\"target\":{\"kyselypohja\":null,\"id\":\"123\"},\"serviceName\":\"aipal\",\"version\":1,")
            (.contains msg
               "\"user\":{\"oid\":\"T-X-oid\",\"ip\":\"192.168.50.1\",\"session\":\"955d43a3-c02d-4ab8-a61f-141f29c44a84\",\"userAgent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36\"}}")
           )))))
