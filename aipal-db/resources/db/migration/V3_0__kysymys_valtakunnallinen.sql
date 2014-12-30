CREATE OR REPLACE FUNCTION valtakunnallinen(kysymys)
  RETURNS BOOLEAN AS
$$
    SELECT
      kysymysryhma.valtakunnallinen
    FROM kysymysryhma
    where kysymysryhma.kysymysryhmaid = $1.kysymysryhmaid;
$$ LANGUAGE SQL STABLE;
