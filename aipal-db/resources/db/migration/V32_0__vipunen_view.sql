CREATE MATERIALIZED VIEW vipunen_view AS
SELECT vastaus.vastausid,
vastaus.vastausaika,
vastaus.numerovalinta,
kysymysryhma.valtakunnallinen,
CASE vastaus.vaihtoehto WHEN 'kylla' THEN 1 WHEN 'ei' THEN 0 END AS vaihtoehto,
REGEXP_REPLACE(COALESCE(monivalintavaihtoehto.teksti_fi, monivalintavaihtoehto.teksti_sv), '\n', ' ', 'g') AS monivalintavaihtoehto,
kysymys.kysymysid,
REGEXP_REPLACE(kysymys.kysymys_fi, '\n', ' ', 'g') AS kysymys_fi,
REGEXP_REPLACE(kysymys.kysymys_sv, '\n', ' ', 'g') AS kysymys_sv,
kysymys.kysymysryhmaid,
REGEXP_REPLACE(kysymysryhma.nimi_fi, '\n', ' ', 'g') AS kysymysryhma_fi,
REGEXP_REPLACE(kysymysryhma.nimi_sv, '\n', ' ', 'g') AS kysymysryhma_sv,
kysymys.vastaustyyppi,
vastaaja.vastaajaid,
rahoitusmuoto.rahoitusmuoto,
tutkinto.tutkintotunnus,
REGEXP_REPLACE(tutkinto.nimi_fi, '\n', ' ', 'g') AS tutkinto_fi,
REGEXP_REPLACE(tutkinto.nimi_sv, '\n', ' ', 'g') AS tutkinto_sv,
opintoala.opintoalatunnus,
REGEXP_REPLACE(opintoala.nimi_fi, '\n', ' ', 'g') AS opintoala_fi,
REGEXP_REPLACE(opintoala.nimi_sv, '\n', ' ', 'g') AS opintoala_sv,
vastaajatunnus.suorituskieli,
vastaajatunnus.valmistavan_koulutuksen_jarjestaja,
REGEXP_REPLACE(valmistava_koulutustoimija.nimi_fi, '\n', ' ', 'g') AS valmistavan_koulutuksen_jarjestaja_fi,
REGEXP_REPLACE(valmistava_koulutustoimija.nimi_sv, '\n', ' ', 'g') AS valmistavan_koulutuksen_jarjestaja_sv,
vastaajatunnus.valmistavan_koulutuksen_oppilaitos,
REGEXP_REPLACE(valmistava_oppilaitos.nimi_fi, '\n', ' ', 'g') AS valmistavan_koulutuksen_oppilaitos_fi,
REGEXP_REPLACE(valmistava_oppilaitos.nimi_sv, '\n', ' ', 'g') AS valmistavan_koulutuksen_oppilaitos_sv,
kyselykerta.kyselykertaid,
REGEXP_REPLACE(kyselykerta.nimi, '\n', ' ', 'g') AS kyselykerta,
kysely.kyselyid,
REGEXP_REPLACE(kysely.nimi_fi, '\n', ' ', 'g') AS kysely_fi,
REGEXP_REPLACE(kysely.nimi_fi, '\n', ' ', 'g') AS kysely_sv,
kysely.koulutustoimija,
REGEXP_REPLACE(koulutustoimija.nimi_fi, '\n', ' ', 'g') AS koulutustoimija_fi,
REGEXP_REPLACE(koulutustoimija.nimi_sv, '\n', ' ', 'g') AS koulutustoimija_sv,
REGEXP_REPLACE(COALESCE(monivalinta7312027.teksti_fi,monivalinta7312034.teksti_fi), '\n', ' ', 'g') AS taustakysymys_sukupuoli,
REGEXP_REPLACE(COALESCE(monivalinta7312028.teksti_fi,monivalinta7312035.teksti_fi), '\n', ' ', 'g') AS taustakysymys_aidinkieli,
REGEXP_REPLACE(COALESCE(monivalinta7312029.teksti_fi,monivalinta7312036.teksti_fi), '\n', ' ', 'g') AS taustakysymys_ika,
REGEXP_REPLACE(COALESCE(monivalinta7312030.teksti_fi,monivalinta7312037.teksti_fi), '\n', ' ', 'g') AS taustakysymys_tutkinto,
REGEXP_REPLACE(COALESCE(monivalinta7312031.teksti_fi,monivalinta7312038.teksti_fi), '\n', ' ', 'g') AS taustakysymys_syy,
REGEXP_REPLACE(COALESCE(monivalinta7312032.teksti_fi), '\n', ' ', 'g') AS taustakysymys_tuleva_tilanne,
REGEXP_REPLACE(COALESCE(monivalinta7312039.teksti_fi), '\n', ' ', 'g') AS taustakysymys_aiempi_tilanne,
REGEXP_REPLACE(COALESCE(monivalinta7312033.teksti_fi,monivalinta7312040.teksti_fi), '\n', ' ', 'g') AS taustakysymys_tavoite
FROM vastaus
INNER JOIN kysymys ON vastaus.kysymysid = kysymys.kysymysid
INNER JOIN kysymysryhma ON kysymys.kysymysryhmaid = kysymysryhma.kysymysryhmaid
LEFT JOIN monivalintavaihtoehto ON kysymys.vastaustyyppi = 'monivalinta' AND monivalintavaihtoehto.kysymysid = kysymys.kysymysid AND vastaus.numerovalinta = monivalintavaihtoehto.jarjestys
INNER JOIN vastaaja ON vastaus.vastaajaid = vastaaja.vastaajaid
INNER JOIN vastaajatunnus ON vastaaja.vastaajatunnusid = vastaajatunnus.vastaajatunnusid
LEFT JOIN tutkinto ON vastaajatunnus.tutkintotunnus = tutkinto.tutkintotunnus
LEFT JOIN opintoala ON tutkinto.opintoala = opintoala.opintoalatunnus
LEFT JOIN koulutustoimija AS valmistava_koulutustoimija ON vastaajatunnus.valmistavan_koulutuksen_jarjestaja = valmistava_koulutustoimija.ytunnus
LEFT JOIN oppilaitos AS valmistava_oppilaitos ON vastaajatunnus.valmistavan_koulutuksen_oppilaitos = valmistava_oppilaitos.oppilaitoskoodi
INNER JOIN rahoitusmuoto ON vastaajatunnus.rahoitusmuotoid = rahoitusmuoto.rahoitusmuotoid
INNER JOIN kyselykerta ON vastaajatunnus.kyselykertaid = kyselykerta.kyselykertaid
INNER JOIN kysely ON kyselykerta.kyselyid = kysely.kyselyid
LEFT JOIN koulutustoimija ON kysely.koulutustoimija = koulutustoimija.ytunnus
LEFT JOIN vastaus AS vastaus7312027 ON vastaus7312027.vastaajaid = vastaaja.vastaajaid AND vastaus7312027.kysymysid = 7312027
LEFT JOIN monivalintavaihtoehto AS monivalinta7312027 ON monivalinta7312027.kysymysid = vastaus7312027.kysymysid AND vastaus7312027.numerovalinta = monivalinta7312027.jarjestys
LEFT JOIN vastaus AS vastaus7312034 ON vastaus7312034.vastaajaid = vastaaja.vastaajaid AND vastaus7312034.kysymysid = 7312034
LEFT JOIN monivalintavaihtoehto AS monivalinta7312034 ON monivalinta7312034.kysymysid = vastaus7312034.kysymysid AND vastaus7312034.numerovalinta = monivalinta7312034.jarjestys
LEFT JOIN vastaus AS vastaus7312028 ON vastaus7312028.vastaajaid = vastaaja.vastaajaid AND vastaus7312028.kysymysid = 7312028
LEFT JOIN monivalintavaihtoehto AS monivalinta7312028 ON monivalinta7312028.kysymysid = vastaus7312028.kysymysid AND vastaus7312028.numerovalinta = monivalinta7312028.jarjestys
LEFT JOIN vastaus AS vastaus7312035 ON vastaus7312035.vastaajaid = vastaaja.vastaajaid AND vastaus7312035.kysymysid = 7312035
LEFT JOIN monivalintavaihtoehto AS monivalinta7312035 ON monivalinta7312035.kysymysid = vastaus7312035.kysymysid AND vastaus7312035.numerovalinta = monivalinta7312035.jarjestys
LEFT JOIN vastaus AS vastaus7312029 ON vastaus7312029.vastaajaid = vastaaja.vastaajaid AND vastaus7312029.kysymysid = 7312029
LEFT JOIN monivalintavaihtoehto AS monivalinta7312029 ON monivalinta7312029.kysymysid = vastaus7312029.kysymysid AND vastaus7312029.numerovalinta = monivalinta7312029.jarjestys
LEFT JOIN vastaus AS vastaus7312036 ON vastaus7312036.vastaajaid = vastaaja.vastaajaid AND vastaus7312036.kysymysid = 7312036
LEFT JOIN monivalintavaihtoehto AS monivalinta7312036 ON monivalinta7312036.kysymysid = vastaus7312036.kysymysid AND vastaus7312036.numerovalinta = monivalinta7312036.jarjestys
LEFT JOIN vastaus AS vastaus7312030 ON vastaus7312030.vastaajaid = vastaaja.vastaajaid AND vastaus7312030.kysymysid = 7312030
LEFT JOIN monivalintavaihtoehto AS monivalinta7312030 ON monivalinta7312030.kysymysid = vastaus7312030.kysymysid AND vastaus7312030.numerovalinta = monivalinta7312030.jarjestys
LEFT JOIN vastaus AS vastaus7312037 ON vastaus7312037.vastaajaid = vastaaja.vastaajaid AND vastaus7312037.kysymysid = 7312037
LEFT JOIN monivalintavaihtoehto AS monivalinta7312037 ON monivalinta7312037.kysymysid = vastaus7312037.kysymysid AND vastaus7312037.numerovalinta = monivalinta7312037.jarjestys
LEFT JOIN vastaus AS vastaus7312031 ON vastaus7312031.vastaajaid = vastaaja.vastaajaid AND vastaus7312031.kysymysid = 7312031
LEFT JOIN monivalintavaihtoehto AS monivalinta7312031 ON monivalinta7312031.kysymysid = vastaus7312031.kysymysid AND vastaus7312031.numerovalinta = monivalinta7312031.jarjestys
LEFT JOIN vastaus AS vastaus7312038 ON vastaus7312038.vastaajaid = vastaaja.vastaajaid AND vastaus7312038.kysymysid = 7312038
LEFT JOIN monivalintavaihtoehto AS monivalinta7312038 ON monivalinta7312038.kysymysid = vastaus7312038.kysymysid AND vastaus7312038.numerovalinta = monivalinta7312038.jarjestys
LEFT JOIN vastaus AS vastaus7312032 ON vastaus7312032.vastaajaid = vastaaja.vastaajaid AND vastaus7312032.kysymysid = 7312032
LEFT JOIN monivalintavaihtoehto AS monivalinta7312032 ON monivalinta7312032.kysymysid = vastaus7312032.kysymysid AND vastaus7312032.numerovalinta = monivalinta7312032.jarjestys
LEFT JOIN vastaus AS vastaus7312039 ON vastaus7312039.vastaajaid = vastaaja.vastaajaid AND vastaus7312039.kysymysid = 7312039
LEFT JOIN monivalintavaihtoehto AS monivalinta7312039 ON monivalinta7312039.kysymysid = vastaus7312039.kysymysid AND vastaus7312039.numerovalinta = monivalinta7312039.jarjestys
LEFT JOIN vastaus AS vastaus7312033 ON vastaus7312033.vastaajaid = vastaaja.vastaajaid AND vastaus7312033.kysymysid = 7312033
LEFT JOIN monivalintavaihtoehto AS monivalinta7312033 ON monivalinta7312033.kysymysid = vastaus7312033.kysymysid AND vastaus7312033.numerovalinta = monivalinta7312033.jarjestys
LEFT JOIN vastaus AS vastaus7312040 ON vastaus7312040.vastaajaid = vastaaja.vastaajaid AND vastaus7312040.kysymysid = 7312040
LEFT JOIN monivalintavaihtoehto AS monivalinta7312040 ON monivalinta7312040.kysymysid = vastaus7312040.kysymysid AND vastaus7312040.numerovalinta = monivalinta7312040.jarjestys
WHERE kysymys.vastaustyyppi <> 'vapaateksti' AND
(monivalinta7312027.teksti_fi is not null OR
monivalinta7312034.teksti_fi is not null OR
monivalinta7312028.teksti_fi is not null OR
monivalinta7312035.teksti_fi is not null OR
monivalinta7312029.teksti_fi is not null OR
monivalinta7312036.teksti_fi is not null OR
monivalinta7312030.teksti_fi is not null OR
monivalinta7312037.teksti_fi is not null OR
monivalinta7312031.teksti_fi is not null OR
monivalinta7312038.teksti_fi is not null OR
monivalinta7312032.teksti_fi is not null OR
monivalinta7312039.teksti_fi is not null OR
monivalinta7312033.teksti_fi is not null OR
monivalinta7312040.teksti_fi is not null)
AND vastaus.kysymysid NOT IN (7312027,7312034,7312028,7312035,7312029,7312036,7312030,7312037,7312031,7312038,7312032,7312039,7312033,7312040);