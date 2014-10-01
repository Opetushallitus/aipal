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

(ns aipal.arkisto.kysymysryhma
  (:require [korma.core :as sql]
            [aipal.integraatio.sql.korma :refer [kysymysryhma kysymys]]))

(defn hae-kysymysryhmat [organisaatio]
  (let [organisaatiosuodatus (fn [query]
                               (-> query
                                 (sql/join :inner :kysymysryhma_organisaatio_view (= :kysymysryhma_organisaatio_view.kysymysryhmaid :kysymysryhmaid))
                                 (sql/where (or {:kysymysryhma_organisaatio_view.koulutustoimija organisaatio}
                                                {:kysymysryhma_organisaatio_view.valtakunnallinen true}))))]
    (sql/select kysymysryhma
      (organisaatiosuodatus)
      (sql/fields :kysymysryhma.kysymysryhmaid :kysymysryhma.nimi_fi :kysymysryhma.nimi_sv :kysymysryhma.selite_fi :kysymysryhma.selite_sv :kysymysryhma.valtakunnallinen)
      (sql/order :muutettuaika :desc))))

(defn lisaa-kysymysryhma! [k]
  (sql/insert kysymysryhma
    (sql/values k)))

(defn lisaa-kysymys! [k]
  (sql/insert kysymys
    (sql/values k)))

(defn lisaa-jatkokysymys! [k]
  (sql/insert :jatkokysymys
    (sql/values k)))

(defn lisaa-monivalintavaihtoehto! [v]
  (sql/insert :monivalintavaihtoehto
    (sql/values v)))
