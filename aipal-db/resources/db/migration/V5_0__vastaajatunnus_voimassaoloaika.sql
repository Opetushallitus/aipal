alter table vastaajatunnus
  add column voimassa_alkupvm date,
  add column voimassa_loppupvm date;

CREATE OR REPLACE FUNCTION kaytettavissa(vastaajatunnus)
  RETURNS BOOLEAN AS
$$
    SELECT
      CASE WHEN
        $1.lukittu = false and kyselykerta.kaytettavissa = true
        and ($1.voimassa_alkupvm is null or $1.voimassa_alkupvm <= current_date)
        and ($1.voimassa_loppupvm is null or $1.voimassa_loppupvm >= current_date)
      THEN true ELSE false END
    FROM kyselykerta
    where kyselykerta.kyselykertaid = $1.kyselykertaid;
$$ LANGUAGE SQL STABLE;

COMMENT ON FUNCTION kaytettavissa(vastaajatunnus)
IS 'Funktio joka kertoo onko vastaajatunnus käytettävissä, eli vastaajatunnus on voimassa ja ei ole lukittu sekä kyselykerta on käytettävissä';
