(ns aipal.rest-api.kysymysryhma
  (:require [compojure.core :as c]
            [oph.common.util.http-util :refer [json-response]]
            [aipal.compojure-util :as cu]
            [clojure.tools.logging :as log]
            [aipal.arkisto.kysymysryhma :as arkisto]
            [aipal.toimiala.kayttajaoikeudet :refer [yllapitaja?]]
            [aipal.infra.kayttaja :refer [*kayttaja*]]))

(defn lisaa-jarjestys [alkiot]
  (map #(assoc %1 :jarjestys %2) alkiot (range)))

(defn valitse-kysymyksen-kentat [kysymys]
  (select-keys kysymys [:pakollinen
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
  (let [jatkokysymysid (some-> kysymys
                         muodosta-jatkokysymys
                         arkisto/lisaa-jatkokysymys!
                         :jatkokysymysid)
        kysymysid (-> kysymys
                    valitse-kysymyksen-kentat
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

(c/defroutes reitit
  (cu/defapi :kysymysryhma-listaaminen nil :get "/" [voimassa]
    (json-response (arkisto/hae-kysymysryhmat (:aktiivinen-koulutustoimija *kayttaja*) (Boolean/parseBoolean voimassa))))

  (cu/defapi :kysymysryhma-luonti nil :post "/" [nimi_fi selite_fi nimi_sv selite_sv valtakunnallinen kysymykset]
    (lisaa-kysymysryhma! {:nimi_fi nimi_fi
                          :selite_fi selite_fi
                          :nimi_sv nimi_sv
                          :selite_sv selite_sv
                          :valtakunnallinen (if (yllapitaja?)
                                              (true? valtakunnallinen)
                                              false)
                          :koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*)}
                         kysymykset))

  (cu/defapi :kysymysryhma-muokkaus kysymysryhmaid :put "/:kysymysryhmaid" [kysymysryhmaid & kysymysryhma]
    (json-response
      (paivita-kysymysryhma! (assoc kysymysryhma :kysymysryhmaid (Integer/parseInt kysymysryhmaid)))))

  (cu/defapi :kysymysryhma-julkaisu kysymysryhmaid :put "/julkaise/:kysymysryhmaid" [kysymysryhmaid]
    (let [kysymysryhmaid (Integer/parseInt kysymysryhmaid)]
      (if (pos? (arkisto/laske-kysymykset kysymysryhmaid))
        (json-response (arkisto/julkaise! kysymysryhmaid))
        {:status 403})))

  (cu/defapi :kysymysryhma-luku kysymysryhmaid :get "/:kysymysryhmaid" [kysymysryhmaid]
    (json-response (arkisto/hae (Integer/parseInt kysymysryhmaid))))

  (cu/defapi :kysymysryhma-luku kysymysryhmaid :get "/:kysymysryhmaid/esikatselu" [kysymysryhmaid]
    (json-response (arkisto/hae-esikatselulle (Integer/parseInt kysymysryhmaid)))))
