INSERT INTO kyselytyyppi (nimi_fi) VALUES ('Kandipalaute');


UPDATE kysely SET tyyppi = (SELECT id FROM kyselytyyppi WHERE nimi_fi = 'Kandipalaute')
WHERE koulutustoimija IN (SELECT koulutustoimija FROM oppilaitos WHERE oppilaitostyyppi = '42')
AND tyyppi = 1;

