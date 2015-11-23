ALTER TABLE kysymys
  DROP CONSTRAINT kysymys_vastaustyyppi_check,
  ADD CONSTRAINT kysymys_vastaustyyppi_check
  CHECK (vastaustyyppi IN ('arvosana', 'arvosana7', 'asteikko', 'kylla_ei_valinta', 'likert_asteikko', 'monivalinta', 'vapaateksti'));