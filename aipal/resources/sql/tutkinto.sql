-- :name poista-koulutustoimijoiden-tutkinnot! :! :n
DELETE FROM koulutustoimija_ja_tutkinto;

-- :name lisaa-koulutustoimijan-tutkinto! :! :n
INSERT INTO koulutustoimija_ja_tutkinto (koulutustoimija, tutkinto, voimassa_alkupvm, voimassa_loppupvm, laaja_oppisopimuskoulutus)
    SELECT :ytunnus, :tutkintotunnus, to_date(:alkupvm, 'YYYY-MM-DD'), to_date(:loppupvm, 'YYYY-MM-DD'), :laaja_oppisopimuskoulutus
    WHERE EXISTS (SELECT ytunnus FROM koulutustoimija WHERE ytunnus = :ytunnus)
    AND   EXISTS(SELECT tutkintotunnus FROM tutkinto WHERE tutkintotunnus = :tutkintotunnus)
ON CONFLICT (koulutustoimija, tutkinto, voimassa_alkupvm) DO UPDATE
SET
voimassa_loppupvm = excluded.voimassa_loppupvm,
laaja_oppisopimuskoulutus = excluded.laaja_oppisopimuskoulutus;

-- :name poista-koulutustoimijan-tutkinto! :! :n
DELETE FROM koulutustoimija_ja_tutkinto WHERE koulutustoimija = :koulutustoimija AND tutkinto = :tutkinto;

-- :name hae-tutkinnot :? :*
SELECT * FROM tutkinto ORDER BY tutkintotunnus;

-- :name hae-tutkinto :? :1
SELECT * FROM tutkinto WHERE tutkintotunnus = :tutkintotunnus;

-- :name lisaa-tutkinto! :! :n
INSERT INTO tutkinto (tutkintotunnus, opintoala, nimi_fi, nimi_sv, voimassa_alkupvm, voimassa_loppupvm, nimi_en, tutkintotyyppi)
    VALUES (:tutkintotunnus, :opintoala, :nimi_fi, :nimi_sv, :voimassa_alkupvm, :voimassa_loppupvm, :nimi_en, :tutkintotyyppi);

-- :name paivita-tutkinto! :! :n
UPDATE tutkinto SET
    opintoala = :opintoala,
    nimi_fi = :nimi_fi, nimi_sv = :nimi_sv, nimi_en = :nimi_en,
    voimassa_alkupvm = :voimassa_alkupvm, voimassa_loppupvm = :voimassa_loppupvm,
    tutkintotyyppi = :tutkintotyyppi
WHERE tutkintotunnus = :tutkintotunnus;

-- :name hae-koulutustoimijan-tutkinnot :? :*
SELECT DISTINCT t.*,
    oa.opintoalatunnus, oa.nimi_fi AS opintoala_nimi_fi, oa.nimi_sv AS opintoala_nimi_sb, oa.nimi_en AS opintoala_nimi_en,
    ka.koulutusalatunnus, ka.nimi_fi AS koulutusala_nimi_fi, ka.nimi_sv AS koulutusala_nimi_sv, ka.nimi_en AS koulutusala_nimi_en
FROM tutkinto t
LEFT JOIN opintoala oa ON t.opintoala = oa.opintoalatunnus
LEFT JOIN koulutusala ka ON oa.koulutusala = ka.koulutusalatunnus
LEFT JOIN koulutustoimija_ja_tutkinto ktt ON t.tutkintotunnus = ktt.tutkinto
WHERE ktt.koulutustoimija = :koulutustoimija
--~(if (:tutkintotyypit params) "OR t.tutkintotyyppi IN (:v*:tutkintotyypit)")
;

-- :name hae-koulutustoimijan-kaikki-tutkinnot :? :*
SELECT t.*,
    oa.opintoalatunnus, oa.nimi_fi AS opintoala_nimi_fi, oa.nimi_sv AS opintoala_nimi_sb, oa.nimi_en AS opintoala_nimi_en,
    ka.koulutusalatunnus, ka.nimi_fi AS koulutusala_nimi_fi, ka.nimi_sv AS koulutusala_nimi_sv, ka.nimi_en AS koulutusala_nimi_en
FROM tutkinto t
    LEFT JOIN opintoala oa ON t.opintoala = oa.opintoalatunnus
    LEFT JOIN koulutusala ka ON oa.koulutusala = ka.koulutusalatunnus
WHERE (t.tutkintotyyppi IS NULL) OR t.tutkintotyyppi NOT IN (:v*:tutkintotyypit);

-- :name hae-tutkinnon-jarjestajat :? :*
SELECT DISTINCT k.*
FROM koulutustoimija k
JOIN koulutustoimija_ja_tutkinto ktt ON k.ytunnus = ktt.koulutustoimija
WHERE ktt.tutkinto = :tutkintotunnus;

-- :name lisaa-opintoala! :! :n
INSERT INTO opintoala (opintoalatunnus, nimi_fi, nimi_sv, nimi_en, koulutusala)
    VALUES (:opintoalatunnus, :nimi_fi, :nimi_sv, :nimi_en, koulutusala);

-- :name paivita-opintoala! :! :n
UPDATE opintoala SET
  nimi_fi = :nimi_fi,nimi_sv = :nimi_sv, nimi_en = :nimi_en, koulutusala = :koulutusala
WHERE opintoalatunnus = :opintoalatunnus;

-- :name hae-opintoalat :? :*
SELECT * FROM opintoala;

-- :name hae-opintoala :? :1
SELECT * FROM opintoala WHERE opintoalatunnus = :opintoalatunnus;

-- :name lisaa-koulutusala! :! :n
INSERT INTO koulutusala (koulutusalatunnus, nimi_fi, nimi_sv, nimi_en)
    VALUES (:koulutusalatunnus, :nimi_fi, :nimi_sv, :nimi_en);

-- :name paivita-koulutusala! :! :n
UPDATE koulutusala SET
  nimi_fi = :nimi_fi,nimi_sv = :nimi_sv, nimi_en = :nimi_en
WHERE koulutusalatunnus = :koulutusalatunnus;

-- :name hae-koulutusalat :? :*
SELECT * FROM koulutusala;

-- :name hae-koulutusala :? :1
SELECT * FROM koulutusala
WHERE koulutusalatunnus = :koulutusalatunnus;

-- :name lisaa-tutkintotyyppi! :! :n
INSERT INTO tutkintotyyppi (tutkintotyyppi, nimi_fi, nimi_sv, nimi_en)
    VALUES (:tutkintotyyppi, :nimi_fi, :nimi_sv, :nimi_en);

-- :name paivita-tutkintotyyppi! :! :n
UPDATE tutkintotyyppi SET nimi_fi = :nimi_fi, nimi_sv = :nimi_sv, nimi_en = :nimi_en
WHERE tutkintotyyppi.tutkintotyyppi = :tutkintotyyppi;

-- :name hae-tutkintotyypit :? :*
SELECT * FROM tutkintotyyppi ORDER BY tutkintotyyppi.tutkintotyyppi ASC;

-- :name hae-kayttajan-tutkintotyypit :? :*
SELECT tt.* FROM oppilaitos o
JOIN oppilaitostyyppi_tutkintotyyppi ottt ON o.oppilaitostyyppi = ottt.oppilaitostyyppi
JOIN tutkintotyyppi tt ON ottt.tutkintotyyppi = tt.tutkintotyyppi
WHERE o.koulutustoimija = :koulutustoimija
ORDER BY tt.tutkintotyyppi ASC;
