-- :name hae-vastaukset :? :*
SELECT v.vastausid, k.kysymysid, vs.vastaajaid,
  v.vapaateksti, v.numerovalinta, v.vaihtoehto, v.en_osaa_sanoa,
  vt.tunnus AS vastaajatunnus, vt.tutkintotunnus, vt.taustatiedot,
  k.kysymysryhmaid, k.vastaustyyppi
FROM vastaus v
  JOIN kysymys k ON v.kysymysid = k.kysymysid
  JOIN kysymysryhma kr ON k.kysymysryhmaid = kr.kysymysryhmaid
  JOIN vastaaja vs ON v.vastaajaid = vs.vastaajaid
  JOIN kyselykerta kk ON vs.kyselykertaid = kk.kyselykertaid
  JOIN vastaajatunnus vt ON vt.vastaajatunnusid = vs.vastaajatunnusid
WHERE kk.kyselyid = :kyselyid;

-- :name hae-kyselyn-kysymykset :? :*
SELECT k.kysymysid, k.kysymys_fi, k.kysymys_sv, k.kysymys_en, k.jarjestys, k.vastaustyyppi,
  kkr.jarjestys AS kysymysryhma_jarjestys,
  kr.nimi_fi AS kysymysryhma_nimi_fi, kr.nimi_sv AS kysymysryhma_nimi_sv, kr.nimi_en AS kysymysryhma_nimi_en,
  kr.taustakysymykset AS taustakysymys
FROM kysymys k
  JOIN kysymysryhma kr ON k.kysymysryhmaid = kr.kysymysryhmaid
  JOIN kysely_kysymysryhma kkr ON kr.kysymysryhmaid = kkr.kysymysryhmaid
  JOIN kysely kysely ON kysely.kyselyid = kkr.kyselyid
WHERE kysely.kyselyid = :kyselyid;

-- :name hae-monivalinnat :? :*
SELECT * FROM monivalintavaihtoehto
WHERE kysymysid IN (:v*:kysymysidt);