(ns aipal.toimiala.raportti.vertailuraportti-perf
    (:require 
    [clj-gatling.core :refer [run-simulation]]
    [org.httpkit.client :as http]
    [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
    [cheshire.core :refer :all]
    [aipal.sql.test-util :refer :all])

    (:use clojure.test))

(use-fixtures :each tietokanta-fixture)

(defn muodosta-vertailuraportin-parametri-json []
  (generate-string {:tyyppi "vertailu"
                    :vertailutyyppi "tutkinto"
                    :koulutusalat []
                    :opintoalat []
                    :tutkinnot ["351301"]
                    :koulutustoimijat []
                    :taustakysymysryhmaid 1
                    :kysymykset {"280" {"monivalinnat" {}}
                                 "289" {"monivalinnat" {}}
                                 "301" {"monivalinnat" {}}
                                 "319" {"monivalinnat" {}}
                                 "343" {"monivalinnat" {}}
                                 "379" {"monivalinnat" {}}}
                    :vertailujakso_alkupvm "2013-11-05T22:00:00.000Z"}))
                    
;  {"tyyppi":"vertailu","vertailutyyppi":"tutkinto",
;  "koulutusalat":[],"opintoalat":[],"tutkinnot":["351301"],
;  "koulutustoimijat":[],"taustakysymysryhmaid":1,
;  "kysymykset":{"280":{"monivalinnat":{}},"289":{"monivalinnat":{}},"301":{"monivalinnat":{}},"319":{"monivalinnat":{}},"343":{"monivalinnat":{}},"379":{"monivalinnat":{}}},
;  "vertailujakso_alkupvm":"2013-11-05T22:00:00.000Z"}


(defn async-http-json-requ [url uid basic-auth body user-id context callback]
  (let [check-status (fn [{:keys [status]}] (callback (= 200 status)))]
    (http/post url {:headers {"uid" uid
                              "x-xsrf-token" "token"
                              "Cookie" "cache=true; XSRF-TOKEN=token"
                              "Content-Type" "application/json;charset=UTF-8"
                              }
                    :basic-auth basic-auth
                    :body body
                    }
      check-status)))

(defn valtakunnallinen-perf-fn [base-url userid basic-auth]
  (let [url (str base-url "/api/raportti/valtakunnallinen")
        json (muodosta-vertailuraportin-parametri-json)
        requ-fn (partial async-http-json-requ url userid basic-auth json)
        requ {:name "valtakunnallinen, vertailu" :fn requ-fn}]
    requ))
        
 
(deftest ^:performance vertailuraportti-raportti []
  (let [base-url (or (System/getenv "AIPAL_URL") "http://192.168.50.1:8082")
        userid (or (System/getenv "AIPAL_UID")  "T-1001")
        basic-auth (or (System/getenv "AIPAL_AUTH") "pfft:thx")
        test-reqv (take 4 (repeatedly #(valtakunnallinen-perf-fn base-url userid basic-auth)))]
    (run-simulation
      [{:name "Satunnaistettu vertailuraportin suorituskykytesti"
        :requests test-reqv}]
      4 {:root "target/perf-report/vertailuraportti"
         :timeout-in-ms 20000})))

; http://localhost:8082/api/raportti/valtakunnallinen
; {"tyyppi":"vertailu","vertailutyyppi":"tutkinto","koulutusalat":[],"opintoalat":[],"tutkinnot":["351301"],"koulutustoimijat":[],"taustakysymysryhmaid":1,"kysymykset":{"280":{"monivalinnat":{}},"289":{"monivalinnat":{}},"301":{"monivalinnat":{}},"319":{"monivalinnat":{}},"343":{"monivalinnat":{}},"379":{"monivalinnat":{}}},"vertailujakso_alkupvm":"2013-11-05T22:00:00.000Z"}
; Content-Type:application/json;charset=UTF-8

