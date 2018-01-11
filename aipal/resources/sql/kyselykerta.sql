-- :name hae-palaute-kyselykerta :? :*
SELECT kk.kyselykertaid FROM kyselykerta kk
  JOIN kysely k ON kk.kyselyid = k.kyselyid
  JOIN oppilaitos o ON k.koulutustoimija = o.koulutustoimija
  WHERE o.oppilaitoskoodi = :oppilaitoskoodi AND kk.nimi = :kyselykertanimi
  AND kk.lukittu = FALSE AND k.tyyppi = 1;

-- :name hae-rekry-kyselykerta :? :*
SELECT kk.kyselykertaid FROM kyselykerta kk
  JOIN kysely k ON kk.kyselyid = k.kyselyid
  JOIN oppilaitos o ON k.koulutustoimija = o.koulutustoimija
  WHERE o.oppilaitoskoodi = :oppilaitoskoodi AND k.tyyppi = 2
    AND kk.automaattinen = TRUE
  AND (kk.kategoria ->'vuosi')::TEXT::INTEGER = :vuosi
  AND kk.lukittu = FALSE;



