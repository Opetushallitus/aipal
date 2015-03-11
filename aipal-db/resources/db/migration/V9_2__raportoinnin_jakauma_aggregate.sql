CREATE OR REPLACE FUNCTION jakauma_sfunc(jakauma INTEGER[], arvo INTEGER) RETURNS INTEGER[] AS $$
BEGIN
  IF arvo IS NOT NULL THEN
    IF jakauma[arvo] IS NULL THEN
      jakauma[arvo] = 1;
    ELSE
      jakauma[arvo] = jakauma[arvo] + 1;
    END IF;
  END IF;
  RETURN jakauma;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION jakauma_ffunc(jakauma INTEGER[]) RETURNS INTEGER[] AS $$
DECLARE
  i INTEGER;
BEGIN
  FOR i IN SELECT * FROM generate_series(0, array_upper(jakauma, 1)) LOOP
    IF jakauma[i] IS NULL THEN
      jakauma[i] = 0;
    END IF;
  END LOOP;
  RETURN jakauma;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

DROP AGGREGATE IF EXISTS jakauma(INTEGER);
CREATE AGGREGATE jakauma(INTEGER) (
  SFUNC = jakauma_sfunc,
  FINALFUNC = jakauma_ffunc,
  STYPE = INTEGER[]
);
