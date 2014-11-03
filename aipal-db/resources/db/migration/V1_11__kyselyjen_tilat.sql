CREATE TABLE tila_enum(
  nimi VARCHAR(20) NOT NULL PRIMARY KEY,
  muutettu_kayttaja varchar(80) NOT NULL references kayttaja(oid),
  luotu_kayttaja varchar(80) NOT NULL references kayttaja(oid),
  muutettuaika timestamp NOT NULL,
  luotuaika timestamp NOT NULL
);
create trigger tila_enum_update before update on tila_enum for each row execute procedure update_stamp() ;
create trigger tila_enuml_insert before insert on tila_enum for each row execute procedure update_created() ;
create trigger tila_enumm_insert before insert on tila_enum for each row execute procedure update_stamp() ;
create trigger tila_enum_mu_update before update on tila_enum for each row execute procedure update_modifier() ;
create trigger tila_enum_cu_insert before insert on tila_enum for each row execute procedure update_creator() ;
create trigger tila_enum_mu_insert before insert on tila_enum for each row execute procedure update_modifier() ;

INSERT INTO tila_enum (nimi)
VALUES ('luonnos'), ('julkaistu'), ('poistettu');

ALTER TABLE kysely ADD COLUMN tila VARCHAR(20) default 'luonnos';
ALTER TABLE kysely ADD CONSTRAINT kysely_tila_enum_FK FOREIGN KEY ( tila ) REFERENCES tila_enum ( nimi ) ;

COMMENT ON COLUMN kysely.tila
IS 'Kyselyn tila. Vain julkaistu tilassa olevan kyselyn kyselykertoihin voi vastata';

ALTER TABLE kyselykerta
  ADD COLUMN lukittu BOOLEAN default false;

COMMENT ON COLUMN kyselykerta.lukittu
IS 'Onko tämä kyselykerta lukittu vai ei. Kenttää ei pidä lukea suoraan vaan voimassaolo pitää tarkastaa kaytettavissa funktion avulla';

COMMENT ON COLUMN vastaajatunnus.lukittu
IS 'Onko tämä vastaajatunnus lukittu vai ei. Kenttää ei pidä lukea suoraan vaan voimassaolo pitää tarkastaa kaytettavissa funktion kautta';

CREATE OR REPLACE FUNCTION kaytettavissa(kyselykerta)
  RETURNS BOOLEAN AS
$$
    SELECT CASE WHEN $1.lukittu = false and kysely.tila = 'julkaistu' THEN true ELSE false END
    FROM kysely
    where kysely.kyselyid = $1.kyselyid;
$$ LANGUAGE SQL STABLE;

COMMENT ON FUNCTION kaytettavissa(kyselykerta)
IS 'Funktio joka kertoo onko kyselykerta käytettävissä, eli kyselykerta ei ole lukittu ja kysely on julkaistu';

CREATE OR REPLACE FUNCTION kaytettavissa(vastaajatunnus)
  RETURNS BOOLEAN AS
$$
    SELECT CASE WHEN $1.lukittu = false and kyselykerta.kaytettavissa = true THEN true ELSE false END
    FROM kyselykerta
    where kyselykerta.kyselykertaid = $1.kyselykertaid;
$$ LANGUAGE SQL STABLE;

COMMENT ON FUNCTION kaytettavissa(vastaajatunnus)
IS 'Funktio joka kertoo onko vastaajatunnus käytettävissä, eli vastaajatunnus ei ole lukittu ja kyselykerta on käytettävissä';
