(ns arvo.toimiala.raportti.csv
  (:require [clojure-csv.core :refer [write-csv]]
            [oph.common.util.http-util :refer [parse-iso-date]]
            [oph.common.util.util :refer [map-by]]
            [clojure.core.match :refer [match]]
            [aipal.toimiala.raportti.util :refer [muuta-kaikki-stringeiksi]]
            [arvo.db.core :as db]
            [clojure.tools.logging :as log]
            [aipal.arkisto.kysely :refer [aseta-jatkokysymysten-jarjestys hae-kyselyn-kysymykset]]))

(def delimiter \;)

(defn get-template-parts [q]
  (filter second (select-keys q [:kysymysid])))

(defn create-row-template [questions]
    (mapcat get-template-parts questions))

(def lang "sv")

(defn get-question-group-text [questions entry]
  (let [question (some #(if (= (get % (first entry)) (second entry))%) questions)]
    (match [(first entry) (:jarjestys question)]
           [:kysymysid 0] [(:kysymysryhma_nimi question)]
           :else [""])))

(defn get-question-group-header [questions template vastaajatunnus-header]
  (concat ["Vastaajatunnus"] vastaajatunnus-header (mapcat #(get-question-group-text questions %) template)))

(defn translate-field [field lang obj]
  (let [translated (get obj (keyword (str field "_" lang)))]
    (if (not-empty translated)
      translated
      (when (not= "fi" lang)
        (translate-field field "fi" obj)))))

(defn get-header-fields [entry]
  (match [(first entry)]
         [:kysymysid] [:kysymys_fi]))

(defn get-header-text [questions entry]
  (let [question (some #(if (= (get % (first entry)) (second entry))%) questions)]
    (translate-field "kysymys" lang question)))

(defn create-header-row [template questions vastaajatunnus-header]
  (concat (repeat (inc (count vastaajatunnus-header)) "") (flatten (map #(get-header-text questions %) template))))

(defn get-choice-text [choices answer]
  (let [kysymysid (:kysymysid answer)
        jarjestys (:numerovalinta answer)
        choice (some #(if (and (= kysymysid (:kysymysid %))
                               (= jarjestys (:jarjestys %))) %)
                     choices)]
    (translate-field "teksti" lang choice)))

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
         ["asteikko5_1"] (:numerovalinta (first answers))
         ["nps"] (:numerovalinta (first answers))
         ["monivalinta"] (->> answers
                              (map #(get-choice-text choices %))
                              (clojure.string/join ", "))
         ["likert_asteikko"] (:numerovalinta (first answers))
         ["vapaateksti"] (when (:vapaateksti (first answers))
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

(def sallitut-taustatiedot ["tutkinto" "henkilonumero" "haun_numero" "ika" "sukupuoli" "koulutusmuoto"])

(defn create-row [template {vastaajatunnus :vastaajatunnus tutkintotunnus :tutkintotunnus} choices answers]
  (let [vastaajatunnus-kentat (filter #(in? sallitut-taustatiedot (:kentta_id %)) (db/vastaajatunnuksen_tiedot {:vastaajatunnus vastaajatunnus}))
        vastaajatunnus-arvot (map #(get-vastaajatunnus-value tutkintotunnus %) vastaajatunnus-kentat)]
    (concat [vastaajatunnus] vastaajatunnus-arvot (mapcat #(get-answer answers choices %) template))))


(defn get-choices [questions]
  (let [monivalinnat (filter #(= "monivalinta" (:vastaustyyppi %)) questions)
        kysymysidt (map :kysymysid monivalinnat)]
    (db/hae-monivalinnat {:kysymysidt kysymysidt})))

(defn muuta-taustakysymykset [kysymykset]
  (if (every? :taustakysymys kysymykset)
    (map #(assoc % :taustakysymys false) kysymykset)
    kysymykset))

(defn poista-valiotsikot [kysymykset]
  (filter #(not= (:vastaustyyppi %) "valiotsikko") kysymykset))

(map aseta-jatkokysymysten-jarjestys)

(defn hae-kysymykset [kyselyid]
  (->> (hae-kyselyn-kysymykset kyselyid)
       flatten
       poista-valiotsikot
       muuta-taustakysymykset))

(defn kysely-csv [kyselyid]
  (let [kysymykset (hae-kysymykset kyselyid)
        taustatieot (filter #(in? sallitut-taustatiedot (:kentta_id % )) (db/kyselyn-kentat {:kyselyid kyselyid}))
        monivalintavaihtoehdot (get-choices kysymykset)
        template (create-row-template kysymykset)
        vastaukset (group-by :vastaajaid (db/hae-vastaukset {:kyselyid kyselyid}))
        vastaajatunnus-header (map #(translate-field "kentta" lang %) taustatieot)
        kysymysryhma-header (get-question-group-header kysymykset template vastaajatunnus-header)
        kysymys-header (create-header-row template kysymykset vastaajatunnus-header)
        ;_ (log/info "HEADER" kysymys-header)
        vastausrivit (map #(create-row template (first (second %)) monivalintavaihtoehdot (second %)) vastaukset)]
    (write-csv
      (muuta-kaikki-stringeiksi (apply concat [[kysymysryhma-header kysymys-header] vastausrivit]))
      :delimiter delimiter)))


(defn hae-vastaus [kysymys vastaukset monivalintavaihtoehdot]
  (let [kysymyksen-vastaukset (filter  #(= (:kysymysid kysymys) (:kysymysid %)) vastaukset)]
    (get-answer-text monivalintavaihtoehdot (:vastaustyyppi kysymys) kysymyksen-vastaukset)))


(defn luo-vastausrivi [[vastaajatunnus vastaukset] kysymykset monivalintavaihtoehdot]
  (let [vanha-tutkintotunnus (:tutkintotunnus (first vastaukset))
        taustatiedot (->> (db/vastaajatunnuksen_tiedot {:vastaajatunnus vastaajatunnus})
                          (filter #(in? sallitut-taustatiedot (:kentta_id %)))
                          (map #(get-vastaajatunnus-value vanha-tutkintotunnus %)))
        taustakysymysten-vastaukset (->> kysymykset
                                         (filter :taustakysymys)
                                         (map #(hae-vastaus % vastaukset monivalintavaihtoehdot)))]
    (->> kysymykset
         (filter (complement :taustakysymys))
         (map #(concat [vastaajatunnus] taustatiedot taustakysymysten-vastaukset
                       [(:kysymysryhma_nimi %) (translate-field "kysymys" lang %) (hae-vastaus % vastaukset monivalintavaihtoehdot)])))))


(defn kysely-csv-vastauksittain [kyselyid]
  (let [kysymykset (hae-kysymykset kyselyid)
        vastaukset (group-by :vastaajatunnus (db/hae-vastaukset {:kyselyid kyselyid}))
        taustatiedot (filter #(in? sallitut-taustatiedot (:kentta_id % )) (db/kyselyn-kentat {:kyselyid kyselyid}))
        monivalintavaihtoehdot (get-choices kysymykset)
        taustakysymykset (->> kysymykset
                              (filter :taustakysymys)
                              (sort-by :jarjestys)
                              (map translate-field "kysymys" lang))
        header (concat ["Vastaajatunnus"] (map #(translate-field "kentta" lang %) taustatiedot) taustakysymykset ["Kysymysryhmä" "Kysymys" "Vastaus"])
        vastausrivit (mapcat #(luo-vastausrivi % kysymykset monivalintavaihtoehdot) vastaukset)]
    (write-csv (muuta-kaikki-stringeiksi (cons header vastausrivit))
               :delimiter delimiter)))