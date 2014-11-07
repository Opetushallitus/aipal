-- poistetaan vastaajatunnukselta alku- ja loppupäivät, koska rajoitus pitäisi tulla kyselykerralta
alter table vastaajatunnus
  drop CONSTRAINT alkupvm_ennen_loppupvm,
  drop column voimassa_alkupvm,
  drop column voimassa_loppupvm;

alter table kyselypohja
  ADD COLUMN tila VARCHAR(20) default 'luonnos',
  ADD CONSTRAINT kyselypohja_tila_enum_FK FOREIGN KEY ( tila ) REFERENCES tila_enum ( nimi ) ;

alter table kysymysryhma
  ADD COLUMN tila VARCHAR(20) default 'luonnos',
  ADD CONSTRAINT kysymysryhma_tila_enum_FK FOREIGN KEY ( tila ) REFERENCES tila_enum ( nimi ) ;

-- kyselylle oma kaytettavissa funktio
CREATE OR REPLACE FUNCTION kaytettavissa(kysely)
  RETURNS BOOLEAN AS
$$
    SELECT
      CASE WHEN
        $1.tila = 'julkaistu'
        and ($1.voimassa_alkupvm is null or $1.voimassa_alkupvm <= current_date)
        and ($1.voimassa_loppupvm is null or $1.voimassa_loppupvm >= current_date)
      THEN true ELSE false END;
$$ LANGUAGE SQL STABLE;

COMMENT ON FUNCTION kaytettavissa(kyselykerta)
IS 'Funktio joka kertoo onko kysely käytettävissä, eli kysely on voimassa ja julkaistu.';

-- kaytettavissa huomioimaan päiväykset
CREATE OR REPLACE FUNCTION kaytettavissa(kyselykerta)
  RETURNS BOOLEAN AS
$$
    SELECT
      CASE WHEN
        $1.lukittu = false and kysely.kaytettavissa = true
        and ($1.voimassa_alkupvm is null or $1.voimassa_alkupvm <= current_date)
        and ($1.voimassa_loppupvm is null or $1.voimassa_loppupvm >= current_date)
      THEN true ELSE false END
    FROM kysely
    where kysely.kyselyid = $1.kyselyid;
$$ LANGUAGE SQL STABLE;

COMMENT ON FUNCTION kaytettavissa(kyselykerta)
IS 'Funktio joka kertoo onko kyselykerta käytettävissä, eli kyselykerta ei ole lukittu ja on voimassa sekä kysely on julkaistu';

-- kysymysryhmalle lisattavissa funktio
CREATE OR REPLACE FUNCTION lisattavissa(kysymysryhma)
  RETURNS BOOLEAN AS
$$
    SELECT
      CASE WHEN
        $1.tila = 'julkaistu'
        and ($1.voimassa_alkupvm is null or $1.voimassa_alkupvm <= current_date)
        and ($1.voimassa_loppupvm is null or $1.voimassa_loppupvm >= current_date)
      THEN true ELSE false END;
$$ LANGUAGE SQL STABLE;

COMMENT ON FUNCTION kaytettavissa(kyselykerta)
IS 'Funktio joka kertoo onko kysymysryhma lisättävissä, eli kysymysryhmä on voimassa ja julkaistu.';

-- kyselypohjalle kaytettavissa funktio
CREATE OR REPLACE FUNCTION kaytettavissa(kyselypohja)
  RETURNS BOOLEAN AS
$$
    SELECT
      CASE WHEN
        $1.tila = 'julkaistu'
        and ($1.voimassa_alkupvm is null or $1.voimassa_alkupvm <= current_date)
        and ($1.voimassa_loppupvm is null or $1.voimassa_loppupvm >= current_date)
      THEN true ELSE false END;
$$ LANGUAGE SQL STABLE;

COMMENT ON FUNCTION kaytettavissa(kyselykerta)
IS 'Funktio joka kertoo onko kyselypohja käytettävissä, eli kyselypohja on voimassa ja julkaistu.';
