ALTER TABLE vastaajatunnus ADD COLUMN valmistavan_koulutuksen_jarjestaja varchar(10);

CREATE MATERIALIZED VIEW vipunen_view
 AS SELECT kysely.koulutustoimija,
    vastaajatunnus.valmistavan_koulutuksen_jarjestaja,
    vastaajatunnus.valmistavan_koulutuksen_oppilaitos,
    regexp_replace(kysymysryhma.selite_fi, '\n'::text, ' '::text, 'g'::text) AS kysymysryhma,
    regexp_replace(kysymysryhma.nimi_fi::text, '\n'::text, ' '::text, 'g'::text) AS kysymysryhma_fi,
    regexp_replace(kysymysryhma.nimi_sv::text, '\n'::text, ' '::text, 'g'::text) AS kysymysryhma_sv,
    regexp_replace(kysymysryhma.nimi_en::text, '\n'::text, ' '::text, 'g'::text) AS kysymysryhma_en,
    kysymysryhma.valtakunnallinen,
    regexp_replace(kysely.nimi_fi::text, '\n'::text, ' '::text, 'g'::text) AS kysely_fi,
    regexp_replace(kysely.nimi_sv::text, '\n'::text, ' '::text, 'g'::text) AS kysely_sv,
    regexp_replace(kysely.nimi_en::text, '\n'::text, ' '::text, 'g'::text) AS kysely_en,
    regexp_replace(kyselykerta.nimi::text, '\n'::text, ' '::text, 'g'::text) AS kyselykerta,
    regexp_replace(kysymys.kysymys_fi::text, '\n'::text, ' '::text, 'g'::text) AS kysymys_fi,
    regexp_replace(kysymys.kysymys_sv::text, '\n'::text, ' '::text, 'g'::text) AS kysymys_sv,
    regexp_replace(kysymys.kysymys_en::text, '\n'::text, ' '::text, 'g'::text) AS kysymys_en,
    tutkinto.tutkintotunnus,
    vastaajatunnus.suorituskieli,
    kysymys.vastaustyyppi,
    vastaus.numerovalinta,
        CASE vastaus.vaihtoehto
            WHEN 'kylla'::text THEN 1
            WHEN 'ei'::text THEN 0
            ELSE NULL::integer
        END AS vaihtoehto,
    regexp_replace(COALESCE(monivalintavaihtoehto.teksti_fi, COALESCE(monivalintavaihtoehto.teksti_sv, monivalintavaihtoehto.teksti_en))::text, '\n'::text, ' '::text, 'g'::text) AS monivalintavaihtoehto,
    kysymysryhma.taustakysymykset,
    COALESCE(vastaajatunnus.kunta, (vastaajatunnus.taustatiedot ->> 'kunta'::text)::character varying) AS kunta,
    COALESCE(vastaajatunnus.koulutusmuoto, (vastaajatunnus.taustatiedot ->> 'koulutusmuoto'::text)::character varying) AS koulutusmuoto,
    vastaajatunnus.tunnus,
    vastaus.vastausaika,
    ( SELECT kysely_kysymysryhma.jarjestys
           FROM kysely_kysymysryhma
          WHERE kysely_kysymysryhma.kyselyid = kysely.kyselyid AND kysely_kysymysryhma.kysymysryhmaid = kysymysryhma.kysymysryhmaid) AS kysymysryhmajarjestys,
    kysymys.jarjestys AS kysymysjarjestys,
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
     LEFT JOIN monivalintavaihtoehto ON kysymys.vastaustyyppi::text = 'monivalinta'::text AND monivalintavaihtoehto.kysymysid = kysymys.kysymysid AND vastaus.numerovalinta = monivalintavaihtoehto.jarjestys
     JOIN vastaaja ON vastaus.vastaajaid = vastaaja.vastaajaid
     JOIN vastaajatunnus ON vastaaja.vastaajatunnusid = vastaajatunnus.vastaajatunnusid
     LEFT JOIN tutkinto ON vastaajatunnus.tutkintotunnus::text = tutkinto.tutkintotunnus::text
     JOIN kyselykerta ON vastaajatunnus.kyselykertaid = kyselykerta.kyselykertaid
     JOIN kysely ON kyselykerta.kyselyid = kysely.kyselyid
  WHERE kysymys.vastaustyyppi::text <> 'vapaateksti'::text AND vastaus.vastausaika > '2018-01-01'::date;

CREATE INDEX vipunen_vastausaika_idx ON vipunen_view(vastausaika);

-- This allows changing view owner using list of possible users.
CREATE OR REPLACE FUNCTION set_view_owner(_view text, _user text)
  RETURNS void AS
$func$
BEGIN
   EXECUTE format('ALTER MATERIALIZED VIEW %I OWNER TO %I', _view, _user);
END
$func$ LANGUAGE plpgsql;

do $$
begin
  PERFORM set_view_owner('vipunen_view', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvo_user', 'arvo_snap_user', 'arvo_test_user') LIMIT 1;
end
$$;
