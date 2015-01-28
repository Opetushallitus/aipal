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
  (:require [oph.common.util.util :refer [some-value-with]]))

(def taustakysymysten-mappaus-yksisuuntainen {7312036 7312029 ; Ikä
                                              7312037 7312030 ; Korkein tutkinto
                                              7312039 7312039 ; Aiempi tilanteesi
                                              7312032 7312032 ; Tuleva tilanteesi
                                              7312034 7312027 ; Sukupuoli
                                              7312040 7312033 ; Tavoitteeni
                                              7312038 7312031 ; Tärkein syy
                                              7312035 7312028 ; Äidinkieli
                                              })

(def valtakunnalliset-duplikaattikysymykset #{7312036 7312037 7312034 7312040 7312038 7312035})

(def taustakysymysten-mappaus
  (into taustakysymysten-mappaus-yksisuuntainen
     (map (comp vec reverse) taustakysymysten-mappaus-yksisuuntainen)))

(defn mappaa-id
  [id]
  [id (taustakysymysten-mappaus id)])

(defn yhdista-taustakysymysten-vastaukset
  [vastaus]
  (update-in vastaus [:kysymysid] #(or (taustakysymysten-mappaus-yksisuuntainen %) %)))

(defn yhdista-taustakysymysten-kysymykset
  [kysymys]
  (update-in kysymys [:kysymysryhmaid] (fn [id]
                                         (if (= id 3341884)
                                           3341885
                                           id))))

(defn yhdista-valtakunnalliset-taustakysymysryhmat [kysymysryhmat]
  (let [hakeutumisvaihe (some-value-with :kysymysryhmaid 3341884 kysymysryhmat)
        suorittamisvaihe (some-value-with :kysymysryhmaid 3341885 kysymysryhmat)
        taustakysymykset (assoc suorittamisvaihe
                                :nimi_fi "Näyttötutkintojen taustakysymykset"
                                :nimi_sv "Bakgrundsfrågor gällande fristående examina")
        muut (remove (comp #{3341884 3341885} :kysymysryhmaid) kysymysryhmat)]
    (conj muut taustakysymykset)))
