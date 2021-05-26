CREATE OR REPLACE FUNCTION vastaajatunnus_update_trigger() RETURNS trigger AS $vastaajatunnus_update_trigger$
    BEGIN
        IF OLD.kohteiden_lkm = 1 AND NEW.kohteiden_lkm != 1 THEN
            RAISE EXCEPTION 'Henkilökohtaista tunnusta ei voi muuttaa ryhmätunnukseksi';
        END IF;
        IF OLD.kohteiden_lkm != 1 AND NEW.kohteiden_lkm = 1 THEN
            RAISE EXCEPTION 'Ryhmätunnusta ei voi muuttaa henkilökohtaiseksi tunnukseksi';
        END IF;
        RETURN NEW;
    END;
$vastaajatunnus_update_trigger$ LANGUAGE plpgsql;
--;;
CREATE TRIGGER vastaajatunnus_update_trigger BEFORE UPDATE ON vastaajatunnus
    FOR EACH ROW EXECUTE PROCEDURE vastaajatunnus_update_trigger();
--;;
ALTER TABLE vastaaja ADD COLUMN tyyppi text;
--;;
WITH query AS (select v.vastaajatunnusid from vastaaja v join vastaajatunnus vt on v.vastaajatunnusid = vt.vastaajatunnusid where vt.kohteiden_lkm = 1 group by v.vastaajatunnusid having count(v.vastaajatunnusid) = 1)
UPDATE vastaaja v
SET tyyppi = 'henkilokohtainen'
FROM query
WHERE query.vastaajatunnusid = v.vastaajatunnusid;
--;;
CREATE UNIQUE INDEX vastaaja_tyyppi_constraint ON vastaaja (vastaajatunnusid) WHERE tyyppi = 'henkilokohtainen';
