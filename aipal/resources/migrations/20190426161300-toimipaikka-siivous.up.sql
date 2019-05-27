-- Vaihtaa toimipaikan primary keyksi toimipaikkakoodi => oid ja siivoaa käyttämättömiä viittauksia toimipaikkaan.

alter table kysely drop constraint kysely_toimipaikka_fk;
alter table kysymysryhma drop constraint kysymysryhma_toimipaikka_fk;
alter table kyselypohja drop constraint kyselypohja_toimipaikka_fk;
alter table vastaajatunnus drop constraint vastaajatunnus_valmistavan_koulutuksen_toimipaikka_fkey;

-- Poistetaan turha näkymä
drop view kayttotilasto_view;

-- kysely (toimipaikka) sarakkeen viittausten siivous ja poisto
drop view kysely_organisaatio_view;
create view kysely_organisaatio_view
AS
 SELECT k.kyselyid,
    o.koulutustoimija,
    k.oppilaitos
   FROM kysely k
     JOIN oppilaitos o ON o.oppilaitoskoodi::text = k.oppilaitos::text
UNION ALL
 SELECT k.kyselyid,
    k.koulutustoimija,
    k.oppilaitos
   FROM kysely k
  WHERE k.koulutustoimija IS NOT NULL;
grant select on kysely_organisaatio_view to PUBLIC;
alter table kysely drop column toimipaikka;

-- kysymysryhma (toimipaikka) sarakkeen viittausten siivous ja poisto
drop view kysymysryhma_organisaatio_view;
create view kysymysryhma_organisaatio_view
AS
 SELECT kr.kysymysryhmaid,
    o.koulutustoimija,
    kr.oppilaitos,
    kr.valtakunnallinen
   FROM kysymysryhma kr
     JOIN oppilaitos o ON o.oppilaitoskoodi::text = kr.oppilaitos::text
UNION ALL
 SELECT kr.kysymysryhmaid,
    kr.koulutustoimija,
    kr.oppilaitos,
    kr.valtakunnallinen
   FROM kysymysryhma kr
  WHERE kr.koulutustoimija IS NOT NULL OR kr.valtakunnallinen = true;
grant select on kysymysryhma_organisaatio_view to PUBLIC;
alter table kysymysryhma drop column toimipaikka;

-- kyselypohja (toimipaikka) sarakkeen viittausten siivous ja poisto
drop view kyselypohja_organisaatio_view;
create view kyselypohja_organisaatio_view
AS
 SELECT kp.kyselypohjaid,
    o.koulutustoimija,
    kp.oppilaitos,
    kp.valtakunnallinen
   FROM kyselypohja kp
     JOIN oppilaitos o ON o.oppilaitoskoodi::text = kp.oppilaitos::text
UNION ALL
 SELECT kp.kyselypohjaid,
    kp.koulutustoimija,
    kp.oppilaitos,
    kp.valtakunnallinen
   FROM kyselypohja kp
  WHERE kp.koulutustoimija IS NOT NULL OR kp.valtakunnallinen = true;
grant select on kyselypohja_organisaatio_view to PUBLIC;
alter table kyselypohja drop column toimipaikka;

-- vastaajatunnus (valmistavan_koulutuksen_toimipaikka) rajakkeen tietojen talteenotto ja poisto
update vastaajatunnus set taustatiedot = '{}' where taustatiedot is null;
update vastaajatunnus set taustatiedot = taustatiedot::jsonb || ('{"toimipaikka":"' || valmistavan_koulutuksen_toimipaikka || '"}')::jsonb where valmistavan_koulutuksen_toimipaikka is not null;
alter table vastaajatunnus drop column valmistavan_koulutuksen_toimipaikka;


-- Tyhjennä toimipaikat
truncate toimipaikka;
-- Tyhjennä aikaleima jolloin kaikki organisaatiot haetaan uudestaan
truncate organisaatiopalvelu_log;
-- Tämän pk vaihdon voi tehdä vasta kun duplikaattioidit on siivottu
alter table toimipaikka drop constraint toimipaikka_pkey;
alter table toimipaikka add primary key (oid);
