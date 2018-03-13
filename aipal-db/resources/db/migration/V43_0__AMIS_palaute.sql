INSERT INTO kyselytyyppi (nimi_fi) VALUES ('AMIS-palaute');

INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi)
    VALUES ((SELECT id FROM kyselytyyppi WHERE nimi_fi = 'AMIS-palaute'), 'hankintakoulutuksen_toteuttaja', 'Hankintakoulutuksen toteuttaja ');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi)
VALUES ((SELECT id FROM kyselytyyppi WHERE nimi_fi = 'AMIS-palaute'), 'tutkintomuoto', 'Tutkintomuoto');

ALTER TABLE kysymys
    DROP CONSTRAINT kysymys_vastaustyyppi_check;