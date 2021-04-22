ALTER TABLE toimipaikka RENAME TO toimipiste;
--;;
ALTER TABLE toimipiste RENAME COLUMN toimipaikkakoodi TO toimipistekoodi;
--;;
UPDATE vastaajatunnus SET taustatiedot = taustatiedot || jsonb_build_object('toimipiste', taustatiedot->>'toimipaikka') WHERE taustatiedot->'toimipaikka' IS NOT NULL;
--;;
UPDATE vastaajatunnus SET taustatiedot = taustatiedot - 'toimipaikka' WHERE taustatiedot->'toimipaikka' IS NOT NULL;
--;;
UPDATE kyselytyyppi_kentat SET kentta_fi = 'Toimipiste', kentta_id = 'toimipiste' WHERE kentta_id = 'toimipaikka';
