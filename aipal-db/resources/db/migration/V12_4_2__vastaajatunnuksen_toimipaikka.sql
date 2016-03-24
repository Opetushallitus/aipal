DO
$$
BEGIN
  IF NOT EXISTS (SELECT column_name
    FROM information_schema.columns
    WHERE table_schema='public' and table_name='vastaajatunnus' and column_name='valmistavan_koulutuksen_toimipaikka')
  THEN
     ALTER TABLE vastaajatunnus ADD COLUMN valmistavan_koulutuksen_toimipaikka VARCHAR(10) REFERENCES toimipaikka(toimipaikkakoodi);
  ELSE
    raise NOTICE 'Already exists';
  END IF;
END
$$
