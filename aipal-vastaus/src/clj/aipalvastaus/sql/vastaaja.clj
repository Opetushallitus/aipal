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

(ns aipalvastaus.sql.vastaaja
  (:require [korma.core :as sql]
            [aipalvastaus.sql.kyselykerta :refer [hae-kyselyn-tiedot]]
            [aipalvastaus.sql.korma :refer [vastaajatunnus-where]]))

(defn vastaajatunnus-voimassa?
  [vastaajatunnus]
  (->
    (sql/select :vastaajatunnus
      (sql/fields :kaytettavissa)
      (vastaajatunnus-where vastaajatunnus))
    first
    :kaytettavissa))

(defn vastaajatunnuksella-vastauskertoja?
  [vastaajatunnus]
  (let [kyselytyyppi (:tyyppi (hae-kyselyn-tiedot vastaajatunnus))
        tulos (first (sql/select :vastaajatunnus
                       (sql/fields :vastaajien_lkm [(sql/subselect :vastaaja
                                                      (sql/aggregate (count :*) :vastaajia)
                                                      (sql/where {:vastaajatunnusid :vastaajatunnus.vastaajatunnusid})) :vastaajia])
                       (vastaajatunnus-where vastaajatunnus)))]
    (or (= kyselytyyppi 4) (> (:vastaajien_lkm tulos) (:vastaajia tulos)))))

(defn validoi-vastaajatunnus
  [vastaajatunnus]
  (and (vastaajatunnus-voimassa? vastaajatunnus) (vastaajatunnuksella-vastauskertoja? vastaajatunnus)))

(defn luo-vastaaja!
  [tunnus]
  ;; Vastaajatunnuksen voimassaolo tarkistetaan vastaajan luonnissa jotta tunnusten lukitseminen estää
  ;; uusien vastausten tallentamisen vaikka sivun olisi aiemmin jättänyt auki selaimeen
  (when (validoi-vastaajatunnus tunnus)
    (let [vastaajatunnus (first (sql/select :vastaajatunnus
                                  (vastaajatunnus-where tunnus)))]
      (sql/insert :vastaaja
        (sql/values {:kyselykertaid (:kyselykertaid vastaajatunnus)
                     :vastaajatunnusid (:vastaajatunnusid vastaajatunnus)})))))

(defn hae-vastaaja [vastaajatunnus]
  (first (sql/select :vastaaja
           (sql/join :inner :vastaajatunnus (= :vastaajatunnus.vastaajatunnusid :vastaaja.vastaajatunnusid))
           (sql/fields :vastaaja.vastaajaid :vastaaja.kyselykertaid :vastaaja.vastaajatunnusid)
           (vastaajatunnus-where vastaajatunnus))))

(defn luo-tai-hae-vastaaja! [vastaajatunnus]
  (let [kyselytyyppi (:tyyppi (hae-kyselyn-tiedot vastaajatunnus))
        vastaaja (hae-vastaaja vastaajatunnus)]
    (println "Tunnus:" vastaajatunnus  "Kyselytyyppi: " kyselytyyppi "Vastaaja:" vastaaja "Käytetään vanhaa vastaajaa:" (and (= kyselytyyppi 4) (some? vastaaja)))
    (if (and (= kyselytyyppi 4) (some? vastaaja))
      vastaaja
      (luo-vastaaja! vastaajatunnus))))

(defn paivata-vastaaja! [vastaajaid]
  (->
    (sql/update* :vastaaja)
    (sql/set-fields {:vastannut true})
    (sql/where {:vastaajaid vastaajaid})
    sql/exec))

