CREATE MATERIALIZED VIEW kysymysryhma_taustakysymysryhma_view AS
SELECT DISTINCT k_kr1.kysymysryhmaid, k_kr2.kysymysryhmaid as taustakysymysryhmaid
FROM kysely_kysymysryhma k_kr1
INNER JOIN kysely_kysymysryhma k_kr2 ON k_kr2.kyselyid = k_kr1.kyselyid
INNER JOIN kysymysryhma kr ON kr.kysymysryhmaid = k_kr2.kysymysryhmaid
WHERE kr.taustakysymykset;

CREATE INDEX kysymysryhma_taustakysymysryhma_idx ON kysymysryhma_taustakysymysryhma_view(kysymysryhmaid, taustakysymysryhmaid);

ALTER MATERIALIZED VIEW kysymysryhma_taustakysymysryhma_view
OWNER TO ${aipal_user};
