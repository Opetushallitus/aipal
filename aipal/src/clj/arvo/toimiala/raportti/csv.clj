(ns arvo.toimiala.raportti.csv
  (:require [clojure-csv.core :refer [write-csv]]
            [oph.common.util.http-util :refer [parse-iso-date]]
            [oph.common.util.util :refer [map-by]]
            [clojure.core.match :refer [match]]
            [aipal.toimiala.raportti.util :refer [muuta-kaikki-stringeiksi]]
            [arvo.db.core :as db]
            [clojure.tools.logging :as log]
            [aipal.arkisto.kysely :refer [aseta-jatkokysymysten-jarjestys hae-kyselyn-kysymykset]]
            [clj-time.core :as time]
            [clj-time.format :as f]))

(def translations {:fi {:vastaajatunnus "Vastaajatunnus" :vastausaika "Vastausaika"}
                   :sv {:vastaajatunnus "Svarskod"}
                   :en {:vastaajatunnus "Answer identifier"}})

(defn translate [lang prop]
  (or (get-in translations [lang prop])
      (get-in translations [:fi prop])))

(defn format-date [datetime]
  (f/unparse (f/formatters :date) datetime))

(def delimiter \;)

(defn get-template-parts [q]
  (filter second (select-keys q [:kysymysid])))

(defn create-row-template [questions]
    (mapcat get-template-parts questions))

(defn get-question-group-text [questions entry]
  (let [question (some #(if (= (get % (first entry)) (second entry))%) questions)]
    (match [(first entry) (:jarjestys question)]
           [:kysymysid 0] [(:kysymysryhma_nimi question)]
           :else [""])))

(defn translate-field [field lang obj]
  (let [translated (get obj (keyword (str field "_" lang)))]
    (if (not-empty translated)
      translated
      (when (not= "fi" lang)
        (translate-field field "fi" obj)))))

(defn get-header-fields [entry]
  (match [(first entry)]
         [:kysymysid] [:kysymys_fi]))

(defn get-header-text [questions lang entry]
  (let [question (some #(if (= (get % (first entry)) (second entry))%) questions)]
    (translate-field "kysymys" lang question)))

(defn create-header-row [template questions vastaajatunnus-header lang]
  (concat
    [(translate (keyword lang) :vastaajatunnus)
     (translate (keyword lang) :vastausaika)]
    vastaajatunnus-header
    (flatten (map #(get-header-text questions lang %) template))))

(defn get-choice-text [choices lang answer]
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

(defn get-answer-text [choices type answers lang]
  (match [type]
         ["arvosana"] (:numerovalinta (first answers))
         ["arvosana4_ja_eos"] (numero-tai-eos (first answers))
         ["arvosana6_ja_eos"] (numero-tai-eos (first answers))
         ["arvosana6"] (:numerovalinta (first answers))
         ["arvosana7"] (:numerovalinta (first answers))
         ["asteikko5_1"] (:numerovalinta (first answers))
         ["nps"] (:numerovalinta (first answers))
         ["monivalinta"] (->> answers
                              (map #(get-choice-text choices lang %))
                              (clojure.string/join ", "))
         ["likert_asteikko"] (:numerovalinta (first answers))
         ["vapaateksti"] (when (:vapaateksti (first answers))
                           (clojure.string/escape (:vapaateksti (first answers)) {\newline " " \tab " " delimiter \,}))
         ["kylla_ei_valinta"] (:vaihtoehto (first answers))
         :else ""))

(defn get-answer [answers choices lang [key value]]
  (let [answers-for-question (filter #(if (= (get % key) value) %) answers)
        first-answer (first answers-for-question)]
    [(get-answer-text choices (:vastaustyyppi first-answer) answers-for-question lang)]))

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

(defn hae-taustatiedot [taustatiedot tutkintotunnus]
  (if (:tutkinto taustatiedot)
    taustatiedot
    (assoc taustatiedot :tutkinto tutkintotunnus)))

(defn create-row [template lang {vastaajatunnus :vastaajatunnus tutkintotunnus :tutkintotunnus taustatiedot :taustatiedot} kyselyn-taustatiedot choices answers]
  (let [vastausaika (format-date (:vastaaja_luotuaika (first answers)))
        vastaajatunnus-arvot (vals (select-keys (hae-taustatiedot taustatiedot tutkintotunnus) kyselyn-taustatiedot))]
    (concat [vastaajatunnus vastausaika] vastaajatunnus-arvot (mapcat #(get-answer answers choices lang %) template))))

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

(defn csv-response [kyselyid lang data]
  (let [kysely (db/hae-kysely {:kyselyid kyselyid})
        koulutustoimija (db/hae-koulutustoimija {:ytunnus (:koulutustoimija kysely)})]
    {:nimi (translate-field "nimi" lang kysely)
     :koulutustoimija (translate-field "nimi" lang koulutustoimija)
     :date (f/unparse (f/formatters :date) (time/now))
     :csv data}))

(defn kysely-csv [kyselyid lang]
  (let [kysymykset (hae-kysymykset kyselyid)
        kyselyn-taustatiedot (filter #(in? sallitut-taustatiedot (:kentta_id % )) (db/kyselyn-kentat {:kyselyid kyselyid}))
        monivalintavaihtoehdot (get-choices kysymykset)
        template (create-row-template kysymykset)
        vastaukset (group-by :vastaajaid (db/hae-vastaukset {:kyselyid kyselyid}))
        vastaajatunnus-header (map #(translate-field "kentta" lang %) kyselyn-taustatiedot)
        kysymys-header (create-header-row template kysymykset vastaajatunnus-header lang)
        vastausrivit (map #(create-row template lang (first (second %))
                                       (map (comp keyword :kentta_id) kyselyn-taustatiedot)
                                       monivalintavaihtoehdot (second %)) vastaukset)]
    (csv-response kyselyid lang
      (write-csv
        (muuta-kaikki-stringeiksi (apply concat [[kysymys-header] vastausrivit]))
        :delimiter delimiter))))

(defn hae-vastaus [kysymys vastaukset monivalintavaihtoehdot lang]
  (let [kysymyksen-vastaukset (filter  #(= (:kysymysid kysymys) (:kysymysid %)) vastaukset)]
    (get-answer-text monivalintavaihtoehdot (:vastaustyyppi kysymys) kysymyksen-vastaukset lang)))



(defn luo-vastausrivi [[vastaajatunnus vastaukset] kysymykset kyselyn-taustatiedot monivalintavaihtoehdot lang]
  (let [vastausaika (format-date (:vastausaika (first vastaukset)))
        taustatiedot (hae-taustatiedot (:taustatiedot (first vastaukset)) (:tutkintotunnus (first vastaukset)))
        taustatieto-arvot (vals (select-keys taustatiedot (map (comp keyword :kentta_id) kyselyn-taustatiedot)))
        taustakysymysten-vastaukset (->> kysymykset
                                         (filter :taustakysymys)
                                         (map #(hae-vastaus % vastaukset monivalintavaihtoehdot lang)))]
    (->> kysymykset
         (filter (complement :taustakysymys))
         (map #(concat [vastaajatunnus vastausaika] taustatieto-arvot taustakysymysten-vastaukset
                       [(translate-field "kysymysryhma" lang %) (translate-field "kysymys" lang %)
                        (hae-vastaus % vastaukset monivalintavaihtoehdot lang)])))))


(defn kysely-csv-vastauksittain [kyselyid lang]
  (let [kysymykset (hae-kysymykset kyselyid)
        vastaukset (group-by :vastaajatunnus (db/hae-vastaukset {:kyselyid kyselyid}))
        kyselyn-taustatiedot (filter #(in? sallitut-taustatiedot (:kentta_id % )) (db/kyselyn-kentat {:kyselyid kyselyid}))
        monivalintavaihtoehdot (get-choices kysymykset)
        taustakysymykset (->> kysymykset
                              (filter :taustakysymys)
                              (sort-by :jarjestys)
                              (map translate-field "kysymys" lang))
        header (concat [(translate (keyword lang) :vastaajatunnus)
                        (translate (keyword lang) :vastausaika)]
                       (map #(translate-field "kentta" lang %) kyselyn-taustatiedot) taustakysymykset ["Kysymysryhmä" "Kysymys" "Vastaus"])
        vastausrivit (mapcat #(luo-vastausrivi % kysymykset kyselyn-taustatiedot monivalintavaihtoehdot lang) vastaukset)]
    (csv-response kyselyid lang
      (write-csv (muuta-kaikki-stringeiksi (cons header vastausrivit))
                 :delimiter delimiter))))