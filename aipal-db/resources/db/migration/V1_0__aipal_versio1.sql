create table kayttajarooli (
    roolitunnus varchar(16) NOT NULL primary key,
    kuvaus varchar(200),
    muutettuaika timestamptz NOT NULL,
    luotuaika timestamptz NOT NULL
);


create table kayttaja(
    oid varchar(80) NOT NULL primary key,
    uid character varying(80),
    etunimi varchar(100) not null,
    sukunimi varchar(100) not null,
    rooli varchar(16) NOT NULL references kayttajarooli(roolitunnus),
    muutettu_kayttaja varchar(80) NOT NULL references kayttaja(oid),
    luotu_kayttaja varchar(80) NOT NULL references kayttaja(oid),
    muutettuaika timestamptz NOT NULL,
    luotuaika timestamptz NOT NULL,
    voimassa boolean not null default(true)
);


insert into kayttajarooli(roolitunnus, kuvaus, muutettuaika, luotuaika)
values ('YLLAPITAJA', 'Ylläpitäjäroolilla on kaikki oikeudet', current_timestamp, current_timestamp);


insert into kayttajarooli(roolitunnus, kuvaus, muutettuaika, luotuaika)
values ('KAYTTAJA', 'Käyttäjäroolin oikeudet riippuvat kontekstisensitiivisistä roolioikeuksista.', current_timestamp, current_timestamp);


insert into kayttaja(oid, uid, etunimi, sukunimi, voimassa, rooli, muutettuaika, luotuaika, luotu_kayttaja, muutettu_kayttaja)
values ('JARJESTELMA', 'JARJESTELMA', 'Järjestelmä', '', true, 'YLLAPITAJA', current_timestamp, current_timestamp, 'JARJESTELMA', 'JARJESTELMA');


CREATE OR REPLACE function update_stamp() returns trigger as $$ begin new.muutettuaika := now(); return new; end; $$ language plpgsql;
CREATE OR REPLACE function update_created() returns trigger as $$ begin new.luotuaika := now(); return new; end; $$ language plpgsql;
create trigger kayttaja_update before update on kayttaja for each row execute procedure update_stamp() ;
create trigger kayttajal_insert before insert on kayttaja for each row execute procedure update_created() ;
create trigger kayttajam_insert before insert on kayttaja for each row execute procedure update_stamp() ;
create trigger kayttajarooli_update before update on kayttajarooli for each row execute procedure update_stamp() ;
create trigger kayttajaroolil_insert before insert on kayttajarooli for each row execute procedure update_created() ;
create trigger kayttajaroolim_insert before insert on kayttajarooli for each row execute procedure update_stamp() ;
CREATE OR REPLACE function update_creator() returns trigger as $$ begin new.luotu_kayttaja := current_setting('aipal.kayttaja'); return new; end; $$ language plpgsql;
CREATE OR REPLACE function update_modifier() returns trigger as $$ begin new.muutettu_kayttaja := current_setting('aipal.kayttaja'); return new; end; $$ language plpgsql;
create trigger kayttaja_mu_update before update on kayttaja for each row execute procedure update_modifier() ;
create trigger kayttaja_cu_insert before insert on kayttaja for each row execute procedure update_creator() ;
create trigger kayttaja_mu_insert before insert on kayttaja for each row execute procedure update_modifier() ;
