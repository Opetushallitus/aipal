-- :name uraseurantakyselyt :? :*
SELECT k.kyselyid, kk.kyselykertaid, k.koulutustoimija, k.nimi_fi AS kysely, kk.nimi AS kyselykerta, o.oppilaitoskoodi, o.nimi_fi FROM kysely k
  JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
  JOIN oppilaitos o ON o.koulutustoimija = k.koulutustoimija WHERE o.lakkautuspaiva IS NULL AND k.tyyppi = 3;

-- :name lisaa-vastaajatunnus! :!
INSERT INTO vastaajatunnus (tunnus, kyselykertaid, suorituskieli, vastaajien_lkm)
    VALUES (:tunnus, :kyselykertaid, :kieli, 1);

-- :name hae-uraseurannat :? :*
SELECT k.kyselyid, k.nimi_fi AS kysely_nimi, k.koulutustoimija, o.oppilaitoskoodi, o.nimi_fi AS oppilaitos_nimi, kk.kyselykertaid,
      kk.nimi AS kyselykerta_nimi, kk.kategoria->>'uraseuranta_tyyppi' AS uraseuranta_tyyppi  FROM kysely k
  JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
  JOIN oppilaitos o ON o.koulutustoimija = k.koulutustoimija
  WHERE k.tyyppi = 3 AND k.tila = 'julkaistu'
        AND (k.voimassa_loppupvm > now() OR k.voimassa_loppupvm IS NULL)
        AND (kk.voimassa_loppupvm > now() OR kk.voimassa_loppupvm IS NULL )
        AND (o.lakkautuspaiva IS NULL)
        ORDER BY kyselyid;