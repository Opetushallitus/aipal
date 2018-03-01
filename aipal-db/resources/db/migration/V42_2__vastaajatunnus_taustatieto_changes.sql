ALTER TABLE vastaajatunnus ADD COLUMN taustatiedot JSONB;

CREATE OR REPLACE FUNCTION get_taustatiedot(id INT) RETURNS JSONB AS
$$
WITH tabular AS (
    SELECT t.kentta_fi, t.arvo FROM (
                                      SELECT vtt.vastaajatunnus_id, ktk.kentta_fi, vtt.arvo FROM vastaajatunnus_tiedot vtt
                                        JOIN kyselytyyppi_kentat ktk ON vtt.kentta = ktk.id) t
    WHERE t.vastaajatunnus_id = id)
SELECT json_object(array_agg(tabular.kentta_fi), array_agg(tabular.arvo)::text[])::jsonb
FROM tabular;
$$ LANGUAGE SQL STABLE;

UPDATE vastaajatunnus SET taustatiedot = get_taustatiedot(vastaajatunnusid);

DROP FUNCTION get_taustatiedot(INTEGER);

INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'oppilaitoskoodi',  'oppilaitoskoodi');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'valmistumisvuosi', 'valmistumisvuosi');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'sukupuoli', 'sukupuoli');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'ika_valmistuessa', 'ika_valmistuessa');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'kansalaisuus', 'kansalaisuus');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'aidinkieli','aidinkieli');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'koulutusalakoodi', 'koulutusalakoodi');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'paaaine', 'paaaine');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'tutkinnon_taso', 'tutkinnon_taso');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'laajuus', 'laajuus');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'valintavuosi', 'valintavuosi');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'asuinkunta_koodi', 'asuinkunta_koodi');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'valmistumisajankohta', 'valmistumisajankohta');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'opiskelupaikkakunta_koodi', 'opiskelupaikkakunta_koodi');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'kirjoilla_olo_kuukausia', 'kirjoilla_olo_kuukausia');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'lasnaolo_lukukausia', 'lasnaolo_lukukausia');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'arvosana', 'arvosana');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'asteikko', 'asteikko');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'koulutuskieli', 'koulutuskieli');
INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi) VALUES (3, 'koulutustyyppi', 'koulutustyyppi');