-- :name poista-koulutustoimijoiden-tutkinnot! :! :n
DELETE FROM koulutustoimija_ja_tutkinto;

-- :name lisaa-koulutustoimijan-tutkinto! :! :n
INSERT INTO koulutustoimija_ja_tutkinto (koulutustoimija, tutkinto, voimassa_alkupvm, voimassa_loppupvm)
    SELECT :ytunnus, :tutkintotunnus, to_date(:alkupvm, 'YYYY-MM-DD'), to_date(:loppupvm, 'YYYY-MM-DD')
    WHERE EXISTS (SELECT ytunnus FROM koulutustoimija WHERE ytunnus = :ytunnus)
    AND   EXISTS(SELECT tutkintotunnus FROM tutkinto WHERE tutkintotunnus = :tutkintotunnus);

-- :name poista-koulutustoimijan-tutkinto! :! :n
DELETE FROM koulutustoimija_ja_tutkinto WHERE koulutustoimija = :koulutustoimija AND tutkinto = :tutkinto;

-- :name hae-tutkinnot :? :*
SELECT * FROM tutkinto ORDER BY tutkintotunnus;

-- :name hae-tutkinto :? :1
SELECT * FROM tutkinto WHERE tutkintotunnus = :tutkintotunnus;

-- :name lisaa-tutkinto! :! :n
INSERT INTO tutkinto (tutkintotunnus, opintoala, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, siirtymaajan_loppupvm, nimi_en, tutkintotyyppi)
    VALUES (:tutkintotunnus, :opintoala, :nimi_fi, :nimi_sv, :voimassa_alkupvm, :voimassa_loppupvm, :siirtymaajan_loppupvm, :nimi_en, :tutkintotyyppi);

-- :name paivita-tutkinto! :! :n
UPDATE tutkinto SET
    opintoala = :opintoala,
    nimi_fi = :nimi_fi, nimi_sv = :nimi_sv, nimi_en = :nimi_en,
    voimassa_alkupvm = :voimassa_alkupvm, voimassa_loppupvm = :voimassa_loppupvm,
    siirtymaajan_loppupvm = :siirtymaajan_loppupvm, tutkintotyyppi = :tutkintotyyppi
WHERE tutkintotunnus = :tutkintotunnus;

-- :name hae-koulutustoimijan-tutkinnot :? :*
SELECT t.*,
    oa.opintoalatunnus, oa.nimi_fi AS opintoala_nimi_fi, oa.nimi_sv AS opintoala_nimi_sb, oa.nimi_en AS opintoala_nimi_en,
    ka.koulutusalatunnus, ka.nimi_fi AS koulutusala_nimi_fi, ka.nimi_sv AS koulutusala_nimi_sv, ka.nimi_en AS koulutusala_nimi_en
FROM tutkinto t
INNER JOIN opintoala oa ON t.opintoala = oa.opintoalatunnus
INNER JOIN koulutusala ka ON oa.koulutusala = ka.koulutusalatunnus
LEFT JOIN oppilaitostyyppi_tutkintotyyppi ot_tt ON ot_tt.tutkintotyyppi = t.tutkintotyyppi
LEFT JOIN koulutustoimija_ja_tutkinto ktt ON t.tutkintotunnus = ktt.tutkinto
WHERE ktt.koulutustoimija = :koulutustoimija
--~(if (:oppilaitostyypit params) "OR ot_tt.oppilaitostyyppi IN (:v*:oppilaitostyypit)")
;

-- :name hae-tutkinnon-jarjestajat :? :*
SELECT k.*
FROM koulutustoimija k
JOIN koulutustoimija_ja_tutkinto ktt ON k.ytunnus = ktt.koulutustoimija
WHERE ktt.tutkinto = :tutkintotunnus;