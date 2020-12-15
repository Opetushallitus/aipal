ALTER TABLE kysymysryhma ADD COLUMN ntm_kysymykset BOOLEAN;

ALTER TABLE kysymysryhma DROP COLUMN kategoria;

ALTER TABLE automaattikysely DROP COLUMN voimassa_loppupvm;
ALTER TABLE automaattikysely DROP COLUMN kysely_kategoria;