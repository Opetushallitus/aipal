ALTER TABLE kyselykerta ALTER COLUMN automaattinen DROP DEFAULT;
--;;
ALTER TABLE kyselykerta ALTER COLUMN automaattinen SET DATA TYPE daterange USING CASE WHEN automaattinen IS TRUE THEN '[,]'::daterange ELSE NULL END;
--;;
ALTER TABLE automaattikysely ADD COLUMN automatisointi_voimassa_alkupvm DATE;
