;; Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
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

(ns aipal.toimiala.vipunen
  (:require [schema.core :as s]))

(def TK_sukupuoli (s/enum "Nainen" "Mies" "En halua vastata"))
(def TK_ika (s/enum "Alle 25 vuotta" "25-34 vuotta" "35-45 vuotta" "Yli 45 vuotta"))

(def TK_tutkinto (s/enum "Ylioppilastutkinto"
                   "Ammatillinen perustutkinto/ammattitutkinto"
                   "Ylioppilastutkinto sekä ammatillinen perustutkinto/ammattitutkinto"
                   "Opistoasteen tai ammatillisen korkea-asteen tutkinto"
                   "Korkeakoulututkinto"
                   "Ulkomailla suoritettu tutkinto"
                   "Ei peruskoulun jälkeistä tutkintoa"
                   "Ammattikorkeakoulututkinto"
                   "Alempi korkeakoulututkinto"
                   "Ylempi korkeakoulututkinto"
                   "Ulkomailla suoritettu tutkinto"
                   "Muu"))

(def TK_vastaustyyppi (s/enum
                        "likert_asteikko"
                        "asteikko"
                        "monivalinta"
                        "kylla_ei_valinta"
                        "arvosana"
                        "arvosana7"
                        "arvosana4_ja_eos"
                        "arvosana6_ja_eos"))

(s/defschema VastauksenTiedot {:vastausid s/Int
                               :monivalintavaihtoehto (s/maybe s/Str)
                               :kysely_sv (s/maybe s/Str)
                               :kysymysryhma_sv (s/maybe s/Str)
                               :opintoala_sv (s/maybe s/Str)
                               :vaihtoehto (s/maybe s/Int)
                               :suorituskieli s/Str
                               :valmistavan_koulutuksen_oppilaitos_fi (s/maybe s/Str)
                               :valmistavan_koulutuksen_oppilaitos_sv (s/maybe s/Str)
                               :kysely_fi s/Str
                               :taustakysymys_ika TK_ika
                               :kysymysryhma_fi s/Str
                               :tutkinto_fi (s/maybe s/Str)
                               :kysymysryhmaid s/Int
                               :koulutustoimija_sv (s/maybe s/Str)
                               :taustakysymys_sukupuoli TK_sukupuoli
                               :valmistavan_koulutuksen_jarjestaja (s/maybe s/Str)
                               :vastaajaid s/Int
                               :opintoala_fi (s/maybe s/Str)
                               :vastausaika org.joda.time.LocalDate
                               :koulutustoimija_fi s/Str
                               :rahoitusmuoto s/Str
                               :valmistavan_koulutuksen_oppilaitos (s/maybe s/Str)
                               :valmistavan_koulutuksen_jarjestaja_sv (s/maybe s/Str)
                               :kysymysid s/Int
                               :kyselykertaid s/Int
                               :koulutustoimija s/Str
                               :taustakysymys_pohjakoulutus TK_tutkinto
                               :vastaustyyppi TK_vastaustyyppi
                               :tutkinto_sv (s/maybe s/Str)
                               :tutkintotunnus (s/maybe s/Str)
                               :kyselykerta s/Str
                               :opintoalatunnus (s/maybe s/Str)
                               :valmistavan_koulutuksen_jarjestaja_fi (s/maybe s/Str)
                               :numerovalinta (s/maybe s/Int)
                               :kysymys_sv s/Str
                               :valtakunnallinen s/Bool
                               :kysymys_fi s/Str
                               :kyselyid s/Int
                               :kysely_en (s/maybe s/Str)
                               :kysymys_en (s/maybe s/Str)
                               :kysymysryhma_en (s/maybe s/Str)
                               :opintoala_en (s/maybe s/Str)
                               :valmistavan_koulutuksen_oppilaitos_en (s/maybe s/Str)
                               :koulutustoimija_en (s/maybe s/Str)
                               :valmistavan_koulutuksen_jarjestaja_en (s/maybe s/Str)
                               :tutkinto_en (s/maybe s/Str)
                               :tunnus (s/maybe s/Str)
                               :kunta (s/maybe s/Str)
                               :koulutusmuoto (s/maybe s/Str)
                               })
