ALTER TABLE jatkokysymys
ADD COLUMN kylla_vastaustyyppi CHARACTER VARYING(20) NOT NULL
DEFAULT 'likert_asteikko';

UPDATE jatkokysymys SET kylla_vastaustyyppi = 'asteikko';

ALTER TABLE jatkokysymys
ADD CHECK (kylla_vastaustyyppi IN ('asteikko', 'likert_asteikko'));
