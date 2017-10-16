ALTER TABLE kysymys ADD COLUMN jatkokysymys BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE kysymys_jatkokysymys(
  kysymysid INTEGER NOT NULL REFERENCES kysymys(kysymysid),
  jatkokysymysid INTEGER NOT NULL REFERENCES kysymys(kysymysid),
  vastaus VARCHAR(20) NOT NULL
);

ALTER TABLE kysymys ADD COLUMN rajoite TEXT;