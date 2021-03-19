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
            [arvo.util :refer [in?]]
            [aipal.integraatio.koodistopalvelu :refer [hae-kunnat]]))

(def default-translations {:fi {:vastaajatunnus "Vastaajatunnus"
                                :vastausaika "Vastausaika"
                                :tunnus "Vastaajatunnus"
                                :luotuaika "Luontiaika"
                                :url "Vastauslinkki"
                                :kysymysryhma "Kysymysryhma"
                                :kysymys "Kysymys"
                                :vastaus "Vastaus"
                                :voimassa_alkupvm "Voimassa alkaen"
                                :voimassa_loppupvm "Voimassaolo päättyy"
                                :tutkintotunnus "Tutkinto"
                                :vastausten_lkm "Vastaajien lkm" :vastaajien_lkm "Vastaajien lkm" :kohteiden_lkm "Kohteiden lkm"
                                :tutkinto_selite "Tutkinnon nimi"
                                :hankintakoulutuksen_toteuttaja_selite "Hankintakoulutuksen toteuttajan nimi"
                                :toimipaikka_selite "Toimipaikan nimi"
                                :koulutusalakoodi_selite "Koulutusala"
                                :asuinkunta_koodi_selite "Asuinkunta selite"
                                :opiskelupaikkakunta_koodi_selite "Opiskelupaikkakunta selite"
                                :oppilaitos_nimi "Oppilaitos"
                                :koulutusmuoto "Koulutusmuoto"
                                :nimi "Kyselykerta"}
                           :sv {:vastaajatunnus "Svarskod"
                                :vastausaika "Svarstid"
                                :tunnus "Svarskod"
                                :luotuaika "Skapat"
                                :url "Svararkod"
                                :voimassa_alkupvm "Första svarsdag"
                                :voimassa_loppupvm "Sista svarsdag"
                                :tutkintotunnus "Tutkinto"
                                :vastausten_lkm "Respondents antal" :vastaajien_lkm "Respondents antal" :kohteiden_lkm "Svarsantal"
                                :tutkinto_selite "Namn på examen"
                                :hankintakoulutuksen_toteuttaja_selite "Namn på anordnaren av anskaffad utbildning"
                                :toimipaikka_selite "Namn på verksamhetsställe"
                                :koulutusalakoodi_selite "Utbildningsområde"
                                :asuinkunta_koodi_selite "Bostadskommun"
                                :opiskelupaikkakunta_koodi_selite "Field of education"
                                :oppilaitos_nimi "Läroanstalt"
                                :koulutusmuoto "Utbildningsform"
                                :nimi "Frågeformulärsomgång"}
                           :en {:vastaajatunnus "Answer identifier" :vastausaika "Response time"
                                :tunnus "Answer identifier"
                                :luotuaika "TimeCreated"
                                :url "Credential"
                                :voimassa_alkupvm "ValidityStartDate"
                                :voimassa_loppupvm "ValidityEndDate"
                                :tutkintotunnus "Tutkinto"
                                :vastausten_lkm "RespondentCount" :vastaajien_lkm "RespondentCount" :kohteiden_lkm "ResponseCount"
                                :tutkinto_selite "Name of degree"
                                :hankintakoulutuksen_toteuttaja_selite "Name of provider (procured training)"
                                :toimipaikka_selite "Name of operational unit"
                                :koulutusalakoodi_selite "Field of education"
                                :asuinkunta_koodi_selite "Municipality of residence"
                                :opiskelupaikkakunta_koodi_selite "Municipality of education"
                                :oppilaitos_nimi "Educational institution"
                                :koulutusmuoto "Form of education"
                                :nimi " Questionnaire instance"}})

(def delimiter \;)

(defn create-csv [data]
  (write-csv (muuta-kaikki-stringeiksi data)
             :delimiter delimiter
             :end-of-line "\r\n"))

(defn select-values-or-nil [m keyseq]
  (let [defaults (zipmap keyseq (repeat nil))]
    (map #(get (merge defaults m) %) keyseq)))

(defn translate [lang prop translations]
  (or (get-in translations [lang prop])
      (get-in translations [:fi prop])))

(defn format-date [datetime]
  (when datetime
    (f/unparse (f/formatters :date) datetime)))

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
  (let [translated (get obj (keyword (str field "_" (name lang))))]
    (if (not-empty translated)
      (replace-control-chars translated)
      (when (not= "fi" lang)
        (translate-field field "fi" obj)))))

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
         ["luku"] (:numerovalinta (first answers))
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

(defn hae-taustatiedot [taustatiedot tutkintotunnus]
  (if (:tutkinto taustatiedot)
    taustatiedot
    (assoc taustatiedot :tutkinto tutkintotunnus)))

(defn hae-monivalinnat [questions]
  (let [monivalinnat (filter #(= "monivalinta" (:vastaustyyppi %)) questions)
        kysymysidt (map :kysymysid monivalinnat)]
    (when (not-empty kysymysidt) (db/hae-monivalinnat {:kysymysidt kysymysidt}))))

(defn muuta-taustakysymykset [kysymykset]
  (if (every? :taustakysymys kysymykset)
    (map #(assoc % :taustakysymys false) kysymykset)
    kysymykset))

(defn poista-valiotsikot [kysymykset]
  (filter #(not= (:vastaustyyppi %) "valiotsikko") kysymykset))

(defn poista-raportoimattomat [kysymykset]
  (filter #(not= false (-> % :metatiedot :raportointi :csv)) kysymykset))

(defn hae-kysymykset [kyselyid]
  (->> (hae-kyselyn-kysymykset kyselyid)
       flatten
       poista-valiotsikot
       poista-raportoimattomat
       muuta-taustakysymykset))

(defn csv-response [kyselyid lang data]
  (let [kysely (db/hae-kysely {:kyselyid kyselyid})
        koulutustoimija (db/hae-koulutustoimija {:ytunnus (:koulutustoimija kysely)})]
    {:nimi (translate-field "nimi" lang kysely)
     :koulutustoimija (translate-field "nimi" lang koulutustoimija)
     :date (f/unparse (f/formatters :date) (time/now))
     :csv data}))

(defn default-csv-fields [kyselytyyppi]
  (if (= "amispalaute" kyselytyyppi)
    [:vastaajatunnus :vastausaika :oppilaitos_nimi]
    [:vastaajatunnus :vastausaika]))

(def default-vastaajatunnus-fields [:tunnus :url :luotuaika :voimassa_alkupvm :voimassa_loppupvm :vastausten_lkm :kohteiden_lkm])

(defn get-csv-field [kentta]
  (if (get-in kentta [:raportointi :csv :selitteet])
    [(keyword (:kentta_id kentta)) (keyword (str (:kentta_id kentta) "_selite"))]
    (keyword (:kentta_id kentta))))

(defn taustatieto-kentat [taustatiedot]
  (->> taustatiedot
       (filter (comp :raportoi :csv :raportointi))
       (sort-by (comp :jarjestys :csv :raportointi))
       (map get-csv-field)
       flatten))

(defn get-csv-fields [kyselytyyppi taustatiedot]
  (concat (default-csv-fields kyselytyyppi) (taustatieto-kentat taustatiedot)))

(defn luo-käännökset [taustatiedot lang]
  (into (lang default-translations)
    (for [taustatieto (sort-by (comp :jarjestys :csv :raportointi) taustatiedot)]
      (let [translate-key (keyword (:kentta_id taustatieto))
            value (translate-field "kentta" lang taustatieto)]
        {translate-key value}))))

(defn hae-vastaus [kysymys vastaukset monivalintavaihtoehdot lang]
  (let [kysymyksen-vastaukset (filter  #(= (:kysymysid kysymys) (:kysymysid %)) vastaukset)]
    (get-answer-text monivalintavaihtoehdot (:vastaustyyppi kysymys) kysymyksen-vastaukset lang)))

(defn lisaa-selitteet [data selitteet lang]
  (-> data
      (assoc :tutkinto_selite
             (translate-field "nimi" lang
               (first (filter #(= (:tutkinto data) (:tutkintotunnus %)) (:tutkinnot selitteet)))))
      (assoc :toimipaikka_selite
             (translate-field "nimi" lang
               (first (filter #(= (:toimipaikka data) (:toimipaikkakoodi %)) (:toimipaikat selitteet)))))
      (assoc :hankintakoulutuksen_toteuttaja_selite
             (translate-field "nimi" lang
               (first (filter #(= (:hankintakoulutuksen_toteuttaja data) (:ytunnus %)) (:koulutustoimijat selitteet)))))
      (assoc :koulutusalakoodi_selite
             (translate-field "nimi" lang
               (first (filter #(= (:koulutusalakoodi data) (:koulutusalatunnus %)) (:koulutusalat selitteet)))))
      (assoc :asuinkunta_koodi_selite
             (translate-field "nimi" lang
               (first (filter #(= (:asuinkunta_koodi data) (:kuntakoodi %)) (:kunnat selitteet)))))
      (assoc :opiskelupaikkakunta_koodi_selite
             (translate-field "nimi" lang
               (first (filter #(= (:opiskelupaikkakunta_koodi data) (:kuntakoodi %)) (:kunnat selitteet)))))))


(defn format-vastaus [vastaus selitteet lang]
  (-> (merge (:taustatiedot vastaus) vastaus)
      (update :vastausaika format-date)
      (assoc :oppilaitos_nimi (translate-field "oppilaitos_nimi" lang vastaus))
      (lisaa-selitteet selitteet lang)))

(defn luo-vastausrivi [template lang taustatieto-fields choices selitteet answers]
  (let [formatted-answers (map #(format-vastaus % selitteet lang) answers)
        taustatiedot (select-values-or-nil (first formatted-answers) taustatieto-fields)]
    (concat taustatiedot (mapcat #(get-answer formatted-answers choices lang %) template))))

(defn luo-vastaajan-vastausrivit [[_ answers] kysymykset taustatieto-fields choices selitteet lang]
  (let [formatted-answers (map #(format-vastaus % selitteet lang) answers)
        taustatiedot (select-values-or-nil (first formatted-answers) taustatieto-fields)]
    (->> kysymykset
         (map #(concat taustatiedot
                       [(translate-field "kysymysryhma" lang %) (translate-field "kysymys" lang %)
                        (hae-vastaus % answers choices lang)])))))

(defn create-header-row [header kysymykset lang translations]
  (let [header-fields (map #(get translations %) header)
        kysymys-fields (map #(translate-field "kysymys" lang %) kysymykset)]
    (concat header-fields kysymys-fields)))

(defn create-header-row-single [taustatieto-fields taustakysymykset translations]
  (concat (map #(get translations %) taustatieto-fields)
          taustakysymykset
          (map #(get translations %) [:kysymysryhma :kysymys :vastaus])))

(defn hae-selitteet [kyselyid]
  {:tutkinnot (db/hae-kyselyn-tutkinnot {:kyselyid kyselyid})
   :toimipaikat (db/hae-kyselyn-toimipaikat {:kyselyid kyselyid})
   :koulutustoimijat (db/hae-kyselyn-koulutustoimijat {:kyselyid kyselyid})
   :koulutusalat (db/hae-kyselyn-koulutusalat {:kyselyid kyselyid})
   :kunnat (hae-kunnat (:koodistopalvelu @asetukset))})

(defn luovutuslupa [[vastaajaid vastaukset] kysymysid]
  (= 0 (:numerovalinta (first (filter #(= kysymysid (:kysymysid %)) vastaukset)))))

(defn filter-not-allowed [kyselytyyppi kysymykset vastaukset]
  (let [lupakysymys (:kysymysid (first (filter #(= "tietojen_luovutus" (-> % :metatiedot :taustakysymyksen_tyyppi)) kysymykset)))]
    (if (and lupakysymys (some #{"amk-uraseuranta"} #{kyselytyyppi}))
      (filter #(luovutuslupa % lupakysymys) vastaukset)
      vastaukset)))

(defn kysely-csv [kyselyid lang]
  (let [kyselytyyppi (:tyyppi (db/hae-kysely {:kyselyid kyselyid}))
        taustatiedot (db/kyselyn-kentat {:kyselyid kyselyid})
        taustatieto-fields (get-csv-fields kyselytyyppi taustatiedot)
        kysymykset (hae-kysymykset kyselyid)
        translations (luo-käännökset taustatiedot lang)
        vastaukset (filter-not-allowed kyselytyyppi kysymykset
                                       (group-by :vastaajaid (db/hae-vastaukset {:kyselyid kyselyid})))
        monivalintavaihtoehdot (hae-monivalinnat kysymykset)
        selitteet (hae-selitteet kyselyid)
        template (create-row-template kysymykset)
        header (create-header-row taustatieto-fields kysymykset lang translations)
        vastausrivit (map #(luo-vastausrivi template lang
                                            taustatieto-fields
                                            monivalintavaihtoehdot
                                            selitteet
                                            (second %)) vastaukset)]
    (csv-response kyselyid lang (create-csv (cons header vastausrivit)))))

(defn kysely-csv-vastauksittain [kyselyid lang]
  (let [kyselytyyppi (:tyyppi (db/hae-kysely {:kyselyid kyselyid}))
        taustatiedot (db/kyselyn-kentat {:kyselyid kyselyid})
        taustatieto-fields (get-csv-fields kyselytyyppi taustatiedot)
        kysymykset (hae-kysymykset kyselyid)
        selitteet (hae-selitteet kyselyid)
        translations (luo-käännökset taustatiedot lang)
        vastaukset (filter-not-allowed kyselytyyppi kysymykset (group-by :vastaajaid (db/hae-vastaukset {:kyselyid kyselyid})))
        monivalintavaihtoehdot (hae-monivalinnat kysymykset)
        header (create-header-row-single taustatieto-fields translations)
        vastausrivit (mapcat #(luo-vastaajan-vastausrivit % kysymykset taustatieto-fields monivalintavaihtoehdot selitteet lang) vastaukset)]
    (csv-response kyselyid lang (create-csv (cons header vastausrivit)))))

(defn vastaajatunnus-url [tunnus]
  (str (:vastaus-base-url @asetukset) "/" (:tunnus tunnus)))

(defn create-header-row-single [taustatieto-fields translations]
  (concat (map #(get translations %) taustatieto-fields)
          (map #(get translations %) [:kysymysryhma :kysymys :vastaus])))

(defn format-tunnus [tunnus selitteet lang]
  (-> (merge (:taustatiedot tunnus) tunnus)
      (assoc :url (vastaajatunnus-url tunnus))
      (update :voimassa_alkupvm format-date)
      (update :voimassa_loppupvm format-date)
      (update :luotuaika format-date)
      (lisaa-selitteet selitteet lang)))

(defn vastaajatunnus-csv [kyselykertaid lang]
  (let [kyselyid (:kyselyid (db/hae-kyselykerta {:kyselykertaid kyselykertaid}))
        selitteet (hae-selitteet kyselyid)
        tunnukset (map #(format-tunnus % selitteet lang) (db/hae-vastaajatunnus {:kyselykertaid kyselykertaid}))
        taustatiedot (db/kyselyn-kentat {:kyselyid kyselyid})
        translations (luo-käännökset taustatiedot lang)
        vastaajatunnus-kentat (concat default-vastaajatunnus-fields (taustatieto-kentat taustatiedot))
        header (map #(get translations %) vastaajatunnus-kentat)
        rows (map #(select-values-or-nil % vastaajatunnus-kentat) tunnukset)]
    (create-csv (cons header rows))))

(def kohteet-fields [:tunnus :nimi :voimassa_alkupvm :kohteiden_lkm :vastaajien_lkm :tutkintotunnus :tutkinto_fi])

(defn kohteet-csv [kyselyid lang]
  (let [res (db/hae-kyselyn-kohteet {:kyselyid kyselyid})
        header (map #(get-in default-translations [lang %]) kohteet-fields)
        rows (map #(select-values-or-nil % kohteet-fields) res)]
    (create-csv (cons header rows))))

(def vastanneet-fields [:tunnus :nimi :voimassa_alkupvm :vastausaika :tutkintotunnus :tutkinto_fi])

(defn vastanneet-csv [kyselyid lang]
  (let [vastanneet (db/hae-kyselyn-vastaajat {:kyselyid kyselyid})
        header (map #(get-in default-translations [lang %]) vastanneet-fields)
        rows (map #(select-values-or-nil % vastanneet-fields) vastanneet)]
    (create-csv (cons header rows))))
