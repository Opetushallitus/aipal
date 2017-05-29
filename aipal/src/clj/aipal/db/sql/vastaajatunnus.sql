
-- :name kyselytyypin_kentat :? :*
SELECT * FROM kyselytyyppi_kentat WHERE kyselytyyppi_id = :kyselytyyppi;

-- :name lisaa :! n
INSERT INTO vastaajatunnus_tiedot (vastaajatunnus_id, kentta, arvo)
    VALUES (:vastaajatunnus-id, :kentta, :arvo);

-- :name kyselykerran-tyyppi :? :1
SELECT k.tyyppi FROM kysely k
  JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
  WHERE kk.kyselykertaid = :kyselykertaid;

