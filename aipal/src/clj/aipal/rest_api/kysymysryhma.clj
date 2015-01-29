(ns aipal.rest-api.kysymysryhma
  (:require [compojure.core :as c]
            [oph.common.util.http-util :refer [json-response]]
            [aipal.compojure-util :as cu]
            [clojure.tools.logging :as log]
            [aipal.arkisto.kysymysryhma :as arkisto]
            [aipal.infra.kayttaja :refer [*kayttaja* yllapitaja?]]))

(defn lisaa-jarjestys [alkiot]
  (map #(assoc %1 :jarjestys %2) alkiot (range)))

(defn korjaa-eos-vastaus-sallittu [{:keys [eos_vastaus_sallittu pakollinen vastaustyyppi] :as kysymys}]
  (assoc kysymys :eos_vastaus_sallittu (and eos_vastaus_sallittu
                                            pakollinen
                                            (not= vastaustyyppi "vapaateksti"))))

(defn valitse-kysymyksen-kentat [kysymys]
  (select-keys kysymys [:pakollinen
                        :eos_vastaus_sallittu
                        :poistettava
                        :vastaustyyppi
                        :kysymys_fi
                        :kysymys_sv
                        :max_vastaus
                        :monivalinta_max
                        :jarjestys]))

(defn valitse-jatkokysymyksen-kentat [jatkokysymys]
  (select-keys jatkokysymys [:kylla_teksti_fi
                             :kylla_teksti_sv
                             :ei_teksti_fi
                             :ei_teksti_sv
                             :max_vastaus]))

(defn valitse-vaihtoehdon-kentat [vaihtoehto]
  (select-keys vaihtoehto [:jarjestys
                           :teksti_fi
                           :teksti_sv]))

(defn muodosta-jatkokysymys [kysymys]
  (when (and (= "kylla_ei_valinta" (:vastaustyyppi kysymys))
             (:jatkokysymys kysymys))
    (valitse-jatkokysymyksen-kentat (:jatkokysymys kysymys))))

(defn lisaa-monivalintavaihtoehdot! [vaihtoehdot kysymysid]
  (when (nil? vaihtoehdot)
    (log/error "Kysymyksellä" kysymysid "ei ole monivalintavaihtoehtoja."))
  (doseq [v (lisaa-jarjestys vaihtoehdot)]
    (-> v
      valitse-vaihtoehdon-kentat
      (assoc :kysymysid kysymysid)
      arkisto/lisaa-monivalintavaihtoehto!)))

(defn lisaa-kysymys! [kysymys kysymysryhmaid]
  (assert (not= (:vastaustyyppi kysymys) "asteikko"))
  (let [jatkokysymysid (some-> kysymys
                         muodosta-jatkokysymys
                         arkisto/lisaa-jatkokysymys!
                         :jatkokysymysid)
        kysymysid (-> kysymys
                    valitse-kysymyksen-kentat
                    korjaa-eos-vastaus-sallittu
                    (assoc :kysymysryhmaid kysymysryhmaid
                           :jatkokysymysid jatkokysymysid)
                    arkisto/lisaa-kysymys!
                    :kysymysid)]
    (when (= "monivalinta" (:vastaustyyppi kysymys))
      (lisaa-monivalintavaihtoehdot! (:monivalintavaihtoehdot kysymys) kysymysid))))

(defn lisaa-kysymykset-kysymysryhmaan! [kysymykset kysymysryhmaid]
  (doseq [k (lisaa-jarjestys kysymykset)]
    (lisaa-kysymys! k kysymysryhmaid)))

(defn lisaa-kysymysryhma! [kysymysryhma kysymykset]
  (let [kysymysryhma (arkisto/lisaa-kysymysryhma! kysymysryhma)
        kysymysryhmaid (:kysymysryhmaid kysymysryhma)]
    (lisaa-kysymykset-kysymysryhmaan! kysymykset kysymysryhmaid)
    (json-response kysymysryhma)))

(defn poista-kysymys! [kysymys]
  (when (= "monivalinta" (:vastaustyyppi kysymys))
    (arkisto/poista-kysymyksen-monivalintavaihtoehdot! (:kysymysid kysymys)))
  (when (:jatkokysymysid kysymys)
    (arkisto/poista-jatkokysymys! (:jatkokysymysid kysymys)))
  (arkisto/poista-kysymys! (:kysymysid kysymys)))

(defn poista-kysymysryhman-kysymykset! [kysymysryhmaid]
  (let [kysymysryhma (arkisto/hae kysymysryhmaid)
        kysymykset (:kysymykset kysymysryhma)]
    (doseq [kysymys kysymykset]
      (poista-kysymys! kysymys))))

(defn paivita-kysymysryhma! [kysymysryhma]
  (let [kysymysryhmaid (:kysymysryhmaid kysymysryhma)
        kysymykset (:kysymykset kysymysryhma)]
    (poista-kysymysryhman-kysymykset! kysymysryhmaid)
    (lisaa-kysymykset-kysymysryhmaan! kysymykset kysymysryhmaid)
    (arkisto/paivita! kysymysryhma)))

(defn poista-kysymysryhma! [kysymysryhmaid]
  (poista-kysymysryhman-kysymykset! kysymysryhmaid)
  (arkisto/poista! kysymysryhmaid))

(c/defroutes reitit
  (cu/defapi :kysymysryhma-listaaminen nil :get "/" [taustakysymysryhmat voimassa]
    (let [taustakysymysryhmat (Boolean/parseBoolean taustakysymysryhmat)
          voimassa (Boolean/parseBoolean voimassa)]
      (json-response
        (if taustakysymysryhmat
          (arkisto/hae-taustakysymysryhmat)
          (arkisto/hae-kysymysryhmat (:aktiivinen-koulutustoimija *kayttaja*) voimassa)))))

  (cu/defapi :kysymysryhma-luonti nil :post "/" [nimi_fi selite_fi nimi_sv selite_sv valtakunnallinen kysymykset taustakysymykset]
    (lisaa-kysymysryhma! {:nimi_fi nimi_fi
                          :selite_fi selite_fi
                          :nimi_sv nimi_sv
                          :selite_sv selite_sv
                          :valtakunnallinen (if (yllapitaja?)
                                              (true? valtakunnallinen)
                                              false)
                          :taustakysymykset (if (yllapitaja?)
                                              (true? taustakysymykset)
                                              false)
                          :koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*)}
                         kysymykset))

  (cu/defapi :kysymysryhma-muokkaus kysymysryhmaid :put "/:kysymysryhmaid" [kysymysryhmaid & kysymysryhma]
    (json-response
      (paivita-kysymysryhma!
        (-> kysymysryhma
          korjaa-eos-vastaus-sallittu
          (assoc :kysymysryhmaid (Integer/parseInt kysymysryhmaid)
                 :valtakunnallinen (if (yllapitaja?) (true? (:valtakunnallinen kysymysryhma)) false)
                 :taustakysymykset (if (yllapitaja?) (true? (:taustakysymykset kysymysryhma)) false))))))

  (cu/defapi :kysymysryhma-poisto kysymysryhmaid :delete "/:kysymysryhmaid" [kysymysryhmaid]
    (let [kysymysryhmaid (Integer/parseInt kysymysryhmaid)]
      (poista-kysymysryhma! kysymysryhmaid)))

  (cu/defapi :kysymysryhma-julkaisu kysymysryhmaid :put "/:kysymysryhmaid/julkaise" [kysymysryhmaid]
    (let [kysymysryhmaid (Integer/parseInt kysymysryhmaid)]
      (if (pos? (arkisto/laske-kysymykset kysymysryhmaid))
        (json-response (arkisto/julkaise! kysymysryhmaid))
        {:status 403})))

  (cu/defapi :kysymysryhma-palautus-luonnokseksi kysymysryhmaid :put "/:kysymysryhmaid/palauta" [kysymysryhmaid]
    (let [kysymysryhmaid (Integer/parseInt kysymysryhmaid)]
      (if (and
            (zero? (arkisto/laske-kyselyt kysymysryhmaid))
            (zero? (arkisto/laske-kyselypohjat kysymysryhmaid)))
        (json-response (arkisto/palauta-luonnokseksi! kysymysryhmaid))
        {:status 403})))

  (cu/defapi :kysymysryhma-sulkeminen kysymysryhmaid :put "/:kysymysryhmaid/sulje" [kysymysryhmaid]
    (let [kysymysryhmaid (Integer/parseInt kysymysryhmaid)]
      (json-response (arkisto/sulje! kysymysryhmaid))))

  (cu/defapi :kysymysryhma-luku kysymysryhmaid :get "/:kysymysryhmaid" [kysymysryhmaid]
    (json-response (arkisto/hae (Integer/parseInt kysymysryhmaid))))

  ;; Muuten sama kuin ylläoleva, mutta haettaessa vuoden 2015 taustakysymysryhmiä yhdistää hakeutumis- ja suoritusvaiheen kysymysryhmät
  (cu/defapi :kysymysryhma-luku kysymysryhmaid :get "/taustakysymysryhma/:kysymysryhmaid" [kysymysryhmaid]
    (json-response (arkisto/hae-taustakysymysryhma (Integer/parseInt kysymysryhmaid))))

  (cu/defapi :kysymysryhma-luku kysymysryhmaid :get "/:kysymysryhmaid/esikatselu" [kysymysryhmaid]
    (json-response (arkisto/hae-esikatselulle (Integer/parseInt kysymysryhmaid)))))
