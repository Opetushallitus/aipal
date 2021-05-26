DROP TRIGGER vastaajatunnus_update_trigger ON vastaajatunnus;
--;;
DROP FUNCTION vastaajatunnus_update_trigger();
--;;
DROP INDEX vastaaja_tyyppi_constraint;
--;;
ALTER TABLE vastaaja DROP COLUMN tyyppi;
