--:name hae-voimassaoleva-kayttaja :? :1
SELECT * FROM kayttaja
WHERE uid ILIKE :uid
      AND voimassa = TRUE
      AND (muutettuaika + :voimassaolo::interval >= now()
           OR uid IN ('JARJESTELMA', 'KONVERSIO', 'INTEGRAATIO', 'VASTAAJA'));

--:name hae-oid->ytunnus :? :*
SELECT oid, ytunnus FROM koulutustoimija WHERE oid IS NOT NULL;

--:name passivoi-kayttaja! :! :n
UPDATE kayttaja SET voimassa = FALSE WHERE uid = :uid;

--:name hae-kayttaja :? :1
SELECT * FROM kayttaja WHERE oid = :kayttajaOid;

--:name paivita-kayttaja! :! :n
UPDATE kayttaja SET etunimi = :etunimi, sukunimi = :sukunimi, voimassa = TRUE
WHERE oid = :kayttajaOid;

--:name lisaa-kayttaja! :! :n
INSERT INTO kayttaja (oid, uid, etunimi, sukunimi, voimassa)
    VALUES (:kayttajaOid, :uid, :etunimi, :sukunimi, TRUE );

--:name hae-roolit :? :*
SELECT organisaatio, kayttooikeus FROM rooli_organisaatio WHERE kayttaja = :kayttaja;

--:name aseta-roolin-tila! :! :n
UPDATE rooli_organisaatio SET voimassa = :voimassa
WHERE kayttaja = :kayttaja AND kayttooikeus = :kayttooikeus AND organisaatio = :organisaatio;

--:name lisaa-rooli! :! :n
INSERT INTO rooli_organisaatio (kayttaja, kayttooikeus, organisaatio, voimassa) VALUES (:kayttaja, :kayttooikeus, :organisaatio, TRUE);

-- :name hae-voimassaolevat-roolit :? :*
SELECT ro.kayttooikeus, ro.organisaatio, ro.rooli_organisaatio_id,
k.nimi_fi AS koulutustoimija_fi, k.nimi_sv AS koulutustoimija_sv, k.nimi_en AS koulutustoimija_en
FROM rooli_organisaatio ro
JOIN koulutustoimija k on ro.organisaatio = k.ytunnus
WHERE ro.kayttaja = :kayttajaOid
AND ro.voimassa = TRUE;

-- :name hae-impersonoitavat-kayttajat :? :*
SELECT oid, uid, etunimi, sukunimi FROM kayttaja
WHERE NOT EXISTS (SELECT rooli_organisaatio_id FROM rooli_organisaatio WHERE kayttaja = oid AND kayttooikeus = 'YLLAPITAJA')
AND oid NOT IN ('JARJESTELMA', 'KONVERSIO', 'INTEGRAATIO', 'VASTAAJA')
AND voimassa = TRUE;

-- :name hae-laajennettu :? :1
SELECT EXISTS(SELECT * FROM koulutustoimija_ja_tutkinto WHERE koulutustoimija IN (:v*:koulutustoimijat) AND laaja_oppisopimuskoulutus = TRUE) AS laajennettu;