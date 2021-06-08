SELECT now();
INSERT INTO kayttaja VALUES ('DATANTUONTI', 'DATANTUONTI', 'Järjestelmä', '', true, now(), now());


SELECT now();
-- oppilaitostyyppi_tutkintotyyppi table is scuffed and only updated with jarjestelma so we exclude it
DO $luotu_kayttaja$
    DECLARE table_to_update text;
BEGIN
    FOR table_to_update IN SELECT DISTINCT table_name FROM information_schema.columns WHERE table_schema = 'public' AND column_name = 'luotu_kayttaja' AND table_name != 'oppilaitostyyppi_tutkintotyyppi'
    LOOP
        EXECUTE FORMAT(
                $$
                UPDATE %I
                SET luotu_kayttaja = 'DATANTUONTI', muutettu_kayttaja = 'DATANTUONTI';
                $$,
                table_to_update
            );
    END LOOP;
END $luotu_kayttaja$;

SELECT now();
DELETE FROM rooli_organisaatio WHERE kayttaja NOT IN ('JARJESTELMA', 'INTEGRAATIO', 'KONVERSIO', 'VASTAAJA');


SELECT now();
-- Tähän menee ~22min tällä hetkellä jos ei pudoteta näitä fk viittauksia. Nyt menee joku minuutti.
ALTER TABLE ONLY vastaajatunnus DROP CONSTRAINT vastaajatunnus_kayttaja_fk;
ALTER TABLE ONLY vastaajatunnus DROP CONSTRAINT vastaajatunnus_kayttaja_fkv1;
DELETE FROM kayttaja WHERE uid not in ('JARJESTELMA', 'KONVERSIO', 'INTEGRAATIO', 'VASTAAJA', 'DATANTUONTI');
ALTER TABLE ONLY vastaajatunnus ADD CONSTRAINT vastaajatunnus_kayttaja_fk FOREIGN KEY (luotu_kayttaja) REFERENCES kayttaja(oid);
ALTER TABLE ONLY vastaajatunnus ADD CONSTRAINT vastaajatunnus_kayttaja_fkv1 FOREIGN KEY (muutettu_kayttaja) REFERENCES kayttaja(oid);

SELECT now();
TRUNCATE organisaatiopalvelu_log, vastaajatunnus_tiedot;

SELECT now();
DO $luotuaika$
    DECLARE table_to_update text;
    BEGIN
        FOR table_to_update IN SELECT DISTINCT table_name FROM information_schema.columns WHERE table_schema = 'public' AND column_name = 'luotuaika' AND table_name != 'oppilaitostyyppi_tutkintotyyppi'
            LOOP
                EXECUTE FORMAT(
                        $$
                UPDATE %I
                SET luotuaika = now(), muutettuaika = now();
                $$,
                        table_to_update
                    );
        END LOOP;
END$luotuaika$;

--
-- kysely anonymisointi
--
SELECT now();
UPDATE kysely
SET uudelleenohjaus_url = null,
    metatiedot = COALESCE(metatiedot || jsonb_build_object('esikatselu_tunniste', md5(random()::text)), jsonb_build_object('esikatselu_tunniste', md5(random()::text)));

--
-- vastaajatunnus anonymisointi
--

SELECT now();
-- vastaajatunnus__un messes with tunnus shuffle, update trigger with muutettuaika
ALTER TABLE vastaajatunnus DISABLE TRIGGER vastaajatunnus_update;
WITH tutkintotunnus_query AS (SELECT row_number() OVER (ORDER BY random()) AS rownumber, tutkintotunnus AS shuffled_tutkintotunnus FROM vastaajatunnus),
     kieli_query AS (SELECT row_number() OVER (ORDER BY random()) AS rownumber, suorituskieli AS shuffled_suorituskieli FROM vastaajatunnus),
     query AS (SELECT row_number() OVER (ORDER BY random()) AS rownumber, vastaajatunnusid AS id, kk.voimassa_alkupvm, kk.voimassa_loppupvm FROM vastaajatunnus vt JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid)
UPDATE vastaajatunnus
SET tutkintotunnus = tutkintotunnus_query.shuffled_tutkintotunnus,
    lukittu = false,
    voimassa_alkupvm = CASE
                           WHEN query.voimassa_loppupvm is not null
                               THEN query.voimassa_alkupvm + (random() * (least(query.voimassa_loppupvm::timestamp, now()) - query.voimassa_alkupvm::timestamp))
                           ELSE query.voimassa_alkupvm + (random() * (now()::timestamp - query.voimassa_alkupvm::timestamp))
        END,
    kunta = null,
    koulutusmuoto = null,
    valmistavan_koulutuksen_oppilaitos = null,
    suorituskieli = kieli_query.shuffled_suorituskieli,
    taustatiedot = null
FROM tutkintotunnus_query
    JOIN query ON tutkintotunnus_query.rownumber = query.rownumber
    JOIN kieli_query ON tutkintotunnus_query.rownumber = kieli_query.rownumber
WHERE vastaajatunnusid = query.id;

SELECT now();
CREATE OR REPLACE FUNCTION make_unique_tunnus() RETURNS text AS $$
DECLARE
    unique_tunnus text;
    done bool;
BEGIN
    done := false;
    WHILE NOT done LOOP
            unique_tunnus := array_to_string(ARRAY(SELECT chr((65 + round(random() * 25)) :: integer) FROM generate_series(1,6)), '');
            done := NOT exists(SELECT 1 FROM vastaajatunnus WHERE tunnus=unique_tunnus);
        END LOOP;
    RETURN unique_tunnus;
END;
$$ LANGUAGE PLPGSQL VOLATILE;
UPDATE vastaajatunnus SET luotuaika = voimassa_alkupvm,
                          muutettuaika = voimassa_alkupvm,
                          tunnus = make_unique_tunnus(),
                          taustatiedot = jsonb_build_object('kieli', suorituskieli, 'toimipaikka', null, 'hankintakoulutuksen_toteuttaja', null, 'tutkinto', tutkintotunnus),
                          voimassa_loppupvm = voimassa_alkupvm + interval '30 days';
ALTER TABLE vastaajatunnus ENABLE TRIGGER vastaajatunnus_update;

--
-- vastaaja anonymisointi
--

SELECT now();
ALTER TABLE vastaaja DISABLE TRIGGER vastaaja_update;
WITH vastaajatunnus_query AS (SELECT vastaajatunnusid, kyselykertaid, voimassa_alkupvm + (random() * (voimassa_loppupvm::timestamp - voimassa_alkupvm::timestamp)) AS rand_luontipvm FROM vastaajatunnus)
UPDATE vastaaja v
SET vastausaika = vastaajatunnus_query.rand_luontipvm,
    kyselykertaid = vastaajatunnus_query.kyselykertaid,
    luotuaika = vastaajatunnus_query.rand_luontipvm,
    muutettuaika = vastaajatunnus_query.rand_luontipvm
FROM vastaajatunnus_query
WHERE v.vastaajatunnusid = vastaajatunnus_query.vastaajatunnusid;
ALTER TABLE vastaaja ENABLE TRIGGER vastaaja_update;

--
-- vastaus anonymisointi
--

SELECT now();
ALTER TABLE vastaus DISABLE TRIGGER vastaus_update;
WITH vastaaja_query AS (SELECT luotuaika, vastaajaid FROM vastaaja),
     kysymys_query AS (SELECT kysymysid, vastaustyyppi FROM kysymys)
UPDATE vastaus v
SET luotuaika = vastaaja_query.luotuaika,
    muutettuaika = vastaaja_query.luotuaika,
    numerovalinta = CASE
        WHEN numerovalinta is not null AND kysymys_query.vastaustyyppi = 'monivalinta'
            THEN floor(random() * (SELECT count(*) FROM monivalintavaihtoehto mv WHERE mv.kysymysid = kysymys_query.kysymysid))  -- monivalinta skaala on 0 - n-1
        WHEN numerovalinta is not null AND kysymys_query.vastaustyyppi = 'alasvetovalikko'
            THEN 246  -- Suomi/Kerimäki
        WHEN numerovalinta is not null
            THEN ceil(random() * 4)  -- 1-4 pitäisi olla aina turvallinen
        END,
    vaihtoehto = CASE WHEN vaihtoehto is not null THEN (array['kylla', 'ei'])[ceil(random() * 2)] END, -- rikkooko tämä jotain?
    en_osaa_sanoa = false,
    vapaateksti = CASE WHEN vapaateksti is not null THEN 'vapaateksti oletusvastaus' || random()::text END
FROM vastaaja_query, kysymys_query
WHERE v.vastaajaid = vastaaja_query.vastaajaid AND v.kysymysid = kysymys_query.kysymysid;
ALTER TABLE vastaus ENABLE TRIGGER vastaus_update;

--
-- nippu anonymisointi
--
SELECT now();
CREATE OR REPLACE FUNCTION make_unique_nippu_tunnus() RETURNS text AS $$
DECLARE
    unique_tunnus text;
    done bool;
BEGIN
    done := false;
    WHILE NOT done LOOP
            unique_tunnus := array_to_string(ARRAY(SELECT chr((65 + round(random() * 25)) :: integer) FROM generate_series(1,6)), '');
            done := NOT exists(SELECT 1 FROM nippu WHERE tunniste=unique_tunnus);
        END LOOP;
    RETURN unique_tunnus;
END;
$$ LANGUAGE PLPGSQL VOLATILE;
WITH query AS (UPDATE vastaajatunnus vt1
    SET metatiedot = vt1.metatiedot || jsonb_build_object('nippu', make_unique_nippu_tunnus())
    FROM vastaajatunnus vt2
    WHERE vt1.vastaajatunnusid = vt2.vastaajatunnusid
    RETURNING vt1.metatiedot->>'nippu' as new_nippu, vt2.metatiedot->>'nippu' as old_nippu)
UPDATE nippu
SET tunniste = query.new_nippu,
    taustatiedot = null
FROM query
WHERE tunniste = query.old_nippu;

--
-- arvo oikeudet
--
SELECT NOW();
CREATE OR REPLACE FUNCTION grant_all_table_access(_permission text, _user text)
    RETURNS void AS
$func$
BEGIN
    EXECUTE format('GRANT %s ON ALL TABLES IN SCHEMA PUBLIC TO %I', _permission, _user);
END
$func$ LANGUAGE plpgsql;

DO $$
    BEGIN
        PERFORM grant_all_table_access('SELECT,INSERT,UPDATE,DELETE', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvo_user', 'arvo_snap_user', 'arvo_test_user') LIMIT 1;
    END
$$;

CREATE OR REPLACE FUNCTION grant_all_sequence_access(_permission text, _user text)
    RETURNS void AS
$func$
BEGIN
    EXECUTE format('GRANT %s ON ALL SEQUENCES IN SCHEMA PUBLIC TO %I', _permission, _user);
END
$func$ LANGUAGE plpgsql;

DO $$
    BEGIN
        PERFORM grant_all_sequence_access('SELECT,USAGE', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvo_user', 'arvo_snap_user', 'arvo_test_user') LIMIT 1;
    END
$$;

--
-- arvovastaus oikeudet
--
-- SELECT
do $$
    begin
        PERFORM grant_table_access('SELECT', 'koodi', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'vastaajatunnus', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'nippu', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'kyselykerta', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'kysely', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'oppilaitos', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'tutkinto', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'kysymysryhma', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'kysely_kysymysryhma', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'kysymys', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'kysymys_jatkokysymys', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'kysely_kysymys', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'monivalintavaihtoehto', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT', 'koulutustoimija', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
-- INSERT
do $$
    begin
        PERFORM grant_table_access('SELECT,INSERT', 'vastaaja', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT,USAGE', 'vastaaja_vastaajaid_seq', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('INSERT', 'vastaus', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
do $$
    begin
        PERFORM grant_table_access('SELECT,USAGE', 'vastaus_vastausid_seq', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;

--
-- materialized view oikeudet
--
CREATE OR REPLACE FUNCTION grant_materialized_view_owner(_materialized_view text, _user text)
    RETURNS void AS
$func$
BEGIN
    EXECUTE format('ALTER MATERIALIZED VIEW %s OWNER TO %I', _materialized_view, _user);
END
$func$ LANGUAGE plpgsql;

DO $$
    BEGIN
        PERFORM grant_materialized_view_owner('kysymysryhma_taustakysymysryhma_view', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvo_user', 'arvo_snap_user', 'arvo_test_user') LIMIT 1;
    END
$$;
