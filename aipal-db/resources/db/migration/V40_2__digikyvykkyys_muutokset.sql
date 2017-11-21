INSERT INTO kyselytyyppi(nimi_fi) VALUES ('Itsearviointi');

ALTER TABLE kysymys ADD COLUMN selite_fi TEXT;
ALTER TABLE kysymys ADD COLUMN selite_sv TEXT;
ALTER TABLE kysymys ADD COLUMN selite_en TEXT;

ALTER TABLE kysymys DROP CONSTRAINT kysymys_vastaustyyppi_check;
