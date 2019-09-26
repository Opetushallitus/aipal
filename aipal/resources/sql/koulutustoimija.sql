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

-- :name hae-toimipaikka :? :*
SELECT * FROM toimipaikka
WHERE toimipaikkakoodi = :toimipaikkakoodi AND voimassa = TRUE;

-- name amispalaute-automatisointi :? :1
SELECT * FROM amispalaute_automatisointi WHERE koulutustoimija = :koulutustoimija;

-- :name lisaa-automatisointiin! :! :1
INSERT INTO amispalaute_automatisointi (koulutustoimija, voimassa_alkaen, lahde)
VALUES (:koulutustoimija, now(), :lahde) ON CONFLICT DO NOTHING;