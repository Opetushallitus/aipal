-- Materialized View: vipunen_view

DROP MATERIALIZED VIEW vipunen_view;

CREATE MATERIALIZED VIEW vipunen_view AS
  SELECT
    kysely.koulutustoimija,

    vastaajatunnus.valmistavan_koulutuksen_jarjestaja,

    vastaajatunnus.valmistavan_koulutuksen_oppilaitos,

    regexp_replace(kysymysryhma.selite_fi, '\n', ' ', 'g') AS kysymysryhma,
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

    vastaajatunnus.suorituskieli,

    kysymys.vastaustyyppi,
    vastaus.numerovalinta,
    CASE vastaus.vaihtoehto WHEN 'kylla' THEN 1 WHEN 'ei' THEN 0 ELSE NULL END AS vaihtoehto,
    regexp_replace(COALESCE(monivalintavaihtoehto.teksti_fi,COALESCE(monivalintavaihtoehto.teksti_sv, monivalintavaihtoehto.teksti_en)), '\n', ' ', 'g') AS monivalintavaihtoehto,

    kysymysryhma.taustakysymykset,

    COALESCE(vastaajatunnus.kunta, vastaajatunnus.taustatiedot->>'kunta') AS kunta,
    COALESCE(vastaajatunnus.koulutusmuoto, vastaajatunnus.taustatiedot->>'koulutusmuoto') AS koulutusmuoto,
    vastaajatunnus.tunnus,

    vastaus.vastausaika,

    (select jarjestys from kysely_kysymysryhma where kyselyid=kysely.kyselyid and kysymysryhmaid=kysymysryhma.kysymysryhmaid) kysymysryhmajarjestys,
    kysymys.jarjestys kysymysjarjestys,

    kysymys.kysymysryhmaid,
    kysely.kyselyid,
    kysely.voimassa_alkupvm AS kysely_alkupvm,
    kysely.voimassa_loppupvm AS kysely_loppupvm,
    kyselykerta.kyselykertaid,
    kysymys.kysymysid,
    vastaaja.vastaajaid,
    vastaus.vastausid

  FROM vastaus
    JOIN kysymys ON vastaus.kysymysid = kysymys.kysymysid
    JOIN kysymysryhma ON kysymys.kysymysryhmaid = kysymysryhma.kysymysryhmaid
    LEFT JOIN monivalintavaihtoehto ON kysymys.vastaustyyppi = 'monivalinta'
                                       AND monivalintavaihtoehto.kysymysid = kysymys.kysymysid
                                       AND vastaus.numerovalinta = monivalintavaihtoehto.jarjestys
    JOIN vastaaja ON vastaus.vastaajaid = vastaaja.vastaajaid
    JOIN vastaajatunnus ON vastaaja.vastaajatunnusid = vastaajatunnus.vastaajatunnusid
    LEFT JOIN tutkinto ON vastaajatunnus.tutkintotunnus = tutkinto.tutkintotunnus
    JOIN kyselykerta ON vastaajatunnus.kyselykertaid = kyselykerta.kyselykertaid
    JOIN kysely ON kyselykerta.kyselyid = kysely.kyselyid
  WHERE kysymys.vastaustyyppi <> 'vapaateksti'
WITH NO DATA;

CREATE INDEX vipunen_vastausaika_idx ON vipunen_view(vastausaika);

ALTER MATERIALIZED VIEW vipunen_view
  OWNER TO ${aipal_user};
