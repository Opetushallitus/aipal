-- :name hae-vastaukset :? :*
SELECT v.vastausid, k.kysymysid, vs.vastaajaid,
  v.vapaateksti, v.numerovalinta, v.vaihtoehto, v.en_osaa_sanoa, v.vastausaika,
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
                  WHERE kk.kyselyid = k.kyselyid AND k.tila IN ('luonnos', 'suljettu')) AS poistettavissa,
       (EXISTS (SELECT 1 FROM amispalaute_automatisointi a WHERE a.voimassa_alkaen < now() AND a.koulutustoimija = k.koulutustoimija))
           OR (k.kategoria->>'automatisointi_tunniste' IS NOT NULL) AS automatisoitu
       FROM kysely k WHERE kyselyid = :kyselyid;

-- :name hae-kyselyt :? :*
SELECT k.kyselyid, k.nimi_fi, k.nimi_sv, k.nimi_en, k.voimassa_alkupvm, k.voimassa_loppupvm,
       k.tila, k.kaytettavissa, k.uudelleenohjaus_url, k.sivutettu, k.koulutustoimija,
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
       (EXISTS(SELECT 1 FROM amispalaute_automatisointi a WHERE a.voimassa_alkaen < now() AND a.koulutustoimija = k.koulutustoimija))
           OR (k.kategoria->>'automatisointi_tunniste' IS NOT NULL) AS automatisoitu
FROM kysely k
WHERE k.koulutustoimija = :koulutustoimija
ORDER BY k.kyselyid;

-- :name hae-kyselykerran-kysely :? :1
SELECT k.* FROM kyselykerta kk
JOIN kysely k on kk.kyselyid = k.kyselyid
WHERE kk.kyselykertaid = :kyselykertaid;

-- :name luo-kysely! :<!
INSERT INTO kysely (koulutustoimija, voimassa_alkupvm, nimi_fi, nimi_sv, nimi_en, selite_fi, selite_sv, selite_en, kyselypohjaid, tyyppi, tila, kategoria, luotu_kayttaja, muutettu_kayttaja)
VALUES (:koulutustoimija, :voimassa_alkupvm, :nimi_fi, :nimi_sv, :nimi_en, :selite_fi, :selite_sv, :selite_en, :kyselypohjaid, :tyyppi, :tila, :kategoria, :kayttaja, :kayttaja)
RETURNING kyselyid;

-- :name lisaa-kyselyn-kysymysryhma! :! :n
INSERT INTO kysely_kysymysryhma (kyselyid, kysymysryhmaid, jarjestys) VALUES (kyselyid, kysymysryhmaid, jarjestys);

-- :name liita-kyselyn-kyselypohja! :! :n
INSERT INTO kysely_kysymysryhma (kyselyid, kysymysryhmaid, jarjestys, luotu_kayttaja, muutettu_kayttaja)
SELECT :kyselyid, kysymysryhmaid, jarjestys, :kayttaja, :kayttaja FROM kysymysryhma_kyselypohja WHERE kyselypohjaid = :kyselypohjaid;

-- :name liita-kyselyn-kysymykset! :! :n
INSERT INTO kysely_kysymys (kyselyid, kysymysid, luotu_kayttaja, muutettu_kayttaja)
SELECT :kyselyid, kysymysid, :kayttaja, :kayttaja FROM kysymys
WHERE kysymysryhmaid IN (SELECT kysymysryhmaid FROM kysely_kysymysryhma WHERE kyselyid = :kyselyid);

-- :name hae-automaattikysely-koulutustoimijat :? :*
SELECT ytunnus, nimi_fi FROM koulutustoimija kt
-- löytyy aiempi halutun tyyppinen kysely
WHERE EXISTS (SELECT 1 FROM kysely k WHERE k.koulutustoimija = kt.ytunnus AND k.tyyppi = :kyselytyyppi)
-- muttei löydy voimassaolevaa automaattisesti luotua
AND NOT EXISTS (SELECT 1 FROM kysely k WHERE k.koulutustoimija = kt.ytunnus AND k.tyyppi = :kyselytyyppi AND k.kyselypohjaid = :kyselypohjaid AND k.kategoria->>'automatisointi_tunniste' = :tunniste);

-- :name hae-automaattikysely-data :? :*
SELECT * FROM automaattikysely WHERE automatisointi_voimassa_loppupvm >= now();
