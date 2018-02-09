DROP TRIGGER jatkokysymys_cu_insert ON jatkokysymys;
DROP TRIGGER jatkokysymys_mu_insert ON jatkokysymys;
DROP TRIGGER jatkokysymys_mu_update ON jatkokysymys;
ALTER TABLE jatkokysymys DROP COLUMN luotu_kayttaja;
ALTER TABLE jatkokysymys DROP COLUMN muutettu_kayttaja;

DROP TRIGGER jatkovastaus_cu_insert ON jatkovastaus;
DROP TRIGGER jatkovastaus_mu_insert ON jatkovastaus;
DROP TRIGGER jatkovastaus_mu_update ON jatkovastaus;
ALTER TABLE jatkovastaus DROP COLUMN luotu_kayttaja;
ALTER TABLE jatkovastaus DROP COLUMN muutettu_kayttaja;

DROP TRIGGER kayttaja_cu_insert ON kayttaja;
DROP TRIGGER kayttaja_mu_insert ON kayttaja;
DROP TRIGGER kayttaja_mu_update ON kayttaja;
ALTER TABLE kayttaja DROP COLUMN luotu_kayttaja;
ALTER TABLE kayttaja DROP COLUMN muutettu_kayttaja;

DROP TRIGGER koulutusala_cu_insert ON koulutusala;
DROP TRIGGER koulutusala_mu_insert ON koulutusala;
DROP TRIGGER koulutusala_mu_update ON koulutusala;
ALTER TABLE koulutusala DROP COLUMN luotu_kayttaja;
ALTER TABLE koulutusala DROP COLUMN muutettu_kayttaja;

DROP TRIGGER koulutustoimija_cu_insert ON koulutustoimija;
DROP TRIGGER koulutustoimija_mu_insert ON koulutustoimija;
DROP TRIGGER koulutustoimija_mu_update ON koulutustoimija;
ALTER TABLE koulutustoimija DROP COLUMN luotu_kayttaja;
ALTER TABLE koulutustoimija DROP COLUMN muutettu_kayttaja;

DROP TRIGGER koulutustoimija_ja_tutkinto_cu_insert ON koulutustoimija_ja_tutkinto;
DROP TRIGGER koulutustoimija_ja_tutkinto_mu_insert ON koulutustoimija_ja_tutkinto;
DROP TRIGGER koulutustoimija_ja_tutkinto_mu_update ON koulutustoimija_ja_tutkinto;
ALTER TABLE koulutustoimija_ja_tutkinto DROP COLUMN luotu_kayttaja;
ALTER TABLE koulutustoimija_ja_tutkinto DROP COLUMN muutettu_kayttaja;

DROP TRIGGER kysely_cu_insert ON kysely;
DROP TRIGGER kysely_mu_insert ON kysely;
DROP TRIGGER kysely_mu_update ON kysely;

DROP TRIGGER kysely_kysymys_cu_insert ON kysely_kysymys;
DROP TRIGGER kysely_kysymys_mu_insert ON kysely_kysymys;
DROP TRIGGER kysely_kysymys_mu_update ON kysely_kysymys;

DROP TRIGGER kysely_kysymysryhma_cu_insert ON kysely_kysymysryhma;
DROP TRIGGER kysely_kysymysryhma_mu_insert ON kysely_kysymysryhma;
DROP TRIGGER kysely_kysymysryhma_mu_update ON kysely_kysymysryhma;

DROP TRIGGER kyselykerta_cu_insert ON kyselykerta;
DROP TRIGGER kyselykerta_mu_insert ON kyselykerta;
DROP TRIGGER kyselykerta_mu_update ON kyselykerta;

DROP TRIGGER kysymysryhma_kyselypohja_cu_insert ON kysymysryhma_kyselypohja;
DROP TRIGGER kysymysryhma_kyselypohja_mu_insert ON kysymysryhma_kyselypohja;
DROP TRIGGER kysymysryhma_kyselypohja_mu_update ON kysymysryhma_kyselypohja;

DROP TRIGGER kyselypohja_cu_insert ON kyselypohja;
DROP TRIGGER kyselypohja_mu_insert ON kyselypohja;
DROP TRIGGER kyselypohja_mu_update ON kyselypohja;

DROP TRIGGER kysymys_cu_insert ON kysymys;
DROP TRIGGER kysymys_mu_insert ON kysymys;
DROP TRIGGER kysymys_mu_update ON kysymys;

DROP TRIGGER kysymysryhma_cu_insert ON kysymysryhma;
DROP TRIGGER kysymysryhma_mu_insert ON kysymysryhma;
DROP TRIGGER kysymysryhma_mu_update ON kysymysryhma;

DROP TRIGGER monivalintavaihtoehto_cu_insert ON monivalintavaihtoehto;
DROP TRIGGER monivalintavaihtoehto_mu_insert ON monivalintavaihtoehto;
DROP TRIGGER monivalintavaihtoehto_mu_update ON monivalintavaihtoehto;

DROP TRIGGER ohje_cu_insert ON ohje;
DROP TRIGGER ohje_mu_insert ON ohje;
DROP TRIGGER ohje_mu_update ON ohje;
ALTER TABLE ohje DROP COLUMN luotu_kayttaja;
ALTER TABLE ohje DROP COLUMN muutettu_kayttaja;

DROP TRIGGER opintoala_cu_insert ON opintoala;
DROP TRIGGER opintoala_mu_insert ON opintoala;
DROP TRIGGER opintoala_mu_update ON opintoala;
ALTER TABLE opintoala DROP COLUMN luotu_kayttaja;
ALTER TABLE opintoala DROP COLUMN muutettu_kayttaja;

DROP TRIGGER oppilaitos_cu_insert ON oppilaitos;
DROP TRIGGER oppilaitos_mu_insert ON oppilaitos;
DROP TRIGGER oppilaitos_mu_update ON oppilaitos;
ALTER TABLE oppilaitos DROP COLUMN luotu_kayttaja;
ALTER TABLE oppilaitos DROP COLUMN muutettu_kayttaja;

DROP TRIGGER organisaatiopalvelu_log_cu_insert ON organisaatiopalvelu_log;
DROP TRIGGER organisaatiopalvelu_log_mu_insert ON organisaatiopalvelu_log;
DROP TRIGGER organisaatiopalvelu_log_mu_update ON organisaatiopalvelu_log;
ALTER TABLE organisaatiopalvelu_log DROP COLUMN luotu_kayttaja;
ALTER TABLE organisaatiopalvelu_log DROP COLUMN muutettu_kayttaja;

DROP TRIGGER rahoitusmuoto_cu_insert ON rahoitusmuoto;
DROP TRIGGER rahoitusmuoto_mu_insert ON rahoitusmuoto;
DROP TRIGGER rahoitusmuoto_mu_update ON rahoitusmuoto;
ALTER TABLE rahoitusmuoto DROP COLUMN luotu_kayttaja;
ALTER TABLE rahoitusmuoto DROP COLUMN muutettu_kayttaja;

DROP TRIGGER rooli_organisaatio_cu_insert ON rooli_organisaatio;
DROP TRIGGER rooli_organisaatio_mu_insert ON rooli_organisaatio;
DROP TRIGGER rooli_organisaatio_mu_update ON rooli_organisaatio;
ALTER TABLE rooli_organisaatio DROP COLUMN luotu_kayttaja;
ALTER TABLE rooli_organisaatio DROP COLUMN muutettu_kayttaja;

DROP TRIGGER tiedote_cu_insert ON tiedote;
DROP TRIGGER tiedote_mu_insert ON tiedote;
DROP TRIGGER tiedote_mu_update ON tiedote;
ALTER TABLE tiedote DROP COLUMN luotu_kayttaja;
ALTER TABLE tiedote DROP COLUMN muutettu_kayttaja;

DROP TRIGGER tila_enum_cu_insert ON tila_enum;
DROP TRIGGER tila_enum_mu_insert ON tila_enum;
DROP TRIGGER tila_enum_mu_insert ON tila_enum;
ALTER TABLE tila_enum DROP COLUMN luotu_kayttaja;
ALTER TABLE tila_enum DROP COLUMN muutettu_kayttaja;

DROP TRIGGER toimipaikka_cu_insert ON toimipaikka;
DROP TRIGGER toimipaikka_mu_insert ON toimipaikka;
DROP TRIGGER toimipaikka_mu_update ON toimipaikka;
ALTER TABLE toimipaikka DROP COLUMN luotu_kayttaja;
ALTER TABLE toimipaikka DROP COLUMN muutettu_kayttaja;

DROP TRIGGER tutkinto_cu_insert ON tutkinto;
DROP TRIGGER tutkinto_mu_insert ON tutkinto;
DROP TRIGGER tutkinto_mu_update ON tutkinto;
ALTER TABLE tutkinto DROP COLUMN luotu_kayttaja;
ALTER TABLE tutkinto DROP COLUMN muutettu_kayttaja;

DROP TRIGGER tutkintotyyppi_cu_insert ON tutkintotyyppi;
DROP TRIGGER tutkintotyyppi_mu_insert ON tutkintotyyppi;
DROP TRIGGER tutkintotyyppi_mu_update ON tutkintotyyppi;
ALTER TABLE tutkintotyyppi DROP COLUMN luotu_kayttaja;
ALTER TABLE tutkintotyyppi DROP COLUMN muutettu_kayttaja;

DROP TRIGGER vastaaja_cu_insert ON vastaaja;
DROP TRIGGER vastaaja_mu_insert ON vastaaja;
DROP TRIGGER vastaaja_mu_update ON vastaaja;
ALTER TABLE vastaaja DROP COLUMN luotu_kayttaja;
ALTER TABLE vastaaja DROP COLUMN muutettu_kayttaja;

DROP TRIGGER vastaajatunnus_cu_insert ON vastaajatunnus;
DROP TRIGGER vastaajatunnus_mu_insert ON vastaajatunnus;
DROP TRIGGER vastaajatunnus_mu_update ON vastaajatunnus;

DROP TRIGGER vastaus_cu_insert ON vastaus;
DROP TRIGGER vastaus_mu_insert ON vastaus;
DROP TRIGGER vastaus_mu_update ON vastaus;
ALTER TABLE vastaus DROP COLUMN luotu_kayttaja;
ALTER TABLE vastaus DROP COLUMN muutettu_kayttaja;