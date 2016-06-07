DELETE FROM oppilaitostyyppi_tutkintotyyppi;

ALTER TABLE oppilaitostyyppi_tutkintotyyppi ADD COLUMN muutettu_kayttaja varchar(80) NOT NULL REFERENCES kayttaja(oid);
ALTER TABLE oppilaitostyyppi_tutkintotyyppi ADD COLUMN luotu_kayttaja varchar(80) NOT NULL REFERENCES kayttaja(oid);
ALTER TABLE oppilaitostyyppi_tutkintotyyppi ADD COLUMN muutettuaika timestamptz NOT NULL;
ALTER TABLE oppilaitostyyppi_tutkintotyyppi ADD COLUMN luotuaika timestamptz NOT NULL;

CREATE TRIGGER oppilaitostyyppi_tutkintotyyppi_update BEFORE UPDATE ON oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE update_stamp();
CREATE TRIGGER oppilaitostyyppi_tutkintotyyppil_insert BEFORE INSERT ON oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE update_created();
CREATE TRIGGER oppilaitostyyppi_tutkintotyyppim_insert BEFORE INSERT ON oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE update_stamp();
CREATE TRIGGER oppilaitostyyppi_tutkintotyyppi_mu_update BEFORE UPDATE ON oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE update_modifier();
CREATE TRIGGER oppilaitostyyppi_tutkintotyyppi_mu_insert BEFORE INSERT ON oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE update_modifier();
CREATE TRIGGER oppilaitostyyppi_tutkintotyyppi_cu_insert BEFORE INSERT ON oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE update_creator();

--Lisätään uudelleen oletusarvot

-- AMK Tutkintotyypit
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('41', '06');
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('41', '07');
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('41', '12');
-- YO Tutkintotyypit
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('42', '13');
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('42', '14');
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('42', '15');
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('42', '16');
