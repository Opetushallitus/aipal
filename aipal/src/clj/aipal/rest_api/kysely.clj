;; Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
;;
;; This program is free software:  Licensed under the EUPL, Version 1.1 or - as
;; soon as they will be approved by the European Commission - subsequent versions
;; of the EUPL (the "Licence");
;;
;; You may not use this work except in compliance with the Licence.
;; You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; European Union Public Licence for more details.

(ns aipal.rest-api.kysely
  (:require [compojure.api.core :refer [defroutes DELETE GET POST PUT]]
            [schema.core :as s]
            [aipal.arkisto.kysely :as arkisto]
            [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
            [aipal.arkisto.kysymysryhma :as kysymysryhma-arkisto]
            [aipal.infra.kayttaja :refer [*kayttaja* yllapitaja?]]
            [aipal.rest-api.kysymysryhma :refer [lisaa-jarjestys]]
            [clojure.tools.logging :as log]
            [oph.common.util.http-util :refer [response-or-404 parse-iso-date]]
            [oph.common.util.util :refer [map-by paivita-arvot]]))

(defn lisaa-kysymysryhma!
  [kyselyid kysymysryhma]
  (let [kayttajan-kysymykset (map-by :kysymysid (:kysymykset kysymysryhma))]
    (doseq [kysymys (arkisto/hae-kysymysten-poistettavuus (:kysymysryhmaid kysymysryhma))
            :let [kysymysid (:kysymysid kysymys)
                  kayttajan-kysymys (get kayttajan-kysymykset kysymysid)]
            :when (not (and (:poistettu kayttajan-kysymys)
                            (:poistettava kysymys)))]
      ;; Assertiin pitäisi törmätä vain jos käyttäjä on muokannut poistetuksi kysymyksen jota ei saisi poistaa
      (assert (not (:poistettu kayttajan-kysymys)))
      (arkisto/lisaa-kysymys! kyselyid kysymysid)))
  (arkisto/lisaa-kysymysryhma! kyselyid kysymysryhma))

(defn lisakysymysten-lukumaara
  [kysymysryhmat]
  (->> kysymysryhmat
     (remove :valtakunnallinen)
     (mapcat :kysymykset)
     (remove :poistettu)
     count))

(def ^:const max-kysymyksia 140)

(defn kysymysryhma-on-julkaistu? [kysymysryhmaid]
  (= "julkaistu" (:tila (kysymysryhma-arkisto/hae kysymysryhmaid false))))

(defn valmistele-luonnos-paivitys [kysely]
  (arkisto/poista-kysymysryhmat! (:kyselyid kysely))
  (arkisto/poista-kysymykset! (:kyselyid kysely))
  (doseq [kysymysryhma (lisaa-jarjestys (:kysymysryhmat kysely))]
    (assert (kysymysryhma-on-julkaistu? (:kysymysryhmaid kysymysryhma)))
    (lisaa-kysymysryhma! (:kyselyid kysely) kysymysryhma)))

(defn validoi-vain-omia-organisaatioita [kysely]
  (let [loytyvat-pakolliset-kysymysryhmaidt (map :kysymysryhmaid (arkisto/get-kyselyn-pakolliset-kysymysryhmaidt (:kyselyid kysely)))
        kyselyn-kysymysryhmaidt (set (map :kysymysryhmaid (:kysymysryhmat kysely)))]
    (log/info loytyvat-pakolliset-kysymysryhmaidt)
    (log/info kyselyn-kysymysryhmaidt)
    (assert (every? kyselyn-kysymysryhmaidt loytyvat-pakolliset-kysymysryhmaidt))))

(defn valmistele-julkaistu-paivitys [kysely]
  (assert (= "julkaistu" (:tila kysely)))
  (validoi-vain-omia-organisaatioita kysely)
  (valmistele-luonnos-paivitys kysely))

(defn paivita-kysely!
  [kysely]
  (assert (not (> (lisakysymysten-lukumaara (:kysymysryhmat kysely)) max-kysymyksia)))
  (if (= "luonnos" (:tila kysely))
    (valmistele-luonnos-paivitys kysely)
    (valmistele-julkaistu-paivitys kysely))
  (arkisto/muokkaa-kyselya! kysely))

(defn valid-url? "jäljittelee angular-puolen äärisimppeliä validointia"
  [url]
  (let [cnt (count url)]
    (or (= cnt 0) (and (<= cnt 2000) (not (nil? (re-matches #"^http(s?):\/\/(.*)$", url)))))))

(defroutes reitit
  (GET  "/" []
    :kayttooikeus :katselu
    (response-or-404 (arkisto/hae-kaikki (:aktiivinen-koulutustoimija *kayttaja*))))
  (POST "/" []
    :body [kysely s/Any]
    :kayttooikeus :kysely
    (let [kysely (assoc (paivita-arvot kysely
                                      [:voimassa_alkupvm :voimassa_loppupvm]
                                      parse-iso-date)
                        :tyyppi (->> kysely :tyyppi :id)
                        :tila "luonnos"
                        :koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*))]
      (if (arkisto/samanniminen-kysely? kysely)
        {:status 400
         :body "kysely.samanniminen_kysely"}
        (response-or-404
          (format "%s" (let [{:keys [kyselyid]}
                             (arkisto/lisaa! (select-keys kysely [:nimi_fi :nimi_sv :nimi_en :selite_fi :selite_sv :selite_en :voimassa_alkupvm :voimassa_loppupvm :tila :koulutustoimija :tyyppi :kyselypohjaid]))]
                        (paivita-kysely! (assoc kysely :kyselyid kyselyid))))))))

  (GET "/kyselytyypit" []
    :kayttooikeus :katselu
    (response-or-404 (arkisto/hae-kyselytyypit)))

  (POST "/:kyselyid" []
    :path-params [kyselyid :- s/Int]
    :body [kysely s/Any]
    :kayttooikeus [:kysely {:kyselyid kyselyid}]
    (let [kysely (assoc (paivita-arvot (assoc kysely :kyselyid kyselyid)
                                       [:voimassa_alkupvm :voimassa_loppupvm]
                                       parse-iso-date)
                        :tyyppi (->> kysely :tyyppi :id))]
      (if (arkisto/samanniminen-kysely? (assoc kysely :koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*)))
        {:status 400
         :body "kysely.samanniminen_kysely"}
        (response-or-404 (format "%s" (paivita-kysely! kysely))))))


  (DELETE "/:kyselyid" []
    :path-params [kyselyid :- s/Int]
    :kayttooikeus [:kysely {:kyselyid kyselyid}]
    (if (arkisto/kysely-poistettavissa? kyselyid)
      (do
        (arkisto/poista-kysely! kyselyid)
        {:status 204})
      {:status 403}))

  (GET "/:kyselyid/vastaustunnustiedot" []
    :path-params [kyselyid :- s/Int]
    :kayttooikeus [:katselu {:kyselyid kyselyid}]
    (response-or-404 (kyselykerta-arkisto/hae-vastaustunnustiedot-kyselylta kyselyid)))

  (GET "/:kyselyid" []
    :path-params [kyselyid :- s/Int]
    :kayttooikeus [:katselu {:kyselyid kyselyid}]
    (response-or-404 (when-let [kysely (arkisto/hae kyselyid)]
                       (assoc kysely :kysymysryhmat (kysymysryhma-arkisto/hae-kyselysta kyselyid)))))

  (PUT "/julkaise/:kyselyid" []
    :path-params [kyselyid :- s/Int]
    :kayttooikeus [:kysely {:kyselyid kyselyid}]
    (if (> (arkisto/laske-kysymysryhmat kyselyid) 0)
      (response-or-404 (arkisto/julkaise-kysely! kyselyid))
      {:status 403}))

  (PUT "/sulje/:kyselyid" []
    :path-params [kyselyid :- s/Int]
    :kayttooikeus [:kysely {:kyselyid kyselyid}]
    (response-or-404 (arkisto/sulje-kysely! kyselyid)))

  (PUT "/palauta/:kyselyid" []
    :path-params [kyselyid :- s/Int]
    :kayttooikeus [:kysely {:kyselyid kyselyid}]
    (response-or-404 (arkisto/julkaise-kysely! kyselyid)))

  (PUT "/palauta-luonnokseksi/:kyselyid" []
    :path-params [kyselyid :- s/Int]
    :kayttooikeus [:kysely {:kyselyid kyselyid}]
    (if (= (arkisto/laske-kyselykerrat kyselyid) 0)
      (response-or-404 (arkisto/palauta-luonnokseksi! kyselyid))
      {:status 403})))
