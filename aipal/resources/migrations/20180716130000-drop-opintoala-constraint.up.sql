ALTER TABLE tutkinto ALTER COLUMN opintoala DROP NOT NULL;
--;;
ALTER TABLE tutkinto DROP CONSTRAINT tutkinto_opintoala_fk;
