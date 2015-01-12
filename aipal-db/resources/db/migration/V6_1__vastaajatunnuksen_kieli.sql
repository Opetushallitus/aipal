create table kieli (
  kieli varchar(2) primary key, 
  muutettu_kayttaja varchar(80) not null references kayttaja(oid),
  luotu_kayttaja varchar(80) not null references kayttaja(oid),
  muutettuaika timestamptz not null,
  luotuaika timestamptz not null
);

create trigger kieli_update before update on kieli for each row execute procedure update_stamp() ;
create trigger kielil_insert before insert on kieli for each row execute procedure update_created() ;
create trigger kielim_insert before insert on kieli for each row execute procedure update_stamp() ;
create trigger kieli_mu_update before update on kieli for each row execute procedure update_modifier() ;
create trigger kieli_cu_insert before insert on kieli for each row execute procedure update_creator() ;
create trigger kieli_mu_insert before insert on kieli for each row execute procedure update_modifier() ;

insert into kieli (kieli) values ('fi'), ('sv');

alter table vastaajatunnus add column suorituskieli varchar(2) references kieli(kieli);
