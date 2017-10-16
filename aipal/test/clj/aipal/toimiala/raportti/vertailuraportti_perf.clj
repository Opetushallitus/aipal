(ns aipal.toimiala.raportti.vertailuraportti-perf
    (:require [clojure.test :refer :all]
              [clj-gatling.core :refer [run-simulation]]
              [cheshire.core :as json]
              [aipal.sql.test-util :refer :all]
              [aipal.toimiala.raportti.perftest-util :refer :all]))

(use-fixtures :each tietokanta-fixture)

(def tutkinnot ["020079" "037413" "048462" "058444" "080401" "080437" "080705" "324128"
                "334103" "334106" "334113" "351105" "351201" "351701" "351803" "354110"
                "354401" "354405" "355102" "355104" "355405" "357204" "357802" "358508"
                "364307" "371104" "371101" "381102" "381504" "387102" "61006"])

(def alkuajat ["2013-11-05T22:00:00.000Z" "2012-11-05T22:00:00.000Z" "2014-01-05T22:00:00.000Z"])

(def koulutustoimijat ["0128756-8" "0212371-7" "2107936-0" "0871305-6" "1958694-5" "0796234-1" "0222804-1"
                       "0172730-8" "2162576-3" "0205303-4" "0204964-1" "0932749-9" "0213834-5" "0166055-3"])

(defn muodosta-tutkintovertailuraportin-parametrit
  "Otetaan satunnainen aikajakso ja tutkinto vertailua varten"
  []
  {:tyyppi "vertailu"
   :tutkintorakennetaso "tutkinto"
   :koulutusalat []
   :opintoalat []
   :tutkinnot [(rand-nth tutkinnot)]
   :koulutustoimijat []
   :taustakysymysryhmaid 1
   :kysymykset {"280" {"monivalinnat" {}}
                "289" {"monivalinnat" {}}
                "301" {"monivalinnat" {}}
                "319" {"monivalinnat" {}}
                "343" {"monivalinnat" {}}
                "379" {"monivalinnat" {}}}
   :vertailujakso_alkupvm (rand-nth alkuajat)})

(defn muodosta-koulutustoimijavertailuraportin-parametrit
  "Otetaan 2-4kpl satunnaisia koulutustoimijoita vertailua varten"
  []
  (let [base-map (muodosta-tutkintovertailuraportin-parametrit)
        toimija-lkm (+ 2 (rand 2))]
    (merge base-map
      {:tutkinnot []
       :koulutustoimijat (vec (take toimija-lkm (repeatedly #(rand-nth koulutustoimijat))))})))

(defn tutkintovertailu-perf-fn [config]
  (let [url (str (:base-url config) "/api/raportti/valtakunnallinen")
        json (json/generate-string (muodosta-tutkintovertailuraportin-parametrit))]
    (url->http-post-fn config url "valtakunnallinen, tutkintovertailu" json)))

(defn toimijavertailu-perf-fn [config]
  (let [url (str (:base-url config) "/api/raportti/valtakunnallinen")
        json (json/generate-string (muodosta-koulutustoimijavertailuraportin-parametrit))]
    (url->http-post-fn config url "valtakunnallinen, koulutustoimijavertailu" json)))

(deftest ^:performance vertailuraportti-raportti
  (let [config (get-configuration)
        raportti-lkm (:request-count config)
        concurrent-users 3
        tutkinto-reqs (take raportti-lkm (repeatedly #(tutkintovertailu-perf-fn config)))
        toimija-reqs (take raportti-lkm (repeatedly #(toimijavertailu-perf-fn config)))
        test-reqv (concat tutkinto-reqs toimija-reqs)]
    (run-simulation
      [{:name "Satunnaistettu vertailuraportin suorituskykytesti"
        :requests test-reqv}]
      concurrent-users {:root "target/perf-report/valtakunnallinen-vertailu"
                        :timeout-in-ms 60000})))

; yksittäisen tutkinnon vertailu
; http://localhost:8082/api/raportti/valtakunnallinen
; {"tyyppi":"vertailu","tutkintorakennetaso":"tutkinto","koulutusalat":[],"opintoalat":[],"tutkinnot":["351301"],"koulutustoimijat":[],"taustakysymysryhmaid":1,"kysymykset":{"280":{"monivalinnat":{}},"289":{"monivalinnat":{}},"301":{"monivalinnat":{}},"319":{"monivalinnat":{}},"343":{"monivalinnat":{}},"379":{"monivalinnat":{}}},"vertailujakso_alkupvm":"2013-11-05T22:00:00.000Z"}
; Content-Type:application/json;charset=UTF-8

; koulutustoimijoiden vertailu
; {"tyyppi":"koulutustoimijat","tutkintorakennetaso":"tutkinto","koulutusalat":[],"opintoalat":[],"tutkinnot":[],"koulutustoimijat":["9090160-2","0868699-1","0871202-9"],"taustakysymysryhmaid":1,"kysymykset":{"280":{"monivalinnat":{}},"289":{"monivalinnat":{}},"301":{"monivalinnat":{}},"319":{"monivalinnat":{}},"343":{"monivalinnat":{}},"379":{"monivalinnat":{}}},"vertailujakso_alkupvm":"2013-11-05T22:00:00.000Z"}
