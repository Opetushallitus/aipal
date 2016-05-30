ALTER TABLE tutkintotyyppi
  ADD COLUMN nimi_fi VARCHAR(200),
  ADD COLUMN nimi_sv VARCHAR(200),
  ADD COLUMN nimi_en VARCHAR(200);

ALTER TABLE tutkinto DROP CONSTRAINT tutkinto_tutkintotyyppi_fkey;
