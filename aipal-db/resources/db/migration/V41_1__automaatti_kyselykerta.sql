ALTER TABLE kyselykerta ADD COLUMN automaattinen BOOLEAN DEFAULT FALSE;

SET aipal.kayttaja = 'JARJESTELMA';

UPDATE kyselykerta SET automaattinen = TRUE WHERE WHERE nimi LIKE 'AUTOMAATTI%2018';

UPDATE kyselykerta SET kategoria = '{"vuosi": 2018}' WHERE nimi LIKE 'AUTOMAATTI%2018';
