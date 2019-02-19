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