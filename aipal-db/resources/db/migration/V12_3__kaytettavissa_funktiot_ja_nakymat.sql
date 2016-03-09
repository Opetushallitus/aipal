CREATE OR REPLACE FUNCTION kaytettavissa(kysely)
  RETURNS BOOLEAN AS
$$
    SELECT
      $1.tila = 'julkaistu'
      AND ($1.voimassa_alkupvm IS NULL OR $1.voimassa_alkupvm <= current_date)
      AND ($1.voimassa_loppupvm IS NULL OR $1.voimassa_loppupvm >= current_date);
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE VIEW kysely_kaytettavissa AS
SELECT kyselyid, tila = 'julkaistu'
                 AND (voimassa_alkupvm IS NULL OR voimassa_alkupvm <= current_date)
                 AND (voimassa_loppupvm IS NULL OR voimassa_loppupvm >= current_date)
                 AS kaytettavissa
from kysely;

CREATE OR REPLACE FUNCTION kaytettavissa(kyselykerta)
  RETURNS BOOLEAN AS
$$
    SELECT
      CASE WHEN
        not $1.lukittu 
        AND ($1.voimassa_alkupvm IS NULL OR $1.voimassa_alkupvm <= current_date)
        AND ($1.voimassa_loppupvm IS NULL OR $1.voimassa_loppupvm >= current_date)
      THEN kysely.kaytettavissa
      ELSE false
      END
    FROM kysely
    where kysely.kyselyid = $1.kyselyid;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE VIEW kyselykerta_kaytettavissa AS
SELECT kyselykertaid, not lukittu
                      AND (voimassa_alkupvm IS NULL OR voimassa_alkupvm <= current_date)
                      AND (voimassa_loppupvm IS NULL OR voimassa_loppupvm >= current_date)
                      AND kysely_kaytettavissa.kaytettavissa
                      AS kaytettavissa
from kyselykerta
join kysely_kaytettavissa on kyselykerta.kyselyid = kysely_kaytettavissa.kyselyid;

CREATE OR REPLACE FUNCTION kaytettavissa(vastaajatunnus)
  RETURNS BOOLEAN AS
$$
    SELECT
      CASE WHEN
        not $1.lukittu
        AND ($1.voimassa_alkupvm IS NULL OR $1.voimassa_alkupvm <= current_date)
        AND ($1.voimassa_loppupvm IS NULL OR $1.voimassa_loppupvm >= current_date)
      THEN kyselykerta.kaytettavissa
      ELSE false
      END
    FROM kyselykerta
    where kyselykerta.kyselykertaid = $1.kyselykertaid;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE VIEW vastaajatunnus_kaytettavissa AS
SELECT vastaajatunnusid, not lukittu
                         AND (voimassa_alkupvm IS NULL OR voimassa_alkupvm <= current_date)
                         AND (voimassa_loppupvm IS NULL OR voimassa_loppupvm >= current_date)
                         AND kyselykerta_kaytettavissa.kaytettavissa
                         AS kaytettavissa
from vastaajatunnus
join kyselykerta_kaytettavissa on vastaajatunnus.kyselykertaid = kyselykerta_kaytettavissa.kyselykertaid;