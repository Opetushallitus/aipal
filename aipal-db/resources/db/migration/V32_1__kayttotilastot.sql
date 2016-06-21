create or replace view kayttotilasto_view as 
 select vt.tutkintotunnus, sum(case when vastannut then vastaajien_lkm else 0 end) as vastauksia, sum(vastaajien_lkm) as tunnuksia, 
    min(v.muutettuaika) as vastaus_alkupvm, max(v.muutettuaika) as vastaus_loppupvm,
        kt.kyselyid, kov.koulutustoimija, kov.oppilaitos, kov.toimipaikka, 
        k.nimi_fi from vastaajatunnus vt
  left join vastaaja v on v.vastaajatunnusid = vt.vastaajatunnusid
  inner join kyselykerta kt on kt.kyselykertaid = vt.kyselykertaid
  inner join kysely k on k.kyselyid = kt.kyselyid
  inner join kysely_organisaatio_view kov on kov.kyselyid = kt.kyselyid
  group by kov.koulutustoimija, kov.oppilaitos, kov.toimipaikka, tutkintotunnus, kt.kyselyid, k.nimi_fi
  order by koulutustoimija, oppilaitos, tutkintotunnus, kyselyid;
