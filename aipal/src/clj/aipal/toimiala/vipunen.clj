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
 
(def TK_sukupuoli (s/enum "nainen" "mies"))
(def TK_kieli (s/enum "suomi" "ruotsi" "muu"))
(def TK_ika (s/enum "19 tai alle" "20 - 24" "25 - 34" "35 - 44" "45 - 54" "55 tai yli"))

(def TK_tutkinto (s/enum "Peruskoulu, keskikoulu tai vastaava"
                   "Lukio/ylioppilastutkinto"
                   "Ammatillinen tutkinto tai opistoasteen tutkinto"
                   "Ammattikorkeakoulututkinto"
                   "Yliopistotutkinto"
                   "Ulkomailla suoritettu tutkinto"
                   "Ei tutkintoa"))

(def TK_syy (s/enum "Ammatin hankkiminen"
              "Ammatin vaihtaminen"
              "Ammattitaidon kehittäminen"
              "Muu peruste"
              ))

(def TK_asema (s/enum "Työssä toisen palveluksessa"
                "Yrittäjä tai itsenäinen ammatinharjoittaja"
                "Työtön"
                "Opiskelija"
                "Muu tilanne (esimerkiksi varusmies-/siviilipalveluksessa,vanhempainvapaalla, kuntoutuksessa tai muu syy)"))

(def TK_tavoite (s/enum "koko tutkinnon suorittaminen"
                  "tutkinnon osan tai osien suorittaminen"
                  ))


(def TK_vastaustyyppi (s/enum
                        "likert_asteikko"
                        "asteikko"
                        "monivalinta"
                        "kylla_ei_valinta"
                        "arvosana"))

(s/defschema VastauksenTiedot {:vastausid s/Int
                               :monivalintavaihtoehto (s/maybe s/Str)
                               :kysely_sv (s/maybe s/Str)
                               :taustakysymys_aiempi_tilanne (s/maybe TK_asema)
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
                               :taustakysymys_tavoite (s/maybe TK_tavoite)
                               :valmistavan_koulutuksen_jarjestaja (s/maybe s/Str)
                               :vastaajaid s/Int
                               :opintoala_fi (s/maybe s/Str)
                               :taustakysymys_tuleva_tilanne (s/maybe s/Str) ; TODO: enum
                               :vastausaika org.joda.time.LocalDate
                               :koulutustoimija_fi s/Str
                               :rahoitusmuoto s/Str
                               :valmistavan_koulutuksen_oppilaitos (s/maybe s/Str)
                               :valmistavan_koulutuksen_jarjestaja_sv (s/maybe s/Str)
                               :kysymysid s/Int
                               :kyselykertaid s/Int
                               :koulutustoimija s/Str
                               :taustakysymys_tutkinto TK_tutkinto
                               :vastaustyyppi TK_vastaustyyppi
                               :tutkinto_sv (s/maybe s/Str)
                               :tutkintotunnus (s/maybe s/Str)
                               :kyselykerta s/Str
                               :opintoalatunnus (s/maybe s/Str)
                               :valmistavan_koulutuksen_jarjestaja_fi (s/maybe s/Str)
                               :numerovalinta (s/maybe s/Int)
                               :taustakysymys_aidinkieli TK_kieli
                               :kysymys_sv s/Str
                               :valtakunnallinen s/Bool
                               :kysymys_fi s/Str
                               :kyselyid s/Int
                               :taustakysymys_syy TK_syy
                               })
