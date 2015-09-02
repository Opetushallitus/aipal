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
            [oph.korma.common :refer [select-unique select-unique-or-nil]]
            [aipal.infra.kayttaja :refer [ntm-vastuukayttaja? yllapitaja?]]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.auditlog :as auditlog]))

(defn ^:private kyselypohja-sisaltaa-ntm-kysymysryhman [kyselypohjaid]
  (sql/sqlfn :exists
             (sql/subselect :kysymysryhma_kyselypohja
               (sql/join :inner :kysymysryhma {:kysymysryhma.kysymysryhmaid :kysymysryhma_kyselypohja.kysymysryhmaid})
               (sql/where (and {:kysymysryhma_kyselypohja.kyselypohjaid kyselypohjaid}
                               {:kysymysryhma.ntm_kysymykset true})))))

(defn ^:private rajaa-kayttajalle-sallittuihin-kyselypohjiin
  [query kyselypohjaid koulutustoimija]
  (let [koulutustoimijan-oma {:kyselypohja_organisaatio_view.koulutustoimija koulutustoimija}
        valtakunnallinen     {:kyselypohja_organisaatio_view.valtakunnallinen true}
        julkaistu            {:kyselypohja.tila "julkaistu"}
        ntm-kyselypohja      (kyselypohja-sisaltaa-ntm-kysymysryhman kyselypohjaid)]
    (cond
      (yllapitaja?)         (-> query
                              (sql/where (or koulutustoimijan-oma
                                             valtakunnallinen)))
      (ntm-vastuukayttaja?) (-> query
                              (sql/where (and (or koulutustoimijan-oma
                                                  (and valtakunnallinen
                                                       julkaistu))
                                              ntm-kyselypohja)))
      :else                 (-> query
                              (sql/where (and (or koulutustoimijan-oma
                                                  (and valtakunnallinen
                                                       julkaistu))
                                              (not ntm-kyselypohja)))))))

(defn hae-kyselypohjat
  ([organisaatio vain-voimassaolevat]
    (-> (sql/select* :kyselypohja)
      (sql/join :inner :kyselypohja_organisaatio_view {:kyselypohja_organisaatio_view.kyselypohjaid :kyselypohja.kyselypohjaid})
      (sql/fields :kyselypohja.kyselypohjaid :kyselypohja.nimi_fi :kyselypohja.nimi_sv :kyselypohja.valtakunnallinen :kyselypohja.tila
                  [:kyselypohja.kaytettavissa :voimassa])
      (rajaa-kayttajalle-sallittuihin-kyselypohjiin :kyselypohja.kyselypohjaid organisaatio)
      (cond->
        vain-voimassaolevat (sql/where {:kyselypohja.kaytettavissa true}))
      (sql/order :muutettuaika :desc)
      sql/exec))
  ([organisaatio]
    (hae-kyselypohjat organisaatio false)))

(defn hae-kyselypohja
  [kyselypohjaid]
  (select-unique-or-nil taulut/kyselypohja
    (sql/where {:kyselypohjaid kyselypohjaid})))

(def muokattavat-kentat [:nimi_fi :nimi_sv :selite_fi :selite_sv :voimassa_alkupvm :voimassa_loppupvm :valtakunnallinen])

(defn tallenna-kyselypohjan-kysymysryhmat!
  [kyselypohjaid kysymysryhmat]
  (auditlog/kyselypohja-muokkaus! kyselypohjaid)
  (sql/delete :kysymysryhma_kyselypohja
              (sql/where {:kyselypohjaid kyselypohjaid}))
  (doseq [[index kysymysryhma] (map-indexed vector kysymysryhmat)]
    (sql/insert :kysymysryhma_kyselypohja
      (sql/values {:kyselypohjaid kyselypohjaid
                   :kysymysryhmaid (:kysymysryhmaid kysymysryhma)
                   :jarjestys index}))))

(defn tallenna-kyselypohja!
  [kyselypohjaid kyselypohja]
  (auditlog/kyselypohja-muokkaus! kyselypohjaid)
  (tallenna-kyselypohjan-kysymysryhmat! kyselypohjaid (:kysymysryhmat kyselypohja))
  (sql/update taulut/kyselypohja
    (sql/where {:kyselypohjaid kyselypohjaid})
    (sql/set-fields (select-keys kyselypohja muokattavat-kentat))))

(defn luo-kyselypohja!
  [kyselypohja]
  (let [luotu-kyselypohja (sql/insert taulut/kyselypohja
                      (sql/values (select-keys kyselypohja (conj muokattavat-kentat :koulutustoimija))))]
    (auditlog/kyselypohja-luonti! (:nimi_fi kyselypohja))
    (tallenna-kyselypohjan-kysymysryhmat! (:kyselypohjaid luotu-kyselypohja) (:kysymysryhmat kyselypohja))
    luotu-kyselypohja))

(defn ^:private aseta-kyselypohjan-tila!
  [kyselypohjaid tila]
  (sql/update taulut/kyselypohja
    (sql/where {:kyselypohjaid kyselypohjaid})
    (sql/set-fields {:tila tila})))

(defn julkaise-kyselypohja! [kyselypohjaid]
  (auditlog/kyselypohja-muokkaus! kyselypohjaid :julkaistu)
  (aseta-kyselypohjan-tila! kyselypohjaid "julkaistu"))

(defn palauta-kyselypohja-luonnokseksi! [kyselypohjaid]
  (auditlog/kyselypohja-muokkaus! kyselypohjaid :luonnos)
  (aseta-kyselypohjan-tila! kyselypohjaid "luonnos"))

(defn sulje-kyselypohja! [kyselypohjaid]
  (auditlog/kyselypohja-muokkaus! kyselypohjaid :suljettu)
  (aseta-kyselypohjan-tila! kyselypohjaid "suljettu"))

(defn poista-kyselypohja! [kyselypohjaid]
  (auditlog/kyselypohja-poisto! kyselypohjaid)
  (sql/delete taulut/kysymysryhma_kyselypohja
    (sql/where {:kyselypohjaid kyselypohjaid}))
  (sql/delete taulut/kyselypohja
    (sql/where {:kyselypohjaid kyselypohjaid})))

(defn hae-organisaatiotieto
  [kyselypohjaid]
  (select-unique :kyselypohja_organisaatio_view
    (sql/fields :koulutustoimija :valtakunnallinen)
    (sql/where {:kyselypohjaid kyselypohjaid})))

(defn ntm-kyselypohja? [kyselypohjaid]
  "Onko NTM-kyselypohja?"
  (boolean
   (seq (sql/select taulut/kyselypohja
          (sql/where (and {:kyselypohjaid kyselypohjaid}
                          (kyselypohja-sisaltaa-ntm-kysymysryhman :kyselypohja.kyselypohjaid)))))))
