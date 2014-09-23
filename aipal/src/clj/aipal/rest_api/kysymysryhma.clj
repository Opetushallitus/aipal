(ns aipal.rest-api.kysymysryhma
  (:require [compojure.core :as c]
            [oph.common.util.http-util :refer [json-response]]
            [aipal.compojure-util :as cu]
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

(c/defroutes reitit
  (cu/defapi :kysymysryhma-listaaminen nil :get "/" []
    (json-response (arkisto/hae-kysymysryhmat)))

  (cu/defapi :kysymysryhma-luonti nil :post "/" [nimi_fi selite_fi nimi_sv selite_sv kysymykset]
    (let [kysymysryhma (arkisto/lisaa-kysymysryhma! {:nimi_fi nimi_fi
                                                     :selite_fi selite_fi
                                                     :nimi_sv nimi_sv
                                                     :selite_sv selite_sv})]
      (doseq [k (jarjesta-alkiot kysymykset)
              :let [jatkokysymys (muodosta-jatkokysymys k)
                    jatkokysymys (when jatkokysymys (arkisto/lisaa-jatkokysymys! jatkokysymys))
                    kysymys (valitse-kysymyksen-kentat k)
                    kysymys (assoc kysymys
                                   :kysymysryhmaid (:kysymysryhmaid kysymysryhma)
                                   :jatkokysymysid (:jatkokysymysid jatkokysymys))
                    kysymys (arkisto/lisaa-kysymys! kysymys)]
              :when (:monivalintavaihtoehdot k)
              v (jarjesta-alkiot (:monivalintavaihtoehdot k))
              :let [vaihtoehto (valitse-vaihtoehdon-kentat v)
                    vaihtoehto (assoc vaihtoehto :kysymysid (:kysymysid kysymys))]]
        (arkisto/lisaa-monivalintavaihtoehto! vaihtoehto))
      (json-response kysymysryhma))))
