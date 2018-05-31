--:name hae-koulutustoimija :? :1
SELECT * FROM koulutustoimija WHERE ytunnus = :ytunnus;

-- :name hae-koulutustoimijat-joilla-koulutuslupa :? :*
SELECT ytunnus, nimi_fi, nimi_sv, nimi_en FROM koulutustoimija
  WHERE ytunnus IN (SELECT koulutustoimija FROM koulutustoimija_ja_tutkinto);