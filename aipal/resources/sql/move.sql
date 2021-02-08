-- :name hae-move-vastaanottajat :? :*
SELECT o.oppilaitoskoodi, o.nimi_fi, o.nimi_sv, o.sahkoposti, vt.tunnus
FROM oppilaitos o
JOIN vastaajatunnus vt on o.oppilaitoskoodi = vt.valmistavan_koulutuksen_oppilaitos
WHERE o.oppilaitostyyppi IN ('11','12','19')
AND o.voimassa = TRUE
AND o.sahkoposti IS NOT NULL
AND o.postinumero NOT ILIKE '22%'
AND vt.kyselykertaid = :kyselykertaid
AND NOT EXISTS (SELECT id FROM email_log l WHERE l.sahkoposti = o.sahkoposti AND l.tunniste = :tunniste)
AND NOT EXISTS (SELECT 1 FROM vastaaja WHERE vastaajatunnusid = vt.vastaajatunnusid);

-- :name hae-move-oppilaitokset-ilman-tunnusta :? :*
SELECT o.oppilaitoskoodi, o.nimi_fi, o.nimi_sv, o.sahkoposti
FROM oppilaitos o
WHERE oppilaitostyyppi IN ('11','12','19')
  AND voimassa = TRUE
  AND postinumero NOT ILIKE '22%'
  AND NOT EXISTS(
      SELECT vastaajatunnusid FROM vastaajatunnus vt
      WHERE vt.kyselykertaid = :kyselykertaid
      AND vt.valmistavan_koulutuksen_oppilaitos = o.oppilaitoskoodi);

-- :name hae-move-kyselykerta :? :*
SELECT kk.kyselykertaid FROM kysely k
JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
WHERE k.tyyppi = 'move' AND k.metatiedot->>'automatisointi_tunniste' = :tunniste
AND kk.automaattinen @> NOW()::DATE;

-- :name lisaa-move-tunnus! :! :n
INSERT INTO vastaajatunnus (kyselykertaid, tunnus, kohteiden_lkm, valmistavan_koulutuksen_oppilaitos, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, voimassa_alkupvm)
VALUES (:kyselykertaid, :tunnus, null, :oppilaitoskoodi, 'JARJESTELMA', 'JARJESTELMA', now(), now(), now());

-- :name hae-move-muistutus-vastaanottajat :? :*
SELECT o.oppilaitoskoodi, o.nimi_fi, o.nimi_sv, o.sahkoposti, vt.tunnus
FROM oppilaitos o
         JOIN vastaajatunnus vt on o.oppilaitoskoodi = vt.valmistavan_koulutuksen_oppilaitos
WHERE o.oppilaitostyyppi IN ('11','12','19')
  AND o.voimassa = TRUE
  AND o.sahkoposti IS NOT NULL
  AND o.postinumero NOT ILIKE '22%'
  AND vt.kyselykertaid = :kyselykertaid
  AND NOT EXISTS (SELECT vastaajaid FROM vastaaja WHERE vastaajatunnusid = vt.vastaajatunnusid);