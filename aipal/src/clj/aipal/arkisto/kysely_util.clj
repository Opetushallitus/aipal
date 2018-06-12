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

(ns aipal.arkisto.kysely-util
  (:require [korma.core :as sql]
            [aipal.infra.kayttaja :refer [ntm-vastuukayttaja? yllapitaja?]]))

(defn kysely-sisaltaa-ntm-kysymysryhman [kyselyid]
  (sql/sqlfn :exists
             (sql/subselect :kysely_kysymysryhma
               (sql/join :inner :kysymysryhma {:kysymysryhma.kysymysryhmaid :kysely_kysymysryhma.kysymysryhmaid})
               (sql/where (and {:kysely_kysymysryhma.kyselyid kyselyid}
                               {:kysymysryhma.ntm_kysymykset true})))))

(defn kysely-on-tyhja [kyselyid]
  (sql/sqlfn :not (sql/sqlfn :exists (sql/subselect :kysely_kysymysryhma
                                                    (sql/join :inner :kysymysryhma {:kysymysryhma.kysymysryhmaid :kysely_kysymysryhma.kysymysryhmaid})
                                                    (sql/where {:kysely_kysymysryhma.kyselyid kyselyid})))))


(defn rajaa-kayttajalle-sallittuihin-kyselyihin [query kyselyid koulutustoimija]
  (let [koulutustoimijan-oma {:kysely_organisaatio_view.koulutustoimija koulutustoimija}
        ntm-kysely           (kysely-sisaltaa-ntm-kysymysryhman kyselyid)
        tyhja-kysely         (kysely-on-tyhja kyselyid)]
    (cond
      (yllapitaja?)         (-> query
                              (sql/where koulutustoimijan-oma))
      (ntm-vastuukayttaja?) (-> query
                              (sql/where (and koulutustoimijan-oma
                                              (or tyhja-kysely
                                               ntm-kysely))))
      :else                 (-> query
                              (sql/where (and koulutustoimijan-oma
                                              (not ntm-kysely)))))))

(defn rajaa-kayttajalle-sallittuihin-kyselyihin-sql []
  (let [koulutustoimijan-oma "(kysely_organisaatio_view.koulutustoimija = ?)"
        ntm-kysely           "(EXISTS (SELECT 1 FROM kysely_kysymysryhma JOIN kysymysryhma ON kysymysryhma.kysymysryhmaid = kysely_kysymysryhma.kysymysryhmaid WHERE kysely_kysymysryhma.kyselyid = kysely.kyselyid AND kysymysryhma.ntm_kysymykset))"
        tyhja-kysely         "(NOT EXISTS (SELECT 1 FROM kysely_kysymysryhma JOIN kysymysryhma ON kysymysryhma.kysymysryhmaid = kysely_kysymysryhma.kysymysryhmaid WHERE kysely_kysymysryhma.kyselyid = kysely.kyselyid))"]
    (cond
      (yllapitaja?)         koulutustoimijan-oma
      (ntm-vastuukayttaja?) (str "(" koulutustoimijan-oma " AND (" tyhja-kysely " OR " ntm-kysely "))")
      :else                 (str "(" koulutustoimijan-oma " AND NOT " ntm-kysely ")"))))
