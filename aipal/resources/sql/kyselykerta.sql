-- :name hae-kyselykerta :? :1
SELECT * FROM kyselykerta WHERE kyselykertaid = :kyselykertaid;

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
  AND (kk.kategoria ->'vuosi')::TEXT::INTEGER = :vuosi
  AND kk.lukittu = FALSE;

-- :name hae-automaatti-kyselykerta :? :1
SELECT kk.kyselykertaid FROM kyselykerta kk
  JOIN kysely k on kk.kyselyid = k.kyselyid
WHERE k.koulutustoimija = :koulutustoimija
AND kk.automaattinen @> NOW()::DATE
AND kk.kategoria ->> 'tarkenne' = :tarkenne
AND k.tila = 'julkaistu' AND kk.lukittu = FALSE;

-- :name hae-kyselykerta-nimella-ja-koulutustoimijalla :? :*
SELECT * FROM kyselykerta kk
JOIN kysely k on kk.kyselyid = k.kyselyid
WHERE kk.nimi = :nimi
AND k.koulutustoimija = :koulutustoimija;

-- :name hae-kyselyn-tutkinnot :? :*
SELECT DISTINCT t.tutkintotunnus, t.nimi_fi, t.nimi_sv, t.nimi_en
FROM vastaajatunnus vt
  JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
  JOIN tutkinto t ON t.tutkintotunnus = vt.taustatiedot->>'tutkinto'
WHERE kk.kyselyid = :kyselyid;

-- :name hae-kyselyn-toimipaikat :? :*
SELECT DISTINCT t.toimipaikkakoodi, t.nimi_fi, t.nimi_sv, t.nimi_en
FROM vastaajatunnus vt
  JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
  JOIN toimipaikka t ON t.toimipaikkakoodi = vt.taustatiedot->>'toimipaikka'
WHERE kk.kyselyid = :kyselyid;

-- :name hae-kyselyn-koulutustoimijat :? :*
SELECT DISTINCT kt.ytunnus, kt.nimi_fi, kt.nimi_sv, kt.nimi_en
FROM vastaajatunnus vt
  JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
  JOIN koulutustoimija kt ON kt.ytunnus = vt.taustatiedot->>'hankintakoulutuksen_toteuttaja'
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

-- :name luo-kyselykerta! :! :n
INSERT INTO kyselykerta (kyselyid, nimi, voimassa_alkupvm, luotu_kayttaja, muutettu_kayttaja, automaattinen, kategoria)
VALUES (:kyselyid, :nimi, :voimassa_alkupvm, :kayttaja, :kayttaja, :automaattinen::DATERANGE, :kategoria);

-- :name paata-kyselykerrat! :! :n
WITH t AS (SELECT kk.kyselykertaid as id FROM kysely k JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
    WHERE k.koulutustoimija = :koulutustoimija AND k.tyyppi = :tyyppi AND kk.automaattinen IS NOT NULL AND UPPER(kk.automaattinen) IS NULL AND kk.automaattinen @> now()::DATE)
UPDATE kyselykerta SET automaattinen = daterange(LOWER(automaattinen), :paattymis_pvm::DATE) FROM t WHERE t.id = kyselykertaid;

-- :name hae-koulutustoimijan-kyselykerrat :? :*
SELECT kk.kyselykertaid, kk.kyselyid, kk.nimi, kk.voimassa_alkupvm, kk.voimassa_loppupvm, kk.kaytettavissa, kk.luotuaika, kk.lukittu,
       (SELECT coalesce(sum(kohteiden_lkm),0) FROM vastaajatunnus v2 WHERE kyselykertaid = kk.kyselykertaid) AS vastaajatunnuksia,
       coalesce(count(DISTINCT vs),0) AS vastaajia,
       coalesce(sum(DISTINCT vt.kohteiden_lkm) FILTER (WHERE vt.kaytettavissa), 0) AS aktiivisia_vastaajatunnuksia,
       count(DISTINCT vs) FILTER (WHERE vt.kaytettavissa) AS aktiivisia_vastaajia
FROM kyselykerta kk
         JOIN kysely k ON kk.kyselyid = k.kyselyid
         LEFT JOIN vastaajatunnus vt on kk.kyselykertaid = vt.kyselykertaid
         LEFT JOIN vastaaja vs on vt.vastaajatunnusid = vs.vastaajatunnusid
WHERE k.koulutustoimija = :koulutustoimija
GROUP BY kk.kyselykertaid, kk.kyselyid, kk.nimi, kk.voimassa_alkupvm, kk.voimassa_loppupvm, kk.kaytettavissa, kk.luotuaika, kk.lukittu;

-- :name hae-kyselyn-kyselykerrat :? :*
SELECT * FROM kyselykerta WHERE kyselyid = :kyselyid;

-- :name poista-kyselyn-kyselykerrat! :! :n
DELETE FROM kyselykerta WHERE kyselyid = :kyselyid;

-- :name laske-kyselyn-kyselykerrat :? :1
SELECT count(*) AS lkm FROM kyselykerta WHERE kyselyid = :kyselyid;