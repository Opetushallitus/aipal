with nimet as 
  (select kyselyid, nimi_fi || ' ' || (row_number() over (partition by koulutustoimija, nimi_fi order by kyselyid)) as nimi_fi
  from kysely k1
  where exists (select 1 from kysely k2 where k1.nimi_fi = k2.nimi_fi and k1.kyselyid != k2.kyselyid and k1.koulutustoimija = k2.koulutustoimija))
update kysely set nimi_fi = nimet.nimi_fi from nimet where nimet.kyselyid = kysely.kyselyid;

with nimet as 
  (select kyselyid, nimi_sv || ' ' || (row_number() over (partition by koulutustoimija, nimi_sv order by kyselyid)) as nimi_sv 
  from kysely k1 
  where exists (select 1 from kysely k2 where k1.nimi_sv = k2.nimi_sv and k1.kyselyid != k2.kyselyid and k1.koulutustoimija = k2.koulutustoimija))
update kysely set nimi_sv = nimet.nimi_sv from nimet where nimet.kyselyid = kysely.kyselyid;

alter table kysely add constraint kysely_nimi_fi_uniq UNIQUE(koulutustoimija, nimi_fi);
alter table kysely add constraint kysely_nimi_sv_uniq UNIQUE(koulutustoimija, nimi_sv);

with nimet as 
  (select kyselyid, nimi_fi || ' ' || (row_number() over (partition by oppilaitos, nimi_fi order by kyselyid)) as nimi_fi
  from kysely k1
  where exists (select 1 from kysely k2 where k1.nimi_fi = k2.nimi_fi and k1.kyselyid != k2.kyselyid and k1.oppilaitos = k2.oppilaitos))
update kysely set nimi_fi = nimet.nimi_fi from nimet where nimet.kyselyid = kysely.kyselyid;

with nimet as 
  (select kyselyid, nimi_sv || ' ' || (row_number() over (partition by oppilaitos, nimi_sv order by kyselyid)) as nimi_sv 
  from kysely k1 
  where exists (select 1 from kysely k2 where k1.nimi_sv = k2.nimi_sv and k1.kyselyid != k2.kyselyid and k1.oppilaitos = k2.oppilaitos))
update kysely set nimi_sv = nimet.nimi_sv from nimet where nimet.kyselyid = kysely.kyselyid;

with nimet as 
  (select kyselykertaid, kk1.nimi || ' ' || (row_number() over (partition by koulutustoimija, kk1.nimi order by kyselykertaid)) as nimi
  from kyselykerta kk1
  join kysely k1 on kk1.kyselyid = k1.kyselyid
  where exists (select 1 from kyselykerta kk2 join kysely k2 on kk2.kyselyid = k2.kyselyid where kk1.nimi = kk2.nimi and kk1.kyselykertaid != kk2.kyselykertaid and k1.koulutustoimija = k2.koulutustoimija))
update kyselykerta set nimi = nimet.nimi from nimet where nimet.kyselykertaid = kyselykerta.kyselykertaid;
