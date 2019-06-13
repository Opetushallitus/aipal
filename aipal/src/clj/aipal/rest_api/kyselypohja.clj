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

(ns aipal.rest-api.kyselypohja
  (:require [compojure.api.core :refer [defroutes DELETE GET POST PUT]]
            [schema.core :as s]
            [aipal.arkisto.kyselypohja :as arkisto]
            [aipal.arkisto.kysymysryhma :as kysymysryhma-arkisto]
            aipal.compojure-util
            [aipal.infra.kayttaja :refer [*kayttaja* yllapitaja?]]
            [oph.common.util.http-util :refer [parse-iso-date response-or-404 file-download-response]]
            [oph.common.util.util :refer [paivita-arvot]]
            [cheshire.core :as cheshire]
            [aipal.rest-api.kysymysryhma :as kysymysryhma]
            [arvo.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]))

(defroutes reitit
  (GET "/" []
    :query-params [{voimassa :- Boolean false}]
    :kayttooikeus :katselu
    (response-or-404 (arkisto/hae-kyselypohjat (:aktiivinen-koulutustoimija *kayttaja*) voimassa)))

  (GET "/:kyselypohjaid" []
    :path-params [kyselypohjaid :- s/Int]
    :kayttooikeus [:katselu {:kyselypohjaid kyselypohjaid}]
    (let [kyselypohja (arkisto/hae-kyselypohja kyselypohjaid)
          kysymysryhmat (kysymysryhma-arkisto/hae-kyselypohjasta kyselypohjaid)]
      (when kyselypohja
        (response-or-404 (assoc kyselypohja :kysymysryhmat kysymysryhmat)))))

  (PUT "/:kyselypohjaid/julkaise" []
    :path-params [kyselypohjaid :- s/Int]
    :kayttooikeus [:kyselypohja {:kyselypohjaid kyselypohjaid}]
    (response-or-404 (arkisto/julkaise-kyselypohja! kyselypohjaid)))

  (PUT "/:kyselypohjaid/palauta" []
    :path-params [kyselypohjaid :- s/Int]
    :kayttooikeus [:kyselypohja {:kyselypohjaid kyselypohjaid}]
    (response-or-404 (arkisto/palauta-kyselypohja-luonnokseksi! kyselypohjaid)))

  (PUT "/:kyselypohjaid/sulje" []
    :path-params [kyselypohjaid :- s/Int]
    :kayttooikeus [:kyselypohja {:kyselypohjaid kyselypohjaid}]
    (response-or-404 (arkisto/sulje-kyselypohja! kyselypohjaid)))

  (PUT "/:kyselypohjaid" []
    :path-params [kyselypohjaid :- s/Int]
    :body [kyselypohja s/Any]
    :kayttooikeus [:kyselypohja {:kyselypohjaid kyselypohjaid}]
    (let [kyselypohja (paivita-arvot kyselypohja [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)
          valtakunnallinen (and (yllapitaja?) (true? (:valtakunnallinen kyselypohja)))]
      (response-or-404 (arkisto/tallenna-kyselypohja! kyselypohjaid (assoc kyselypohja :valtakunnallinen valtakunnallinen)))))

  (POST "/" []
    :body [kyselypohja s/Any]
    :kayttooikeus :kyselypohja
    (let [kyselypohja (paivita-arvot kyselypohja [:voimassa_alkupvm :voimassa_loppupvm] parse-iso-date)
          valtakunnallinen (and (yllapitaja?) (true? (:valtakunnallinen kyselypohja)))]
      (response-or-404
        (arkisto/luo-kyselypohja!
          (assoc kyselypohja
                 :koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*)
                 :valtakunnallinen valtakunnallinen)))))

  (GET "/:kyselypohjaid/kysymysryhmat" []
    :path-params [kyselypohjaid :- s/Int]
    :kayttooikeus [:katselu {:kyselypohjaid kyselypohjaid}]
    (response-or-404 (kysymysryhma-arkisto/hae-kyselypohjasta kyselypohjaid)))

  (DELETE "/:kyselypohjaid" []
    :path-params [kyselypohjaid :- s/Int]
    :kayttooikeus [:kyselypohja {:kyselypohjaid kyselypohjaid}]
    (arkisto/poista-kyselypohja! kyselypohjaid)
    {:status 204}))

(defn lisaa-kysymysryhma! [kysymysryhma]
   (kysymysryhma/lisaa-kysymysryhma! kysymysryhma (:kysymykset kysymysryhma)))

(defroutes tiedosto-reitit
  (GET "/:kyselypohjaid/lataa" []
       :path-params [kyselypohjaid :- s/Int]
       :kayttooikeus [:kyselypohja {:kyselypohjaid kyselypohjaid}]
       (let [id kyselypohjaid
             kyselypohja (arkisto/hae-kyselypohja id)
             kysymysryhmat (kysymysryhma-arkisto/hae-kyselypohjaan-kuuluvat id)
             filename (str "Kyselypohja_"(:kyselypohjaid kyselypohja)".json")
             data (-> kyselypohja
                      (assoc :kysymysryhmat kysymysryhmat)
                      (dissoc :luotuaika :muutettuaika :luotu_kayttaja :muutettu_kayttaja :voimassa_alkupvm :voimassa_loppupvm)
                      (cheshire/generate-string))]
         (file-download-response data filename "application/json" {:charset "UTF-8"})))

  (POST "/lisaa-tiedostosta" []
        :body [kyselypohja s/Any]
        :kayttooikeus :kyselypohja
    (let [tallennettu-pohja (arkisto/lisaa-kyselypohja! kyselypohja)
          kyselypohjaid (:kyselypohjaid tallennettu-pohja)
          kysymysryhmat (doall (map lisaa-kysymysryhma! (:kysymysryhmat kyselypohja)))
          kysymysryhmaidt (map :kysymysryhmaid kysymysryhmat)]
      (arkisto/tallenna-kyselypohjan-kysymysryhmat! kyselypohjaid kysymysryhmat)
      (doseq [kysymysryhmaid kysymysryhmaidt]
        (kysymysryhma-arkisto/julkaise! kysymysryhmaid))
      (response-or-404 (assoc tallennettu-pohja :kysymysryhmat kysymysryhmat)))))