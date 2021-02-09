DROP TABLE kyselytyyppi CASCADE ;
--;;
CREATE TABLE kyselytyyppi(id TEXT NOT NULL PRIMARY KEY,
                          nimi_fi TEXT,
                          nimi_sv TEXT,
                          nimi_en TEXT
);
--;;

INSERT INTO kyselytyyppi (id, nimi_fi) VALUES
('avop', 'AVOP'),
('rekrykysely', 'rekrykysely'),
('yo-uraseuranta', 'YO-uraseuranta'),
('itsearviointi', 'Itsearviointi'),
('amispalaute', 'AMIS-palaute'),
('kandipalaute', 'Kandipalaute'),
('amk-uraseuranta', 'AMK-uraseuranta');
--;;

ALTER TABLE kysely ADD COLUMN tyyppi_uusi TEXT;
--;;

UPDATE kysely SET tyyppi_uusi = CASE tyyppi
                                    WHEN 1 THEN 'avop'
                                    WHEN 2 THEN 'rekrykysely'
                                    WHEN 3 THEN 'yo-uraseuranta'
                                    WHEN 4 THEN 'itsearviointi'
                                    WHEN 5 THEN 'amispalaute'
                                    WHEN 6 THEN 'kandipalaute'
                                    WHEN 7 THEN 'amk-uraseuranta' END;
--;;

ALTER TABLE kysely DROP COLUMN tyyppi;
--;;

ALTER TABLE kysely RENAME COLUMN tyyppi_uusi TO tyyppi;
--;;
ALTER TABLE kysely ADD CONSTRAINT kysely_tyyppi_fkey FOREIGN KEY (tyyppi) REFERENCES kyselytyyppi (id);
--;;

ALTER TABLE kyselytyyppi_kentat ADD COLUMN kyselytyyppi TEXT REFERENCES kyselytyyppi(id);
--;;

UPDATE kyselytyyppi_kentat SET kyselytyyppi = CASE kyselytyyppi_id
                                                  WHEN 1 THEN 'avop'
                                                  WHEN 2 THEN 'rekrykysely'
                                                  WHEN 3 THEN 'yo-uraseuranta'
                                                  WHEN 4 THEN 'itsearviointi'
                                                  WHEN 5 THEN 'amispalaute'
                                                  WHEN 6 THEN 'kandipalaute'
                                                  WHEN 7 THEN 'amk-uraseuranta' END;
--;;

ALTER TABLE kyselytyyppi_kentat DROP COLUMN kyselytyyppi_id;
