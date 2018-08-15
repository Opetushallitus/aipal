(ns arvo.toimiala.raportti.csv
  (:require [clojure-csv.core :refer [write-csv]]
            [oph.common.util.http-util :refer [parse-iso-date]]
            [oph.common.util.util :refer [map-by]]
            [clojure.core.match :refer [match]]
            [aipal.toimiala.raportti.util :refer [muuta-kaikki-stringeiksi]]
            [arvo.db.core :as db]
            [aipal.arkisto.kysely :refer [aseta-jatkokysymysten-jarjestys hae-kyselyn-kysymykset]]
            [clj-time.core :as time]
            [clj-time.format :as f]
            [aipal.asetukset :refer [asetukset]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(def translations {:fi {:vastaajatunnus "Vastaajatunnus" :vastausaika "Vastausaika"
                        :tunnus "Vastaajatunnus" :luotuaika "luotuaika"
                        :url "Vastauslinkki"
                        :voimassa_alkupvm "Voimassa alkaen" :voimassa_loppupvm "Voimassaolo päättyy"
                        :valmistavan_koulutuksen_jarjestaja "Koulutuksen järjestäjä"
                        :tutkintotunnus "Tutkinto" :vastausten_lkm "Vastauksia" :vastaajien_lkm "Vastaajien lkm"}
                   :sv {:vastaajatunnus "Svarskod" :vastausaika "Svarstid"}
                   :en {:vastaajatunnus "Answer identifier" :vastausaika "Response time"}})


(defn select-keys-or-nil [map keyseq]
  (let [defaults (zipmap keyseq (repeat nil))]
    (select-keys (merge defaults map) keyseq)))

(defn translate [lang prop]
  (or (get-in translations [lang prop])
      (get-in translations [:fi prop])))

(defn format-date [datetime]
  (when datetime
    (f/unparse (f/formatters :date) datetime)))

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

(defn replace-control-chars [text]
  (clojure.string/escape text {\newline " " \tab " " delimiter \,}))

(defn translate-field [field lang obj]
  (let [translated (get obj (keyword (str field "_" lang)))]
    (if (not-empty translated)
      (replace-control-chars translated)
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
                           (replace-control-chars (:vapaateksti (first answers))))
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

(defn in? [coll elem]
  (some #(= elem %) coll))

(defn hae-taustatiedot [taustatiedot tutkintotunnus]
  (if (:tutkinto taustatiedot)
    taustatiedot
    (assoc taustatiedot :tutkinto tutkintotunnus)))

(defn create-row [template lang {vastaajatunnus :vastaajatunnus tutkintotunnus :tutkintotunnus taustatiedot :taustatiedot} kyselyn-taustatiedot choices answers]
  (let [vastausaika (format-date (:vastaaja_luotuaika (first answers)))
        vastaajatunnus-arvot (vals (select-keys-or-nil (hae-taustatiedot taustatiedot tutkintotunnus) kyselyn-taustatiedot))]
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
        kyselyn-taustatiedot (filter #(get-in % [:raportointi :csv]) (db/kyselyn-kentat {:kyselyid kyselyid}))
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
        :delimiter delimiter
        :end-of-line "\r\n"))))

(defn hae-vastaus [kysymys vastaukset monivalintavaihtoehdot lang]
  (let [kysymyksen-vastaukset (filter  #(= (:kysymysid kysymys) (:kysymysid %)) vastaukset)]
    (get-answer-text monivalintavaihtoehdot (:vastaustyyppi kysymys) kysymyksen-vastaukset lang)))



(defn luo-vastausrivi [[vastaajatunnus vastaukset] kysymykset kyselyn-taustatiedot monivalintavaihtoehdot lang]
  (let [vastausaika (format-date (:vastausaika (first vastaukset)))
        taustatiedot (hae-taustatiedot (:taustatiedot (first vastaukset)) (:tutkintotunnus (first vastaukset)))
        taustatieto-arvot (vals (select-keys-or-nil taustatiedot (map (comp keyword :kentta_id) kyselyn-taustatiedot)))
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
        kyselyn-taustatiedot (filter #(get-in % [:raportointi :csv]) (db/kyselyn-kentat {:kyselyid kyselyid}))
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
                 :delimiter delimiter
                 :end-of-line "\r\n"))))

(def vastaajatunnus-kentat [:tunnus :url :luotuaika :voimassa_alkupvm :voimassa_loppupvm :valmistavan_koulutuksen_jarjestaja :vastausten_lkm :vastaajien_lkm])

(defn vastaajatunnus-header [kyselyn-taustatiedot lang]
  (concat (map (partial translate (keyword lang)) vastaajatunnus-kentat)
          (map (partial translate-field "kentta" lang) kyselyn-taustatiedot)))

(defn vastaajatunnus-url [tunnus]
  (str (:vastaus-base-url @asetukset) "/" (:tunnus tunnus)))

(defn vastaajatunnus-rivi [vastaajatunnus kyselyn-taustatiedot]
  (concat (-> (select-keys-or-nil (assoc vastaajatunnus :url (vastaajatunnus-url vastaajatunnus)) vastaajatunnus-kentat)
              (update :voimassa_alkupvm format-date)
              (update :voimassa_loppupvm format-date)
              (update :luotuaika format-date)
              vals)
          (vals (select-keys-or-nil (:taustatiedot vastaajatunnus) (map (comp keyword :kentta_id) kyselyn-taustatiedot)))))

(defn vastaajatunnus-csv [kyselykertaid lang]
  (let [tunnukset (db/hae-vastaajatunnus {:kyselykertaid kyselykertaid})
        kyselyn-taustatiedot (filter #(get-in % [:raportointi :csv]) (db/kyselyn-kentat {:kyselykertaid kyselykertaid}))
        data (concat
               [(vastaajatunnus-header kyselyn-taustatiedot lang)]
               (map #(vastaajatunnus-rivi % kyselyn-taustatiedot) tunnukset))]
    (write-csv (muuta-kaikki-stringeiksi data)
               :delimiter delimiter
               :end-of-line "\r\n")))

