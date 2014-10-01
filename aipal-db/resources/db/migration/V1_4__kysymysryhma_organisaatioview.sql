create or replace view kysymysryhma_organisaatio_view as
select
  kr.kysymysryhmaid, o.koulutustoimija, kr.oppilaitos, kr.toimipaikka, kr.valtakunnallinen
from kysymysryhma kr
  inner join toimipaikka t on t.toimipaikkakoodi = kr.toimipaikka
  inner join oppilaitos o on o.oppilaitoskoodi = t.oppilaitos
union all
  select
    kr.kysymysryhmaid, o.koulutustoimija, kr.oppilaitos, kr.toimipaikka, kr.valtakunnallinen
  from kysymysryhma kr
    inner join oppilaitos o on o.oppilaitoskoodi = kr.oppilaitos
union all
  select
    kr.kysymysryhmaid, kr.koulutustoimija, kr.oppilaitos, kr.toimipaikka, kr.valtakunnallinen
  from kysymysryhma kr
  where kr.koulutustoimija is not null or kr.valtakunnallinen = true
;

COMMENT ON VIEW kysymysryhma_organisaatio_view is 'kysymysryhmien omistaja-organisaatiot sekä valtakunnalliset kysymysryhmät.';
