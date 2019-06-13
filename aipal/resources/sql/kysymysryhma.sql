-- :name lisaa-kysymys! :<!
INSERT INTO kysymys (pakollinen, poistettava, vastaustyyppi, kysymysryhmaid, kysymys_fi, kysymys_sv, kysymys_en,
                     jarjestys, monivalinta_max, max_vastaus, eos_vastaus_sallittu, jatkokysymys, luotu_kayttaja, muutettu_kayttaja)
    VALUES (:pakollinen, :poistettava, :vastaustyyppi, :kysymysryhmaid, :kysymys_fi, :kysymys_sv, :kysymys_en, :jarjestys,
                         :monivalinta_max, :max_vastaus, :eos_vastaus_sallittu, :jatkokysymys, :kayttaja, :kayttaja)
    RETURNING kysymysid;

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