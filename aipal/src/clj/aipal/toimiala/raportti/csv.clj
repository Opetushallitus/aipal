(ns aipal.toimiala.raportti.csv
  (:require [korma.core :as sql]
            [clojure-csv.core :refer [write-csv]]
            [aipal.rest-api.i18n :as i18n]
            [clj-time.core :as t]
            [oph.common.util.http-util :refer [parse-iso-date]]
            [oph.common.util.util :refer [map-by]]
            [aipal.arkisto.csv :as csv]
            [clojure.core.match :refer [match]]
            [aipal.toimiala.raportti.util :refer [muuta-kaikki-stringeiksi]]))


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

(defn get-question-group-header [questions template]
  (cons "Vastaajatunnus" (mapcat #(get-question-group-text questions %) template)))

(defn get-header-fields [entry]
  (match [(first entry)]
         [:kysymysid] [:kysymys_fi]
         [:jatkokysymysid] [:jatkokysymys_kylla :jatkokysymys_ei]))

(defn get-header-text [questions entry]
  (let [question (some #(if (= (get % (first entry)) (second entry))%) questions)
        header-fields (get-header-fields entry)]
    (map question header-fields)))

(defn create-header-row [template questions]
  (cons "" (mapcat #(get-header-text questions %) template)))

(defn get-choice-text [choices answer]
  (let [kysymysid (:kysymysid answer)
        jarjestys (:numerovalinta answer)
        choice (some #(if (and (= kysymysid (:kysymysid %))
                               (= jarjestys (:jarjestys %))) %)
                     choices)]
    (:teksti_fi choice)))

(defn get-answer-text [choices type answer]
  (match [type]
         ["arvosana"] (:numerovalinta answer)
         ["arvosana7"] (:numerovalinta answer)
         ["monivalinta"] (get-choice-text choices answer)
         ["likert_asteikko"] (:numerovalinta answer)
         ["vapaateksti"] (:vapaateksti answer)
         ["kylla_ei_valinta"] (:vaihtoehto answer)
         :else ""))

(defn find [pred coll]
  (some #(if pred %) coll))

(defn get-jatkovastaus-text [answer]
  (select-keys answer [:jatkokysymys_kylla :jatkokysymys_ei]))

(defn get-answer [answers choices entry]
  (let [key (first entry)
        answer (some #(if (= (get % key) (second entry)) %) answers)]
    (match [key]
           [:kysymysid] [(get-answer-text choices (:vastaustyyppi answer) answer)]
           [:jatkokysymysid] (get-jatkovastaus-text answer))))


(defn create-row [template vastaajatunnus choices answers]
  (cons vastaajatunnus (mapcat #(get-answer answers choices %) template)))

(defn get-choices [questions]
  (let [monivalinnat (filter #(= "monivalinta" (:vastaustyyppi %)) questions)
        kysymysidt (map :kysymysid monivalinnat)]
    (csv/hae-monivalinnat kysymysidt)))

(defn kysely-csv [kyselyid]
  (let [questions (csv/hae-kysymykset kyselyid)
        choices (get-choices questions)
        template (create-row-template questions)
        all-answers (csv/hae-vastaukset kyselyid)
        answers (group-by :vastaajaid all-answers)
        question-group-header-row (get-question-group-header questions template)
        header-row (create-header-row template questions)
        answer-rows (map #(create-row template (:vastaajatunnus (first (second %))) choices (second %)) answers)]
    (write-csv
      (muuta-kaikki-stringeiksi (apply concat [[question-group-header-row header-row] answer-rows]))
      :delimiter \,)))