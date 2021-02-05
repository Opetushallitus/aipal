-- :name hae-vastaukset :? :*
SELECT v.vastausid, k.kysymysid, vs.vastaajaid,
  v.vapaateksti, v.numerovalinta::int, v.vaihtoehto, v.en_osaa_sanoa, v.vastausaika,
  vs.luotuaika AS vastaaja_luotuaika,
  vt.tunnus AS vastaajatunnus, vt.tutkintotunnus, vt.taustatiedot,
  k.kysymysryhmaid, k.vastaustyyppi, o.oppilaitoskoodi,
       o.nimi_fi AS oppilaitos_nimi_fi, o.nimi_sv AS oppilaitos_nimi_sv, o.nimi_en AS oppilaitos_nimi_en
FROM vastaus v
  JOIN kysymys k ON v.kysymysid = k.kysymysid
  JOIN kysymysryhma kr ON k.kysymysryhmaid = kr.kysymysryhmaid
  JOIN vastaaja vs ON v.vastaajaid = vs.vastaajaid
  JOIN kyselykerta kk ON vs.kyselykertaid = kk.kyselykertaid
  JOIN vastaajatunnus vt ON vt.vastaajatunnusid = vs.vastaajatunnusid
  LEFT JOIN oppilaitos o on vt.valmistavan_koulutuksen_oppilaitos = o.oppilaitoskoodi
WHERE kk.kyselyid = :kyselyid;

-- :name hae-monivalinnat :? :*
SELECT * FROM monivalintavaihtoehto
WHERE kysymysid IN (:v*:kysymysidt);
-- :name hae-kyselytyypit :? :*
SELECT * FROM kyselytyyppi;

-- :name hae-kysely :? :1
SELECT *, k.kaytettavissa,
       NOT EXISTS(SELECT 1
                  FROM kyselykerta kk
                           JOIN vastaajatunnus vt ON kk.kyselykertaid = vt.kyselykertaid
                           JOIN vastaaja v on kk.kyselykertaid = vt.kyselykertaid
                  WHERE kk.kyselyid = k.kyselyid) AS poistettavissa,
       (EXISTS (SELECT 1 FROM amispalaute_automatisointi a WHERE a.voimassa_alkaen < now() AND a.koulutustoimija = k.koulutustoimija)) AS automatisoitu
       FROM kysely k WHERE kyselyid = :kyselyid;

-- :name hae-kyselyt :? :*
SELECT k.kyselyid, k.nimi_fi, k.nimi_sv, k.nimi_en, k.voimassa_alkupvm, k.voimassa_loppupvm,
       k.tila, k.kaytettavissa, k.uudelleenohjaus_url, k.sivutettu, k.koulutustoimija,
       k.kategoria,
       NOT EXISTS(SELECT 1
                  FROM kyselykerta kk
                           JOIN vastaajatunnus vt ON kk.kyselykertaid = vt.kyselykertaid
                           JOIN vastaaja v on kk.kyselykertaid = vt.kyselykertaid
                  WHERE kk.kyselyid = k.kyselyid AND k.tila IN ('luonnos', 'suljettu')) AS poistettavissa,
       (SELECT COUNT(*) FROM kysely_kysymysryhma kkr WHERE kkr.kyselyid = k.kyselyid) AS kysymysryhmien_lkm,
       (SELECT now() < k.voimassa_alkupvm) AS tulevaisuudessa,
       (SELECT CASE WHEN k.tila = 'luonnos' THEN 'luonnos'
                    WHEN k.kaytettavissa OR now() < k.voimassa_alkupvm THEN 'julkaistu'
                    ELSE 'suljettu' END) AS sijainti,
       (EXISTS(SELECT 1 FROM amispalaute_automatisointi a WHERE a.voimassa_alkaen < now() AND a.koulutustoimija = k.koulutustoimija))  AS automatisoitu
FROM kysely k
WHERE k.koulutustoimija = :koulutustoimija
ORDER BY k.kyselyid;

-- :name hae-kyselykerran-kysely :? :1
SELECT k.* FROM kyselykerta kk
JOIN kysely k on kk.kyselyid = k.kyselyid
WHERE kk.kyselykertaid = :kyselykertaid;

-- :name aseta-kyselyn-tila! :! :n
UPDATE kysely SET tila = :tila,
                  muutettu_kayttaja = :muutettu_kayttaja
WHERE kyselyid = :kyselyid;

-- :name luo-kysely! :<!
INSERT INTO kysely (koulutustoimija, voimassa_alkupvm, voimassa_loppupvm, nimi_fi, nimi_sv, nimi_en, selite_fi, selite_sv, selite_en, kyselypohjaid, tyyppi, tila,
                    kategoria, uudelleenohjaus_url, luotu_kayttaja, muutettu_kayttaja, muutettuaika, luotuaika)
VALUES (:koulutustoimija, :voimassa_alkupvm, :voimassa_loppupvm, :nimi_fi, :nimi_sv, :nimi_en, :selite_fi, :selite_sv, :selite_en, :kyselypohjaid, :tyyppi, :tila,
        :kategoria, :uudelleenohjaus_url, :kayttaja, :kayttaja, now(), now())
RETURNING kyselyid;

-- :name muokkaa-kyselya! :<!
UPDATE kysely SET
      nimi_fi = :nimi_fi, nimi_sv = :nimi_sv, nimi_en = :nimi_en, selite_fi = :selite_fi, selite_sv = :selite_sv, selite_en = :selite_en,
      voimassa_alkupvm = :voimassa_alkupvm, voimassa_loppupvm = :voimassa_loppupvm,
      tila = :tila, uudelleenohjaus_url = :uudelleenohjaus_url, sivutettu = :sivutettu, tyyppi = :tyyppi
WHERE kyselyid = :kyselyid
RETURNING kyselyid;

-- :name lisaa-kyselyn-kysymysryhma! :! :n
INSERT INTO kysely_kysymysryhma (kyselyid, kysymysryhmaid, jarjestys, luotu_kayttaja, muutettu_kayttaja)
VALUES (:kyselyid, :kysymysryhmaid, :jarjestys, :kayttaja, :kayttaja);


-- :name poista-kyselyn-kysymysryhmat! :! :n
DELETE FROM kysely_kysymysryhma WHERE kyselyid = :kyselyid;

-- :name liita-kyselyn-kyselypohja! :! :n
INSERT INTO kysely_kysymysryhma (kyselyid, kysymysryhmaid, jarjestys, luotu_kayttaja, muutettu_kayttaja)
SELECT :kyselyid, kysymysryhmaid, jarjestys, :kayttaja, :kayttaja FROM kysymysryhma_kyselypohja WHERE kyselypohjaid = :kyselypohjaid;

-- :name liita-kyselyn-kysymykset! :! :n
INSERT INTO kysely_kysymys (kyselyid, kysymysid, luotu_kayttaja, muutettu_kayttaja)
SELECT :kyselyid, kysymysid, :kayttaja, :kayttaja FROM kysymys
WHERE kysymysryhmaid IN (SELECT kysymysryhmaid FROM kysely_kysymysryhma WHERE kyselyid = :kyselyid);

-- :name hae-automaattikysely-data :? :*
SELECT * FROM automaattikysely WHERE tunniste IN (SELECT MAX(tunniste) FROM automaattikysely GROUP BY LEFT(tunniste, -4));

-- :name muuta-kyselyn-tila! :! :n
UPDATE kysely SET tila = :tila, muutettu_kayttaja = :kayttaja WHERE kyselyid = :kyselyid;

-- :name kysely-poistettavissa? :? :1
SELECT 1 AS poistettavissa FROM kysely
WHERE NOT EXISTS (SELECT 1
            FROM (vastaaja JOIN vastaajatunnus ON vastaajatunnus.vastaajatunnusid = vastaaja.vastaajatunnusid)
                           JOIN kyselykerta ON kyselykerta.kyselykertaid = vastaajatunnus.kyselykertaid
            WHERE (kyselykerta.kyselyid = kysely.kyselyid))
  AND tila IN ('luonnos', 'suljettu') AND kyselyid = :kyselyid;

-- :name poista-kyselyn-kysymysryhmat! :! :n
DELETE FROM kysely_kysymysryhma WHERE kyselyid = :kyselyid ;

-- :name lisaa-kysymys-kyselyyn! :! :n
INSERT INTO kysely_kysymys (kyselyid, kysymysid, luotu_kayttaja, muutettu_kayttaja)
VALUES (:kyselyid, :kysymysid, :kayttaja, :kayttaja);

-- :name poista-kyselyn-kysymykset! :! :n
DELETE FROM kysely_kysymys WHERE kyselyid = :kyselyid;

-- :name poista-kysely! :! :n
DELETE FROM kysely WHERE kyselyid = :kyselyid;

-- :name hae-kyselyn-pakolliset-kysymysryhmat :? :*
SELECT kkr.kysymysryhmaid FROM kysely_kysymysryhma kkr
                                   JOIN kysymysryhma kr ON kkr.kysymysryhmaid = kr.kysymysryhmaid
WHERE kkr.kyselyid = :kyselyid AND (kr.taustakysymykset = TRUE
                                    OR (kr.valtakunnallinen = TRUE AND kr.kategoria->>'lisattavissa_kyselyyn' IS NULL));

-- :name samanniminen-kysely? :? :1
SELECT TRUE FROM kysely
WHERE koulutustoimija = :koulutustoimija
AND (nimi_fi = :nimi_fi OR nimi_sv = :nimi_sv OR nimi_en = :nimi_en);

-- :name hae-kyselyn-taustakysymysryhmaid :? :1
SELECT kkr.kysymysryhmaid FROM kysely_kysymysryhma kkr
JOIN kysymysryhma k on kkr.kysymysryhmaid = k.kysymysryhmaid
WHERE k.valtakunnallinen = TRUE AND k.taustakysymykset = TRUE
AND kkr.kyselyid = :kyselyid

-- :name hae-kyselyn-oppilaitokset :? :*
SELECT DISTINCT o.oppilaitoskoodi, o.nimi_fi, o.nimi_sv, o.nimi_en
FROM kysely k
         JOIN kyselykerta kk ON kk.kyselyid = k.kyselyid
         JOIN vastaajatunnus vt on kk.kyselykertaid = vt.kyselykertaid
         JOIN oppilaitos o on vt.valmistavan_koulutuksen_oppilaitos = o.oppilaitoskoodi
WHERE k.kyselyid = :kyselyid;