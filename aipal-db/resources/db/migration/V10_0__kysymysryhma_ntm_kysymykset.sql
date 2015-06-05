ALTER TABLE kysymysryhma
  ADD COLUMN ntm_kysymykset BOOLEAN DEFAULT false NOT NULL;

COMMENT ON COLUMN kysymysryhma.ntm_kysymykset
IS
  'Kuuluuko kysymysryhmä NTM-kysymyksiin' ;
