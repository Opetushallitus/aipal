CREATE MATERIALIZED VIEW vastaaja_taustakysymysryhma_view AS
SELECT DISTINCT vastaaja.vastaajaid,
  CASE
    WHEN kysymysryhma.kysymysryhmaid = 3341884 THEN 3341885
    ELSE kysymysryhma.kysymysryhmaid
  END AS taustakysymysryhmaid
FROM vastaaja
INNER JOIN kyselykerta ON kyselykerta.kyselykertaid = vastaaja.kyselykertaid
INNER JOIN kysely ON kysely.kyselyid = kyselykerta.kyselyid
INNER JOIN kysely_kysymysryhma ON kysely.kyselyid = kysely_kysymysryhma.kyselyid
INNER JOIN kysymysryhma ON kysymysryhma.kysymysryhmaid = kysely_kysymysryhma.kysymysryhmaid
WHERE kysymysryhma.taustakysymykset;

CREATE INDEX vastaaja_taustakysymysryhma_idx ON vastaaja_taustakysymysryhma_view(taustakysymysryhmaid, vastaajaid);

ALTER MATERIALIZED VIEW vastaaja_taustakysymysryhma_view
OWNER TO ${aipal_user};
