DELETE FROM tutkinto WHERE opintoala IS NULL;
--;;
ALTER TABLE tutkinto ALTER COLUMN opintoala SET NOT NULL;
--;;
ALTER TABLE tutkinto
    ADD CONSTRAINT tutkinto_opintoala_fk
    FOREIGN KEY (opintoala)
    REFERENCES opintoala(opintoalatunnus);
