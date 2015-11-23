ALTER TABLE vastaajatunnus ADD COLUMN valmistavan_koulutuksen_toimipaikka VARCHAR(10)
  REFERENCES toimipaikka(toimipaikkakoodi);
