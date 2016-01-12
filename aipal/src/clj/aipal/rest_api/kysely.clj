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
  (:require [compojure.core :as c]
            [compojure.coercions :refer [as-int]]
            [aipal.compojure-util :as cu]
            [aipal.arkisto.kysely :as arkisto]
            [aipal.arkisto.kyselykerta :as kyselykerta-arkisto]
            [aipal.arkisto.kysymysryhma :as kysymysryhma-arkisto]
            [aipal.toimiala.kayttajaoikeudet :refer [kysymysryhma-luku? kysymysryhma-on-julkaistu?]]
            [aipal.rest-api.kysymysryhma :refer [lisaa-jarjestys]]
            [oph.common.util.http-util :refer [json-response parse-iso-date]]
            [oph.common.util.util :refer [map-by paivita-arvot]]
            [aipal.infra.kayttaja :refer [*kayttaja* yllapitaja?]]))

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

(defn valtakunnallisia-ryhmia?
  [kysymysryhmat]
  (some :valtakunnallinen kysymysryhmat))

(defn paivita-kysely!
  [kysely]
  (let [valtakunnallisia (valtakunnallisia-ryhmia? (:kysymysryhmat kysely))
        max-kysymyksia (if valtakunnallisia 10 30)]
    (assert (not (> (lisakysymysten-lukumaara (:kysymysryhmat kysely)) max-kysymyksia))))
  (arkisto/poista-kysymysryhmat! (:kyselyid kysely))
  (arkisto/poista-kysymykset! (:kyselyid kysely))
  (doseq [kysymysryhma (lisaa-jarjestys (:kysymysryhmat kysely))]
    (assert (kysymysryhma-luku? (:kysymysryhmaid kysymysryhma)))
    (assert (kysymysryhma-on-julkaistu? (:kysymysryhmaid kysymysryhma)))
    (lisaa-kysymysryhma! (:kyselyid kysely) kysymysryhma))
  (arkisto/muokkaa-kyselya! kysely))

(c/defroutes reitit
  (cu/defapi :kysely nil :get "/" []
    (json-response (arkisto/hae-kaikki (:aktiivinen-koulutustoimija *kayttaja*))))

  (cu/defapi :kysely-luonti nil :post "/" [& kysely]
    (let [kysely (assoc (paivita-arvot kysely
                                      [:voimassa_alkupvm :voimassa_loppupvm]
                                      parse-iso-date)
                        :koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*))]
      (if (arkisto/samanniminen-kysely? kysely)
        {:status 400
         :body "kysely.samanniminen_kysely"}
        (json-response
          (let [{:keys [kyselyid]}
                (arkisto/lisaa! (select-keys kysely [:nimi_fi :nimi_sv :selite_fi :selite_sv :voimassa_alkupvm :voimassa_loppupvm :tila :koulutustoimija]))]
            (paivita-kysely! (assoc kysely :kyselyid kyselyid)))))))

  (cu/defapi :kysely-muokkaus kyselyid :post "/:kyselyid" [kyselyid :<< as-int & kysely]
    (let [kysely (paivita-arvot (assoc kysely :kyselyid kyselyid)
                               [:voimassa_alkupvm :voimassa_loppupvm]
                               parse-iso-date)]
      (if (arkisto/samanniminen-kysely? (assoc kysely :koulutustoimija (:aktiivinen-koulutustoimija *kayttaja*)))
        {:status 400
         :body "kysely.samanniminen_kysely"}
        (json-response (paivita-kysely! kysely)))))

  (cu/defapi :kysely-muokkaus kyselyid :delete "/:kyselyid" [kyselyid :<< as-int]
    (let [kyselyid kyselyid]
      (if (= (:tila (arkisto/hae kyselyid)) "luonnos")
        (arkisto/poista-kysely! kyselyid)
        {:status 403})))

  (cu/defapi :kysely-luku kyselyid :get "/:kyselyid/vastaustunnustiedot" [kyselyid :<< as-int]
    (json-response (kyselykerta-arkisto/hae-vastaustunnustiedot-kyselylta kyselyid)))

  (cu/defapi :kysely-luku kyselyid :get "/:kyselyid" [kyselyid :<< as-int]
    (json-response (when-let [kysely (arkisto/hae kyselyid)]
                     (assoc kysely :kysymysryhmat (kysymysryhma-arkisto/hae-kyselysta kyselyid)))))

  (cu/defapi :kysely-tilamuutos kyselyid :put "/julkaise/:kyselyid" [kyselyid :<< as-int]
    (let [kyselyid kyselyid]
      (if (> (arkisto/laske-kysymysryhmat kyselyid) 0)
        (json-response (arkisto/julkaise-kysely! kyselyid))
        {:status 403})))

  (cu/defapi :kysely-tilamuutos kyselyid :put "/sulje/:kyselyid" [kyselyid :<< as-int]
    (json-response (arkisto/sulje-kysely! kyselyid)))

  (cu/defapi :kysely-tilamuutos kyselyid :put "/palauta/:kyselyid" [kyselyid :<< as-int]
    (json-response (arkisto/julkaise-kysely! kyselyid)))

  (cu/defapi :kysely-tilamuutos kyselyid :put "/palauta-luonnokseksi/:kyselyid" [kyselyid :<< as-int]
    (let [kyselyid kyselyid]
      (if (= (arkisto/laske-kyselykerrat kyselyid) 0)
        (json-response (arkisto/palauta-luonnokseksi! kyselyid))
        {:status 403}))))
