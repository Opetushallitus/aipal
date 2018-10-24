-- :name hae-kyselykerta :? :1
SELECT * FROM kyselykerta WHERE kyselykertaid = :kyselykertaid;

-- :name hae-palaute-kyselykerta :? :*
SELECT kk.kyselykertaid FROM kyselykerta kk
  JOIN kysely k ON kk.kyselyid = k.kyselyid
  JOIN oppilaitos o ON k.koulutustoimija = o.koulutustoimija
  WHERE o.oppilaitoskoodi = :oppilaitoskoodi AND kk.nimi = :kyselykertanimi
  AND kk.lukittu = FALSE AND k.tyyppi = 1;

-- :name hae-rekry-kyselykerta :? :*
SELECT kk.kyselykertaid FROM kyselykerta kk
  JOIN kysely k ON kk.kyselyid = k.kyselyid
  JOIN oppilaitos o ON k.koulutustoimija = o.koulutustoimija
  WHERE o.oppilaitoskoodi = :oppilaitoskoodi AND k.tyyppi = 2
    AND kk.automaattinen = TRUE
  AND (kk.kategoria ->'vuosi')::TEXT::INTEGER = :vuosi
  AND kk.lukittu = FALSE;

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
  JOIN koulutustoimija kt ON kt.ytunnus = vt.valmistavan_koulutuksen_jarjestaja OR kt.ytunnus = vt.taustatiedot->>'hankintakoulutuksen_toteuttaja'
WHERE kk.kyselyid = :kyselyid;