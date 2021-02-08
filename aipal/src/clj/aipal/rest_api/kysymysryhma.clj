(ns aipal.rest-api.kysymysryhma
  (:require [compojure.api.core :refer [defroutes DELETE GET POST PUT]]
            [clojure.tools.logging :as log]
            [schema.core :as s]
            [aipal.arkisto.kysymysryhma :as arkisto]
            aipal.compojure-util
            [aipal.infra.kayttaja :refer [*kayttaja* yllapitaja?]]
            [oph.common.util.http-util :refer [response-or-404]]
            [arvo.util :refer [add-index]]
            [ring.util.http-response :as response]))

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
                        :kysymys_en
                        :selite_fi
                        :selite_sv
                        :selite_en
                        :max_vastaus
                        :monivalinta_max
                        :rajoite
                        :jarjestys
                        :metatiedot]))

(defn valitse-vaihtoehdon-kentat [vaihtoehto]
  (select-keys vaihtoehto [:jarjestys
                           :teksti_fi
                           :teksti_sv
                           :teksti_en]))

(def kysymys-fields [:kysymysryhmaid :kysymys_fi :kysymys_sv :kysymys_en :jarjestys :monivalinta_max :max_vastaus, :eos_vastaus_sallittu :vastaustyyppi :jatkokysymys :pakollinen :poistettava :rajoite])
(def kysymys-defaults (zipmap kysymys-fields (repeat nil)))

(def jatkokysymys-defaults {:vastaustyyppi "vapaateksti"
                            :pakollinen false
                            :jatkokysymys true
                            :poistettava false
                            :rajoite nil})

(defn muodosta-jatkokysymykset [kysymys kysymysryhmaid]
  (when (and (= "kylla_ei_valinta" (:vastaustyyppi kysymys))
             (:jatkokysymykset kysymys))
    (let [merge-kys (fn [jk] (assoc (second jk)
                               :vastaus (name (first jk))
                               :kysymysryhmaid kysymysryhmaid))]
         (->> (:jatkokysymykset kysymys)
              (map merge-kys)
              (map #(merge jatkokysymys-defaults %))
              (map #(merge kysymys-defaults %))))))

(defn lisaa-monivalintavaihtoehdot! [vaihtoehdot kysymysid]
  (when (nil? vaihtoehdot)
    (log/error "Kysymyksellä" kysymysid "ei ole monivalintavaihtoehtoja."))
  (doseq [v (add-index :jarjestys vaihtoehdot)]
    (-> v
      valitse-vaihtoehdon-kentat
      (assoc :kysymysid kysymysid)
      (assoc :luotu_kayttaja (:oid *kayttaja*))
      (assoc :muutettu_kayttaja (:oid *kayttaja*))
      arkisto/lisaa-monivalintavaihtoehto!)))

(defn lisaa-kysymys! [kysymys kysymysryhmaid]
  (let [kysymysid (-> kysymys
                    valitse-kysymyksen-kentat
                    korjaa-eos-vastaus-sallittu
                    (assoc :kysymysryhmaid kysymysryhmaid)
                    (assoc :luotu_kayttaja (:oid *kayttaja*))
                    (assoc :muutettu_kayttaja (:oid *kayttaja*))
                    (arkisto/lisaa-kysymys! kysymysryhmaid)
                    first
                    :kysymysid)
        jatkokysymykset (muodosta-jatkokysymykset kysymys kysymysryhmaid)]
    (doseq [jatkokysymys jatkokysymykset]
      (let [jatkokysymysid (arkisto/lisaa-kysymys! (select-keys jatkokysymys kysymys-fields) kysymysryhmaid)]
        (log/info kysymysid)
        (log/info jatkokysymysid)
        (arkisto/liita-jatkokysymys! kysymysid, (:kysymysid (first jatkokysymysid)), (:vastaus jatkokysymys))))

    (when (= "monivalinta" (:vastaustyyppi kysymys))
      (lisaa-monivalintavaihtoehdot! (:monivalintavaihtoehdot kysymys) kysymysid))))

(defn lisaa-kysymykset-kysymysryhmaan! [kysymykset kysymysryhmaid]
  (doseq [k (add-index :jarjestys kysymykset)]
    (lisaa-kysymys! k kysymysryhmaid)))

(defn ^:private valitse-kysymysryhman-peruskentat [kysymysryhma]
  (select-keys kysymysryhma [:nimi_fi
                             :nimi_sv
                             :nimi_en
                             :selite_fi
                             :selite_sv
                             :selite_en
                             :kuvaus_fi
                             :kuvaus_sv
                             :kuvaus_en]))

(defn ^:private suodata-vain-yllapitajalle [kysymysryhma kentta]
  (if (yllapitaja?)
    (true? (kentta kysymysryhma))
    false))

(defn lisaa-kysymysryhma! [kysymysryhma kysymykset]
  (let [kysymysryhma (arkisto/lisaa-kysymysryhma! (merge (valitse-kysymysryhman-peruskentat kysymysryhma)
                                                         {:koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*)
                                                          :taustakysymykset (suodata-vain-yllapitajalle kysymysryhma :taustakysymykset)
                                                          :valtakunnallinen (suodata-vain-yllapitajalle kysymysryhma :valtakunnallinen)}))]
    (lisaa-kysymykset-kysymysryhmaan! kysymykset (:kysymysryhmaid kysymysryhma))
    (doall kysymysryhma)))

(defn paivita-kysymysryhma! [kysymysryhma]
  (when (arkisto/luonnos? (:kysymysryhmaid kysymysryhma))
    (let [kysymysryhma (-> kysymysryhma
                         korjaa-eos-vastaus-sallittu
                         (assoc :valtakunnallinen (suodata-vain-yllapitajalle kysymysryhma :valtakunnallinen)
                                :taustakysymykset (suodata-vain-yllapitajalle kysymysryhma :taustakysymykset)))
          kysymysryhmaid (:kysymysryhmaid kysymysryhma)
          kysymykset (:kysymykset kysymysryhma)]
      (arkisto/poista-kysymysryhman-kysymykset! kysymysryhmaid)
      (lisaa-kysymykset-kysymysryhmaan! kysymykset kysymysryhmaid)
      (arkisto/paivita! kysymysryhma)
      kysymysryhma)))


(defn poista-kysymysryhma! [kysymysryhmaid]
  (when (arkisto/luonnos? kysymysryhmaid)
    (arkisto/poista-kysymysryhman-kysymykset! kysymysryhmaid)
    (arkisto/poista! kysymysryhmaid)))

(defroutes reitit
  (GET "/" []
    :query-params [{taustakysymysryhmat :- Boolean false}
                   {voimassa :- Boolean false}]
    :kayttooikeus :katselu
    (response-or-404
      (if taustakysymysryhmat
        (arkisto/hae-taustakysymysryhmat)
        (arkisto/hae-kysymysryhmat (:aktiivinen-koulutustoimija *kayttaja*) voimassa))))

  (GET "/asteikot" []
    :kayttooikeus :katselu
    (response-or-404 (arkisto/hae-asteikot (:aktiivinen-koulutustoimija *kayttaja*))))

  (POST "/asteikot" []
    :body [asteikko s/Any]
    :kayttooikeus :kysymysryhma
    (let [tallennettava-asteikko (assoc asteikko :koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*))]
      (response-or-404 (arkisto/tallenna-asteikko tallennettava-asteikko))))

  (POST "/" []
    :body [kysymysryhma s/Any]
    :kayttooikeus :kysymysryhma
    (response-or-404 (lisaa-kysymysryhma! kysymysryhma (:kysymykset kysymysryhma))))

  (PUT "/:kysymysryhmaid" []
    :path-params [kysymysryhmaid :- s/Int]
    :body [kysymysryhma s/Any]
    :kayttooikeus [:kysymysryhma {:kysymysryhmaid kysymysryhmaid}]
    (response-or-404 (paivita-kysymysryhma! (assoc kysymysryhma :kysymysryhmaid kysymysryhmaid))))

  (DELETE "/:kysymysryhmaid" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:kysymysryhma {:kysymysryhmaid kysymysryhmaid}]
    (poista-kysymysryhma! kysymysryhmaid)
    {:status 204})

  (PUT "/:kysymysryhmaid/julkaise" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:kysymysryhma {:kysymysryhmaid kysymysryhmaid}]
    (if (pos? (arkisto/laske-kysymykset kysymysryhmaid))
      (response-or-404 (arkisto/julkaise! kysymysryhmaid))
      {:status 403}))

  (PUT "/:kysymysryhmaid/palauta" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:kysymysryhma {:kysymysryhmaid kysymysryhmaid}]
    (if (and
          (zero? (arkisto/laske-kyselyt kysymysryhmaid))
          (zero? (arkisto/laske-kyselypohjat kysymysryhmaid)))
      (response-or-404 (arkisto/palauta-luonnokseksi! kysymysryhmaid))
      {:status 403}))

  (PUT "/:kysymysryhmaid/sulje" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:kysymysryhma {:kysymysryhmaid kysymysryhmaid}]
    (response-or-404 (arkisto/sulje! kysymysryhmaid)))

  (GET "/:kysymysryhmaid" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:katselu {:kysymysryhmaid kysymysryhmaid}]
    (response-or-404 (arkisto/hae kysymysryhmaid)))

  ;; Muuten sama kuin ylläoleva, mutta haettaessa vuoden 2015 taustakysymysryhmiä yhdistää hakeutumis- ja suoritusvaiheen kysymysryhmät
  (GET "/taustakysymysryhma/:kysymysryhmaid" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:katselu {:kysymysryhmaid kysymysryhmaid}]
    (response-or-404 (arkisto/hae-taustakysymysryhma kysymysryhmaid)))

  (GET "/:kysymysryhmaid/esikatselu" []
    :path-params [kysymysryhmaid :- s/Int]
    :kayttooikeus [:katselu {:kysymysryhmaid kysymysryhmaid}]
    (response-or-404 (arkisto/hae-esikatselulle kysymysryhmaid))))