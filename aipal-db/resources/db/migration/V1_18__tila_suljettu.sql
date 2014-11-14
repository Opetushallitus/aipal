-- https://issues.solita.fi/browse/OPH-1032
INSERT INTO tila_enum (nimi) VALUES ('suljettu');

UPDATE kysymysryhma SET tila='suljettu' WHERE tila='poistettu';
UPDATE kyselypohja SET tila='suljettu' WHERE tila='poistettu';
UPDATE kysely SET tila='suljettu' WHERE tila='poistettu';

DELETE FROM tila_enum WHERE nimi='poistettu';
