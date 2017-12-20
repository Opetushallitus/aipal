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

(ns oph.common.infra.common-audit-log
  "Yhteinen audit-lokituksen abstrahoiva rajapinta."
  (:require
    [clojure.tools.logging :as log]
    [schema.core :as s]
    [cheshire.core :as cheshire]
    [cheshire.generate :as json-gen]
    [clj-time.core :as time]
    [clj-time.local :as time-local]
    ))

;; Nämä otetaan ring-wrapperilla requestista
(s/defschema Request-meta {:ip         s/Str
                           :session    s/Str
                           :user-agent s/Str})

(s/defschema Env-meta {:boot-time        s/Any
                       :hostname         s/Str
                       :service-name     s/Str
                       :application-type (s/enum "oppija" "virkailija")})

(s/defschema Logientry {:operation (s/enum :kirjautuminen :lisays :paivitys :poisto)
                        :user {:oid  s/Str}
                        :resource    s/Str           ;; taulun nimi
                        :resourceOid (s/maybe s/Str) ;; mahdollinen objektin id
                        :id          (s/maybe s/Str) ;; taulun pääavain
                        (s/optional-key :delta) [{:op (s/enum "lisäys" "päivitys" "poisto")
                                                  :path s/Str
                                                  :value s/Any}]
                        (s/optional-key :message) s/Str})

(def ^:dynamic *request-meta*)

(def ^:private version  1)
(def ^:private type-log "log")  ;; Meillä ei tueta "alive"-logiviestejä
(def ^:private log-seq  (atom (bigint 0)))
(def ^:private environment-meta (atom {:boot-time        nil
                                       :hostname         nil
                                       :service-name     nil
                                       :application-type nil}))

(def operaatiot {:kirjautuminen "kirjautuminen"
                 :lisays "lisäys"
                 :paivitys "päivitys"
                 :poisto "poisto"})

(defn ^:private date-stringina [c]
  (condp instance? c
     org.joda.time.DateTime      (.toString c)
     org.joda.time.LocalDateTime (.toString c)
     org.joda.time.LocalDate     (.toString c "dd.MM.yyyy")
     c))

(defn konfiguroi-common-audit-lokitus [metadata]
  (log/info "Alustetaan common audit logituksen metadata arvoihin:" metadata)
  (reset! environment-meta metadata))

(defn ->audit-log-entry [log-contents]

  ;; pidä nämä alussa
  (s/validate Request-meta *request-meta*)
  (s/validate Env-meta  @environment-meta)
  (s/validate Logientry log-contents)

  (swap! log-seq inc)
  (cheshire/generate-string (merge
                              {:version         version
                               :logSeq          @log-seq
                               :type            type-log
                               :bootTime        (date-stringina (:boot-time @environment-meta))
                               :hostname        (:hostname @environment-meta)
                               :timestamp       (date-stringina (time-local/local-now))
                               :serviceName     (:service-name @environment-meta)
                               :applicationType (:application-type @environment-meta)
                               :operation       (get operaatiot (:operation log-contents))
                               :user            {:oid       (-> log-contents :user :oid)
                                                 :ip        (:ip *request-meta*)
                                                 :session   (:session *request-meta*)
                                                 :userAgent (:user-agent *request-meta*)}
                               }
                              (when (:delta log-contents)
                                {:delta (map #(update-in % [:value] date-stringina) (:delta log-contents))})
                              (when (:resource log-contents) #_(and (:resource log-contents) (:resourceOid log-contents) (:id log-contents))
                                {:target {(:resource log-contents) (:resourceOid log-contents)
                                          :id (:id log-contents)}})
                              (when (:message log-contents)
                                {:message (:message log-contents)})
                              )))

(defn req-metadata-saver-wrapper
  "Tallentaa requestista tietoa logitusta varten"
  [ring-handler]
  (fn [request]
    (binding [*request-meta* {:user-agent (get-in request [:headers "user-agent"])
                              :session    (get-in request [:cookies "ring-session" :value])
                              :ip         (or (get-in request [:headers "X-Forwarded-For"]) (:remote-addr request))}]
      (ring-handler request))))
