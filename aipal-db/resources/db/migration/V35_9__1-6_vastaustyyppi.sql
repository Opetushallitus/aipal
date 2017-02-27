ALTER TABLE kysymys
  DROP CONSTRAINT kysymys_vastaustyyppi_check,
  ADD CONSTRAINT kysymys_vastaustyyppi_check
CHECK (vastaustyyppi IN ('arvosana', 'arvosana6', 'arvosana7', 'asteikko', 'kylla_ei_valinta', 'likert_asteikko', 'monivalinta', 'vapaateksti', 'arvosana4_ja_eos', 'arvosana6_ja_eos'));
