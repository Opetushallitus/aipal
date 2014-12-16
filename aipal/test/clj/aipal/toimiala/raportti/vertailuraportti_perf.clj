(ns aipal.toimiala.raportti.vertailuraportti-perf
    (:require 
    [aipal.toimiala.raportti.perftest-util :refer :all]
    [clj-gatling.core :refer [run-simulation]]
    [cheshire.core :refer :all]
    [aipal.sql.test-util :refer :all])

    (:use clojure.test))

(use-fixtures :each tietokanta-fixture)

(def tutkinnot ["020079" "037413" "048462" "058444" "080401" "080437" "080705" "324128"
                "334103" "334106" "334113" "351105" "351201" "351701" "351803" "354110"
                "354401" "354405" "355102" "355104" "355405" "357204" "357802" "358508" 
                "364307" "371104" "371101" "381102" "381504" "387102" "61006"])

(def alkuajat ["2013-11-05T22:00:00.000Z" "2012-11-05T22:00:00.000Z" "2014-01-05T22:00:00.000Z"])

(defn muodosta-vertailuraportin-parametri-json []
  (generate-string {:tyyppi "vertailu"
                    :vertailutyyppi "tutkinto"
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
                    :vertailujakso_alkupvm (rand-nth alkuajat)}))

(defn valtakunnallinen-perf-fn [base-url userid basic-auth]
  (let [url (str base-url "/api/raportti/valtakunnallinen")
        json (muodosta-vertailuraportin-parametri-json)
        requ-fn (partial async-http-json-requ url userid basic-auth json)
        requ {:name "valtakunnallinen, vertailu" :fn requ-fn}]
    requ))
 
(deftest ^:performance vertailuraportti-raportti []
  (let [config (get-configuration)
        test-reqv (take 35 (repeatedly #(valtakunnallinen-perf-fn (:base-url config) 
                                          (:userid config) 
                                          (:basic-auth config))))]
    (run-simulation
      [{:name "Satunnaistettu vertailuraportin suorituskykytesti"
        :requests test-reqv}]
      3 {:root "target/perf-report/valtakunnallinen-vertailu"
         :timeout-in-ms 20000})))

; http://localhost:8082/api/raportti/valtakunnallinen
; {"tyyppi":"vertailu","vertailutyyppi":"tutkinto","koulutusalat":[],"opintoalat":[],"tutkinnot":["351301"],"koulutustoimijat":[],"taustakysymysryhmaid":1,"kysymykset":{"280":{"monivalinnat":{}},"289":{"monivalinnat":{}},"301":{"monivalinnat":{}},"319":{"monivalinnat":{}},"343":{"monivalinnat":{}},"379":{"monivalinnat":{}}},"vertailujakso_alkupvm":"2013-11-05T22:00:00.000Z"}
; Content-Type:application/json;charset=UTF-8

