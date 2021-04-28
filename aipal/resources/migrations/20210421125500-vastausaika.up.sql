ALTER TABLE vastaaja ADD COLUMN vastausaika DATE;
--;;
ALTER TABLE vastaus DISABLE TRIGGER vastaus_update;
--;;
ALTER TABLE vastaaja DISABLE TRIGGER vastaaja_update;
--;;
UPDATE vastaus SET vastausaika = luotuaika WHERE vastausaika IS NULL;
--;;
WITH query AS (SELECT vastausaika, vastaajaid FROM vastaus)
UPDATE vastaaja v
SET vastausaika = query.vastausaika
FROM query
WHERE query.vastaajaid = v.vastaajaid AND query.vastausaika IS NOT NULL;
--;;
-- There are some empty answers so previous query doesn't update those.
UPDATE vastaaja SET vastausaika = luotuaika WHERE vastausaika IS NULL;
--;;
ALTER TABLE vastaus ENABLE TRIGGER vastaus_update;
--;;
ALTER TABLE vastaaja ENABLE TRIGGER vastaaja_update;
--;;
ALTER TABLE vastaus DROP COLUMN vastausaika;
--;;
ALTER TABLE vastaaja ALTER COLUMN vastausaika SET NOT NULL;
--;;
CREATE INDEX IF NOT EXISTS vastaaja_vastausaika_idx ON vastaaja (vastausaika);
