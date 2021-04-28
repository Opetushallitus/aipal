
-- :name kyselytyypin-kentat :? :*
SELECT * FROM kyselytyyppi_kentat WHERE kyselytyyppi = :kyselytyyppi;

-- :name kyselykerran-tyyppi :? :1
SELECT k.tyyppi, ktk.kentta_id FROM kysely k
  JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
  JOIN kyselytyyppi_kentat ktk ON k.tyyppi = ktk.kyselytyyppi
  WHERE kk.kyselykertaid = :kyselykertaid;

-- :name kyselyn-kentat :? :*
SELECT ktk.id, ktk.kentta_id, ktk.kentta_fi, ktk.kentta_sv, ktk.kentta_en, ktk.raportointi FROM kyselytyyppi kt
  JOIN kyselytyyppi_kentat ktk ON ktk.kyselytyyppi = kt.id
  JOIN kysely k ON k.tyyppi = kt.id
--~ (if (:kyselykertaid params) "WHERE k.kyselyid = (SELECT kyselyid FROM kyselykerta WHERE kyselykertaid = :kyselykertaid)")
--~ (if (:kyselyid params) "WHERE k.kyselyid = :kyselyid")
ORDER BY ktk.id;


-- :name lisaa-vastaajatunnus! :<!
INSERT INTO vastaajatunnus (tunnus, kyselykertaid, suorituskieli, tutkintotunnus, taustatiedot, metatiedot,
                          kohteiden_lkm,
                          valmistavan_koulutuksen_oppilaitos,
                          voimassa_alkupvm, voimassa_loppupvm, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika)
VALUES (:tunnus, :kyselykertaid, :kieli, :tutkinto, :taustatiedot, :metatiedot,
      :kohteiden_lkm, :valmistavan_koulutuksen_oppilaitos,
      :voimassa_alkupvm, :voimassa_loppupvm, :kayttaja, :kayttaja, now(), now())
RETURNING vastaajatunnusid;

-- :name paivita-taustatiedot! :! :n
UPDATE vastaajatunnus SET
tutkintotunnus = :tutkintotunnus,
valmistavan_koulutuksen_oppilaitos = :oppilaitos,
taustatiedot = :taustatiedot
WHERE tunnus = :vastaajatunnus;

-- :name paivita-metatiedot! :! :n
UPDATE vastaajatunnus
SET metatiedot = COALESCE(metatiedot || :metatiedot, :metatiedot), muutettu_kayttaja = :kayttaja
WHERE tunnus = :tunnus AND luotu_kayttaja = :kayttaja;

-- :name hae-viimeisin-tutkinto :? :*
SELECT t.* FROM vastaajatunnus vt
JOIN tutkinto t ON t.tutkintotunnus = vt.tutkintotunnus
JOIN koulutustoimija_ja_tutkinto ktt
  ON (ktt.tutkinto = t.tutkintotunnus AND ktt.koulutustoimija = :koulutustoimija)
WHERE vt.kyselykertaid = :kyselykertaid
ORDER BY vt.luotuaika DESC;

-- :name hae-vastaajatunnus :? :*
SELECT vt.vastaajatunnusid, vt.kyselykertaid, vt.tutkintotunnus, vt.tunnus, vt.lukittu, vt.luotu_kayttaja, vt.muutettu_kayttaja,
       vt.luotuaika, vt.muutettuaika, vt.valmistavan_koulutuksen_oppilaitos, vt.metatiedot,
       vt.suorituskieli, vt.kunta, vt.taustatiedot, vt.voimassa_alkupvm, vt.voimassa_loppupvm, vt.kohteiden_lkm, vt.kaytettavissa,
t.nimi_fi, t.nimi_sv, t.nimi_en, kaytettavissa(vt) AS kaytettavissa, (vt.taustatiedot ->> 'koulutusmuoto') AS koulutusmuoto,
COALESCE(COALESCE(vt.voimassa_loppupvm, kk.voimassa_loppupvm, k.voimassa_loppupvm) + 30 > CURRENT_DATE, TRUE) AS muokattavissa,
(SELECT count(*) FROM vastaaja WHERE vastaajatunnusid = vt.vastaajatunnusid) AS vastausten_lkm,
o.oppilaitoskoodi, o.nimi_fi AS oppilaitos_nimi_fi, o.nimi_sv AS oppilaitos_nimi_sv, o.nimi_en AS oppilaitos_nimi_en,
tmp.toimipaikkakoodi, tmp.nimi_fi AS toimipaikka_nimi_fi, tmp.nimi_sv AS toimipaikka_nimi_sv, tmp.nimi_en AS toimipaikka_nimi_en
FROM vastaajatunnus vt
LEFT JOIN tutkinto t ON vt.tutkintotunnus = t.tutkintotunnus
LEFT JOIN oppilaitos o ON vt.valmistavan_koulutuksen_oppilaitos = o.oppilaitoskoodi
LEFT JOIN toimipaikka tmp ON vt.taustatiedot->>'toimipaikka' = tmp.toimipaikkakoodi
JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
JOIN kysely k ON kk.kyselyid = k.kyselyid
WHERE vt.kyselykertaid = :kyselykertaid
--~ (if (:vastaajatunnusid params) "AND vastaajatunnusid = :vastaajatunnusid")
--~ (if (:oid params) "AND vt.luotu_kayttaja = :oid")
ORDER BY vt.luotuaika DESC;

-- :name vastaajatunnus-olemassa? :? :1
SELECT TRUE AS olemassa FROM vastaajatunnus WHERE tunnus = :vastaajatunnus;

-- :name lukitse-vastaajatunnus! :! :n
UPDATE vastaajatunnus SET lukittu = :lukittu WHERE vastaajatunnusid = :vastaajatunnusid;

-- :name poista-vastaajatunnus! :! :n
DELETE FROM vastaajatunnus WHERE vastaajatunnusid = :vastaajatunnusid;

-- :name poista-kyselykerran-tunnukset! :! :n
DELETE FROM vastaajatunnus WHERE kyselykertaid = :kyselykertaid;

-- :name muokkaa-vastaajien-maaraa! :! :n
UPDATE vastaajatunnus SET kohteiden_lkm = :vastaajia WHERE vastaajatunnusid = :vastaajatunnusid;

-- :name vastaajien-lkm :? :1
SELECT count(*) FROM vastaaja AS vastaajia WHERE vastaajatunnusid = :vastaajatunnusid;

-- :name vastaajatunnus-status :? :1
SELECT vt.vastaajatunnusid, vt.tunnus, vt.voimassa_loppupvm,
EXISTS(SELECT 1 FROM vastaaja v WHERE v.vastaajatunnusid = vt.vastaajatunnusid) AS vastattu
FROM vastaajatunnus vt
WHERE vt.tunnus = :tunnus;

-- :name hae-kyselyn-kohteet :? :*
SELECT vt.tunnus, kk.nimi, vt.voimassa_alkupvm,
       vt.kohteiden_lkm, count(v) AS vastaajien_lkm,
       t.tutkintotunnus, t.nimi_fi AS tutkinto_fi, t.nimi_sv AS tutkinto_sv, t.nimi_en AS tutkinto_en
FROM vastaajatunnus vt
LEFT JOIN vastaaja v on vt.vastaajatunnusid = v.vastaajatunnusid
JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
JOIN tutkinto t on vt.taustatiedot->>'tutkinto' = t.tutkintotunnus
WHERE kk.kyselyid = :kyselyid
GROUP BY vt.tunnus, kk.nimi, vt.voimassa_alkupvm, vt.kohteiden_lkm,
         t.tutkintotunnus, t.nimi_fi, t.nimi_sv, t.nimi_en;

-- :name hae-kyselyn-vastaajat :? :*
SELECT vt.tunnus, kk.nimi, vt.voimassa_alkupvm, v.vastausaika, t.tutkintotunnus,
       t.nimi_fi AS tutkinto_fi, t.nimi_sv AS tutkinto_sv, t.nimi_en AS tutkinto_en
FROM vastaaja v
JOIN vastaajatunnus vt ON v.vastaajatunnusid = vt.vastaajatunnusid
JOIN tutkinto t ON vt.taustatiedot->>'tutkinto' = t.tutkintotunnus
JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
WHERE kk.kyselyid = :kyselyid;

--:name lisaa-nippu! :! :n
INSERT INTO nippu (tunniste, kyselyid, voimassa_alkupvm, voimassa_loppupvm, taustatiedot)
VALUES (:tunniste, :kyselyid, :voimassa_alkupvm, :voimassa_loppupvm, :taustatiedot);

--:name liita-tunnukset-nippuun! :! :n
UPDATE vastaajatunnus SET metatiedot = coalesce(metatiedot, '{}') || jsonb_build_object('nippu', :tunniste)
WHERE tunnus IN (:v*:tunnukset);

--:name hae-tunnukset :? :*
SELECT vt.tunnus, vt.valmistavan_koulutuksen_oppilaitos, vt.taustatiedot, k.koulutustoimija FROM vastaajatunnus vt
JOIN kyselykerta kk on vt.kyselykertaid = kk.kyselykertaid
JOIN kysely k on kk.kyselyid = k.kyselyid
WHERE tunnus IN (:v*:tunnukset);

-- :name hae-kyselykerran-niput :? :*
SELECT DISTINCT n.tunniste, n.kyselyid, n.voimassa_alkupvm, n.voimassa_loppupvm, n.taustatiedot,
                count(vt) AS kohteiden_lkm, count(v) AS vastausten_lkm FROM nippu n
JOIN kysely k ON n.kyselyid = k.kyselyid
JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
LEFT JOIN vastaajatunnus vt ON vt.metatiedot->>'nippu' = n.tunniste
LEFT JOIN vastaaja v ON vt.vastaajatunnusid = v.vastaajatunnusid
WHERE kk.kyselykertaid = :kyselykertaid
GROUP BY n.tunniste, n.kyselyid, n.voimassa_alkupvm, n.kyselyid, n.tunniste, n.voimassa_loppupvm, n.taustatiedot;

-- :name hae-nippu :? :1
SELECT * FROM nippu WHERE tunniste = :tunniste;

-- :name hae-nipun-tunnukset :? :*
SELECT vt.*,
       EXISTS(SELECT 1 FROM vastaaja v WHERE v.vastaajatunnusid = vt.vastaajatunnusid) AS vastattu
       FROM vastaajatunnus vt
WHERE vt.metatiedot->>'nippu' = :tunniste;

-- :name poista-nippu! :! :n
DELETE FROM nippu WHERE tunniste = :tunniste;

-- :name poista-tunnukset-nipusta! :! :n
UPDATE vastaajatunnus SET metatiedot = metatiedot - 'nippu' WHERE metatiedot->>'nippu' = :tunniste;
