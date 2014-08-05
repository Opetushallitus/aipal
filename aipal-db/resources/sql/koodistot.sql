-- Koodistodata, joka on osa konversiodataa mutta luonteeltaan muuttumatonta.
-- Tämä data on myös "tyhjässä" tietokannassa. Skripti on kirjoitettu siten että sen voi ajaa myös konversion jälkeen turvallisesti.

set session aipal.kayttaja='KONVERSIO';

CREATE TABLE rahoitusmuoto_tmp
  (
    rahoitusmuotoid   INT NOT NULL ,
    rahoitusmuoto     VARCHAR(80) NOT NULL
  );
  
insert into rahoitusmuoto_tmp (rahoitusmuotoid, rahoitusmuoto) values (1, 'valtionosuus');
insert into rahoitusmuoto_tmp (rahoitusmuotoid, rahoitusmuoto) values (2, 'oppisopimus');
insert into rahoitusmuoto_tmp (rahoitusmuotoid, rahoitusmuoto) values (3, 'tyovoimapoliittinen');
insert into rahoitusmuoto_tmp (rahoitusmuotoid, rahoitusmuoto) values (4, 'henkilostokoulutus');
insert into rahoitusmuoto_tmp (rahoitusmuotoid, rahoitusmuoto) values (5, 'ei_rahoitusmuotoa');

insert into rahoitusmuoto (rahoitusmuotoid, rahoitusmuoto)
  select rahoitusmuotoid, rahoitusmuoto from rahoitusmuoto_tmp r_tmp
  where not exists (select 1 from rahoitusmuoto rr where rr.rahoitusmuotoid = r_tmp.rahoitusmuotoid);

drop table rahoitusmuoto_tmp;

  