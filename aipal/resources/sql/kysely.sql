-- :name hae-vastaukset :? :*
SELECT v.vastausid, k.kysymysid, vs.vastaajaid,
  v.vapaateksti, v.numerovalinta, v.vaihtoehto, v.en_osaa_sanoa, v.vastausaika,
  vs.luotuaika AS vastaaja_luotuaika,
  vt.tunnus AS vastaajatunnus, vt.tutkintotunnus, vt.taustatiedot,
  k.kysymysryhmaid, k.vastaustyyppi
FROM vastaus v
  JOIN kysymys k ON v.kysymysid = k.kysymysid
  JOIN kysymysryhma kr ON k.kysymysryhmaid = kr.kysymysryhmaid
  JOIN vastaaja vs ON v.vastaajaid = vs.vastaajaid
  JOIN kyselykerta kk ON vs.kyselykertaid = kk.kyselykertaid
  JOIN vastaajatunnus vt ON vt.vastaajatunnusid = vs.vastaajatunnusid
WHERE kk.kyselyid = :kyselyid;

-- :name hae-monivalinnat :? :*
SELECT * FROM monivalintavaihtoehto
WHERE kysymysid IN (:v*:kysymysidt);
-- :name hae-kyselytyypit :? :*
SELECT * FROM kyselytyyppi;

-- :name hae-kysely :? :1
SELECT * FROM kysely WHERE kyselyid = :kyselyid;

-- :name hae-kyselykerran-kysely :? :1
SELECT k.* FROM kyselykerta kk
JOIN kysely k on kk.kyselyid = k.kyselyid
WHERE kk.kyselykertaid = :kyselykertaid;