ALTER TABLE vastaajatunnus ALTER COLUMN kohteiden_lkm DROP NOT NULL;
--;;

INSERT INTO kyselytyyppi(id, nimi_fi, nimi_sv, nimi_en) VALUES ('move','Move!', 'Move!', 'Move!');
--;;

INSERT INTO kyselytyyppi_kentat(kentta_id, kentta_fi, kentta_sv, kentta_en, raportointi, kyselytyyppi)
VALUES ('koulu', 'Koulu','koulu','koulu', '{"vipunen": false}', 'move');
--;;

INSERT INTO kyselytyyppi_kentat(kentta_id, kentta_fi, kentta_sv, kentta_en, raportointi, kyselytyyppi)
VALUES ('kunta', 'Kunta','kunta','kunta', '{"vipunen": false}', 'move');
--;;

INSERT INTO kyselytyyppi_kentat(kentta_id, kentta_fi, kentta_sv, kentta_en, raportointi, kyselytyyppi)
VALUES ('maakunta', 'Maakunta','maakunta','maakunta', '{"vipunen": false}', 'move');
--;;

INSERT INTO kyselytyyppi_kentat(kentta_id, kentta_fi, kentta_sv, kentta_en, raportointi, kyselytyyppi)
VALUES ('maakunta', 'Maakunta','maakunta','maakunta', '{"vipunen": false}', 'move');
--;;

ALTER TABLE vastaus ALTER COLUMN numerovalinta TYPE DECIMAL;
--;;

ALTER TABLE kysymys_jatkokysymys ADD COLUMN nakyvissa BOOLEAN DEFAULT TRUE;
--;;

ALTER TABLE vastaaja DROP COLUMN vastannut;
--;;
-- ALTER TABLE kysymysryhma DROP COLUMN oppilaitos;
--;;
ALTER TABLE kysymysryhma DROP COLUMN voimassa_alkupvm;
--;;
ALTER TABLE kysymysryhma DROP COLUMN voimassa_loppupvm;
--;;
-- ALTER TABLE kysymysryhma DROP COLUMN ntm_kysymykset;
--;;

CREATE TABLE email_log (
    id SERIAL PRIMARY KEY,
    sahkoposti TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    tunniste TEXT NOT NULL,
    taustatiedot JSONB,
    status TEXT
);
--;;

CREATE OR REPLACE FUNCTION jakauma_sfunc(jakauma NUMERIC[], arvo NUMERIC) RETURNS NUMERIC[] AS $$
BEGIN
    IF arvo IS NOT NULL THEN
        IF jakauma[arvo] IS NULL THEN
            jakauma[arvo] = 1;
        ELSE
            jakauma[arvo] = jakauma[arvo] + 1;
        END IF;
    END IF;
    RETURN jakauma;
END;
$$ LANGUAGE plpgsql IMMUTABLE;
--;;

CREATE OR REPLACE FUNCTION jakauma_ffunc(jakauma NUMERIC[]) RETURNS NUMERIC[] AS $$
DECLARE
    i INTEGER;
BEGIN
    FOR i IN SELECT * FROM generate_series(0, array_upper(jakauma, 1)) LOOP
            IF jakauma[i] IS NULL THEN
                jakauma[i] = 0;
            END IF;
        END LOOP;
    RETURN jakauma;
END;
$$ LANGUAGE plpgsql IMMUTABLE;
--;;
DROP AGGREGATE IF EXISTS jakauma(NUMERIC);
--;;
CREATE AGGREGATE jakauma(NUMERIC) (
    SFUNC = jakauma_sfunc,
    FINALFUNC = jakauma_ffunc,
    STYPE = NUMERIC[]
    );
