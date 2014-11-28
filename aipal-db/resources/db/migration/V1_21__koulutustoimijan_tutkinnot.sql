create table koulutustoimija_ja_tutkinto (
  koulutustoimija varchar(10) references koulutustoimija(ytunnus),
  tutkinto varchar(6) references tutkinto(tutkintotunnus),
  muutettu_kayttaja varchar(80) NOT NULL references kayttaja(oid),
  luotu_kayttaja varchar(80) NOT NULL references kayttaja(oid),
  muutettuaika timestamptz NOT NULL,
  luotuaika timestamptz NOT NULL,
  primary key (koulutustoimija, tutkinto));

create trigger koulutustoimija_ja_tutkinto_update before update on koulutustoimija_ja_tutkinto for each row execute procedure update_stamp();
create trigger koulutustoimija_ja_tutkintol_insert before insert on koulutustoimija_ja_tutkinto for each row execute procedure update_created();
create trigger koulutustoimija_ja_tutkintom_insert before insert on koulutustoimija_ja_tutkinto for each row execute procedure update_stamp();
create trigger koulutustoimija_ja_tutkinto_mu_update before update on koulutustoimija_ja_tutkinto for each row execute procedure update_modifier();
create trigger koulutustoimija_ja_tutkinto_mu_insert before insert on koulutustoimija_ja_tutkinto for each row execute procedure update_modifier();
create trigger koulutustoimija_ja_tutkinto_cu_insert before insert on koulutustoimija_ja_tutkinto for each row execute procedure update_creator();
