
-- :name kyselytyypin_kentat :? :*
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
INSERT INTO vastaajatunnus (tunnus, kyselykertaid, suorituskieli, tutkintotunnus, taustatiedot,
                          vastaajien_lkm,
                          valmistavan_koulutuksen_oppilaitos,
                          voimassa_alkupvm, voimassa_loppupvm, luotu_kayttaja, muutettu_kayttaja)
VALUES (:tunnus, :kyselykertaid, :kieli, :tutkinto, :taustatiedot,
      :vastaajien_lkm, :valmistavan_koulutuksen_oppilaitos,
      :voimassa_alkupvm, :voimassa_loppupvm, :kayttaja, :kayttaja)
RETURNING vastaajatunnusid;

-- :name paivita-taustatiedot! :! :n
UPDATE vastaajatunnus SET
tutkintotunnus = :tutkintotunnus,
valmistavan_koulutuksen_oppilaitos = :oppilaitos,
taustatiedot = :taustatiedot
WHERE tunnus = :vastaajatunnus;

-- :name hae-viimeisin-tutkinto :? :*
SELECT t.* FROM vastaajatunnus vt
JOIN tutkinto t ON t.tutkintotunnus = vt.tutkintotunnus
JOIN koulutustoimija_ja_tutkinto ktt
  ON (ktt.tutkinto = t.tutkintotunnus AND ktt.koulutustoimija = :koulutustoimija)
WHERE vt.kyselykertaid = :kyselykertaid
ORDER BY vt.luotuaika DESC;

-- :name hae-vastaajatunnus :? :*
SELECT vt.vastaajatunnusid, vt.kyselykertaid, vt.tutkintotunnus, vt.tunnus, vt.lukittu, vt.luotu_kayttaja, vt.muutettu_kayttaja,
       vt.luotuaika, vt.muutettuaika, vt.valmistavan_koulutuksen_jarjestaja, vt.valmistavan_koulutuksen_oppilaitos,
       vt.suorituskieli, vt.kunta, vt.taustatiedot, vt.voimassa_alkupvm, vt.voimassa_loppupvm, vt.vastaajien_lkm, vt.kaytettavissa,
-- query vt.*, merge with taustatiedot??
-- drop old columns, check migration to jsonb
t.nimi_fi, t.nimi_sv, t.nimi_en, kaytettavissa(vt) AS kaytettavissa, (vt.taustatiedot ->> 'koulutusmuoto') AS koulutusmuoto,
COALESCE(COALESCE(vt.voimassa_loppupvm, kk.voimassa_loppupvm, k.voimassa_loppupvm) + 30 > CURRENT_DATE, TRUE) AS muokattavissa,
(SELECT count(*) FROM vastaaja WHERE vastannut = TRUE AND vastaajatunnusid = vt.vastaajatunnusid) AS vastausten_lkm,
o.oppilaitoskoodi, o.nimi_fi AS oppilaitos_nimi_fi, o.nimi_sv AS oppilaitos_nimi_sv, o.nimi_en AS oppilaitos_nimi_en,
kt.ytunnus, kt.nimi_fi AS koulutustoimija_nimi_fi, kt.nimi_sv AS koulutustoimija_nimi_sv, kt.nimi_en AS koulutustoimija_nimi_en,
tmp.toimipaikkakoodi, tmp.nimi_fi AS toimipaikka_nimi_fi, tmp.nimi_sv AS toimipaikka_nimi_sv, tmp.nimi_en AS toimipaikka_nimi_en
FROM vastaajatunnus vt
LEFT JOIN tutkinto t ON vt.tutkintotunnus = t.tutkintotunnus
LEFT JOIN koulutustoimija kt ON vt.valmistavan_koulutuksen_jarjestaja = kt.ytunnus
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

-- :name muokkaa-vastaajien-maaraa! :! :n
UPDATE vastaajatunnus SET vastaajien_lkm = :vastaajia WHERE vastaajatunnusid = :vastaajatunnusid;

-- :name vastaajien-lkm :? :1
SELECT count(*) FROM vastaaja AS vastaajia WHERE vastaajatunnusid = :vastaajatunnusid;
