-- Materialized View: vipunen_view

DROP MATERIALIZED VIEW vipunen_view;

CREATE MATERIALIZED VIEW vipunen_view AS
SELECT
kysely.koulutustoimija,
regexp_replace(koulutustoimija.nimi_fi, '\n', ' ', 'g') AS koulutustoimija_fi,
regexp_replace(koulutustoimija.nimi_sv, '\n', ' ', 'g') AS koulutustoimija_sv,
regexp_replace(koulutustoimija.nimi_en, '\n', ' ', 'g') AS koulutustoimija_en,

vastaajatunnus.valmistavan_koulutuksen_jarjestaja,
regexp_replace(valmistava_koulutustoimija.nimi_fi, '\n', ' ', 'g') AS valmistavan_koulutuksen_jarjestaja_fi,
regexp_replace(valmistava_koulutustoimija.nimi_sv, '\n', ' ', 'g') AS valmistavan_koulutuksen_jarjestaja_sv,
regexp_replace(valmistava_koulutustoimija.nimi_en, '\n', ' ', 'g') AS valmistavan_koulutuksen_jarjestaja_en,
vastaajatunnus.valmistavan_koulutuksen_oppilaitos,
regexp_replace(valmistava_oppilaitos.nimi_fi, '\n', ' ', 'g') AS valmistavan_koulutuksen_oppilaitos_fi,
regexp_replace(valmistava_oppilaitos.nimi_sv, '\n', ' ', 'g') AS valmistavan_koulutuksen_oppilaitos_sv,
regexp_replace(valmistava_oppilaitos.nimi_en, '\n', ' ', 'g') AS valmistavan_koulutuksen_oppilaitos_en,

regexp_replace(kysymysryhma.nimi_fi, '\n', ' ', 'g') AS kysymysryhma_fi,
regexp_replace(kysymysryhma.nimi_sv, '\n', ' ', 'g') AS kysymysryhma_sv,
regexp_replace(kysymysryhma.nimi_en, '\n', ' ', 'g') AS kysymysryhma_en,
kysymysryhma.valtakunnallinen,
regexp_replace(kysely.nimi_fi, '\n', ' ', 'g') AS kysely_fi,
regexp_replace(kysely.nimi_sv, '\n', ' ', 'g') AS kysely_sv,
regexp_replace(kysely.nimi_en, '\n', ' ', 'g') AS kysely_en,
regexp_replace(kyselykerta.nimi, '\n', ' ', 'g') AS kyselykerta,
regexp_replace(kysymys.kysymys_fi, '\n', ' ', 'g') AS kysymys_fi,
regexp_replace(kysymys.kysymys_sv, '\n', ' ', 'g') AS kysymys_sv,
regexp_replace(kysymys.kysymys_en, '\n', ' ', 'g') AS kysymys_en,

tutkinto.tutkintotunnus,
regexp_replace(tutkinto.nimi_fi, '\n', ' ', 'g') AS tutkinto_fi,
regexp_replace(tutkinto.nimi_sv, '\n', ' ', 'g') AS tutkinto_sv,
regexp_replace(tutkinto.nimi_en, '\n', ' ', 'g') AS tutkinto_en,
opintoala.opintoalatunnus,
regexp_replace(opintoala.nimi_fi, '\n', ' ', 'g') AS opintoala_fi,
regexp_replace(opintoala.nimi_sv, '\n', ' ', 'g') AS opintoala_sv,
regexp_replace(opintoala.nimi_en, '\n', ' ', 'g') AS opintoala_en,

vastaajatunnus.suorituskieli,

kysymys.vastaustyyppi,
vastaus.numerovalinta,
CASE vastaus.vaihtoehto WHEN 'kylla' THEN 1 WHEN 'ei' THEN 0 ELSE NULL END AS vaihtoehto,
regexp_replace(COALESCE(monivalintavaihtoehto.teksti_fi,COALESCE(monivalintavaihtoehto.teksti_sv, monivalintavaihtoehto.teksti_en)), '\n', ' ', 'g') AS monivalintavaihtoehto,

rahoitusmuoto.rahoitusmuoto,

regexp_replace(monivalintasukupuoli.teksti_fi, '\n', ' ', 'g') AS taustakysymys_sukupuoli,
regexp_replace(monivalintaika.teksti_fi, '\n', ' ', 'g') AS taustakysymys_ika,
regexp_replace(monivalintapohjakoulutus.teksti_fi, '\n', ' ', 'g') AS taustakysymys_pohjakoulutus,

vastaajatunnus.kunta,
vastaajatunnus.koulutusmuoto,
vastaajatunnus.tunnus,

vastaus.vastausaika,

(select jarjestys from kysely_kysymysryhma where kyselyid=kysely.kyselyid and kysymysryhmaid=kysymysryhma.kysymysryhmaid) kysymysryhmajarjestys,
kysymys.jarjestys kysymysjarjestys,

kysymys.kysymysryhmaid,
kysely.kyselyid,
kyselykerta.kyselykertaid,
kysymys.kysymysid,
vastaaja.vastaajaid,
vastaus.vastausid

FROM vastaus
JOIN kysymys ON vastaus.kysymysid = kysymys.kysymysid
JOIN kysymysryhma ON kysymys.kysymysryhmaid = kysymysryhma.kysymysryhmaid
LEFT JOIN monivalintavaihtoehto ON kysymys.vastaustyyppi = 'monivalinta' AND monivalintavaihtoehto.kysymysid = kysymys.kysymysid AND vastaus.numerovalinta = monivalintavaihtoehto.jarjestys
JOIN vastaaja ON vastaus.vastaajaid = vastaaja.vastaajaid
JOIN vastaajatunnus ON vastaaja.vastaajatunnusid = vastaajatunnus.vastaajatunnusid
LEFT JOIN tutkinto ON vastaajatunnus.tutkintotunnus = tutkinto.tutkintotunnus
LEFT JOIN opintoala ON tutkinto.opintoala = opintoala.opintoalatunnus
LEFT JOIN koulutustoimija valmistava_koulutustoimija ON vastaajatunnus.valmistavan_koulutuksen_jarjestaja = valmistava_koulutustoimija.ytunnus
LEFT JOIN oppilaitos valmistava_oppilaitos ON vastaajatunnus.valmistavan_koulutuksen_oppilaitos = valmistava_oppilaitos.oppilaitoskoodi
JOIN rahoitusmuoto ON vastaajatunnus.rahoitusmuotoid = rahoitusmuoto.rahoitusmuotoid
JOIN kyselykerta ON vastaajatunnus.kyselykertaid = kyselykerta.kyselykertaid
JOIN kysely ON kyselykerta.kyselyid = kysely.kyselyid
LEFT JOIN koulutustoimija ON kysely.koulutustoimija = koulutustoimija.ytunnus
LEFT JOIN vastaus vastaussukupuoli ON vastaussukupuoli.vastaajaid = vastaaja.vastaajaid AND vastaussukupuoli.kysymysid = (( SELECT kysymys_1.kysymysid
FROM kysymys kysymys_1
WHERE (kysymys_1.kysymysryhmaid IN ( SELECT kysymysryhma_1.kysymysryhmaid
FROM kysymysryhma kysymysryhma_1
WHERE kysymysryhma_1.valtakunnallinen = true AND kysymysryhma_1.taustakysymykset = true AND kysymysryhma_1.tila = 'julkaistu' AND kysymysryhma_1.selite_fi = 'AVOP Taustatiedot')) AND kysymys_1.kysymys_fi = 'Sukupuoli'))
LEFT JOIN monivalintavaihtoehto monivalintasukupuoli ON monivalintasukupuoli.kysymysid = vastaussukupuoli.kysymysid AND vastaussukupuoli.numerovalinta = monivalintasukupuoli.jarjestys
LEFT JOIN vastaus vastausika ON vastausika.vastaajaid = vastaaja.vastaajaid AND vastausika.kysymysid = (( SELECT kysymys_1.kysymysid
FROM kysymys kysymys_1
WHERE (kysymys_1.kysymysryhmaid IN ( SELECT kysymysryhma_1.kysymysryhmaid
FROM kysymysryhma kysymysryhma_1
WHERE kysymysryhma_1.valtakunnallinen = true AND kysymysryhma_1.taustakysymykset = true AND kysymysryhma_1.tila = 'julkaistu' AND kysymysryhma_1.selite_fi = 'AVOP Taustatiedot')) AND kysymys_1.kysymys_fi = 'Ikä'))
LEFT JOIN monivalintavaihtoehto monivalintaika ON monivalintaika.kysymysid = vastausika.kysymysid AND vastausika.numerovalinta = monivalintaika.jarjestys
LEFT JOIN vastaus vastauspohjakoulutus ON vastauspohjakoulutus.vastaajaid = vastaaja.vastaajaid AND vastauspohjakoulutus.kysymysid = (( SELECT kysymys_1.kysymysid
FROM kysymys kysymys_1
WHERE (kysymys_1.kysymysryhmaid IN ( SELECT kysymysryhma_1.kysymysryhmaid
FROM kysymysryhma kysymysryhma_1
WHERE kysymysryhma_1.valtakunnallinen = true AND kysymysryhma_1.taustakysymykset = true AND kysymysryhma_1.tila = 'julkaistu' AND kysymysryhma_1.selite_fi = 'AVOP Taustatiedot')) AND kysymys_1.kysymys_fi = 'Pohjakoulutus'))
LEFT JOIN monivalintavaihtoehto monivalintapohjakoulutus ON monivalintapohjakoulutus.kysymysid = vastauspohjakoulutus.kysymysid AND vastauspohjakoulutus.numerovalinta = monivalintapohjakoulutus.jarjestys
WHERE kysymys.vastaustyyppi <> 'vapaateksti' AND NOT (vastaus.kysymysid IN ( SELECT kysymys_1.kysymysid
FROM kysymys kysymys_1
WHERE (kysymys_1.kysymysryhmaid IN ( SELECT kysymysryhma_1.kysymysryhmaid
FROM kysymysryhma kysymysryhma_1
WHERE kysymysryhma_1.valtakunnallinen = true AND kysymysryhma_1.taustakysymykset = true AND kysymysryhma_1.tila = 'julkaistu' AND kysymysryhma_1.selite_fi = 'AVOP Taustatiedot')))) AND (monivalintasukupuoli.teksti_fi IS NOT NULL OR monivalintaika.teksti_fi IS NOT NULL OR monivalintapohjakoulutus.teksti_fi IS NOT NULL)
WITH NO DATA;

ALTER MATERIALIZED VIEW vipunen_view
OWNER TO ${aipal_user};
