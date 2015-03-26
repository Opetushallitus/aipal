CREATE MATERIALIZED VIEW kysymys_vastaaja_view AS
SELECT DISTINCT vastaajaid, (kysymysid in (280, 289, 301, 319, 343, 379)) AS vanha, (kysymysid in (7312034, 7312035, 7312036, 7312037, 7312038, 7312039, 7312040, 7312027, 7312028, 7312029, 7312030, 7312031, 7312032, 7312033)) AS uusi
FROM vastaus
WHERE kysymysid in (280, 289, 301, 319, 343, 379, 7312034, 7312035, 7312036, 7312037, 7312038, 7312039, 7312040, 7312027, 7312028, 7312029, 7312030, 7312031, 7312032, 7312033);

CREATE INDEX kysymys_vastaaja_uusi_idx ON kysymys_vastaaja_view(uusi, vastaajaid);
CREATE UNIQUE INDEX kysymys_vastaaja_uniq_idx ON kysymys_vastaaja_view(vastaajaid);

CREATE MATERIALIZED VIEW vastaus_jatkovastaus_valtakunnallinen_view AS
SELECT vastaus.vastausid, vastaus.kysymysid, vastaus.vastaajaid, vastaus.numerovalinta, vastaus.vaihtoehto,
vastaus.vapaateksti, vastaus.en_osaa_sanoa, jatkovastaus.kylla_asteikko, jatkovastaus.ei_vastausteksti, vastaus.vastausaika
FROM vastaus
LEFT JOIN jatkovastaus ON jatkovastaus.jatkovastausid = vastaus.jatkovastausid
INNER JOIN kysymys ON vastaus.kysymysid = kysymys.kysymysid INNER JOIN kysymysryhma ON (kysymys.kysymysryhmaid = kysymysryhma.kysymysryhmaid AND kysymysryhma.valtakunnallinen = TRUE);

CREATE UNIQUE INDEX vastaus_jatkovastaus_valtakunnallinen_view_uniq_idx ON vastaus_jatkovastaus_valtakunnallinen_view(vastausid);
CREATE INDEX vastaus_jatkovastaus_kysymys_valtakunnallinen_vastausaika_idx ON vastaus_jatkovastaus_valtakunnallinen_view(vastausaika);

ALTER MATERIALIZED VIEW kysymys_vastaaja_view
OWNER TO ${aipal_user};

ALTER MATERIALIZED VIEW vastaus_jatkovastaus_valtakunnallinen_view
OWNER TO ${aipal_user};
