-- :name hae-kyselypohjat :? :*
SELECT kp.kyselypohjaid, kp.nimi_fi, kp.nimi_sv, kp.nimi_en, kp.valtakunnallinen, kp.tila, kp.kaytettavissa AS voimassa
FROM kyselypohja kp
WHERE kp.koulutustoimija = :koulutustoimija
--~(if (:valtakunnallinen params) "OR kp.valtakunnallinen = :valtakunnallinen")
--~(if (:voimassa params) "AND kp.voimassa_alkupvm < now() AND kp.voimassa_loppupvm > now())
AND kp.tila = 'julkaistu';

-- :name hae-kyselypohja :? :1
SELECT * FROM kyselypohja WHERE kyselypohjaid = :kyselypohjaid;

-- :name poista-kyselypohjan-kysymysryhmat! :! :n
DELETE FROM kysymysryhma_kyselypohja WHERE kyselypohjaid = :kyselypohjaid;

-- :name tallenna-kyselypohjan-kysymysryhma! :! :n
INSERT INTO kysymysryhma_kyselypohja (kysymysryhmaid, kyselypohjaid, jarjestys, luotu_kayttaja, muutettu_kayttaja)
    VALUES (:kysymysryhmaid,:kyselypohjaid, :jarjestys, :kayttaja, :kayttaja);

-- :name tallenna-kyselypohja! :! :n
UPDATE kyselypohja SET valtakunnallinen = :valtakunnallinen, voimassa_alkupvm = :voimassa_alkupvm, voimassa_loppupvm = :voimassa_loppupvm,
  nimi_fi = :nimi_fi, nimi_sv = :nimi_sv, nimi_en = :nimi_en, selite_fi = :selite_fi, selite_en = :selite_en, muutettu_kayttaja = :kayttaja
WHERE kyselypohjaid = :kyselypohjaid;

-- :name luo-kyselypohja! :! :n
INSERT INTO kyselypohja (valtakunnallinen, voimassa_alkupvm, voimassa_loppupvm, nimi_fi, nimi_sv, nimi_en,
selite_fi, selite_sv, selite_en, koulutustoimija, luotu_kayttaja, muutettu_kayttaja)
VALUES (:valtakunnallinen, :voimassa_alkupvm, :voimassa_loppupvm, :nimi_fi, :nimi_sv, :nimi_en,
:selite_fi, :selite_sv, :selite_en, :koulutustoimija, :kayttaja, :kayttaja);

-- :name aseta-kyselypohjan-tila! :! :n
UPDATE kyselypohja SET tila = :tila, muutettu_kayttaja = :muutettu_kayttaja WHERE kyselypohjaid = :kyselypohjaid;
-- poista-kyselypohja!

-- :name hae-kyselypohjan-organisaatio! :? :1
SELECT koulutustoimija, valtakunnallinen FROM kyselypohja WHERE kyselypohjaid = :kyselypohjaid;
