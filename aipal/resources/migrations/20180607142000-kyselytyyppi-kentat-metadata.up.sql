ALTER TABLE kyselytyyppi_kentat ADD COLUMN raportointi JSONB;
--;;
UPDATE kyselytyyppi_kentat SET raportointi = '{"csv": true}'
WHERE kyselytyyppi_id = 1 AND kentta_id IN ('kieli','koulutusmuoto', 'tutkinto', 'kunta', 'toimipaikka');
--;;
UPDATE kyselytyyppi_kentat SET raportointi = '{"csv": true}'
WHERE kyselytyyppi_id = 2 AND kentta_id IN ('eppn','henkilonumero', 'haun_numero');
--;;
UPDATE kyselytyyppi_kentat SET raportointi = '{"csv": true}'
WHERE kyselytyyppi_id = 3 AND kentta_id = 'sukupuoli';
--;;
-- Lisätään puuttuvat taustatiedot amispalautteellse
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi, kentta_sv, kentta_en, raportointi)
    VALUES (5, 'tutkinto', 'Tutkinto', 'Examen', 'Degree', '{"csv": true}');
--;;
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi, kentta_sv, kentta_en, raportointi)
VALUES (5, 'kieli', 'Suorituskieli', 'Examensspråk', 'Language of degree', '{"csv": true}');
--;;
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi, kentta_sv, kentta_en, raportointi)
VALUES (5, 'toimipaikka', 'Toimipaikka', 'Verksamhetsställe', 'Operational unit', '{"csv": true}');
--;;
DELETE FROM kyselytyyppi_kentat WHERE kentta_id = 'tutkintomuoto';
--;;
UPDATE kyselytyyppi_kentat SET raportointi = '{"csv": true}'
WHERE kyselytyyppi_id = 5 AND kentta_id = 'hankintakoulutuksen_toteuttaja';
--;;
UPDATE kyselytyyppi_kentat SET raportointi = '{"csv": true}'
WHERE kyselytyyppi_id = 6 AND kentta_id IN ('kieli','tutkinto', 'kunta', 'toimipaikka');
