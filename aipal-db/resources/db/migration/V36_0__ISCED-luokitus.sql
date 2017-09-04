-- ISCED-luokitukseen siirtyminen edellyttää tietokantamuutoksia, koska uudessa luokituksessa koulutusalan tunnuksellekin pitää varata kolme merkkiä tilaa.
alter table opintoala drop column koulutusala;
alter table koulutusala drop constraint koulutusala_pkey;
alter table koulutusala alter column koulutusalatunnus  type varchar(3);
alter table koulutusala add primary key (koulutusalatunnus);

alter table opintoala 
  add column koulutusala varchar(3),
  add constraint koulutusala_fk foreign key (koulutusala) references koulutusala(koulutusalatunnus);  

-- Testitapausten kannalta tietokanta on tyhjä ja insert ei tapahdu. Asennuksessa olemassaolevan tietokannan päälle tapahtuu.
insert into opintoala(opintoalatunnus, koulutusala, nimi_fi)
  select '007', koulutusalatunnus, 'Keksitty opintoala ISCED-päivitystä varten'
  from koulutusala where koulutusalatunnus = '0';

-- Näille tutkinnoille ei löydy uuden luokituksen mukaista luokitusta Koodistopalvelusta.
update tutkinto set opintoala = '007' where tutkintotunnus not in
('381176','377111','371113','200003','354204','010001', '354116');


delete from opintoala o where opintoalatunnus != '007' and opintoalatunnus not in (select opintoala from tutkinto where tutkintotunnus in ('381176','377111','371113','200003','354204','010001', '354116'));

delete from koulutusala k where not exists (select null from opintoala o where o.koulutusala = k.koulutusalatunnus);
