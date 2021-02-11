ALTER TABLE kyselykerta ADD COLUMN ntm_kysymykset BOOLEAN DEFAULT FALSE;
--;;

CREATE TABLE jatkokysymys
(
    jatkokysymysid SERIAL PRIMARY KEY,
    kylla_teksti_fi VARCHAR(500),
    kylla_teksti_sv VARCHAR(500),
    ei_teksti_fi VARCHAR(500),
    ei_teksti_sv VARCHAR(500),
    max_vastaus INTEGER,
    luotuaika TIMESTAMP WITH TIME ZONE NOT NULL,
    muutettuaika TIMESTAMP WITH TIME ZONE NOT NULL,
    kylla_vastaustyyppi VARCHAR(20) default 'likert_asteikko'::character varying NOT NULL
        CONSTRAINT jatkokysymys_kylla_vastaustyyppi_check
            CHECK ((kylla_vastaustyyppi)::TEXT = ANY (ARRAY[('asteikko'::character varying)::TEXT, ('likert_asteikko'::character varying)::TEXT])),
    kylla_teksti_en VARCHAR(500),
    ei_teksti_en VARCHAR(500),
    CONSTRAINT jatkokysymys_check
        CHECK ((max_vastaus IS NOT NULL) OR ((ei_teksti_fi IS NULL) AND (ei_teksti_sv IS NULL))),
    CONSTRAINT jatkokysymys_tekstit_check
        CHECK (COALESCE(kylla_teksti_fi, kylla_teksti_sv, kylla_teksti_en, ei_teksti_fi, ei_teksti_sv, ei_teksti_en) IS NOT NULL)
);
--;;

CREATE TRIGGER jatkokysymys_update
BEFORE UPDATE ON jatkokysymys FOR EACH ROW EXECUTE PROCEDURE update_stamp();
--;;

CREATE TRIGGER jatkokysymysl_insert
BEFORE INSERT ON jatkokysymys FOR EACH ROW EXECUTE PROCEDURE update_created();
--;;

CREATE TRIGGER jatkokysymysm_insert
BEFORE INSERT ON jatkokysymys FOR EACH ROW EXECUTE PROCEDURE update_stamp();
--;;

CREATE TABLE jatkovastaus
(
    jatkovastausid SERIAL NOT NULL PRIMARY KEY,
    jatkokysymysid INTEGER NOT NULL REFERENCES jatkokysymys(jatkokysymysid),
    kylla_asteikko INTEGER,
    ei_vastausteksti TEXT,
    muutettuaika TIMESTAMP WITH TIME ZONE NOT NULL,
    luotuaika TIMESTAMP WITH TIME ZONE NOT NULL
);
--;;

CREATE TRIGGER jatkovastaus_update
BEFORE UPDATE ON jatkovastaus FOR EACH ROW EXECUTE PROCEDURE update_stamp();
--;;

CREATE TRIGGER jatkovastausl_insert
BEFORE INSERT ON jatkovastaus FOR EACH ROW EXECUTE PROCEDURE update_created();
--;;

CREATE TRIGGER jatkovastausm_insert
BEFORE INSERT ON jatkovastaus FOR EACH ROW EXECUTE PROCEDURE update_stamp();
--;;

ALTER TABLE vastaus ADD COLUMN jatkovastausid INTEGER REFERENCES jatkovastaus(jatkovastausid);
--;;
ALTER TABLE kysymys ADD COLUMN jatkokysymysid INTEGER REFERENCES jatkokysymys(jatkokysymysid);
--;;

CREATE TABLE kieli
(
    kieli VARCHAR(2) PRIMARY KEY,
    muutettu_kayttaja VARCHAR(80) NOT NULL REFERENCES kayttaja(oid),
    luotu_kayttaja VARCHAR(80) NOT NULL REFERENCES kayttaja(oid),
    muutettuaika TIMESTAMP WITH TIME ZONE NOT NULL,
    luotuaika TIMESTAMP WITH TIME ZONE NOT NULL
);
--;;


CREATE TRIGGER kieli_cu_insert
BEFORE INSERT ON kieli FOR EACH ROW EXECUTE PROCEDURE update_creator();
--;;

CREATE TRIGGER kieli_mu_insert
BEFORE INSERT ON kieli FOR EACH ROW EXECUTE PROCEDURE update_modifier();
--;;

CREATE TRIGGER kieli_mu_update
BEFORE UPDATE ON  kieli FOR EACH ROW EXECUTE PROCEDURE update_modifier();
--;;

CREATE TRIGGER kieli_update
BEFORE UPDATE ON kieli FOR EACH ROW EXECUTE PROCEDURE update_stamp();
--;;

CREATE TRIGGER kielil_insert
BEFORE INSERT ON kieli FOR EACH ROW EXECUTE PROCEDURE update_created();
--;;

CREATE TRIGGER kielim_insert
BEFORE INSERT ON kieli FOR EACH ROW EXECUTE PROCEDURE update_stamp();
--;;

CREATE MATERIALIZED VIEW public.kysymys_vastaaja_view AS
SELECT DISTINCT vastaus.vastaajaid,
                (vastaus.kysymysid = ANY (ARRAY[280, 289, 301, 319, 343, 379])) AS vanha,
                (vastaus.kysymysid = ANY (ARRAY[7312034, 7312035, 7312036, 7312037, 7312038, 7312039, 7312040, 7312027, 7312028, 7312029, 7312030, 7312031, 7312032, 7312033])) AS uusi
FROM public.vastaus
WHERE (vastaus.kysymysid = ANY (ARRAY[280, 289, 301, 319, 343, 379, 7312034, 7312035, 7312036, 7312037, 7312038, 7312039, 7312040, 7312027, 7312028, 7312029, 7312030, 7312031, 7312032, 7312033]))
WITH NO DATA;
--;;

ALTER TABLE kyselykerta
ALTER COLUMN kategoria SET DATA TYPE json
USING kategoria::json;
--;;

CREATE FUNCTION ei_kysymys(jatkokysymys) RETURNS BOOLEAN STABLE LANGUAGE SQL
as $$
SELECT CASE WHEN $1.ei_teksti_fi IS NOT NULL OR $1.ei_teksti_sv IS NOT NULL THEN true ELSE false END;
$$;
--;;


CREATE FUNCTION kylla_kysymys(jatkokysymys) RETURNS BOOLEAN STABLE LANGUAGE SQL
as $$
SELECT CASE WHEN $1.kylla_teksti_fi IS NOT NULL OR $1.kylla_teksti_sv IS NOT NULL THEN true ELSE false END;
$$;
