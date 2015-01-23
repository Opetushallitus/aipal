ALTER TABLE koulutustoimija_ja_tutkinto ADD COLUMN voimassa_alkupvm DATE;
ALTER TABLE koulutustoimija_ja_tutkinto ADD COLUMN voimassa_loppupvm DATE;

COMMENT ON COLUMN koulutustoimija_ja_tutkinto.voimassa_alkupvm IS 'Järjestämissopimuksen alkupvm';
COMMENT ON COLUMN koulutustoimija_ja_tutkinto.voimassa_loppupvm IS 'Järjestämissopimuksen loppupvm';

