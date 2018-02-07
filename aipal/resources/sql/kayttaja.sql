--:name hae-voimassaoleva-kayttaja :? :1
SELECT * FROM kayttaja
WHERE uid = :uid
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
UPDATE kayttaja SET etunimi = :etunimi, sukunimi = :sukunimi
WHERE oid = :kayttajaOid;

--:name lisaa-kayttaja! :! :n
INSERT INTO kayttaja (oid, uid, etunimi, sukunimi, voimassa)
    VALUES (:kayttajaOid, :uid, :etunimi, :sukunimi, TRUE );

--:name hae-rooli :? :1
SELECT * FROM rooli_organisaatio WHERE kayttaja = :kayttaja AND rooli = :rooli AND :organisaatio = :organisaatio;

--:name hae-roolit :? :*
SELECT * FROM rooli_organisaatio WHERE kayttaja = :kayttaja;

--:name aseta-roolin-tila! :! :n
UPDATE rooli_organisaatio SET voimassa = :voimassa
WHERE kayttaja = :kayttaja AND rooli = :rooli AND organisaatio = :organisaatio;

--:name lisaa-rooli! :! :n
INSERT INTO rooli_organisaatio (kayttaja, rooli, organisaatio, voimassa) VALUES (:kayttaja, :rooli, :organisaatio, TRUE);