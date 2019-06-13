-- Toimipaikat pitää nollata koska ei voida olla varmoja onko uudelle primary keylle tullut duplikaatteja
truncate toimipaikka;
alter table toimipaikka add unique (toimipaikkakoodi);
alter table kysely
    add column toimipaikka character varying(7),
    add constraint kysely_toimipaikka_fk foreign key (toimipaikka) references toimipaikka(toimipaikkakoodi);
alter table kysymysryhma
    add column toimipaikka character varying(7),
    add constraint kysymysryhma_toimipaikka_fk foreign key (toimipaikka) references toimipaikka(toimipaikkakoodi);
alter table kyselypohja
    add column toimipaikka character varying(7),
    add constraint kyselypohja_toimipaikka_fk foreign key (toimipaikka) references toimipaikka(toimipaikkakoodi);
alter table vastaajatunnus
    add column valmistavan_koulutuksen_toimipaikka character varying(10),
    add constraint vastaajatunnus_valmistavan_koulutuksen_toimipaikka_fkey foreign key (valmistavan_koulutuksen_toimipaikka) references toimipaikka(toimipaikkakoodi);

drop view kysely_organisaatio_view;
create view kysely_organisaatio_view
AS
 SELECT k.kyselyid,
    o.koulutustoimija,
    k.oppilaitos,
    k.toimipaikka
   FROM kysely k
     JOIN toimipaikka t ON t.toimipaikkakoodi::text = k.toimipaikka::text
     JOIN oppilaitos o ON o.oppilaitoskoodi::text = t.oppilaitos::text
UNION ALL
 SELECT k.kyselyid,
    o.koulutustoimija,
    k.oppilaitos,
    k.toimipaikka
   FROM kysely k
     JOIN oppilaitos o ON o.oppilaitoskoodi::text = k.oppilaitos::text
UNION ALL
 SELECT k.kyselyid,
    k.koulutustoimija,
    k.oppilaitos,
    k.toimipaikka
   FROM kysely k
  WHERE k.koulutustoimija IS NOT NULL;
grant select on kysely_organisaatio_view to PUBLIC;

drop view kysymysryhma_organisaatio_view;
create view kysymysryhma_organisaatio_view
AS
 SELECT kr.kysymysryhmaid,
    o.koulutustoimija,
    kr.oppilaitos,
    kr.toimipaikka,
    kr.valtakunnallinen
   FROM kysymysryhma kr
     JOIN toimipaikka t ON t.toimipaikkakoodi::text = kr.toimipaikka::text
     JOIN oppilaitos o ON o.oppilaitoskoodi::text = t.oppilaitos::text
UNION ALL
 SELECT kr.kysymysryhmaid,
    o.koulutustoimija,
    kr.oppilaitos,
    kr.toimipaikka,
    kr.valtakunnallinen
   FROM kysymysryhma kr
     JOIN oppilaitos o ON o.oppilaitoskoodi::text = kr.oppilaitos::text
UNION ALL
 SELECT kr.kysymysryhmaid,
    kr.koulutustoimija,
    kr.oppilaitos,
    kr.toimipaikka,
    kr.valtakunnallinen
   FROM kysymysryhma kr
  WHERE kr.koulutustoimija IS NOT NULL OR kr.valtakunnallinen = true;
grant select on kysymysryhma_organisaatio_view to PUBLIC;

drop view kyselypohja_organisaatio_view;
create view kyselypohja_organisaatio_view
AS
 SELECT kp.kyselypohjaid,
    o.koulutustoimija,
    kp.oppilaitos,
    kp.toimipaikka,
    kp.valtakunnallinen
   FROM kyselypohja kp
     JOIN toimipaikka t ON t.toimipaikkakoodi::text = kp.toimipaikka::text
     JOIN oppilaitos o ON o.oppilaitoskoodi::text = t.oppilaitos::text
UNION ALL
 SELECT kp.kyselypohjaid,
    o.koulutustoimija,
    kp.oppilaitos,
    kp.toimipaikka,
    kp.valtakunnallinen
   FROM kyselypohja kp
     JOIN oppilaitos o ON o.oppilaitoskoodi::text = kp.oppilaitos::text
UNION ALL
 SELECT kp.kyselypohjaid,
    kp.koulutustoimija,
    kp.oppilaitos,
    kp.toimipaikka,
    kp.valtakunnallinen
   FROM kyselypohja kp
  WHERE kp.koulutustoimija IS NOT NULL OR kp.valtakunnallinen = true;
grant select on kyselypohja_organisaatio_view to PUBLIC;

create view kayttotilasto_view
AS
 SELECT vt.tutkintotunnus,
    sum(
        CASE
            WHEN v.vastannut THEN vt.vastaajien_lkm
            ELSE 0
        END) AS vastauksia,
    sum(vt.vastaajien_lkm) AS tunnuksia,
    min(v.muutettuaika) AS vastaus_alkupvm,
    max(v.muutettuaika) AS vastaus_loppupvm,
    kt.kyselyid,
    kov.koulutustoimija,
    kov.oppilaitos,
    kov.toimipaikka,
    k.nimi_fi
   FROM vastaajatunnus vt
     LEFT JOIN vastaaja v ON v.vastaajatunnusid = vt.vastaajatunnusid
     JOIN kyselykerta kt ON kt.kyselykertaid = vt.kyselykertaid
     JOIN kysely k ON k.kyselyid = kt.kyselyid
     JOIN kysely_organisaatio_view kov ON kov.kyselyid = kt.kyselyid
  GROUP BY kov.koulutustoimija, kov.oppilaitos, kov.toimipaikka, vt.tutkintotunnus, kt.kyselyid, k.nimi_fi
  ORDER BY kov.koulutustoimija, kov.oppilaitos, vt.tutkintotunnus, kt.kyselyid;
grant select on kayttotilasto_view to PUBLIC;

-- Haetaan kaikki organisaatiot uudestaan
truncate organisaatiopalvelu_log;
-- Tämän pk vaihdon voi tehdä vasta kun duplikaattitoimipaikkakoodit on siivottu
alter table toimipaikka drop constraint toimipaikka_pkey;
alter table toimipaikka add primary key (toimipaikkakoodi);
