(ns aipal.rest-api.kysymysryhma
  (:require [compojure.core :as c]
            [oph.common.util.http-util :refer [json-response]]
            [aipal.compojure-util :as cu]
            [clojure.tools.logging :as log]
            [aipal.arkisto.kysymysryhma :as arkisto]))

(defn jarjesta-alkiot [alkiot]
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
  (doseq [v (jarjesta-alkiot vaihtoehdot)]
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

(defn lisaa-kysymysryhma! [kysymysryhma kysymykset]
  (let [kysymysryhma (arkisto/lisaa-kysymysryhma! kysymysryhma)]
    (doseq [k (jarjesta-alkiot kysymykset)]
      (lisaa-kysymys! k (:kysymysryhmaid kysymysryhma)))
    (json-response kysymysryhma)))

(c/defroutes reitit
  (cu/defapi :kysymysryhma-listaaminen nil :get "/" []
    (json-response (arkisto/hae-kysymysryhmat)))

  (cu/defapi :kysymysryhma-luonti nil :post "/" [nimi_fi selite_fi nimi_sv selite_sv kysymykset]
    (lisaa-kysymysryhma! {:nimi_fi nimi_fi
                          :selite_fi selite_fi
                          :nimi_sv nimi_sv
                          :selite_sv selite_sv}
                         kysymykset)))
