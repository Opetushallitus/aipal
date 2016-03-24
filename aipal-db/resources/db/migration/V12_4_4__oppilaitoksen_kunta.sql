DO
$$
BEGIN
  IF NOT EXISTS (SELECT column_name FROM information_schema.columns
    WHERE table_schema='public' and table_name='toimipaikka' and column_name='kunta')
  THEN
     ALTER TABLE toimipaikka ADD COLUMN kunta VARCHAR(3);
     ALTER TABLE vastaajatunnus ALTER COLUMN kunta TYPE VARCHAR(3) USING kunta::VARCHAR(3);
  ELSE
    raise NOTICE 'Already exists';
  END IF;
END
$$


