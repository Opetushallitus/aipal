ALTER TABLE kyselypohja RENAME COLUMN metatiedot TO kategoria;
ALTER TABLE kysely RENAME COLUMN metatiedot TO kategoria;
ALTER TABLE kyselykerta RENAME COLUMN metatiedot TO kategoria;
ALTER TABLE kysymysryhma RENAME COLUMN metatiedot TO kategoria;
ALTER TABLE kysymys RENAME COLUMN metatiedot TO kategoria;
ALTER TABLE vastaajatunnus DROP COLUMN metatiedot;

ALTER TABLE automaattikysely RENAME COLUMN kysely_metatiedot TO kysely_kategoria;