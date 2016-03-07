DO
$$
BEGIN
  IF NOT EXISTS (SELECT column_name
    FROM information_schema.columns
    WHERE table_schema='public' and table_name='kysely' and column_name='uudelleenohjaus_url')
  THEN
    ALTER TABLE kysely ADD COLUMN uudelleenohjaus_url varchar(2000);
  ELSE
    raise NOTICE 'Already exists';
  END IF;
END
$$
