ALTER TABLE kyselykerta DROP COLUMN IF EXISTS ntm_kysymykset;
ALTER TABLE vastaus DROP COLUMN IF EXISTS jatkovastausid;
ALTER TABLE kysymys DROP COLUMN IF EXISTS jatkokysymysid;

DROP TABLE jatkovastaus;

DROP FUNCTION ei_kysymys(jatkokysymys);
DROP FUNCTION kylla_kysymys(jatkokysymys);

DROP TABLE jatkokysymys;

DROP TABLE kieli;

DROP MATERIALIZED VIEW kysymys_vastaaja_view;

ALTER TABLE kyselykerta
ALTER COLUMN kategoria SET DATA TYPE jsonb
USING kategoria::jsonb;