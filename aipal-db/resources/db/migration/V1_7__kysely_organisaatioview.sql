create or replace view kysely_organisaatio_view as
select
  k.kyselyid, o.koulutustoimija, k.oppilaitos, k.toimipaikka
from kysely k
  inner join toimipaikka t on t.toimipaikkakoodi = k.toimipaikka
  inner join oppilaitos o on o.oppilaitoskoodi = t.oppilaitos
union all
  select
    k.kyselyid, o.koulutustoimija, k.oppilaitos, k.toimipaikka
  from kysely k
    inner join oppilaitos o on o.oppilaitoskoodi = k.oppilaitos
union all
  select
    k.kyselyid, k.koulutustoimija, k.oppilaitos, k.toimipaikka
  from kysely k
  where k.koulutustoimija is not null
;

COMMENT ON VIEW kysely_organisaatio_view is 'kyselyjen omistaja-organisaatiot';
