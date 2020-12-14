-- :name lisaa-kysymysryhma! <:!
INSERT INTO kysymysryhma (taustakysymykset, valtakunnallinen, nimi_fi, nimi_sv, selite_fi, selite_sv, koulutustoimija, oppilaitos, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, tila, kuvaus_fi, kuvaus_sv, nimi_en, selite_en, kuvaus_en)
VALUES (:taustakysymykset, :valtakunnallinen, :nimi_fi, :nimi_sv, :selite_fi, :selite_sv, :koulutustoimija, :oppilaitos, :kayttaja, :kayttaja, now(), now(), :tila, :kuvaus_fi, :kuvaus_sv, :nimi_en, :selite_en, :kuvaus_en)
RETURNING kysymysryhmaid;

-- :name paivita-kysymysryhma! :! :n
UPDATE kysymysryhma SET taustakysymykset = :taustakysymykset,
                        valtakunnallinen = :valtakunnallinen,
                        nimi_fi = :nimi_fi, nimi_sv = :nimi_sv, nimi_en = :nimi_en,
                        selite_fi = :selite_fi, selite_sv = :selite_sv, selite_en = selite_en,
                        kuvaus_fi = :kuvaus_fi, kuvaus_sv = :kuvaus_sv, :kuvaus_en = kuvaus_en,
                        muutettu_kayttaja = :kayttaja, muutettuaika = now()
WHERE kysymysryhmaid = :kysymysryhmaid;

--:name poista-kysymysryhma! :! :n
DELETE FROM kysymysryhma WHERE kysymysryhmaid = :kysymysryhmaid;

-- :name lisaa-kysymys! :<!
INSERT INTO kysymys (pakollinen, poistettava, vastaustyyppi, kysymysryhmaid, kysymys_fi, kysymys_sv, kysymys_en,
                     jarjestys, monivalinta_max, max_vastaus, eos_vastaus_sallittu, luotu_kayttaja, muutettu_kayttaja, kategoria,
                     luotuaika, muutettuaika, jatkokysymys)
    VALUES (:pakollinen, :poistettava, :vastaustyyppi, :kysymysryhmaid, :kysymys_fi, :kysymys_sv, :kysymys_en,
            :jarjestys, :monivalinta_max, :max_vastaus, :eos_vastaus_sallittu, :kayttaja, :kayttaja, :kategoria,
            now(), now(), :jatkokysymys)
    RETURNING kysymysid;

-- :name paivita-kysymys! :! :n
UPDATE kysymys SET pakollinen = :pakollinen, poistettava = :poistettava, vastaustyyppi = :vastaustyyppi, jatkokysymys = :jatkokysymys,
                   kysymys_fi = :kysymys_fi, kysymys_sv = :kysymys_sv, kysymys_en = :kysymys_en,
                   jarjestys = :jarjestys, monivalinta_max = :monivalinta_max, max_vastaus = max_vastaus, eos_vastaus_sallittu = :eos_vastaus_sallittu,
                   muutettu_kayttaja = :kayttaja, muutettuaika = now()
WHERE kysymysid = :kysymysid;

-- :name poista-kysymykset! :! :n
DELETE FROM kysymys WHERE kysymysid IN (:kysymysidt);

-- :name poista-jatkokysymykset! :! :n
DELETE FROM kysymys_jatkokysymys WHERE kysymysid IN (:kysymysidt) or jatkokysymysid IN (:kysymysidt);

-- :name lisaa-monivalintavaihtoehto! :! :n
INSERT INTO monivalintavaihtoehto(kysymysid, teksti_fi, teksti_sv, luotu_kayttaja, muutettu_kayttaja, luotuaika, muutettuaika, teksti_en)
VALUES(:kysymysid, :teksti_fi, :teksti_sv, :kayttaja, :kayttaja, now(), now(), :teksti_en);

-- :name paivita-monivalintavaihtoehto! :! :n
UPDATE monivalintavaihtoehto SET jarjestys = :jarjestys, teksti_fi = :teksti_fi, teksti_sv = :teksti_sv, teksti_en = :teksti_en,
                                 muutettu_kayttaja = :kayttaja, muutettuaika = now()
WHERE monivalintavaihtoehtoid = :monivalintavaihtoehtoid;

-- :name poista-monivalintavaihtoehdot! :! :n
DELETE FROM monivalintavaihtoehto WHERE kysymysid IN (:kysymysidt);

-- :name liita-jatkokysymys! :! :n
INSERT INTO kysymys_jatkokysymys(kysymysid, jatkokysymysid, vastaus) VALUES (:kysymysid, :jatkokysymysid, :vastaus);

-- :name hae-kysymysryhman-kysymykset :? :*
SELECT k.*, kr.nimi_fi AS kysymysryhma_fi, kr.nimi_sv AS kysymysryhma_sv, kr.nimi_en AS kysymysryhma_sn,
       jk.kysymysid AS jatkokysymys_kysymysid, jk.vastaus AS jatkokysymys_vastaus, kr.taustakysymykset AS taustakysymys FROM kysymys k
  JOIN kysymysryhma kr ON k.kysymysryhmaid = kr.kysymysryhmaid
  LEFT JOIN kysymys_jatkokysymys jk ON k.kysymysid = jk.jatkokysymysid
WHERE k.kysymysryhmaid = :kysymysryhmaid
ORDER BY k.jarjestys;

-- :name hae-kysymysryhma :? :1
SELECT * FROM kysymysryhma WHERE kysymysryhmaid = :kysymysryhmaid;

-- :name hae-kyselypohjan-kysymysryhmat :? :*
SELECT * FROM kysymysryhma_kyselypohja WHERE kyselypohjaid = :kyselypohjaid;

-- :name hae-asteikot :? :*
SELECT * FROM asteikko WHERE koulutustoimija = :koulutustoimija;

-- :name tallenna-asteikko :! :n
INSERT INTO asteikko (koulutustoimija, nimi, asteikko) VALUES (:koulutustoimija, :nimi, :asteikko::json);

-- :name hae-kyselyn-kysymysryhmat :? :*
SELECT kysymysryhmaid FROM kysely_kysymysryhma WHERE kyselyid = :kyselyid ORDER BY jarjestys;
