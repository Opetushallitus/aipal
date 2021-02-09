-- ALTER TABLE vastaajatunnus ALTER COLUMN kohteiden_lkm DROP NOT NULL;
--;;

DELETE FROM kyselytyyppi_kentat WHERE kyselytyyppi = 'move';
--;;
DELETE FROM kyselytyyppi WHERE id = 'move';
--;;

ALTER TABLE vastaus ALTER COLUMN numerovalinta TYPE INTEGER;
--;;

ALTER TABLE kysymys_jatkokysymys DROP COLUMN nakyvissa;
--;;

ALTER TABLE vastaaja ADD COLUMN vastannut BOOLEAN DEFAULT TRUE;
--;;
-- ALTER TABLE kysymysryhma ADD COLUMN oppilaitos TEXT REFERENCES oppilaitos(oppilaitoskoodi);
--;;
ALTER TABLE kysymysryhma ADD COLUMN voimassa_alkupvm DATE;
--;;
ALTER TABLE kysymysryhma ADD COLUMN voimassa_loppupvm DATE;
--;;
-- ALTER TABLE kysymysryhma ADD COLUMN ntm_kysymykset BOOLEAN DEFAULT FALSE;
--;;

DROP TABLE email_log;
--;;

CREATE OR REPLACE FUNCTION jakauma_sfunc(jakauma INTEGER[], arvo INTEGER) RETURNS INTEGER[] AS $$
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

CREATE OR REPLACE FUNCTION jakauma_ffunc(jakauma INTEGER[]) RETURNS INTEGER[] AS $$
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

DROP AGGREGATE IF EXISTS jakauma(INTEGER);
--;;
CREATE AGGREGATE jakauma(INTEGER) (
    SFUNC = jakauma_sfunc,
    FINALFUNC = jakauma_ffunc,
    STYPE = INTEGER[]
    );
