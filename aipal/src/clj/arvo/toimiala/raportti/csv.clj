(ns arvo.toimiala.raportti.csv
  (:require [clojure-csv.core :refer [write-csv]]
            [oph.common.util.http-util :refer [parse-iso-date]]
            [oph.common.util.util :refer [map-by]]
            [arvo.arkisto.csv :as csv]
            [clojure.core.match :refer [match]]
            [aipal.toimiala.raportti.util :refer [muuta-kaikki-stringeiksi]]
            [arvo.db.core :as vastaajatunnus]
            [clojure.tools.logging :as log]))

(def delimiter \;)

(defn get-template-parts [q]
  (filter second (select-keys q [:kysymysid])))

(defn create-row-template [questions]
  (let [sorted (sort-by (juxt :kysymysryhma_jarjestys :jarjestys) questions)]
    (mapcat get-template-parts sorted)))

(defn get-question-group-text [questions entry]
  (let [question (some #(if (= (get % (first entry)) (second entry))%) questions)]
    (match [(first entry) (:jarjestys question)]
           [:kysymysid 0] [(:kysymysryhma_nimi question)]
           :else [""])))

(defn get-question-group-header [questions template vastaajatunnus-header]
  (concat ["Vastaajatunnus"] vastaajatunnus-header (mapcat #(get-question-group-text questions %) template)))

(defn get-header-fields [entry]
  (match [(first entry)]
         [:kysymysid] [:kysymys_fi]))

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
         ["vapaateksti"] (when (:vapaateksti answers)
                           (clojure.string/escape (:vapaateksti (first answers)) {\newline " " \tab " " delimiter \,}))
         ["kylla_ei_valinta"] (:vaihtoehto (first answers))
         :else ""))

(defn get-answer [answers choices [key value]]
  (let [answers-for-question (filter #(if (= (get % key) value) %) answers)
        first-answer (first answers-for-question)]
    [(get-answer-text choices (:vastaustyyppi first-answer) answers-for-question)]))

(defn get-value [tutkintotunnus-old entry]
  (let [entry-missing (nil? entry)
        value-missing (and (= "tutkinto" (:kentta_id entry)) (nil? (:arvo entry)))]
    (if (or entry-missing value-missing)
      tutkintotunnus-old
      (:arvo entry))))

(defn get-vastaajatunnus-value [tutkintotunnus-old entry]
  (let [arvo (get-value tutkintotunnus-old entry)]
    (if (nil? arvo) "" arvo)))

(defn in? [coll elem]
  (some #(= elem %) coll))

(def sallitut-taustatiedot ["tutkinto" "henkilonumero" "haun_numero" "ika" "sukupuoli"])

(defn create-row [template {vastaajatunnus :vastaajatunnus tutkintotunnus :tutkintotunnus} choices answers]
  (let [vastaajatunnus-kentat (filter #(in? sallitut-taustatiedot (:kentta_id %)) (vastaajatunnus/vastaajatunnuksen_tiedot {:vastaajatunnus vastaajatunnus}))
        vastaajatunnus-arvot (map #(get-vastaajatunnus-value tutkintotunnus %) vastaajatunnus-kentat)]
    (concat [vastaajatunnus] vastaajatunnus-arvot (mapcat #(get-answer answers choices %) template))))


(defn get-choices [questions]
  (let [monivalinnat (filter #(= "monivalinta" (:vastaustyyppi %)) questions)
        kysymysidt (map :kysymysid monivalinnat)]
    (csv/hae-monivalinnat kysymysidt)))

(defn hae-kysymykset [kyselyid]
  (filter #(not= (:vastaustyyppi %) "valiotsikko") (csv/hae-kysymykset kyselyid)))

(defn kysely-csv [kyselyid]
  (let [kysymykset (hae-kysymykset kyselyid)
        taustatieot (filter #(in? sallitut-taustatiedot (:kentta_id % )) (vastaajatunnus/kyselyn_kentat {:kyselyid kyselyid}))
        monivalintavaihtoehdot (get-choices kysymykset)
        template (create-row-template kysymykset)
        vastaukset (group-by :vastaajaid (csv/hae-vastaukset kyselyid))
        vastaajatunnus-header (map :kentta_fi taustatieot)
        kysymysryhma-header (get-question-group-header kysymykset template vastaajatunnus-header)
        kysymys-header (create-header-row template kysymykset vastaajatunnus-header)
        vastausrivit (map #(create-row template (first (second %)) monivalintavaihtoehdot (second %)) vastaukset)]
    (write-csv
      (muuta-kaikki-stringeiksi (apply concat [[kysymysryhma-header kysymys-header] vastausrivit]))
      :delimiter delimiter)))


(defn hae-vastaus [kysymys vastaukset monivalintavaihtoehdot]
  (let [kysymyksen-vastaukset (filter  #(= (:kysymysid kysymys) (:kysymysid %)) vastaukset)]
    (get-answer-text monivalintavaihtoehdot (:vastaustyyppi kysymys) kysymyksen-vastaukset)))


(defn luo-vastausrivi [[vastaajatunnus vastaukset] kysymykset monivalintavaihtoehdot]
  (let [vanha-tutkintotunnus (:tutkintotunnus (first vastaukset))
        taustatiedot (->> (vastaajatunnus/vastaajatunnuksen_tiedot {:vastaajatunnus vastaajatunnus})
                          (filter #(in? sallitut-taustatiedot (:kentta_id %)))
                          (map #(get-vastaajatunnus-value vanha-tutkintotunnus %)))
        taustakysymysten-vastaukset (->> kysymykset
                                         (filter :taustakysymys)
                                         (map #(hae-vastaus % vastaukset monivalintavaihtoehdot)))]
    (->> kysymykset
         (sort-by (juxt :kysymysryhma_jarjestys :jarjestys))
         (filter (complement :taustakysymys))
         (map #(concat [vastaajatunnus] taustatiedot taustakysymysten-vastaukset [(:kysymysryhma_nimi %) (:kysymys_fi %) (hae-vastaus % vastaukset monivalintavaihtoehdot)])))))


(defn kysely-csv-vastauksittain [kyselyid]
  (let [kysymykset (hae-kysymykset kyselyid)
        vastaukset (group-by :vastaajatunnus (csv/hae-vastaukset kyselyid))
        taustatiedot (filter #(in? sallitut-taustatiedot (:kentta_id % )) (vastaajatunnus/kyselyn_kentat {:kyselyid kyselyid}))
        monivalintavaihtoehdot (get-choices kysymykset)
        taustakysymykset (->> kysymykset
                              (filter :taustakysymys)
                              (sort-by :jarjestys)
                              (map :kysymys_fi))
        header (concat ["Vastaajatunnus"] (map :kentta_fi taustatiedot) taustakysymykset ["Kysymysryhmä" "Kysymys" "Vastaus"])
        vastausrivit (mapcat #(luo-vastausrivi % kysymykset monivalintavaihtoehdot) vastaukset)]
    (write-csv (muuta-kaikki-stringeiksi (cons header vastausrivit))
               :delimiter delimiter)))