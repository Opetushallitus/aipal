ALTER TABLE kyselypohja RENAME COLUMN kategoria TO metatiedot;
ALTER TABLE kysely RENAME COLUMN kategoria TO metatiedot;
ALTER TABLE kyselykerta RENAME COLUMN kategoria TO metatiedot;
ALTER TABLE kysymysryhma RENAME COLUMN kategoria TO metatiedot;
ALTER TABLE kysymys RENAME COLUMN kategoria TO metatiedot;
ALTER TABLE vastaajatunnus ADD COLUMN metatiedot JSONB;

ALTER TABLE automaattikysely RENAME COLUMN kysely_kategoria TO kysely_metatiedot;