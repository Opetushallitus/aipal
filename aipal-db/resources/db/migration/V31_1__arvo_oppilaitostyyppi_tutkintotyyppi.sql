ALTER TABLE oppilaitos ADD COLUMN oppilaitostyyppi VARCHAR(5);

CREATE TABLE oppilaitostyyppi_tutkintotyyppi(
  tutkintotyyppi VARCHAR(5) NOT NULL ,
  oppilaitostyyppi VARCHAR(5) NOT NULL,
  PRIMARY KEY (tutkintotyyppi, oppilaitostyyppi)
);

-- AMK Tutkintotyypit
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('41', '06');
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('41', '07');
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('41', '12');
-- YO Tutkintotyypit
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('42', '13');
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('42', '14');
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('42', '15');
INSERT INTO oppilaitostyyppi_tutkintotyyppi (oppilaitostyyppi, tutkintotyyppi) VALUES ('42', '16');

DELETE FROM organisaatiopalvelu_log;