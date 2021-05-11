ALTER TABLE toimipiste RENAME TO toimipaikka;
--;;
ALTER TABLE toimipaikka RENAME COLUMN toimipistekoodi TO toimipaikkakoodi;
--;;
UPDATE vastaajatunnus SET taustatiedot = taustatiedot || jsonb_build_object('toimipaikka', taustatiedot->>'toimipiste') WHERE taustatiedot->'toimipiste' IS NOT NULL;
--;;
UPDATE vastaajatunnus SET taustatiedot = taustatiedot - 'toimipiste' WHERE taustatiedot->'toimipiste' IS NOT NULL;
--;;
UPDATE kyselytyyppi_kentat SET kentta_fi = 'Toimipaikka', kentta_id = 'toimipaikka' WHERE kentta_id = 'toimipiste';
