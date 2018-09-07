;; Copyright (c) 2015 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.toimiala.raportti.taustakysymykset
  (:require [clj-time.core :as time]
            [oph.common.util.util :refer [some-value-with]]))

(def ^:private uusi-aipal-kaytossa (time/date-time 2015 1 1))

;(defn aseta-kysymyksen-jarjestys
;  [kysymys]
;  (let [id (:kysymysid kysymys)
;        taustakysymyksen-jarjestys (yhdistettyjen-taustakysymysten-jarjestys id)]
;    (cond
;      taustakysymyksen-jarjestys (assoc kysymys :jarjestys taustakysymyksen-jarjestys)
;      (time/before? (:luotuaika kysymys) uusi-aipal-kaytossa) kysymys
;      :else (update-in kysymys [:jarjestys] inc))))

;(defn lisaa-selite-taustakysymykseen
;  [kysymys]
;  (cond-> kysymys
;    (= (:kysymysid kysymys) taustakysymys-6a-id) (assoc :taustakysymyksen_selite_raportointiin "raportit.taustakysymyksen_6a_selite")
;    (= (:kysymysid kysymys) taustakysymys-6b-id) (assoc :taustakysymyksen_selite_raportointiin "raportit.taustakysymyksen_6b_selite")))

;(def valtakunnalliset-duplikaattikysymykset #{7312036 7312037 7312034 7312040 7312038 7312035})

;(def taustakysymysten-mappaus
;  "Mappaa hakeutumisvaiheen taustakysymysten ID:t niitä vastaaviin suoritusvaiheen taustakysymyksiin ja päinvastoin."
;  (into taustakysymysten-mappaus-yksisuuntainen
;     (map (comp vec reverse) taustakysymysten-mappaus-yksisuuntainen)))

;(defn mappaa-kysymysryhmaid
;  [id]
;  (if (or (= id suorittamisvaihe-id)
;          (= id hakeutumisvaihe-id))
;    [suorittamisvaihe-id hakeutumisvaihe-id]
;    [id]))

;(defn mappaa-kysymysid
;  [id]
;  (if-let [toinen-id (taustakysymysten-mappaus id)]
;    [id toinen-id]
;    [id]))

(defn yhdista-taustakysymysten-vastaukset
  [vastaus]
  vastaus)
  ;(update-in vastaus [:kysymysid] #(or (taustakysymysten-mappaus-yksisuuntainen %) %)))

(defn yhdista-taustakysymysten-kysymykset
  [kysymys]
  kysymys)
  ;(update-in kysymys [:kysymysryhmaid] (fn [id]
  ;                                       (if (= id hakeutumisvaihe-id)
  ;                                         suorittamisvaihe-id
  ;                                         id))))

(defn yhdista-valtakunnalliset-taustakysymysryhmat [kysymysryhmat]
  ;(let [hakeutumisvaihe (some-value-with :kysymysryhmaid hakeutumisvaihe-id kysymysryhmat)
  ;      suorittamisvaihe (some-value-with :kysymysryhmaid suorittamisvaihe-id kysymysryhmat)
  ;      taustakysymykset (assoc (or hakeutumisvaihe suorittamisvaihe)
  ;                              :kysymysryhmaid suorittamisvaihe-id
  ;                              :nimi_fi "Näyttötutkintojen taustakysymykset"
  ;                              :nimi_sv "Bakgrundsfrågor gällande fristående examina")
  ;      muut (remove (comp #{hakeutumisvaihe-id suorittamisvaihe-id} :kysymysryhmaid) kysymysryhmat)]
  ;  (if (or hakeutumisvaihe suorittamisvaihe)
  ;    (conj muut taustakysymykset)
      kysymysryhmat)

(defn ^:private poista-kysymys-kysymysryhmasta
  [kysymysryhma poistettava-kysymys]
  (update-in kysymysryhma [:kysymykset]
             (fn [kysymykset]
               (remove (fn [kysymys] (= (:kysymysid kysymys)
                                        poistettava-kysymys))
                       kysymykset))))

(defn ^:private poista-taustakysymys-raportista
  [valtakunnallinen-raportti poistettava-taustakysymys]
  valtakunnallinen-raportti)
  ;(update-in valtakunnallinen-raportti [:raportti]
  ;           (fn [kysymysryhmat]
  ;             (map (fn [kysymysryhma]
  ;                    (cond-> kysymysryhma (= (:kysymysryhmaid kysymysryhma)
  ;                                            suorittamisvaihe-id)
  ;                      (poista-kysymys-kysymysryhmasta poistettava-taustakysymys)))
  ;                  kysymysryhmat))))

;(defn ^:private poista-toinen-taustakysymys-raportista
;  [valtakunnallinen-raportti kyselyn-taustakysymys-6]
;  (let [toinen {taustakysymys-6a-id taustakysymys-6b-id
;                taustakysymys-6b-id taustakysymys-6a-id}]
;    (poista-taustakysymys-raportista valtakunnallinen-raportti (toinen kyselyn-taustakysymys-6))))
;
;(defn ^:private hae-raportista-taustakysymys-6
;  [raportti]
;  (let [kysymykset (mapcat :kysymykset (:raportti raportti))
;        kysymykset-idt (map :kysymysid kysymykset)]
;    (some #{taustakysymys-6a-id taustakysymys-6b-id}
;          kysymykset-idt)))

;(defn valitse-kyselyn-taustakysymykset
;  [valtakunnallinen-raportti kysely-raportti]
;  (let [taustakysymys-6 (hae-raportista-taustakysymys-6 kysely-raportti)]
;    (poista-toinen-taustakysymys-raportista valtakunnallinen-raportti taustakysymys-6)))
