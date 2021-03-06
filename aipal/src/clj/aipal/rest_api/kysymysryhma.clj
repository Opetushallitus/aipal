(ns aipal.rest-api.kysymysryhma
  (:require [compojure.api.core :refer [defroutes DELETE GET POST PUT]]
            [clojure.tools.logging :as log]
            [schema.core :as s]
            [aipal.arkisto.kysymysryhma :as arkisto]
            aipal.compojure-util
            [aipal.infra.kayttaja :refer [*kayttaja* ntm-vastuukayttaja? yllapitaja?]]
            [oph.common.util.http-util :refer [response-or-404]]))

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

(defn ^:private valitse-kysymysryhman-peruskentat [kysymysryhma]
  (select-keys kysymysryhma [:nimi_fi
                             :nimi_sv
                             :selite_fi
                             :selite_sv]))

(defn ^:private suodata-vain-yllapitajalle [kysymysryhma kentta]
  (if (yllapitaja?)
    (true? (kentta kysymysryhma))
    false))

(defn ^:private suodata-vain-ntm-vastuukayttajille [kysymysryhma kentta]
  (if (or (yllapitaja?)
          (ntm-vastuukayttaja?))
    (true? (kentta kysymysryhma))
    false))

(defn lisaa-kysymysryhma! [kysymysryhma kysymykset]
  (let [kysymysryhma (arkisto/lisaa-kysymysryhma! (merge (valitse-kysymysryhman-peruskentat kysymysryhma)
                                                         {:koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*)
                                                          :ntm_kysymykset (suodata-vain-ntm-vastuukayttajille kysymysryhma :ntm_kysymykset)
                                                          :taustakysymykset (suodata-vain-yllapitajalle kysymysryhma :taustakysymykset)
                                                          :valtakunnallinen (suodata-vain-yllapitajalle kysymysryhma :valtakunnallinen)}))]
    (lisaa-kysymykset-kysymysryhmaan! kysymykset (:kysymysryhmaid kysymysryhma))
    kysymysryhma))

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
  (let [kysymysryhma (-> kysymysryhma
                       korjaa-eos-vastaus-sallittu
                       (assoc :valtakunnallinen (suodata-vain-yllapitajalle kysymysryhma :valtakunnallinen)
                              :taustakysymykset (suodata-vain-yllapitajalle kysymysryhma :taustakysymykset)
                              :ntm_kysymykset (suodata-vain-ntm-vastuukayttajille kysymysryhma :ntm_kysymykset)))
        kysymysryhmaid (:kysymysryhmaid kysymysryhma)
        kysymykset (:kysymykset kysymysryhma)]
    (poista-kysymysryhman-kysymykset! kysymysryhmaid)
    (lisaa-kysymykset-kysymysryhmaan! kysymykset kysymysryhmaid)
    (arkisto/paivita! kysymysryhma)
    kysymysryhma))

(defn poista-kysymysryhma! [kysymysryhmaid]
  (poista-kysymysryhman-kysymykset! kysymysryhmaid)
  (arkisto/poista! kysymysryhmaid))

(defroutes reitit
  (GET "/" []
    :query-params [{taustakysymysryhmat :- Boolean false}
                   {voimassa :- Boolean false}]
    :kayttooikeus :kysymysryhma-listaaminen
    (response-or-404
      (if taustakysymysryhmat
        (arkisto/hae-taustakysymysryhmat)
        (arkisto/hae-kysymysryhmat (:aktiivinen-koulutustoimija *kayttaja*) voimassa))))

  (POST "/" []
    :body [kysymysryhma s/Any]
    :kayttooikeus :kysymysryhma-luonti
    (response-or-404 (lisaa-kysymysryhma! kysymysryhma (:kysymykset kysymysryhma))))

  (PUT "/:kysymysryhmaid" []
    :path-params [kysymysryhmaid :- s/Int]
    :body [kysymysryhma s/Any]
    :kayttooikeus [:kysymysryhma-muokkaus kysymysryhmaid]
    (response-or-404 (paivita-kysymysryhma! (assoc kysymysryhma :kysymysryhmaid kysymysryhmaid))))

  (DELETE "/:kysymysryhmaid" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:kysymysryhma-poisto kysymysryhmaid]
    (poista-kysymysryhma! kysymysryhmaid)
    {:status 204})

  (PUT "/:kysymysryhmaid/julkaise" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:kysymysryhma-julkaisu kysymysryhmaid]
    (if (pos? (arkisto/laske-kysymykset kysymysryhmaid))
      (response-or-404 (arkisto/julkaise! kysymysryhmaid))
      {:status 403}))

  (PUT "/:kysymysryhmaid/palauta" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:kysymysryhma-palautus-luonnokseksi kysymysryhmaid]
    (if (and
          (zero? (arkisto/laske-kyselyt kysymysryhmaid))
          (zero? (arkisto/laske-kyselypohjat kysymysryhmaid)))
      (response-or-404 (arkisto/palauta-luonnokseksi! kysymysryhmaid))
      {:status 403}))

  (PUT "/:kysymysryhmaid/sulje" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:kysymysryhma-sulkeminen kysymysryhmaid]
    (response-or-404 (arkisto/sulje! kysymysryhmaid)))

  (GET "/:kysymysryhmaid" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:kysymysryhma-luku kysymysryhmaid]
    (response-or-404 (arkisto/hae kysymysryhmaid)))

  ;; Muuten sama kuin ylläoleva, mutta haettaessa vuoden 2015 taustakysymysryhmiä yhdistää hakeutumis- ja suoritusvaiheen kysymysryhmät
  (GET "/taustakysymysryhma/:kysymysryhmaid" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:kysymysryhma-luku kysymysryhmaid]
    (response-or-404 (arkisto/hae-taustakysymysryhma kysymysryhmaid)))

  (GET "/:kysymysryhmaid/esikatselu" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:kysymysryhma-luku kysymysryhmaid]
    (response-or-404 (arkisto/hae-esikatselulle kysymysryhmaid))))