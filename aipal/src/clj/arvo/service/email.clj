(ns arvo.service.email
  (:require [selmer.parser :as selmer]
            [postal.core :as postal]
            [clojure.core.match :refer [match]]
            [clojure.tools.logging :as log]
            [arvo.db.core :refer [*db*] :as db]
            [aipal.asetukset :refer [asetukset]]))

(defn format-email [tunnus template]
  (selmer/render-file template {:tunnus tunnus}))

(defn send-email [email-data]
  (log/info "lähetetään viesti: " (:sahkoposti email-data))
  (try
    (let [resp (postal/send-message (:email asetukset)
                                    {:from (:from email-data)
                                     :to (:sahkoposti email-data)
                                     :subject (:title email-data)
                                     :body [:alternative
                                            {:type "text/plain; charset=utf-8"
                                             :content (:plain-content email-data)}
                                            {:type "text/html; charset=utf-8"
                                             :content (:html-content email-data)}]})
          _ (log/info "SMTP resp: " resp)]
      (db/lisaa-lahetystieto! (assoc email-data :status (name (:error resp)))))
    (catch Exception e
      (log/info "Exception sending email: " (:sahkoposti email-data) e))))