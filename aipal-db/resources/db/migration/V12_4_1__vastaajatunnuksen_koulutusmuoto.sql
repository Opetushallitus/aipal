
DO
$$
BEGIN
  IF NOT EXISTS (SELECT column_name
    FROM information_schema.columns
    WHERE table_schema='public' and table_name='vastaajatunnus' and column_name='koulutusmuoto')
  THEN
    ALTER TABLE vastaajatunnus ADD COLUMN koulutusmuoto VARCHAR(255);
  ELSE
    raise NOTICE 'Already exists';
  END IF;
END
$$
