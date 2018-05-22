
-- :name kyselytyypin_kentat :? :*
SELECT * FROM kyselytyyppi_kentat WHERE kyselytyyppi_id = :kyselytyyppi;

-- :name lisaa! :! :n
INSERT INTO vastaajatunnus_tiedot (vastaajatunnus_id, kentta, arvo)
    VALUES (:vastaajatunnus-id, :kentta, :arvo);

-- :name kyselykerran-tyyppi :? :1
SELECT k.tyyppi, ktk.kentta_id FROM kysely k
  JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
  JOIN kyselytyyppi_kentat ktk ON k.tyyppi = ktk.kyselytyyppi_id
  WHERE kk.kyselykertaid = :kyselykertaid;

-- :name kyselyn-kentat :? :*
SELECT ktk.id, ktk.kentta_id, ktk.kentta_fi, ktk.kentta_sv, ktk.kentta_en FROM kyselytyyppi kt
  JOIN kyselytyyppi_kentat ktk ON ktk.kyselytyyppi_id = kt.id
  JOIN kysely k ON k.tyyppi = kt.id
  WHERE k.kyselyid = :kyselyid
  ORDER BY ktk.id;

-- :name lisaa-vastaajatunnus! :!
INSERT INTO vastaajatunnus (tunnus, kyselykertaid, suorituskieli, tutkintotunnus, taustatiedot,
                            vastaajien_lkm,
                            valmistavan_koulutuksen_jarjestaja,
                            valmistavan_koulutuksen_oppilaitos,
                            valmistavan_koulutuksen_toimipaikka,
                            voimassa_alkupvm, voimassa_loppupvm, luotu_kayttaja, muutettu_kayttaja, rahoitusmuotoid)
VALUES (:tunnus, :kyselykertaid, :kieli, :tutkinto, :taustatiedot,
        :vastaajien_lkm, :valmistavan_koulutuksen_jarjestaja, :valmistavan_koulutuksen_oppilaitos, :toimipaikka,
                 :voimassa_alkupvm, :voimassa_loppupvm, :kayttaja, :kayttaja, 5);

-- :name paivita-taustatiedot! :! :n
UPDATE vastaajatunnus SET
  tutkintotunnus = :tutkintotunnus,
  valmistavan_koulutuksen_oppilaitos = :oppilaitos,
  taustatiedot = :taustatiedot
WHERE tunnus = :vastaajatunnus;