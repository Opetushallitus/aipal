ALTER TABLE kysymysryhma DROP COLUMN IF EXISTS ntm_kysymykset;
--;;
ALTER TABLE kysymysryhma ADD COLUMN kategoria JSONB;
--;;
ALTER TABLE automaattikysely ADD COLUMN voimassa_loppupvm DATE;
--;;
ALTER TABLE automaattikysely ADD COLUMN kysely_kategoria JSONB;
