-- funktiot joilla saadaan suoraan selectissä valittua tieto siitä onko jatkokysymyksessä kyllä tai ei kysymys
-- emuloivat virtuaalisarakkeita
CREATE OR REPLACE FUNCTION kylla_kysymys(jatkokysymys)
  RETURNS boolean AS
$$
    SELECT CASE WHEN $1.kylla_teksti_fi IS NOT NULL OR $1.kylla_teksti_sv IS NOT NULL THEN true ELSE false END;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION ei_kysymys(jatkokysymys)
  RETURNS boolean AS
$$
    SELECT CASE WHEN $1.ei_teksti_fi IS NOT NULL OR $1.ei_teksti_sv IS NOT NULL THEN true ELSE false END;
$$ LANGUAGE SQL STABLE;