-- :name hae-kyselykerta :? :1
SELECT kk.*,
       NOT EXISTS (SELECT 1 FROM vastaajatunnus vt
                    JOIN vastaaja v ON vt.vastaajatunnusid = v.vastaajatunnusid
                    WHERE vt.kyselykertaid = kk.kyselykertaid) AS poistettavissa
FROM kyselykerta kk WHERE kyselykertaid = :kyselykertaid;

-- :name hae-palaute-kyselykerta :? :*
SELECT kk.kyselykertaid FROM kyselykerta kk
  JOIN kysely k ON kk.kyselyid = k.kyselyid
  JOIN oppilaitos o ON k.koulutustoimija = o.koulutustoimija
  WHERE o.oppilaitoskoodi = :oppilaitoskoodi AND kk.nimi = :kyselykertanimi
  AND kk.lukittu = FALSE AND k.tyyppi = 'avop';

-- :name hae-rekry-kyselykerta :? :*
SELECT kk.kyselykertaid FROM kyselykerta kk
  JOIN kysely k ON kk.kyselyid = k.kyselyid
  JOIN oppilaitos o ON k.koulutustoimija = o.koulutustoimija
  WHERE o.oppilaitoskoodi = :oppilaitoskoodi AND k.tyyppi = 'rekrykysely'
    AND kk.automaattinen @> NOW()::DATE
  AND (kk.metatiedot ->'vuosi')::TEXT::INTEGER = :vuosi
  AND kk.lukittu = FALSE;

-- :name hae-automaatti-kyselykerta :? :1
SELECT kk.kyselykertaid, kk.kyselyid FROM kyselykerta kk
  JOIN kysely k ON kk.kyselyid = k.kyselyid
WHERE k.koulutustoimija = :koulutustoimija AND k.tyyppi = :kyselytyyppi
AND kk.automaattinen @> now()::DATE
  --~(if (:tarkenne params) "AND kk.metatiedot ->> 'tarkenne' = :tarkenne")
AND k.voimassa_alkupvm <= now() AND (k.voimassa_loppupvm IS NULL OR k.voimassa_loppupvm >= now())
AND k.tila = 'julkaistu' AND kk.lukittu = FALSE;

-- :name hae-amispalaute-kyselykerta :? :1
SELECT kk.kyselykertaid FROM kyselykerta kk
                                 JOIN kysely k on kk.kyselyid = k.kyselyid
WHERE k.koulutustoimija = :koulutustoimija
  AND kk.automaattinen @> NOW()::DATE
  AND kk.metatiedot ->> 'tarkenne' = :tarkenne
  AND k.tila = 'julkaistu' AND kk.lukittu = FALSE;

-- :name hae-kyselyn-tutkinnot :? :*
SELECT DISTINCT t.tutkintotunnus, t.nimi_fi, t.nimi_sv, t.nimi_en
FROM vastaajatunnus vt
  JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
  JOIN tutkinto t ON t.tutkintotunnus = vt.taustatiedot->>'tutkinto'
WHERE kk.kyselyid = :kyselyid;

-- :name hae-kyselyn-toimipaikat :? :*
SELECT DISTINCT t.toimipistekoodi, t.nimi_fi, t.nimi_sv, t.nimi_en
FROM vastaajatunnus vt
  JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
  JOIN toimipiste t ON t.toimipistekoodi = vt.taustatiedot->>'toimipiste'
WHERE kk.kyselyid = :kyselyid;

-- :name hae-kyselyn-hankintakoulutuksen-toteuttajat :? :*
SELECT DISTINCT kt.ytunnus, kt.nimi_fi, kt.nimi_sv, kt.nimi_en
FROM vastaajatunnus vt
  JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
  JOIN koulutustoimija kt ON kt.ytunnus = vt.taustatiedot->>'hankintakoulutuksen_toteuttaja'
WHERE kk.kyselyid = :kyselyid;

-- :name hae-kyselyn-koulutustoimijat :? :*
SELECT DISTINCT kt.ytunnus, kt.nimi_fi, kt.nimi_sv, kt.nimi_en
FROM vastaajatunnus vt
  JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
  JOIN koulutustoimija kt ON kt.ytunnus = vt.taustatiedot->>'koulutustoimija'
WHERE kk.kyselyid = :kyselyid;

-- :name hae-kyselyn-koulutusalat :? :*
SELECT DISTINCT ka.koulutusalatunnus, ka.nimi_fi, ka.nimi_sv, ka.nimi_en
FROM vastaajatunnus vt
  JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
  JOIN koulutusala ka ON vt.taustatiedot->>'koulutusalakoodi' = ka.koulutusalatunnus
WHERE kk.kyselyid = :kyselyid;

-- :name hae-kyselykerran-organisaatio :? :1
SELECT k.koulutustoimija FROM kyselykerta kk
JOIN kysely k on kk.kyselyid = k.kyselyid
WHERE kk.kyselykertaid = :kyselykertaid;

-- :name luo-kyselykerta! :<!
INSERT INTO kyselykerta (kyselyid, nimi, voimassa_alkupvm, voimassa_loppupvm, luotu_kayttaja, muutettu_kayttaja, automaattinen, metatiedot)
VALUES (:kyselyid, :nimi, :voimassa_alkupvm, :voimassa_loppupvm, :kayttaja, :kayttaja, :automaattinen::DATERANGE, :metatiedot)
RETURNING kyselykertaid;

-- :name paivita-kyselykerta! :! :n
UPDATE kyselykerta SET
    nimi = :nimi, voimassa_alkupvm = :voimassa_alkupvm, voimassa_loppupvm = :voimassa_loppupvm,
    lukittu = :lukittu, muutettu_kayttaja = :kayttaja
WHERE kyselykertaid = :kyselykertaid;

-- :name paata-kyselykerrat! :! :n
WITH t AS (SELECT kk.kyselykertaid as id FROM kysely k JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
    WHERE k.koulutustoimija = :koulutustoimija AND k.tyyppi = :tyyppi AND kk.automaattinen IS NOT NULL AND UPPER(kk.automaattinen) IS NULL AND kk.automaattinen @> now()::DATE)
UPDATE kyselykerta SET automaattinen = daterange(LOWER(automaattinen), :paattymis_pvm::DATE) FROM t WHERE t.id = kyselykertaid;

-- :name hae-koulutustoimijan-kyselykerrat :? :*
SELECT kk.kyselykertaid, kk.kyselyid, kk.nimi, kk.voimassa_alkupvm, kk.voimassa_loppupvm, kk.kaytettavissa, kk.luotuaika, kk.lukittu, kk.automaattinen,
       (SELECT coalesce(sum(kohteiden_lkm),0) FROM vastaajatunnus v2 WHERE kyselykertaid = kk.kyselykertaid) AS vastaajatunnuksia,
       coalesce(count(DISTINCT vs),0) AS vastaajia,
       count(vt) = 0 AS poistettavissa,
       max(vs.vastausaika) AS viimeisin_vastaus
FROM kyselykerta kk
         JOIN kysely k ON kk.kyselyid = k.kyselyid
         LEFT JOIN vastaajatunnus vt on kk.kyselykertaid = vt.kyselykertaid
         LEFT JOIN vastaaja vs on vt.vastaajatunnusid = vs.vastaajatunnusid
WHERE k.koulutustoimija = :koulutustoimija
GROUP BY kk.kyselykertaid, kk.kyselyid, kk.nimi, kk.voimassa_alkupvm, kk.voimassa_loppupvm, kk.kaytettavissa, kk.luotuaika, kk.lukittu;

-- :name hae-vastattavissa-tiedot :? :*
SELECT k.tila AS kyselytila, k.voimassa_alkupvm AS kyselyvoimassa_alkupvm, k.voimassa_loppupvm AS kyselyvoimassa_loppupvm,
       kk.kyselykertaid, kk.lukittu AS kyselykertalukittu, kk.voimassa_alkupvm AS kyselykertavoimassa_alkupvm, kk.voimassa_loppupvm AS kyselykertavoimassa_loppupvm,
       vt.lukittu AS vastaajatunnuslukittu, vt.voimassa_alkupvm AS vastaajatunnusvoimassa_alkupvm, vt.voimassa_loppupvm AS vastaajatunnusvoimassa_loppupvm,
       (SELECT count(*) FROM vastaaja WHERE vastaajatunnusid = vt.vastaajatunnusid) AS vastaus_lkm,
       coalesce(vt.kohteiden_lkm, 0) AS kohteiden_lkm  -- for some reason there are null values
FROM kysely k
         JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
         JOIN vastaajatunnus vt ON vt.kyselykertaid = kk.kyselykertaid
WHERE k.koulutustoimija = :ytunnus;

-- :name hae-kyselyn-kyselykerrat :? :*
SELECT kk.*,
NOT EXISTS (SELECT 1 FROM vastaajatunnus vt
    JOIN vastaaja v ON vt.vastaajatunnusid = v.vastaajatunnusid
    WHERE vt.kyselykertaid = kk.kyselykertaid) AS poistettavissa
FROM kyselykerta kk WHERE kyselyid = :kyselyid;

-- :name poista-kyselyn-kyselykerrat! :! :n
DELETE FROM kyselykerta WHERE kyselyid = :kyselyid;

--:name poista-kyselykerta! :! :n
DELETE FROM kyselykerta WHERE kyselykertaid = :kyselykertaid;

-- :name laske-kyselyn-kyselykerrat :? :1
SELECT count(*) AS lkm FROM kyselykerta WHERE kyselyid = :kyselyid;

-- :name laske-kyselykerran-vastaajat :? :1
SELECT count(v) AS vastaajia FROM kyselykerta kk
JOIN vastaajatunnus vt ON kk.kyselykertaid = vt.kyselykertaid
JOIN vastaaja v on vt.vastaajatunnusid = v.vastaajatunnusid
WHERE kk.kyselykertaid = :kyselykertaid;

-- :name set-kyselykerta-lukittu! :! :n
UPDATE kyselykerta SET lukittu = :lukittu,
                       muutettu_kayttaja = :kayttaja
WHERE kyselykertaid = :kyselykertaid;

-- :name hae-kyselykerran-oppilaitokset :? :*
SELECT DISTINCT o.oppilaitoskoodi, o.nimi_fi, o.nimi_sv, o.nimi_en
FROM kyselykerta kk
JOIN vastaajatunnus vt on kk.kyselykertaid = vt.kyselykertaid
JOIN oppilaitos o on vt.valmistavan_koulutuksen_oppilaitos = o.oppilaitoskoodi
WHERE kk.kyselykertaid = :kyselykertaid;

-- :name samanniminen-kyselykerta? :? :*
SELECT 1 AS samanniminen FROM kysely k
JOIN kyselykerta kk ON kk.kyselyid = k.kyselyid
WHERE k.koulutustoimija IN (SELECT koulutustoimija FROM kysely WHERE kyselyid = :kyselyid)
AND kk.nimi = :nimi;

-- :name hae-kyselykerran-kysely-tyyppi :? :1
SELECT k.tyyppi FROM kyselykerta kk JOIN kysely k ON k.kyselyid = kk.kyselyid WHERE kk.kyselykertaid = :kyselykertaid;
