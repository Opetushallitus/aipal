--:name hae-koulutustoimija :? :1
SELECT * FROM koulutustoimija WHERE ytunnus = :ytunnus;

-- :name hae-koulutustoimijat-joilla-koulutuslupa :? :*
SELECT DISTINCT ytunnus, nimi_fi, nimi_sv, nimi_en FROM koulutustoimija
  WHERE ytunnus IN (SELECT koulutustoimija FROM koulutustoimija_ja_tutkinto);

-- :name hae-koulutustoimijan-oppilaitokset :? :*
SELECT * FROM oppilaitos WHERE koulutustoimija = :koulutustoimija

-- :name hae-koulutustoimija-nimella :? :*
SELECT * FROM koulutustoimija
WHERE nimi_fi ILIKE '%' || :termi  || '%'
OR nimi_sv ILIKE '%' || :termi  || '%'
OR nimi_sv ILIKE '%' || :termi  || '%';

-- :name hae-oidilla :? :1
SELECT * FROM :i:taulu WHERE oid = :oid
AND voimassa = TRUE;

-- :name hae-oppilaitos :? :*
SELECT * FROM oppilaitos
WHERE oppilaitoskoodi = :oppilaitoskoodi AND voimassa = TRUE;

-- name amispalaute-automatisointi :? :1
SELECT * FROM amispalaute_automatisointi WHERE koulutustoimija = :koulutustoimija;

-- :name lisaa-automatisointiin! :! :1
INSERT INTO amispalaute_automatisointi (koulutustoimija, voimassa_alkaen, lahde)
VALUES (:koulutustoimija, now(), :lahde) ON CONFLICT DO NOTHING;

-- :name hae-automaattikysely-korkeakoulut :? :*
SELECT DISTINCT kt.ytunnus FROM koulutustoimija kt
 JOIN oppilaitos o on kt.ytunnus = o.koulutustoimija
 JOIN oppilaitostyyppi_tutkintotyyppi ot ON o.oppilaitostyyppi = ot.oppilaitostyyppi
WHERE kt.lakkautuspaiva IS NULL
 AND o.lakkautuspaiva IS NULL
AND NOT EXISTS (SELECT 1 FROM kysely k
    WHERE k.metatiedot->>'automatisointi_tunniste' = :tunniste
    AND k.koulutustoimija = kt.ytunnus);

-- :name hae-automaattikysely-koulutustoimijat :? :*
SELECT ytunnus, nimi_fi FROM koulutustoimija kt
-- löytyy aiempi halutun tyyppinen kysely
WHERE EXISTS (SELECT 1 FROM kysely k WHERE k.koulutustoimija = kt.ytunnus AND k.tyyppi = :kyselytyyppi)
-- muttei löydy voimassaolevaa automaattisesti luotua
  AND NOT EXISTS (
        SELECT 1
        FROM kysely k
        WHERE k.metatiedot->>'automatisointi_tunniste' = :tunniste
          AND koulutustoimija = kt.ytunnus
    );

-- :name hae-ammatilliset-koulutustoimijat :?
SELECT DISTINCT kt.ytunnus FROM koulutustoimija_ja_tutkinto ktt
JOIN koulutustoimija kt on ktt.koulutustoimija = kt.ytunnus
WHERE kt.lakkautuspaiva is NULL
  AND NOT EXISTS (
        SELECT 1
        FROM kysely k
        WHERE k.metatiedot->>'automatisointi_tunniste' = :tunniste
          AND koulutustoimija != kt.ytunnus
    );
