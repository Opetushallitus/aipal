-- poistetaan virheellisiin koulutustoimijoihin liittyvät toimipaikat
delete from toimipaikka t
where t.oppilaitos in
  (select o.oppilaitoskoodi from oppilaitos o
    where o.koulutustoimija in
    (select kt.ytunnus from koulutustoimija kt
      where (substring(kt.ytunnus, 8, 1) != '-' or substring(kt.ytunnus,1,3) = '000')
        and not exists (select 1 from kysely_organisaatio_view kv where kv.koulutustoimija = kt.ytunnus)
        and not exists (select 1 from kyselypohja_organisaatio_view kpv where kpv.koulutustoimija = kt.ytunnus)
        and not exists (select 1 from kysymysryhma_organisaatio_view krv where krv.koulutustoimija = kt.ytunnus)
        and not exists (select 1 from rooli_organisaatio ro where ro.organisaatio = kt.ytunnus)
        and not exists (select 1 from koulutustoimija_ja_tutkinto tut where tut.koulutustoimija = kt.ytunnus)
        and not exists (select 1 from vastaajatunnus vt where vt.valmistavan_koulutuksen_jarjestaja = kt.ytunnus))
    and not exists (select 1 from vastaajatunnus vt2 where vt2.valmistavan_koulutuksen_oppilaitos = o.oppilaitoskoodi)
    and not exists (select 1 from kysely kv where kv.oppilaitos = o.oppilaitoskoodi)
    and not exists (select 1 from kyselypohja kp where kp.oppilaitos = o.oppilaitoskoodi)
    and not exists (select 1 from kysymysryhma kr where kr.oppilaitos = o.oppilaitoskoodi))
  and not exists (select 1 from kysely kv2 where kv2.toimipaikka = t.toimipaikkakoodi)
  and not exists (select 1 from kyselypohja kp2 where kp2.toimipaikka = t.toimipaikkakoodi)
  and not exists (select 1 from kysymysryhma kr2 where kr2.toimipaikka = t.toimipaikkakoodi);

-- poistetaan virheellisiin koulutustoimijoihin liittyvät oppilaitokset
delete from oppilaitos o
where o.koulutustoimija in
  (select kt.ytunnus from koulutustoimija kt
    where (substring(kt.ytunnus, 8, 1) != '-' or substring(kt.ytunnus,1,3) = '000')
      and not exists (select 1 from kysely_organisaatio_view kv where kv.koulutustoimija = kt.ytunnus)
      and not exists (select 1 from kyselypohja_organisaatio_view kpv where kpv.koulutustoimija = kt.ytunnus)
      and not exists (select 1 from kysymysryhma_organisaatio_view krv where krv.koulutustoimija = kt.ytunnus)
      and not exists (select 1 from rooli_organisaatio ro where ro.organisaatio = kt.ytunnus)
      and not exists (select 1 from koulutustoimija_ja_tutkinto tut where tut.koulutustoimija = kt.ytunnus)
      and not exists (select 1 from vastaajatunnus vt where vt.valmistavan_koulutuksen_jarjestaja = kt.ytunnus))
  and not exists (select 1 from vastaajatunnus vt2 where vt2.valmistavan_koulutuksen_oppilaitos = o.oppilaitoskoodi)
  and not exists (select 1 from kysely ky where ky.oppilaitos = o.oppilaitoskoodi)
  and not exists (select 1 from kyselypohja kp where kp.oppilaitos = o.oppilaitoskoodi)
  and not exists (select 1 from kysymysryhma kr where kr.oppilaitos = o.oppilaitoskoodi);

-- poistetaan kaikki virheelliset koulutustoimijat joihin ei ole kannassa viitteitä
delete from koulutustoimija kt
where (substring(kt.ytunnus, 8, 1) != '-' or substring(kt.ytunnus,1,3) = '000')
  and not exists (select 1 from kysely_organisaatio_view kv where kv.koulutustoimija = kt.ytunnus)
  and not exists (select 1 from kyselypohja_organisaatio_view kpv where kpv.koulutustoimija = kt.ytunnus)
  and not exists (select 1 from kysymysryhma_organisaatio_view krv where krv.koulutustoimija = kt.ytunnus)
  and not exists (select 1 from oppilaitos o where o.koulutustoimija = kt.ytunnus)
  and not exists (select 1 from rooli_organisaatio ro where ro.organisaatio = kt.ytunnus)
  and not exists (select 1 from koulutustoimija_ja_tutkinto tut where tut.koulutustoimija = kt.ytunnus)
  and not exists (select 1 from vastaajatunnus vt where vt.valmistavan_koulutuksen_jarjestaja = kt.ytunnus);
