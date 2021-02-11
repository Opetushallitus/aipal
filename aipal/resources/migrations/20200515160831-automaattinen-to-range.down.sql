ALTER TABLE kyselykerta ALTER COLUMN automaattinen SET DATA TYPE boolean USING CASE WHEN automaattinen IS NULL THEN FALSE ELSE TRUE END;
--;;
ALTER TABLE kyselykerta ALTER COLUMN automaattinen SET DEFAULT FALSE;
--;;
ALTER TABLE automaattikysely DROP COLUMN automatisointi_voimassa_alkupvm;
