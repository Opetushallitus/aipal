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

(ns aipal.arkisto.kyselypohja
  (:import java.sql.Date)
  (:require [korma.core :as sql]
            [aipal.integraatio.sql.korma :as taulut]))

(defn hae-kyselypohjat
  ([organisaatio vain-voimassaolevat]
    (-> (sql/select* :kyselypohja)
      (sql/join :inner :kyselypohja_organisaatio_view {:kyselypohja_organisaatio_view.kyselypohjaid :kyselypohja.kyselypohjaid})
      (sql/fields :kyselypohja.kyselypohjaid :kyselypohja.nimi_fi :kyselypohja.nimi_sv :kyselypohja.valtakunnallinen :kyselypohja.tila
                  [:kyselypohja.kaytettavissa :voimassa])
      (sql/where (or {:kyselypohja_organisaatio_view.koulutustoimija organisaatio}
                     {:kyselypohja_organisaatio_view.valtakunnallinen true}))
      (cond->
        vain-voimassaolevat (sql/where {:kyselypohja.kaytettavissa true}))
      (sql/order :muutettuaika :desc)
      sql/exec))
  ([organisaatio]
    (hae-kyselypohjat organisaatio false)))

(defn hae-kyselypohja
  [kyselypohjaid]
  (first
    (sql/select taulut/kyselypohja
      (sql/where {:kyselypohjaid kyselypohjaid}))))

(def muokattavat-kentat [:nimi_fi :nimi_sv :selite_fi :selite_sv :voimassa_alkupvm :voimassa_loppupvm :valtakunnallinen])

(defn tallenna-kyselypohjan-kysymysryhmat
  [kyselypohjaid kysymysryhmat]
  (sql/delete :kysymysryhma_kyselypohja
              (sql/where {:kyselypohjaid kyselypohjaid}))
  (doseq [[index kysymysryhma] (map-indexed vector kysymysryhmat)]
    (sql/insert :kysymysryhma_kyselypohja
      (sql/values {:kyselypohjaid kyselypohjaid
                   :kysymysryhmaid (:kysymysryhmaid kysymysryhma)
                   :jarjestys index}))))

(defn tallenna-kyselypohja
  [kyselypohjaid kyselypohja]
  (tallenna-kyselypohjan-kysymysryhmat kyselypohjaid (:kysymysryhmat kyselypohja))
  (sql/update taulut/kyselypohja
    (sql/where {:kyselypohjaid kyselypohjaid})
    (sql/set-fields (select-keys kyselypohja muokattavat-kentat))))

(defn luo-kyselypohja
  [kyselypohja]
  (let [luotu-kyselypohja (sql/insert taulut/kyselypohja
                      (sql/values (select-keys kyselypohja (conj muokattavat-kentat :koulutustoimija))))]
    (tallenna-kyselypohjan-kysymysryhmat (:kyselypohjaid luotu-kyselypohja) (:kysymysryhmat kyselypohja))
    luotu-kyselypohja))

(defn julkaise-kyselypohja
  [kyselypohjaid]
  (sql/update taulut/kyselypohja
    (sql/where {:kyselypohjaid kyselypohjaid})
    (sql/set-fields {:tila "julkaistu"})))

(defn hae-organisaatiotieto
  [kyselypohjaid]
  (first
    (sql/select :kyselypohja_organisaatio_view
      (sql/fields :koulutustoimija :valtakunnallinen)
      (sql/where {:kyselypohjaid kyselypohjaid}))))
