
-- :name kyselytyypin_kentat :? :*
SELECT * FROM kyselytyyppi_kentat WHERE kyselytyyppi_id = :kyselytyyppi;

-- :name lisaa :! n
INSERT INTO vastaajatunnus_tiedot (vastaajatunnus_id, kentta, arvo)
    VALUES (:vastaajatunnus-id, :kentta, :arvo);

-- :name kyselykerran-tyyppi :? :1
SELECT k.tyyppi FROM kysely k
  JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
  WHERE kk.kyselykertaid = :kyselykertaid;

-- :name vastaajatunnuksen_tiedot :? :*
SELECT ktk.id, vt.tunnus, ktk.kentta_id, vtt.arvo FROM kyselytyyppi_kentat ktk
  LEFT JOIN vastaajatunnus_tiedot vtt ON vtt.kentta = ktk.id AND ktk.id IN (:v*:kentat)
  LEFT JOIN vastaajatunnus vt ON vt.vastaajatunnusid = vtt.vastaajatunnus_id
  WHERE vt.tunnus = :vastaajatunnus OR vt.tunnus IS NULL
  AND ktk.id IN (:v*:kentat)
  ORDER BY ktk.id;

-- :name kyselyn_kentat :? :*
SELECT ktk.id, ktk.kentta_id, ktk.kentta_fi FROM kyselytyyppi kt
  JOIN kyselytyyppi_kentat ktk ON ktk.kyselytyyppi_id = kt.id
  JOIN kysely k ON k.tyyppi = kt.id
  WHERE k.kyselyid = :kyselyid
  ORDER BY ktk.id;