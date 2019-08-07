DROP TABLE kyselytyyppi CASCADE;
CREATE TABLE kyselytyyppi(id INTEGER NOT NULL PRIMARY KEY,
                          nimi_fi TEXT,
                          nimi_sv TEXT,
                          nimi_en TEXT
);

INSERT INTO kyselytyyppi (id, nimi_fi) VALUES
(1, 'AVOP'),
(2, 'rekrykysely'),
(3, 'YO-uraseuranta'),
(4, 'Itsearviointi'),
(5, 'AMIS-palaute'),
(6, 'Kandipalaute'),
(7, 'AMK-uraseuranta');

ALTER TABLE kysely ADD COLUMN tyyppi_uusi INTEGER;

UPDATE kysely SET tyyppi_uusi = CASE tyyppi
                                    WHEN 'avop' THEN 1
                                    WHEN 'rekrykysely' THEN 2
                                    WHEN 'yo-uraseuranta' THEN 3
                                    WHEN 'itsearviointi' THEN 4
                                    WHEN 'amispalaute'THEN 5
                                    WHEN 'kandipalaute' THEN 6
                                    WHEN 'amk-uraseuranta' THEN 7 END;


ALTER TABLE kysely DROP COLUMN tyyppi;
ALTER TABLE kysely RENAME COLUMN tyyppi_uusi TO tyyppi;
ALTER TABLE kysely ADD CONSTRAINT kysely_tyyppi_fkey FOREIGN KEY (tyyppi) REFERENCES kyselytyyppi (id);

ALTER TABLE kyselypohja ADD COLUMN tyyppi_uusi INTEGER REFERENCES kyselytyyppi(id);

UPDATE kyselypohja SET tyyppi_uusi = CASE tyyppi
                                         WHEN 'avop' THEN 1
                                         WHEN 'rekrykysely' THEN 2
                                         WHEN 'yo-uraseuranta' THEN 3
                                         WHEN 'itsearviointi' THEN 4
                                         WHEN 'amispalaute'THEN 5
                                         WHEN 'kandipalaute' THEN 6
                                         WHEN 'amk-uraseuranta' THEN 7 END;

ALTER TABLE kyselypohja DROP COLUMN tyyppi;
ALTER TABLE kyselypohja RENAME COLUMN tyyppi_uusi TO tyyppi;
ALTER TABLE kysely ADD CONSTRAINT kyselypohja_tyyppi_fkey FOREIGN KEY (tyyppi) REFERENCES kyselytyyppi (id);

ALTER TABLE kyselytyyppi_kentat ADD COLUMN kyselytyyppi_id INTEGER REFERENCES kyselytyyppi(id);

UPDATE kyselytyyppi_kentat SET kyselytyyppi_id = CASE kyselytyyppi
                                                  WHEN 'avop' THEN 1
                                                  WHEN 'rekrykysely' THEN 2
                                                  WHEN 'yo-uraseuranta' THEN 3
                                                  WHEN 'itsearviointi' THEN 4
                                                  WHEN 'amispalaute'THEN 5
                                                  WHEN 'kandipalaute' THEN 6
                                                  WHEN 'amk-uraseuranta' THEN 7 END;


ALTER TABLE kyselytyyppi_kentat DROP COLUMN kyselytyyppi;