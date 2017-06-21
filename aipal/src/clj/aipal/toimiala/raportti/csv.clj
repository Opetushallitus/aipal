(ns aipal.toimiala.raportti.csv
  (:require [clojure-csv.core :refer [write-csv]]
            [oph.common.util.http-util :refer [parse-iso-date]]
            [oph.common.util.util :refer [map-by]]
            [aipal.arkisto.csv :as csv]
            [clojure.core.match :refer [match]]
            [aipal.toimiala.raportti.util :refer [muuta-kaikki-stringeiksi]]
            [aipal.db.core :refer [db]]
            [aipal.db.vastaajatunnus :as vastaajatunnus]
            [clojure.tools.logging :as log]))

(def delimiter \;)

(defn get-template-parts [q]
  (filter second (select-keys q [:kysymysid :jatkokysymysid])))

(defn create-row-template [questions]
  (let [sorted (sort-by (juxt :kysymysryhma_jarjestys :jarjestys) questions)]
    (mapcat get-template-parts sorted)))

(defn get-question-group-text [questions entry]
  (let [question (some #(if (= (get % (first entry)) (second entry))%) questions)]
    (match [(first entry) (:jarjestys question)]
           [:kysymysid 0] [(:kysymysryhma_nimi question)]
           [:jatkokysymysid _] ["" ""]
           :else [""])))

(defn get-question-group-header [questions template vastaajatunnus-header]
  (concat ["Vastaajatunnus"] vastaajatunnus-header (mapcat #(get-question-group-text questions %) template)))

(defn get-header-fields [entry]
  (match [(first entry)]
         [:kysymysid] [:kysymys_fi]
         [:jatkokysymysid] [:jatkokysymys_kylla :jatkokysymys_ei]))

(defn get-header-text [questions entry]
  (let [question (some #(if (= (get % (first entry)) (second entry))%) questions)
        header-fields (get-header-fields entry)]
    (map question header-fields)))

(defn create-header-row [template questions vastaajatunnus-header]
  (concat (repeat (inc (count vastaajatunnus-header)) "") (mapcat #(get-header-text questions %) template)))

(defn get-choice-text [choices answer]
  (let [kysymysid (:kysymysid answer)
        jarjestys (:numerovalinta answer)
        choice (some #(if (and (= kysymysid (:kysymysid %))
                               (= jarjestys (:jarjestys %))) %)
                     choices)]
    (:teksti_fi choice)))

(defn numero-tai-eos [answer]
  (match [(some? (:numerovalinta answer)) (some? (:en_osaa_sanoa answer))]
         [true _] (:numerovalinta answer)
         [false true] "eos"
         [false false] ""))

(defn get-answer-text [choices type answers]
  (match [type]
         ["arvosana"] (:numerovalinta (first answers))
         ["arvosana4_ja_eos"] (numero-tai-eos (first answers))
         ["arvosana6_ja_eos"] (numero-tai-eos (first answers))
         ["arvosana6"] (:numerovalinta (first answers))
         ["arvosana7"] (:numerovalinta (first answers))
         ["monivalinta"] (->> answers
                              (map #(get-choice-text choices %))
                              (clojure.string/join ", "))
         ["likert_asteikko"] (:numerovalinta (first answers))
         ["vapaateksti"] (clojure.string/escape (:vapaateksti (first answers)) {\newline " " \tab " " delimiter \,})
         ["kylla_ei_valinta"] (:vaihtoehto (first answers))
         :else ""))

(defn find [pred coll]
  (first (filter pred coll)))

(defn get-jatkovastaus-text [answer]
  (select-keys answer [:jatkokysymys_kylla :jatkokysymys_ei]))

(defn get-answer [answers choices entry]
  (let [key (first entry)
        answers-for-question (filter #(if (= (get % key) (second entry)) %) answers)
        first-answer (first answers-for-question)]
    (match [key]
           [:kysymysid] [(get-answer-text choices (:vastaustyyppi first-answer) answers-for-question)]
           [:jatkokysymysid] (get-jatkovastaus-text first-answer))))


(defn get-value [tutkintotunnus-old entry]
  (let [entry-missing (nil? entry)
        value-missing (and (= "tutkinto" (:kentta_id entry) (nil? (:arvo entry))))]
    (if (or entry-missing value-missing)
      tutkintotunnus-old
      (:arvo entry))))

(defn get-vastaajatunnus-value [tutkintotunnus-old entry]
  (let [arvo (get-value tutkintotunnus-old entry)]
    (println "TUTKINTOTUNNUS-OLD: " tutkintotunnus-old "ENTRY:" entry "VALUE:" arvo)
    (if (nil? arvo) "" arvo)))

(defn in? [coll elem]
  (some #(= elem %) coll))

(def sallitut-kentat ["tutkinto" "henkilonumero" "haun_numero"])

(defn create-row [template {vastaajatunnus :vastaajatunnus tutkintotunnus :tutkintotunnus} choices answers]
  (let [vastaajatunnus-kentat (filter #(in? sallitut-kentat (:kentta_id %)) (vastaajatunnus/vastaajatunnuksen_tiedot (db) {:vastaajatunnus vastaajatunnus}))
        vastaajatunnus-arvot (map #(get-vastaajatunnus-value tutkintotunnus %) vastaajatunnus-kentat)]
    (concat [vastaajatunnus] vastaajatunnus-arvot (mapcat #(get-answer answers choices %) template))))


(defn get-choices [questions]
  (let [monivalinnat (filter #(= "monivalinta" (:vastaustyyppi %)) questions)
        kysymysidt (map :kysymysid monivalinnat)]
    (csv/hae-monivalinnat kysymysidt)))

(defn kysely-csv [kyselyid]
  (let [questions (csv/hae-kysymykset kyselyid)
        kysely-fields (filter #(in? sallitut-kentat (:kentta_id % )) (vastaajatunnus/kyselyn_kentat (db) {:kyselyid kyselyid}))
        choices (get-choices questions)
        template (create-row-template questions)
        all-answers (csv/hae-vastaukset kyselyid)
        answers (group-by :vastaajaid all-answers)
        vastaajatunnus-header (map :kentta_fi kysely-fields)
        question-group-header-row (get-question-group-header questions template vastaajatunnus-header)
        header-row (create-header-row template questions vastaajatunnus-header)
        answer-rows (map #(create-row template (first (second %)) choices (second %)) answers)]
    (write-csv
      (muuta-kaikki-stringeiksi (apply concat [[question-group-header-row header-row] answer-rows]))
      :delimiter delimiter)))