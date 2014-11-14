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
  (let [tulos (first (sql/select :vastaajatunnus
                       (sql/fields :vastaajien_lkm [(sql/subselect :vastaaja
                                                      (sql/aggregate (count :*) :vastaajia)
                                                      (sql/where {:vastaajatunnusid :vastaajatunnus.vastaajatunnusid})) :vastaajia])
                       (vastaajatunnus-where vastaajatunnus)))]
    (> (:vastaajien_lkm tulos) (:vastaajia tulos))))

(defn validoi-vastaajatunnus
  [vastaajatunnus]
  (and (vastaajatunnus-voimassa? vastaajatunnus) (vastaajatunnuksella-vastauskertoja? vastaajatunnus)))

(defn luo-vastaaja!
  [tunnus]
  ;; Vastaajatunnuksen voimassaolo tarkistetaan vastaajan luonnissa jotta tunnusten lukitseminen estää
  ;; uusien vastausten tallentamisen vaikka sivun olisi aiemmin jättänyt auki selaimeen
  (when (vastaajatunnus-voimassa? tunnus)
    (let [vastaajatunnus (first (sql/select :vastaajatunnus
                                  (vastaajatunnus-where tunnus)))]
      (sql/insert :vastaaja
        (sql/values {:kyselykertaid (:kyselykertaid vastaajatunnus)
                     :vastaajatunnusid (:vastaajatunnusid vastaajatunnus)})))))

(defn paivata-vastaaja! [vastaajaid]
  (->
    (sql/update* :vastaaja)
    (sql/set-fields {:vastannut true})
    (sql/where {:vastaajaid vastaajaid})
    sql/exec))

