ALTER TABLE kysymys DROP CONSTRAINT kysymys_vastaustyyppi_check ;
ALTER TABLE kysymys ADD CONSTRAINT kysymys_vastaustyyppi_check
  CHECK ( vastaustyyppi IN ('asteikko', 'kylla_ei_valinta', 'likert_asteikko', 'monivalinta', 'vapaateksti')) ;

COMMENT ON COLUMN kysymys.vastaustyyppi
IS
  'Vastauksen tyyppi (kylla_ei_valinta, asteikko, likert_asteikko, monivalinta, vapaateksti)' ;

COMMENT ON COLUMN vastaus.numerovalinta
IS
  'vastausvalinta (asteikko, likert_asteikko tai monivalinta)' ;
