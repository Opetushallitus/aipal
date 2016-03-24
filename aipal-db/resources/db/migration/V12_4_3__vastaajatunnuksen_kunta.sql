DO
$$
BEGIN
  IF NOT EXISTS (SELECT column_name FROM information_schema.columns
    WHERE table_schema='public' and table_name='vastaajatunnus' and column_name='kunta')
  THEN
    ALTER TABLE vastaajatunnus ADD COLUMN kunta VARCHAR(255);
  ELSE
    raise NOTICE 'Already exists';
  END IF;
END
$$
