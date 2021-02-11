CREATE MATERIALIZED VIEW vastaus_jatkovastaus_valtakunnallinen_view AS
  SELECT vastaus.vastausid, vastaus.kysymysid, vastaus.vastaajaid, vastaus.numerovalinta, vastaus.vaihtoehto,
    vastaus.vapaateksti, vastaus.en_osaa_sanoa, jatkovastaus.kylla_asteikko, jatkovastaus.ei_vastausteksti, vastaus.vastausaika
  FROM vastaus
    LEFT JOIN jatkovastaus ON jatkovastaus.jatkovastausid = vastaus.jatkovastausid
    INNER JOIN kysymys ON vastaus.kysymysid = kysymys.kysymysid INNER JOIN kysymysryhma ON (kysymys.kysymysryhmaid = kysymysryhma.kysymysryhmaid AND kysymysryhma.valtakunnallinen = TRUE);
--;;

CREATE UNIQUE INDEX vastaus_jatkovastaus_valtakunnallinen_view_uniq_idx ON vastaus_jatkovastaus_valtakunnallinen_view(vastausid);
--;;
CREATE INDEX vastaus_jatkovastaus_kysymys_valtakunnallinen_vastausaika_idx ON vastaus_jatkovastaus_valtakunnallinen_view(vastausaika);
--;;


CREATE MATERIALIZED VIEW vastaaja_taustakysymysryhma_view AS
  SELECT DISTINCT vastaaja.vastaajaid,
    CASE
    WHEN kysymysryhma.kysymysryhmaid = 3341884 THEN 3341885
    ELSE kysymysryhma.kysymysryhmaid
    END AS taustakysymysryhmaid
  FROM vastaaja
    INNER JOIN kyselykerta ON kyselykerta.kyselykertaid = vastaaja.kyselykertaid
    INNER JOIN kysely ON kysely.kyselyid = kyselykerta.kyselyid
    INNER JOIN kysely_kysymysryhma ON kysely.kyselyid = kysely_kysymysryhma.kyselyid
    INNER JOIN kysymysryhma ON kysymysryhma.kysymysryhmaid = kysely_kysymysryhma.kysymysryhmaid
  WHERE kysymysryhma.taustakysymykset ;
--;;

CREATE INDEX vastaaja_taustakysymysryhma_idx ON vastaaja_taustakysymysryhma_view(taustakysymysryhmaid, vastaajaid);
--;;

CREATE INDEX vastaus_raportointi_idx ON vastaus(kysymysid, numerovalinta, vastaajaid);
