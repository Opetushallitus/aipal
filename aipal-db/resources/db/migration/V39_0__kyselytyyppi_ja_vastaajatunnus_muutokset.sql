CREATE TABLE kyselytyyppi(
  id SERIAL PRIMARY KEY NOT NULL,
  nimi_fi VARCHAR(100) NOT NULL,
  nimi_sv VARCHAR(100),
  nimi_en VARCHAR(100)
);

INSERT INTO kyselytyyppi(id, nimi_fi) VALUES (1,  'Palautekysely');
INSERT INTO kyselytyyppi(id, nimi_fi) VALUES (2, 'Rekrykysely');

ALTER TABLE kysely ADD COLUMN tyyppi INTEGER NOT NULL REFERENCES kyselytyyppi(id) DEFAULT 1;

SELECT setval('kyselytyyppi_id_seq', (SELECT MAX(id) FROM kyselytyyppi));

CREATE TABLE kyselytyyppi_kentat(
  id SERIAL PRIMARY KEY NOT NULL,
  kyselytyyppi_id INTEGER NOT NULL REFERENCES kyselytyyppi(id),
  kentta_id CHARACTER VARYING(50) NOT NULL,
  kentta_fi VARCHAR(100) NOT NULL,
  kentta_sv VARCHAR(100),
  kentta_en VARCHAR(100)
);

INSERT INTO kyselytyyppi_kentat(kyselytyyppi_id, kentta_id, kentta_fi)VALUES (1, 'kieli', 'Suorituskieli');
INSERT INTO kyselytyyppi_kentat(kyselytyyppi_id, kentta_id, kentta_fi)VALUES (1, 'tutkinto', 'Tutkinto');
INSERT INTO kyselytyyppi_kentat(kyselytyyppi_id, kentta_id, kentta_fi)VALUES (1, 'koulutusmuoto', 'Koulutusmuoto');
INSERT INTO kyselytyyppi_kentat(kyselytyyppi_id, kentta_id, kentta_fi)VALUES (1, 'toimipaikka', 'Toimipaikka');
INSERT INTO kyselytyyppi_kentat(kyselytyyppi_id, kentta_id, kentta_fi)VALUES (1, 'kunta', 'Kunta');

INSERT INTO kyselytyyppi_kentat(kyselytyyppi_id, kentta_id, kentta_fi)VALUES (2, 'henkilonumero', 'Henkilönumero');
INSERT INTO kyselytyyppi_kentat(kyselytyyppi_id, kentta_id, kentta_fi)VALUES (2, 'haun_numero', 'Haun numero');

CREATE TABLE vastaajatunnus_tiedot
(
  vastaajatunnus_id INTEGER NOT NULL REFERENCES vastaajatunnus(vastaajatunnusid),
  kentta INTEGER NOT NULL REFERENCES kyselytyyppi_kentat(id),
  arvo CHARACTER VARYING(100)
);