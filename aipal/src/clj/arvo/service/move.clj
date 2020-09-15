(ns arvo.service.move
  (:require [selmer.parser :as selmer]
            [arvo.service.email :as email]
            [aipal.arkisto.vastaajatunnus :refer [get-vastaajatunnukset]]
            [clojure.java.jdbc :as jdbc]
            [arvo.db.core :refer [*db*] :as db]))

(def message-header "TÄRKEÄ/VIKTIGT: Linkki Move!-mittaustulosten syöttämiseen 2020 / Länk till inmatningen av mätresultaten i Move! 2020")
(def from-address "move@arvokyselyt.fi")

(defn format-message [oppilaitos tunniste]
  (let [plain-msg (selmer/render-file "template/move-email.plain.txt" oppilaitos)
        html-msg (selmer/render-file "template/move-email-html.html" oppilaitos)]
    {:sahkoposti (:sahkoposti oppilaitos)
     :taustatiedot {:oppilaitoskoodi (:oppilaitoskoodi oppilaitos) :tunnus (:tunnus oppilaitos)}
     :title message-header
     :from from-address
     :plain-content plain-msg
     :html-content html-msg
     :tunniste tunniste}))


(def test-vastaanottajat [{:sahkoposti "mikko.tyrvainen@visma.com" :oppilaitoskoodi "03094" :tunnus "MOVETESTI1" :nimi_fi "Pukinmäenkaaren peruskoulu" :nimi_sv "Pukinmäenkaaren peruskoulu"}
                          {:sahkoposti "kaisa.kotomaki@csc.fi" :oppilaitoskoodi "03094" :tunnus "MOVETESTI1" :nimi_fi "Pukinmäenkaaren peruskoulu" :nimi_sv "Pukinmäenkaaren peruskoulu"}])

(defn email-test []
  (doseq [recipient test-vastaanottajat]
    (let [message (format-message recipient "move-2020")]
      (email/send-email message))))

(defn vastaanottajat []
  (let [kyselykerta (first (db/hae-move-kyselykerta {:tunniste "move-2020"}))]
    (db/hae-move-vastaanottajat (merge kyselykerta {:tunniste "move-2020"}))))

(defn muistutus-vastaanottajat []
  (let [kyselykerta (first (db/hae-move-kyselykerta {:tunniste "move-2020"}))]
    (db/hae-move-muistutus-vastaanottajat (merge kyselykerta {:tunniste "move-2020"}))))

(defn luo-tunnukset []
  (let [kyselykerta (first (db/hae-move-kyselykerta {:tunniste "move-2020"}))
        oppilaitokset (db/hae-move-oppilaitokset-ilman-tunnusta kyselykerta)
        tunnukset (get-vastaajatunnukset (count oppilaitokset))
        luotavat-tunnukset (map #(assoc %1 :tunnus %2) oppilaitokset tunnukset)]
    (jdbc/with-db-transaction [tx *db*]
      (doseq [tunnus luotavat-tunnukset]
        (db/lisaa-move-tunnus! tx (merge tunnus kyselykerta))))))

(defn status []
  (let [kyselykertaid (:kyselykertaid (first(db/hae-move-kyselykerta {:tunniste "move-2020"})))]
    {:kyselykerta kyselykertaid
     :recipients (count (db/hae-move-vastaanottajat {:kyselykertaid kyselykertaid :tunniste "move-2020"}))}))

(defn laheta-viestit []
  (let [kyselykerta (first (db/hae-move-kyselykerta {:tunniste "move-2020"}))
        recipients (db/hae-move-vastaanottajat (merge kyselykerta {:tunniste "move-2020"}))]
    (doseq [recipient recipients]
      (let [message (format-message recipient "move-2020")]
        (email/send-email message)))))
