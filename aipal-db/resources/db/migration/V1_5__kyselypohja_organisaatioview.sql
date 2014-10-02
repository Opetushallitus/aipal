create or replace view kyselypohja_organisaatio_view as
select
  kp.kyselypohjaid, o.koulutustoimija, kp.oppilaitos, kp.toimipaikka, kp.valtakunnallinen
from kyselypohja kp
  inner join toimipaikka t on t.toimipaikkakoodi = kp.toimipaikka
  inner join oppilaitos o on o.oppilaitoskoodi = t.oppilaitos
union all
  select
    kp.kyselypohjaid, o.koulutustoimija, kp.oppilaitos, kp.toimipaikka, kp.valtakunnallinen
  from kyselypohja kp
    inner join oppilaitos o on o.oppilaitoskoodi = kp.oppilaitos
union all
  select
    kp.kyselypohjaid, kp.koulutustoimija, kp.oppilaitos, kp.toimipaikka, kp.valtakunnallinen
  from kyselypohja kp
  where kp.koulutustoimija is not null or kp.valtakunnallinen = true
;

COMMENT ON VIEW kyselypohja_organisaatio_view is 'kyselypohjien omistaja-organisaatiot sekä valtakunnalliset kyselypohjat.';
