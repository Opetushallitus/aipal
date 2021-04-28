ALTER TABLE vastaus ADD COLUMN vastausaika DATE;
--;;
ALTER TABLE vastaus DISABLE TRIGGER vastaus_update;
--;;
-- NOTE: This is quite slow with production sized data ~30M rows
UPDATE vastaus SET vastausaika = luotuaika;
--;;
ALTER TABLE vastaus ENABLE TRIGGER vastaus_update;
--;;
DROP INDEX vastaaja_vastausaika_idx;
--;;
ALTER TABLE vastaaja DROP COLUMN vastausaika;
