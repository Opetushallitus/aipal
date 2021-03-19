DROP TABLE kayttajarooli CASCADE;
--;;
DELETE FROM rooli_organisaatio WHERE kayttaja NOT IN ('JARJESTELMA', 'INTEGRAATIO', 'KONVERSIO', 'VASTAAJA');
--;;
ALTER TABLE rooli_organisaatio RENAME COLUMN rooli TO kayttooikeus;
--;;
ALTER TABLE kayttaja DISABLE TRIGGER kayttaja_update;
--;;
UPDATE kayttaja SET muutettuaika = luotuaika;
--;;
ALTER TABLE kayttaja ENABLE TRIGGER kayttaja_update;

