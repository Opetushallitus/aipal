

CREATE TABLE public.jatkokysymys (
                                     jatkokysymysid integer NOT NULL,
                                     kylla_teksti_fi character varying(500),
                                     kylla_teksti_sv character varying(500),
                                     ei_teksti_fi character varying(500),
                                     ei_teksti_sv character varying(500),
                                     max_vastaus integer,
                                     luotuaika timestamp with time zone NOT NULL,
                                     muutettuaika timestamp with time zone NOT NULL,
                                     kylla_vastaustyyppi character varying(20) DEFAULT 'likert_asteikko'::character varying NOT NULL,
                                     kylla_teksti_en character varying(500),
                                     ei_teksti_en character varying(500),
                                     CONSTRAINT jatkokysymys_check CHECK (((max_vastaus IS NOT NULL) OR ((ei_teksti_fi IS NULL) AND (ei_teksti_sv IS NULL)))),
                                     CONSTRAINT jatkokysymys_kylla_vastaustyyppi_check CHECK (((kylla_vastaustyyppi)::text = ANY ((ARRAY['asteikko'::character varying, 'likert_asteikko'::character varying])::text[]))),
    CONSTRAINT jatkokysymys_tekstit_check CHECK ((COALESCE(kylla_teksti_fi, kylla_teksti_sv, kylla_teksti_en, ei_teksti_fi, ei_teksti_sv, ei_teksti_en) IS NOT NULL))
);




COMMENT ON COLUMN public.jatkokysymys.kylla_teksti_fi IS 'Kyllä vastauksen jatkokysymys (asteikko)';



COMMENT ON COLUMN public.jatkokysymys.ei_teksti_fi IS 'Ei vastauksen jatkokysymys (vapaateksti)';



COMMENT ON COLUMN public.jatkokysymys.max_vastaus IS 'Ei vastauksen maksimipituus';



CREATE FUNCTION public.ei_kysymys(public.jatkokysymys) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
SELECT CASE WHEN $1.ei_teksti_fi IS NOT NULL OR $1.ei_teksti_sv IS NOT NULL THEN true ELSE false END;
$_$;



CREATE FUNCTION public.jakauma_ffunc(jakauma integer[]) RETURNS integer[]
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
i INTEGER;
BEGIN
FOR i IN SELECT * FROM generate_series(0, array_upper(jakauma, 1)) LOOP
    IF jakauma[i] IS NULL THEN
      jakauma[i] = 0;
END IF;
END LOOP;
RETURN jakauma;
END;
$$;



CREATE FUNCTION public.jakauma_sfunc(jakauma integer[], arvo integer) RETURNS integer[]
    LANGUAGE plpgsql IMMUTABLE
    AS $$
BEGIN
  IF arvo IS NOT NULL THEN
    IF jakauma[arvo] IS NULL THEN
      jakauma[arvo] = 1;
ELSE
      jakauma[arvo] = jakauma[arvo] + 1;
END IF;
END IF;
RETURN jakauma;
END;
$$;



CREATE TABLE public.kysely (
                               kyselyid integer NOT NULL,
                               voimassa_alkupvm date,
                               voimassa_loppupvm date,
                               nimi_fi character varying(200),
                               nimi_sv character varying(200),
                               selite_fi text,
                               selite_sv text,
                               koulutustoimija character varying(10),
                               oppilaitos character varying(5),
                               toimipaikka character varying(7),
                               luotu_kayttaja character varying(80) NOT NULL,
                               muutettu_kayttaja character varying(80) NOT NULL,
                               luotuaika timestamp with time zone NOT NULL,
                               muutettuaika timestamp with time zone NOT NULL,
                               tila character varying(20) DEFAULT 'luonnos'::character varying,
                               nimi_en character varying(200),
                               selite_en text,
                               uudelleenohjaus_url character varying(2000),
                               sivutettu boolean DEFAULT false,
                               tyyppi integer DEFAULT 1 NOT NULL,
                               CONSTRAINT alkupvm_ennen_loppupvm CHECK (((voimassa_alkupvm IS NULL) OR (voimassa_loppupvm IS NULL) OR (voimassa_alkupvm <= voimassa_loppupvm))),
                               CONSTRAINT kysely_organisaatio_check CHECK (((koulutustoimija IS NOT NULL) OR (oppilaitos IS NOT NULL) OR (toimipaikka IS NOT NULL))),
                               CONSTRAINT nimi_fi_sv_tai_en_pakollinen CHECK ((COALESCE(nimi_fi, nimi_sv, nimi_en) IS NOT NULL))
);




COMMENT ON COLUMN public.kysely.voimassa_alkupvm IS 'Kyselyn voimaantulopäivä';



COMMENT ON COLUMN public.kysely.voimassa_loppupvm IS 'Kyselyn voimassaolon päättymispäivä';



COMMENT ON COLUMN public.kysely.tila IS 'Kyselyn tila. Vain julkaistu tilassa olevan kyselyn kyselykertoihin voi vastata';



CREATE FUNCTION public.kaytettavissa(public.kysely) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
SELECT
            $1.tila = 'julkaistu'
        AND ($1.voimassa_alkupvm IS NULL OR $1.voimassa_alkupvm <= current_date)
        AND ($1.voimassa_loppupvm IS NULL OR $1.voimassa_loppupvm >= current_date);
$_$;



CREATE TABLE public.kyselykerta (
                                    kyselykertaid integer NOT NULL,
                                    kyselyid integer NOT NULL,
                                    nimi character varying(200) NOT NULL,
                                    voimassa_alkupvm date NOT NULL,
                                    voimassa_loppupvm date,
                                    luotu_kayttaja character varying(80) NOT NULL,
                                    muutettu_kayttaja character varying(80) NOT NULL,
                                    luotuaika timestamp with time zone NOT NULL,
                                    muutettuaika timestamp with time zone NOT NULL,
                                    lukittu boolean DEFAULT false,
                                    kategoria json,
                                    automaattinen boolean DEFAULT false,
                                    CONSTRAINT alkupvm_ennen_loppupvm CHECK (((voimassa_alkupvm IS NULL) OR (voimassa_loppupvm IS NULL) OR (voimassa_alkupvm <= voimassa_loppupvm)))
);




COMMENT ON COLUMN public.kyselykerta.voimassa_alkupvm IS 'Kyselykerran voimaantulopäivä';



COMMENT ON COLUMN public.kyselykerta.voimassa_loppupvm IS 'Kyselykerran voimassaolon päättyminen';



COMMENT ON COLUMN public.kyselykerta.lukittu IS 'Onko tämä kyselykerta lukittu vai ei. Kenttää ei pidä lukea suoraan vaan voimassaolo pitää tarkastaa kaytettavissa funktion avulla';



CREATE FUNCTION public.kaytettavissa(public.kyselykerta) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
SELECT
    CASE WHEN
                 not $1.lukittu
                 AND ($1.voimassa_alkupvm IS NULL OR $1.voimassa_alkupvm <= current_date)
                 AND ($1.voimassa_loppupvm IS NULL OR $1.voimassa_loppupvm >= current_date)
             THEN kysely.kaytettavissa
         ELSE false
        END
FROM kysely
where kysely.kyselyid = $1.kyselyid;
$_$;



COMMENT ON FUNCTION public.kaytettavissa(public.kyselykerta) IS 'Funktio joka kertoo onko kyselypohja käytettävissä, eli kyselypohja on voimassa ja julkaistu.';



CREATE TABLE public.kyselypohja (
                                    kyselypohjaid integer NOT NULL,
                                    valtakunnallinen boolean DEFAULT false NOT NULL,
                                    voimassa_alkupvm date,
                                    voimassa_loppupvm date,
                                    nimi_fi character varying(200),
                                    nimi_sv character varying(200),
                                    selite_fi text,
                                    selite_sv text,
                                    koulutustoimija character varying(10),
                                    oppilaitos character varying(5),
                                    toimipaikka character varying(7),
                                    luotu_kayttaja character varying(80) NOT NULL,
                                    muutettu_kayttaja character varying(80) NOT NULL,
                                    luotuaika timestamp with time zone NOT NULL,
                                    muutettuaika timestamp with time zone NOT NULL,
                                    tila character varying(20) DEFAULT 'luonnos'::character varying,
                                    nimi_en character varying(200),
                                    selite_en text,
                                    CONSTRAINT alkupvm_ennen_loppupvm CHECK (((voimassa_alkupvm IS NULL) OR (voimassa_loppupvm IS NULL) OR (voimassa_alkupvm <= voimassa_loppupvm))),
                                    CONSTRAINT kyselypohja_organisaatio_check CHECK (((koulutustoimija IS NOT NULL) OR (oppilaitos IS NOT NULL) OR (toimipaikka IS NOT NULL) OR valtakunnallinen)),
                                    CONSTRAINT nimi_fi_sv_tai_en_pakollinen CHECK ((COALESCE(nimi_fi, nimi_sv, nimi_en) IS NOT NULL))
);




COMMENT ON COLUMN public.kyselypohja.valtakunnallinen IS 'Onko kyselypohja valtakunnallinen';



COMMENT ON COLUMN public.kyselypohja.voimassa_alkupvm IS 'Kyselypohjan voimaantuloaika';



COMMENT ON COLUMN public.kyselypohja.voimassa_loppupvm IS 'Kyselypohjan lakkautuspäivä';



CREATE FUNCTION public.kaytettavissa(public.kyselypohja) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
SELECT
    CASE WHEN
                     $1.tila = 'julkaistu'
                 and ($1.voimassa_alkupvm is null or $1.voimassa_alkupvm <= current_date)
                 and ($1.voimassa_loppupvm is null or $1.voimassa_loppupvm >= current_date)
             THEN true ELSE false END;
$_$;



CREATE TABLE public.vastaajatunnus (
                                       vastaajatunnusid integer NOT NULL,
                                       kyselykertaid integer NOT NULL,
                                       rahoitusmuotoid integer,
                                       tutkintotunnus character varying(6),
                                       tunnus character varying(30) NOT NULL,
                                       vastaajien_lkm integer NOT NULL,
                                       lukittu boolean DEFAULT false NOT NULL,
                                       luotu_kayttaja character varying(80) NOT NULL,
                                       muutettu_kayttaja character varying(80) NOT NULL,
                                       luotuaika timestamp with time zone NOT NULL,
                                       muutettuaika timestamp with time zone NOT NULL,
                                       valmistavan_koulutuksen_jarjestaja character varying(10),
                                       voimassa_alkupvm date,
                                       voimassa_loppupvm date,
                                       valmistavan_koulutuksen_oppilaitos character varying(5),
                                       suorituskieli character varying(2),
                                       koulutusmuoto character varying(255),
                                       valmistavan_koulutuksen_toimipaikka character varying(10),
                                       kunta character varying(3),
                                       taustatiedot jsonb
);




COMMENT ON COLUMN public.vastaajatunnus.tunnus IS 'Generoitu tunnus vastaajille. Määrittelee samalla URL:n jossa kyselyyn voi vastata.';



COMMENT ON COLUMN public.vastaajatunnus.vastaajien_lkm IS 'Maksimi vastaajien lukumäärä';



COMMENT ON COLUMN public.vastaajatunnus.lukittu IS 'Onko tämä vastaajatunnus lukittu vai ei. Kenttää ei pidä lukea suoraan vaan voimassaolo pitää tarkastaa kaytettavissa funktion kautta';



CREATE FUNCTION public.kaytettavissa(public.vastaajatunnus) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
SELECT
    CASE WHEN
                 not $1.lukittu
                 AND ($1.voimassa_alkupvm IS NULL OR $1.voimassa_alkupvm <= current_date)
                 AND ($1.voimassa_loppupvm IS NULL OR $1.voimassa_loppupvm >= current_date)
             THEN kyselykerta.kaytettavissa
         ELSE false
        END
FROM kyselykerta
where kyselykerta.kyselykertaid = $1.kyselykertaid;
$_$;



COMMENT ON FUNCTION public.kaytettavissa(public.vastaajatunnus) IS 'Funktio joka kertoo onko vastaajatunnus käytettävissä, eli vastaajatunnus on voimassa ja ei ole lukittu sekä kyselykerta on käytettävissä';



CREATE FUNCTION public.kylla_kysymys(public.jatkokysymys) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
SELECT CASE WHEN $1.kylla_teksti_fi IS NOT NULL OR $1.kylla_teksti_sv IS NOT NULL THEN true ELSE false END;
$_$;



CREATE TABLE public.kysymysryhma (
                                     kysymysryhmaid integer NOT NULL,
                                     voimassa_alkupvm date,
                                     voimassa_loppupvm date,
                                     taustakysymykset boolean DEFAULT false NOT NULL,
                                     valtakunnallinen boolean DEFAULT false NOT NULL,
                                     nimi_fi character varying(200),
                                     nimi_sv character varying(200),
                                     selite_fi text,
                                     selite_sv text,
                                     koulutustoimija character varying(10),
                                     oppilaitos character varying(5),
                                     toimipaikka character varying(7),
                                     luotu_kayttaja character varying(80) NOT NULL,
                                     muutettu_kayttaja character varying(80) NOT NULL,
                                     luotuaika timestamp with time zone NOT NULL,
                                     muutettuaika timestamp with time zone NOT NULL,
                                     tila character varying(20) DEFAULT 'luonnos'::character varying,
                                     kuvaus_fi character varying(800),
                                     kuvaus_sv character varying(800),
                                     ntm_kysymykset boolean DEFAULT false NOT NULL,
                                     nimi_en character varying(200),
                                     selite_en text,
                                     kuvaus_en character varying(800),
                                     CONSTRAINT alkupvm_ennen_loppupvm CHECK (((voimassa_alkupvm IS NULL) OR (voimassa_loppupvm IS NULL) OR (voimassa_alkupvm <= voimassa_loppupvm))),
                                     CONSTRAINT kysymysryhma_organisaatio_check CHECK (((koulutustoimija IS NOT NULL) OR (oppilaitos IS NOT NULL) OR (toimipaikka IS NOT NULL) OR valtakunnallinen)),
                                     CONSTRAINT nimi_fi_sv_tai_en_pakollinen CHECK ((COALESCE(nimi_fi, nimi_sv, nimi_en) IS NOT NULL))
);




COMMENT ON COLUMN public.kysymysryhma.voimassa_alkupvm IS 'Kysymysryhmän voimaantuloaika';



COMMENT ON COLUMN public.kysymysryhma.voimassa_loppupvm IS 'Kysymysryhmän lakkautusaika';



COMMENT ON COLUMN public.kysymysryhma.taustakysymykset IS 'Kuuluuko kysymysryhmä taustakysymyksiin';



COMMENT ON COLUMN public.kysymysryhma.valtakunnallinen IS 'Onko kysymysryhmä valtakunnallinen';



COMMENT ON COLUMN public.kysymysryhma.ntm_kysymykset IS 'Kuuluuko kysymysryhmä NTM-kysymyksiin';



CREATE FUNCTION public.lisattavissa(public.kysymysryhma) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
SELECT
    CASE WHEN
                     $1.tila = 'julkaistu'
                 and ($1.voimassa_alkupvm is null or $1.voimassa_alkupvm <= current_date)
                 and ($1.voimassa_loppupvm is null or $1.voimassa_loppupvm >= current_date)
             THEN true ELSE false END;
$_$;



CREATE FUNCTION public.update_created() RETURNS trigger
    LANGUAGE plpgsql
    AS $$ begin new.luotuaika := now(); return new; end; $$;



CREATE FUNCTION public.update_creator() RETURNS trigger
    LANGUAGE plpgsql
    AS $$ begin new.luotu_kayttaja := current_setting('aipal.kayttaja'); return new; end; $$;



CREATE FUNCTION public.update_modifier() RETURNS trigger
    LANGUAGE plpgsql
    AS $$ begin new.muutettu_kayttaja := current_setting('aipal.kayttaja'); return new; end; $$;



CREATE FUNCTION public.update_stamp() RETURNS trigger
    LANGUAGE plpgsql
    AS $$ begin new.muutettuaika := now(); return new; end; $$;



CREATE TABLE public.kysymys (
                                kysymysid integer NOT NULL,
                                pakollinen boolean NOT NULL,
                                poistettava boolean NOT NULL,
                                vastaustyyppi character varying(20) NOT NULL,
                                kysymysryhmaid integer NOT NULL,
                                kysymys_fi character varying(500),
                                kysymys_sv character varying(500),
                                jarjestys integer,
                                jatkokysymysid integer,
                                monivalinta_max integer,
                                max_vastaus integer,
                                luotu_kayttaja character varying(80) NOT NULL,
                                muutettu_kayttaja character varying(80) NOT NULL,
                                luotuaika timestamp with time zone NOT NULL,
                                muutettuaika timestamp with time zone NOT NULL,
                                eos_vastaus_sallittu boolean,
                                kysymys_en character varying(500),
                                jatkokysymys boolean DEFAULT false NOT NULL,
                                rajoite text,
                                selite_fi text,
                                selite_sv text,
                                selite_en text,
                                raportoitava boolean DEFAULT true,
                                CONSTRAINT kysymys_check CHECK (((max_vastaus IS NOT NULL) OR ((vastaustyyppi)::text <> 'vapaateksti'::text))),
    CONSTRAINT kysymys_fi_sv_tai_en_pakollinen CHECK ((COALESCE(kysymys_fi, kysymys_sv, kysymys_en) IS NOT NULL))
);




COMMENT ON COLUMN public.kysymys.pakollinen IS 'onko kysymykseen pakko vastata';



COMMENT ON COLUMN public.kysymys.poistettava IS 'Voidaanko kysymys poistaa kyselystä';



COMMENT ON COLUMN public.kysymys.vastaustyyppi IS 'Vastauksen tyyppi (kylla_ei_valinta, asteikko, arvosana, likert_asteikko, monivalinta, vapaateksti)';



COMMENT ON COLUMN public.kysymys.jarjestys IS 'Kysymyksen järjestys kysymysryhmän sisällä';



COMMENT ON COLUMN public.kysymys.monivalinta_max IS 'Monivalintakysymyksen vastausvalintojen maksimimäärä';



CREATE FUNCTION public.valtakunnallinen(public.kysymys) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
SELECT
    kysymysryhma.valtakunnallinen
FROM kysymysryhma
where kysymysryhma.kysymysryhmaid = $1.kysymysryhmaid;
$_$;



CREATE FUNCTION public.yhdistetty_kysymysid(kysymysid integer) RETURNS integer
    LANGUAGE plpgsql IMMUTABLE
    AS $$
BEGIN
CASE kysymysid
    WHEN 7312034 THEN RETURN 7312027;
WHEN 7312035 THEN RETURN 7312028;
WHEN 7312036 THEN RETURN 7312029;
WHEN 7312037 THEN RETURN 7312030;
WHEN 7312038 THEN RETURN 7312031;
WHEN 7312040 THEN RETURN 7312033;
ELSE RETURN kysymysid;
END CASE;
END;
$$;



CREATE AGGREGATE public.jakauma(integer) (
    SFUNC = public.jakauma_sfunc,
    STYPE = integer[],
    FINALFUNC = public.jakauma_ffunc
);



CREATE TABLE public.asteikko (
                                 koulutustoimija character varying(10),
                                 nimi text NOT NULL,
                                 asteikko json NOT NULL
);




CREATE SEQUENCE public.jatkokysymys_jatkokysymysid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.jatkokysymys_jatkokysymysid_seq OWNED BY public.jatkokysymys.jatkokysymysid;



CREATE TABLE public.jatkovastaus (
                                     jatkovastausid integer NOT NULL,
                                     jatkokysymysid integer NOT NULL,
                                     kylla_asteikko integer,
                                     ei_vastausteksti text,
                                     muutettuaika timestamp with time zone NOT NULL,
                                     luotuaika timestamp with time zone NOT NULL
);




COMMENT ON COLUMN public.jatkovastaus.kylla_asteikko IS 'Jatkokysymyksen kyllä-vastaus';



COMMENT ON COLUMN public.jatkovastaus.ei_vastausteksti IS 'Jatkokysymyksen ei-vastaus';



CREATE SEQUENCE public.jatkovastaus_jatkovastausid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.jatkovastaus_jatkovastausid_seq OWNED BY public.jatkovastaus.jatkovastausid;



CREATE TABLE public.kayttaja (
                                 oid character varying(80) NOT NULL,
                                 uid character varying(80) NOT NULL,
                                 etunimi character varying(100),
                                 sukunimi character varying(100),
                                 voimassa boolean DEFAULT false NOT NULL,
                                 luotuaika timestamp with time zone NOT NULL,
                                 muutettuaika timestamp with time zone NOT NULL
);




CREATE TABLE public.kayttajarooli (
                                      roolitunnus character varying(32) NOT NULL,
                                      kuvaus character varying(200),
                                      luotuaika timestamp with time zone NOT NULL,
                                      muutettuaika timestamp with time zone NOT NULL
);




COMMENT ON TABLE public.kayttajarooli IS 'AIPAL-käyttäjäroolit. Organisaatiokohtaiset oikeudet erillisen liitostaulun kautta.';



CREATE TABLE public.oppilaitos (
                                   oppilaitoskoodi character varying(5) NOT NULL,
                                   koulutustoimija character varying(10) NOT NULL,
                                   nimi_fi character varying(200) NOT NULL,
                                   nimi_sv character varying(200),
                                   sahkoposti character varying(100),
                                   puhelin character varying(100),
                                   osoite character varying(100),
                                   postinumero character varying(5),
                                   postitoimipaikka character varying(40),
                                   www_osoite character varying(200),
                                   oid character varying(40),
                                   luotuaika timestamp with time zone NOT NULL,
                                   muutettuaika timestamp with time zone NOT NULL,
                                   voimassa boolean DEFAULT true NOT NULL,
                                   lakkautuspaiva date,
                                   nimi_en character varying(200),
                                   oppilaitostyyppi character varying(5)
);




CREATE TABLE public.toimipaikka (
                                    toimipaikkakoodi character varying(7) NOT NULL,
                                    oppilaitos character varying(5) NOT NULL,
                                    nimi_fi character varying(200) NOT NULL,
                                    nimi_sv character varying(200),
                                    sahkoposti character varying(100),
                                    puhelin character varying(100),
                                    osoite character varying(100),
                                    postinumero character varying(5),
                                    postitoimipaikka character varying(40),
                                    www_osoite character varying(200),
                                    oid character varying(40),
                                    luotuaika timestamp with time zone NOT NULL,
                                    muutettuaika timestamp with time zone NOT NULL,
                                    voimassa boolean DEFAULT true NOT NULL,
                                    lakkautuspaiva date,
                                    nimi_en character varying(200),
                                    kunta character varying(3)
);




CREATE VIEW public.kysely_organisaatio_view AS
SELECT k.kyselyid,
       o.koulutustoimija,
       k.oppilaitos,
       k.toimipaikka
FROM ((public.kysely k
    JOIN public.toimipaikka t ON (((t.toimipaikkakoodi)::text = (k.toimipaikka)::text)))
     JOIN public.oppilaitos o ON (((o.oppilaitoskoodi)::text = (t.oppilaitos)::text)))
UNION ALL
SELECT k.kyselyid,
       o.koulutustoimija,
       k.oppilaitos,
       k.toimipaikka
FROM (public.kysely k
         JOIN public.oppilaitos o ON (((o.oppilaitoskoodi)::text = (k.oppilaitos)::text)))
UNION ALL
SELECT k.kyselyid,
       k.koulutustoimija,
       k.oppilaitos,
       k.toimipaikka
FROM public.kysely k
WHERE (k.koulutustoimija IS NOT NULL);




COMMENT ON VIEW public.kysely_organisaatio_view IS 'kyselyjen omistaja-organisaatiot';



CREATE TABLE public.vastaaja (
                                 vastaajaid integer NOT NULL,
                                 kyselykertaid integer NOT NULL,
                                 vastaajatunnusid integer NOT NULL,
                                 vastannut boolean DEFAULT false NOT NULL,
                                 luotuaika timestamp with time zone NOT NULL,
                                 muutettuaika timestamp with time zone NOT NULL
);




COMMENT ON COLUMN public.vastaaja.vastannut IS 'Vastaaja on vastannut koko kyselyyn';



CREATE VIEW public.kayttotilasto_view AS
SELECT vt.tutkintotunnus,
       sum(
               CASE
                   WHEN v.vastannut THEN vt.vastaajien_lkm
                   ELSE 0
                   END) AS vastauksia,
       sum(vt.vastaajien_lkm) AS tunnuksia,
       min(v.muutettuaika) AS vastaus_alkupvm,
       max(v.muutettuaika) AS vastaus_loppupvm,
       kt.kyselyid,
       kov.koulutustoimija,
       kov.oppilaitos,
       kov.toimipaikka,
       k.nimi_fi
FROM ((((public.vastaajatunnus vt
    LEFT JOIN public.vastaaja v ON ((v.vastaajatunnusid = vt.vastaajatunnusid)))
    JOIN public.kyselykerta kt ON ((kt.kyselykertaid = vt.kyselykertaid)))
    JOIN public.kysely k ON ((k.kyselyid = kt.kyselyid)))
         JOIN public.kysely_organisaatio_view kov ON ((kov.kyselyid = kt.kyselyid)))
GROUP BY kov.koulutustoimija, kov.oppilaitos, kov.toimipaikka, vt.tutkintotunnus, kt.kyselyid, k.nimi_fi
ORDER BY kov.koulutustoimija, kov.oppilaitos, vt.tutkintotunnus, kt.kyselyid;




CREATE TABLE public.kieli (
                              kieli character varying(2) NOT NULL,
                              muutettu_kayttaja character varying(80) NOT NULL,
                              luotu_kayttaja character varying(80) NOT NULL,
                              muutettuaika timestamp with time zone NOT NULL,
                              luotuaika timestamp with time zone NOT NULL
);




CREATE TABLE public.koulutusala (
                                    koulutusalatunnus character varying(3) NOT NULL,
                                    nimi_fi character varying(200) NOT NULL,
                                    nimi_sv character varying(200),
                                    luotuaika timestamp with time zone NOT NULL,
                                    muutettuaika timestamp with time zone NOT NULL,
                                    nimi_en character varying(200)
);




CREATE TABLE public.koulutustoimija (
                                        ytunnus character varying(10) NOT NULL,
                                        nimi_fi character varying(200) NOT NULL,
                                        nimi_sv character varying(200),
                                        sahkoposti character varying(100),
                                        puhelin character varying(100),
                                        osoite character varying(100),
                                        postinumero character varying(5),
                                        postitoimipaikka character varying(40),
                                        www_osoite character varying(200),
                                        oid character varying(40),
                                        luotuaika timestamp with time zone NOT NULL,
                                        muutettuaika timestamp with time zone NOT NULL,
                                        voimassa boolean DEFAULT true NOT NULL,
                                        lakkautuspaiva date,
                                        nimi_en character varying(200)
);




CREATE TABLE public.koulutustoimija_ja_tutkinto (
                                                    koulutustoimija character varying(10) NOT NULL,
                                                    tutkinto character varying(6) NOT NULL,
                                                    muutettuaika timestamp with time zone NOT NULL,
                                                    luotuaika timestamp with time zone NOT NULL,
                                                    voimassa_alkupvm date,
                                                    voimassa_loppupvm date
);




COMMENT ON COLUMN public.koulutustoimija_ja_tutkinto.voimassa_alkupvm IS 'Järjestämissopimuksen alkupvm';



COMMENT ON COLUMN public.koulutustoimija_ja_tutkinto.voimassa_loppupvm IS 'Järjestämissopimuksen loppupvm';



CREATE VIEW public.kysely_kaytettavissa AS
SELECT kysely.kyselyid,
       (((kysely.tila)::text = 'julkaistu'::text) AND ((kysely.voimassa_alkupvm IS NULL) OR (kysely.voimassa_alkupvm <= ('now'::text)::date)) AND ((kysely.voimassa_loppupvm IS NULL) OR (kysely.voimassa_loppupvm >= ('now'::text)::date))) AS kaytettavissa
   FROM public.kysely;




CREATE SEQUENCE public.kysely_kyselyid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.kysely_kyselyid_seq OWNED BY public.kysely.kyselyid;



CREATE TABLE public.kysely_kysymys (
                                       kyselyid integer NOT NULL,
                                       kysymysid integer NOT NULL,
                                       luotu_kayttaja character varying(80) NOT NULL,
                                       muutettu_kayttaja character varying(80) NOT NULL,
                                       luotuaika timestamp with time zone NOT NULL,
                                       muutettuaika timestamp with time zone NOT NULL
);




CREATE TABLE public.kysely_kysymysryhma (
                                            kyselyid integer NOT NULL,
                                            kysymysryhmaid integer NOT NULL,
                                            jarjestys integer,
                                            luotu_kayttaja character varying(80) NOT NULL,
                                            muutettu_kayttaja character varying(80) NOT NULL,
                                            luotuaika timestamp with time zone NOT NULL,
                                            muutettuaika timestamp with time zone NOT NULL
);




COMMENT ON COLUMN public.kysely_kysymysryhma.jarjestys IS 'kysymysryhmän järjestys kyselyn sisällä';



CREATE VIEW public.kyselykerta_kaytettavissa AS
SELECT kyselykerta.kyselykertaid,
       ((NOT kyselykerta.lukittu) AND ((kyselykerta.voimassa_alkupvm IS NULL) OR (kyselykerta.voimassa_alkupvm <= ('now'::text)::date)) AND ((kyselykerta.voimassa_loppupvm IS NULL) OR (kyselykerta.voimassa_loppupvm >= ('now'::text)::date)) AND kysely_kaytettavissa.kaytettavissa) AS kaytettavissa
FROM (public.kyselykerta
    JOIN public.kysely_kaytettavissa ON ((kyselykerta.kyselyid = kysely_kaytettavissa.kyselyid)));




CREATE SEQUENCE public.kyselykerta_kyselykertaid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.kyselykerta_kyselykertaid_seq OWNED BY public.kyselykerta.kyselykertaid;



CREATE SEQUENCE public.kyselypohja_kyselypohjaid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.kyselypohja_kyselypohjaid_seq OWNED BY public.kyselypohja.kyselypohjaid;



CREATE VIEW public.kyselypohja_organisaatio_view AS
SELECT kp.kyselypohjaid,
       o.koulutustoimija,
       kp.oppilaitos,
       kp.toimipaikka,
       kp.valtakunnallinen
FROM ((public.kyselypohja kp
    JOIN public.toimipaikka t ON (((t.toimipaikkakoodi)::text = (kp.toimipaikka)::text)))
     JOIN public.oppilaitos o ON (((o.oppilaitoskoodi)::text = (t.oppilaitos)::text)))
UNION ALL
SELECT kp.kyselypohjaid,
       o.koulutustoimija,
       kp.oppilaitos,
       kp.toimipaikka,
       kp.valtakunnallinen
FROM (public.kyselypohja kp
         JOIN public.oppilaitos o ON (((o.oppilaitoskoodi)::text = (kp.oppilaitos)::text)))
UNION ALL
SELECT kp.kyselypohjaid,
       kp.koulutustoimija,
       kp.oppilaitos,
       kp.toimipaikka,
       kp.valtakunnallinen
FROM public.kyselypohja kp
WHERE ((kp.koulutustoimija IS NOT NULL) OR (kp.valtakunnallinen = true));




COMMENT ON VIEW public.kyselypohja_organisaatio_view IS 'kyselypohjien omistaja-organisaatiot sekä valtakunnalliset kyselypohjat.';



CREATE TABLE public.kyselytyyppi (
                                     id integer NOT NULL,
                                     nimi_fi character varying(100) NOT NULL,
                                     nimi_sv character varying(100),
                                     nimi_en character varying(100)
);




CREATE SEQUENCE public.kyselytyyppi_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.kyselytyyppi_id_seq OWNED BY public.kyselytyyppi.id;



CREATE TABLE public.kyselytyyppi_kentat (
                                            id integer NOT NULL,
                                            kyselytyyppi_id integer NOT NULL,
                                            kentta_id character varying(50) NOT NULL,
                                            kentta_fi character varying(100) NOT NULL,
                                            kentta_sv character varying(100),
                                            kentta_en character varying(100)
);




CREATE SEQUENCE public.kyselytyyppi_kentat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.kyselytyyppi_kentat_id_seq OWNED BY public.kyselytyyppi_kentat.id;



CREATE TABLE public.kysymys_jatkokysymys (
                                             kysymysid integer NOT NULL,
                                             jatkokysymysid integer NOT NULL,
                                             vastaus character varying(20) NOT NULL
);




CREATE SEQUENCE public.kysymys_kysymysid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.kysymys_kysymysid_seq OWNED BY public.kysymys.kysymysid;



CREATE TABLE public.vastaus (
                                vastausid integer NOT NULL,
                                kysymysid integer NOT NULL,
                                vastaajaid integer NOT NULL,
                                vastausaika date,
                                vapaateksti text,
                                numerovalinta integer,
                                vaihtoehto character varying(10),
                                jatkovastausid integer,
                                luotuaika timestamp with time zone NOT NULL,
                                muutettuaika timestamp with time zone NOT NULL,
                                en_osaa_sanoa boolean,
                                CONSTRAINT vastaus_vaihtoehto_check CHECK (((vaihtoehto)::text = ANY ((ARRAY['ei'::character varying, 'kylla'::character varying])::text[])))
);




COMMENT ON COLUMN public.vastaus.vastausaika IS 'Vastausaika';



COMMENT ON COLUMN public.vastaus.vapaateksti IS 'vapaatekstivastaus';



COMMENT ON COLUMN public.vastaus.numerovalinta IS 'vastausvalinta (asteikko, arvosana, likert_asteikko tai monivalinta)';



COMMENT ON COLUMN public.vastaus.vaihtoehto IS 'kyllä/ei vastausvaihtoehto';



CREATE MATERIALIZED VIEW public.kysymys_vastaaja_view AS
SELECT DISTINCT vastaus.vastaajaid,
                (vastaus.kysymysid = ANY (ARRAY[280, 289, 301, 319, 343, 379])) AS vanha,
                (vastaus.kysymysid = ANY (ARRAY[7312034, 7312035, 7312036, 7312037, 7312038, 7312039, 7312040, 7312027, 7312028, 7312029, 7312030, 7312031, 7312032, 7312033])) AS uusi
FROM public.vastaus
WHERE (vastaus.kysymysid = ANY (ARRAY[280, 289, 301, 319, 343, 379, 7312034, 7312035, 7312036, 7312037, 7312038, 7312039, 7312040, 7312027, 7312028, 7312029, 7312030, 7312031, 7312032, 7312033]))
    WITH NO DATA;



CREATE TABLE public.kysymysryhma_kyselypohja (
                                                 kysymysryhmaid integer NOT NULL,
                                                 kyselypohjaid integer NOT NULL,
                                                 jarjestys integer NOT NULL,
                                                 luotu_kayttaja character varying(80) NOT NULL,
                                                 muutettu_kayttaja character varying(80) NOT NULL,
                                                 luotuaika timestamp with time zone NOT NULL,
                                                 muutettuaika timestamp with time zone NOT NULL
);




COMMENT ON COLUMN public.kysymysryhma_kyselypohja.jarjestys IS 'Kysymysryhmän järjestys kyselypohjan sisällä';



CREATE SEQUENCE public.kysymysryhma_kysymysryhmaid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.kysymysryhma_kysymysryhmaid_seq OWNED BY public.kysymysryhma.kysymysryhmaid;



CREATE VIEW public.kysymysryhma_organisaatio_view AS
SELECT kr.kysymysryhmaid,
       o.koulutustoimija,
       kr.oppilaitos,
       kr.toimipaikka,
       kr.valtakunnallinen
FROM ((public.kysymysryhma kr
    JOIN public.toimipaikka t ON (((t.toimipaikkakoodi)::text = (kr.toimipaikka)::text)))
     JOIN public.oppilaitos o ON (((o.oppilaitoskoodi)::text = (t.oppilaitos)::text)))
UNION ALL
SELECT kr.kysymysryhmaid,
       o.koulutustoimija,
       kr.oppilaitos,
       kr.toimipaikka,
       kr.valtakunnallinen
FROM (public.kysymysryhma kr
         JOIN public.oppilaitos o ON (((o.oppilaitoskoodi)::text = (kr.oppilaitos)::text)))
UNION ALL
SELECT kr.kysymysryhmaid,
       kr.koulutustoimija,
       kr.oppilaitos,
       kr.toimipaikka,
       kr.valtakunnallinen
FROM public.kysymysryhma kr
WHERE ((kr.koulutustoimija IS NOT NULL) OR (kr.valtakunnallinen = true));




COMMENT ON VIEW public.kysymysryhma_organisaatio_view IS 'kysymysryhmien omistaja-organisaatiot sekä valtakunnalliset kysymysryhmät.';



CREATE MATERIALIZED VIEW public.kysymysryhma_taustakysymysryhma_view AS
SELECT DISTINCT k_kr1.kysymysryhmaid,
                k_kr2.kysymysryhmaid AS taustakysymysryhmaid
FROM ((public.kysely_kysymysryhma k_kr1
    JOIN public.kysely_kysymysryhma k_kr2 ON ((k_kr2.kyselyid = k_kr1.kyselyid)))
         JOIN public.kysymysryhma kr ON ((kr.kysymysryhmaid = k_kr2.kysymysryhmaid)))
WHERE kr.taustakysymykset
    WITH NO DATA;



CREATE TABLE public.monivalintavaihtoehto (
                                              monivalintavaihtoehtoid integer NOT NULL,
                                              kysymysid integer NOT NULL,
                                              jarjestys integer DEFAULT 0 NOT NULL,
                                              teksti_fi character varying(400),
                                              teksti_sv character varying(400),
                                              luotu_kayttaja character varying(80) NOT NULL,
                                              muutettu_kayttaja character varying(80) NOT NULL,
                                              luotuaika timestamp with time zone NOT NULL,
                                              muutettuaika timestamp with time zone NOT NULL,
                                              teksti_en character varying(400),
                                              CONSTRAINT teksti_fi_sv_tai_en_pakollinen CHECK ((COALESCE(teksti_fi, teksti_sv, teksti_en) IS NOT NULL))
);




COMMENT ON COLUMN public.monivalintavaihtoehto.jarjestys IS 'Monivalintavaihtoehdon järjestys kysymyksessä';



CREATE SEQUENCE public.monivalintavaihtoehto_monivalintavaihtoehtoid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.monivalintavaihtoehto_monivalintavaihtoehtoid_seq OWNED BY public.monivalintavaihtoehto.monivalintavaihtoehtoid;



CREATE TABLE public.ohje (
                             ohjetunniste character varying(80) NOT NULL,
                             teksti_fi text,
                             teksti_sv text,
                             muutettuaika timestamp with time zone NOT NULL,
                             luotuaika timestamp with time zone NOT NULL,
                             teksti_en character varying(400)
);




CREATE TABLE public.opintoala (
                                  opintoalatunnus character varying(3) NOT NULL,
                                  nimi_fi character varying(200) NOT NULL,
                                  nimi_sv character varying(200),
                                  luotuaika timestamp with time zone NOT NULL,
                                  muutettuaika timestamp with time zone NOT NULL,
                                  nimi_en character varying(200),
                                  koulutusala character varying(3)
);




CREATE TABLE public.oppilaitostyyppi_tutkintotyyppi (
                                                        tutkintotyyppi character varying(5) NOT NULL,
                                                        oppilaitostyyppi character varying(5) NOT NULL,
                                                        muutettu_kayttaja character varying(80) NOT NULL,
                                                        luotu_kayttaja character varying(80) NOT NULL,
                                                        muutettuaika timestamp with time zone NOT NULL,
                                                        luotuaika timestamp with time zone NOT NULL
);




CREATE TABLE public.organisaatiopalvelu_log (
                                                id integer NOT NULL,
                                                paivitetty timestamp with time zone,
                                                muutettuaika timestamp with time zone NOT NULL,
                                                luotuaika timestamp with time zone NOT NULL
);




CREATE SEQUENCE public.organisaatiopalvelu_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.organisaatiopalvelu_log_id_seq OWNED BY public.organisaatiopalvelu_log.id;



CREATE TABLE public.rahoitusmuoto (
                                      rahoitusmuotoid integer NOT NULL,
                                      rahoitusmuoto character varying(80) NOT NULL,
                                      luotuaika timestamp with time zone NOT NULL,
                                      muutettuaika timestamp with time zone NOT NULL
);




CREATE SEQUENCE public.rahoitusmuoto_rahoitusmuotoid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.rahoitusmuoto_rahoitusmuotoid_seq OWNED BY public.rahoitusmuoto.rahoitusmuotoid;



CREATE TABLE public.rooli_organisaatio (
                                           rooli_organisaatio_id integer NOT NULL,
                                           organisaatio character varying(9),
                                           rooli character varying(32) NOT NULL,
                                           kayttaja character varying(80) NOT NULL,
                                           voimassa boolean DEFAULT false NOT NULL,
                                           muutettuaika timestamp with time zone NOT NULL,
                                           luotuaika timestamp with time zone NOT NULL,
                                           CONSTRAINT rooli_organisaatio_null CHECK ((((rooli)::text = ANY ((ARRAY['YLLAPITAJA'::character varying, 'OPH-KATSELIJA'::character varying, 'TTK-KATSELIJA'::character varying, 'KATSELIJA'::character varying, 'AIPAL-VASTAAJA'::character varying])::text[])) OR (organisaatio IS NOT NULL)))
);




COMMENT ON TABLE public.rooli_organisaatio IS 'Kytkee käyttäjän, käyttöoikeusroolin ja tietyn organisaation yhteen.';



CREATE SEQUENCE public.rooli_organisaatio_rooli_organisaatio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.rooli_organisaatio_rooli_organisaatio_id_seq OWNED BY public.rooli_organisaatio.rooli_organisaatio_id;



CREATE TABLE public.tiedote (
                                tiedoteid integer DEFAULT 1 NOT NULL,
                                teksti_fi text,
                                teksti_sv text,
                                muutettuaika timestamp with time zone NOT NULL,
                                luotuaika timestamp with time zone NOT NULL,
                                teksti_en text,
                                CONSTRAINT vain_yksi_tiedote CHECK ((tiedoteid = 1))
);




CREATE TABLE public.tila_enum (
                                  nimi character varying(20) NOT NULL,
                                  muutettuaika timestamp without time zone NOT NULL,
                                  luotuaika timestamp without time zone NOT NULL
);




CREATE TABLE public.tutkinto (
                                 tutkintotunnus character varying(6) NOT NULL,
                                 opintoala character varying(3) NOT NULL,
                                 nimi_fi character varying(200) NOT NULL,
                                 nimi_sv character varying(200),
                                 voimassa_alkupvm date,
                                 voimassa_loppupvm date,
                                 luotuaika timestamp with time zone NOT NULL,
                                 muutettuaika timestamp with time zone NOT NULL,
                                 siirtymaajan_loppupvm date,
                                 nimi_en character varying(200),
                                 tutkintotyyppi character varying(25)
);




CREATE TABLE public.tutkintotyyppi (
                                       tutkintotyyppi character varying(25) NOT NULL,
                                       luotuaika timestamp with time zone NOT NULL,
                                       muutettuaika timestamp with time zone NOT NULL,
                                       nimi_fi character varying(200),
                                       nimi_sv character varying(200),
                                       nimi_en character varying(200)
);




CREATE MATERIALIZED VIEW public.vastaaja_taustakysymysryhma_view AS
SELECT DISTINCT vastaaja.vastaajaid,
                CASE
                    WHEN (kysymysryhma.kysymysryhmaid = 3341884) THEN 3341885
                    ELSE kysymysryhma.kysymysryhmaid
                    END AS taustakysymysryhmaid
FROM ((((public.vastaaja
    JOIN public.kyselykerta ON ((kyselykerta.kyselykertaid = vastaaja.kyselykertaid)))
    JOIN public.kysely ON ((kysely.kyselyid = kyselykerta.kyselyid)))
    JOIN public.kysely_kysymysryhma ON ((kysely.kyselyid = kysely_kysymysryhma.kyselyid)))
         JOIN public.kysymysryhma ON ((kysymysryhma.kysymysryhmaid = kysely_kysymysryhma.kysymysryhmaid)))
WHERE kysymysryhma.taustakysymykset
    WITH NO DATA;



CREATE SEQUENCE public.vastaaja_vastaajaid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.vastaaja_vastaajaid_seq OWNED BY public.vastaaja.vastaajaid;



CREATE VIEW public.vastaajatunnus_kaytettavissa AS
SELECT vastaajatunnus.vastaajatunnusid,
       ((NOT vastaajatunnus.lukittu) AND ((vastaajatunnus.voimassa_alkupvm IS NULL) OR (vastaajatunnus.voimassa_alkupvm <= ('now'::text)::date)) AND ((vastaajatunnus.voimassa_loppupvm IS NULL) OR (vastaajatunnus.voimassa_loppupvm >= ('now'::text)::date)) AND kyselykerta_kaytettavissa.kaytettavissa) AS kaytettavissa
FROM (public.vastaajatunnus
    JOIN public.kyselykerta_kaytettavissa ON ((vastaajatunnus.kyselykertaid = kyselykerta_kaytettavissa.kyselykertaid)));




CREATE TABLE public.vastaajatunnus_tiedot (
                                              vastaajatunnus_id integer NOT NULL,
                                              kentta integer NOT NULL,
                                              arvo character varying(100)
);




CREATE SEQUENCE public.vastaajatunnus_vastaajatunnusid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.vastaajatunnus_vastaajatunnusid_seq OWNED BY public.vastaajatunnus.vastaajatunnusid;



CREATE MATERIALIZED VIEW public.vastaus_jatkovastaus_valtakunnallinen_view AS
SELECT vastaus.vastausid,
       vastaus.kysymysid,
       vastaus.vastaajaid,
       vastaus.numerovalinta,
       vastaus.vaihtoehto,
       vastaus.vapaateksti,
       vastaus.en_osaa_sanoa,
       jatkovastaus.kylla_asteikko,
       jatkovastaus.ei_vastausteksti,
       vastaus.vastausaika
FROM (((public.vastaus
    LEFT JOIN public.jatkovastaus ON ((jatkovastaus.jatkovastausid = vastaus.jatkovastausid)))
    JOIN public.kysymys ON ((vastaus.kysymysid = kysymys.kysymysid)))
         JOIN public.kysymysryhma ON (((kysymys.kysymysryhmaid = kysymysryhma.kysymysryhmaid) AND (kysymysryhma.valtakunnallinen = true))))
    WITH NO DATA;



CREATE SEQUENCE public.vastaus_vastausid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




ALTER SEQUENCE public.vastaus_vastausid_seq OWNED BY public.vastaus.vastausid;



CREATE MATERIALIZED VIEW public.vipunen_view AS
SELECT kysely.koulutustoimija,
       vastaajatunnus.valmistavan_koulutuksen_jarjestaja,
       vastaajatunnus.valmistavan_koulutuksen_oppilaitos,
       regexp_replace(kysymysryhma.selite_fi, '\n'::text, ' '::text, 'g'::text) AS kysymysryhma,
       regexp_replace((kysymysryhma.nimi_fi)::text, '\n'::text, ' '::text, 'g'::text) AS kysymysryhma_fi,
       regexp_replace((kysymysryhma.nimi_sv)::text, '\n'::text, ' '::text, 'g'::text) AS kysymysryhma_sv,
       regexp_replace((kysymysryhma.nimi_en)::text, '\n'::text, ' '::text, 'g'::text) AS kysymysryhma_en,
       kysymysryhma.valtakunnallinen,
       regexp_replace((kysely.nimi_fi)::text, '\n'::text, ' '::text, 'g'::text) AS kysely_fi,
       regexp_replace((kysely.nimi_sv)::text, '\n'::text, ' '::text, 'g'::text) AS kysely_sv,
       regexp_replace((kysely.nimi_en)::text, '\n'::text, ' '::text, 'g'::text) AS kysely_en,
       regexp_replace((kyselykerta.nimi)::text, '\n'::text, ' '::text, 'g'::text) AS kyselykerta,
       regexp_replace((kysymys.kysymys_fi)::text, '\n'::text, ' '::text, 'g'::text) AS kysymys_fi,
       regexp_replace((kysymys.kysymys_sv)::text, '\n'::text, ' '::text, 'g'::text) AS kysymys_sv,
       regexp_replace((kysymys.kysymys_en)::text, '\n'::text, ' '::text, 'g'::text) AS kysymys_en,
       tutkinto.tutkintotunnus,
       vastaajatunnus.suorituskieli,
       kysymys.vastaustyyppi,
       vastaus.numerovalinta,
       CASE vastaus.vaihtoehto
           WHEN 'kylla'::text THEN 1
           WHEN 'ei'::text THEN 0
           ELSE NULL::integer
END AS vaihtoehto,
    regexp_replace((COALESCE(monivalintavaihtoehto.teksti_fi, COALESCE(monivalintavaihtoehto.teksti_sv, monivalintavaihtoehto.teksti_en)))::text, '\n'::text, ' '::text, 'g'::text) AS monivalintavaihtoehto,
    kysymysryhma.taustakysymykset,
    COALESCE(vastaajatunnus.kunta, ((vastaajatunnus.taustatiedot ->> 'kunta'::text))::character varying) AS kunta,
    COALESCE(vastaajatunnus.koulutusmuoto, ((vastaajatunnus.taustatiedot ->> 'koulutusmuoto'::text))::character varying) AS koulutusmuoto,
    vastaajatunnus.tunnus,
    vastaus.vastausaika,
    ( SELECT kysely_kysymysryhma.jarjestys
           FROM public.kysely_kysymysryhma
          WHERE ((kysely_kysymysryhma.kyselyid = kysely.kyselyid) AND (kysely_kysymysryhma.kysymysryhmaid = kysymysryhma.kysymysryhmaid))) AS kysymysryhmajarjestys,
    kysymys.jarjestys AS kysymysjarjestys,
    kysymys.kysymysryhmaid,
    kysely.kyselyid,
    kysely.voimassa_alkupvm AS kysely_alkupvm,
    kysely.voimassa_loppupvm AS kysely_loppupvm,
    kyselykerta.kyselykertaid,
    kysymys.kysymysid,
    vastaaja.vastaajaid,
    vastaus.vastausid
   FROM ((((((((public.vastaus
     JOIN public.kysymys ON ((vastaus.kysymysid = kysymys.kysymysid)))
     JOIN public.kysymysryhma ON ((kysymys.kysymysryhmaid = kysymysryhma.kysymysryhmaid)))
     LEFT JOIN public.monivalintavaihtoehto ON ((((kysymys.vastaustyyppi)::text = 'monivalinta'::text) AND (monivalintavaihtoehto.kysymysid = kysymys.kysymysid) AND (vastaus.numerovalinta = monivalintavaihtoehto.jarjestys))))
     JOIN public.vastaaja ON ((vastaus.vastaajaid = vastaaja.vastaajaid)))
     JOIN public.vastaajatunnus ON ((vastaaja.vastaajatunnusid = vastaajatunnus.vastaajatunnusid)))
     LEFT JOIN public.tutkinto ON (((vastaajatunnus.tutkintotunnus)::text = (tutkinto.tutkintotunnus)::text)))
     JOIN public.kyselykerta ON ((vastaajatunnus.kyselykertaid = kyselykerta.kyselykertaid)))
     JOIN public.kysely ON ((kyselykerta.kyselyid = kysely.kyselyid)))
  WHERE ((kysymys.vastaustyyppi)::text <> 'vapaateksti'::text)
  WITH NO DATA;



ALTER TABLE ONLY public.jatkokysymys ALTER COLUMN jatkokysymysid SET DEFAULT nextval('public.jatkokysymys_jatkokysymysid_seq'::regclass);



ALTER TABLE ONLY public.jatkovastaus ALTER COLUMN jatkovastausid SET DEFAULT nextval('public.jatkovastaus_jatkovastausid_seq'::regclass);



ALTER TABLE ONLY public.kysely ALTER COLUMN kyselyid SET DEFAULT nextval('public.kysely_kyselyid_seq'::regclass);



ALTER TABLE ONLY public.kyselykerta ALTER COLUMN kyselykertaid SET DEFAULT nextval('public.kyselykerta_kyselykertaid_seq'::regclass);



ALTER TABLE ONLY public.kyselypohja ALTER COLUMN kyselypohjaid SET DEFAULT nextval('public.kyselypohja_kyselypohjaid_seq'::regclass);



ALTER TABLE ONLY public.kyselytyyppi ALTER COLUMN id SET DEFAULT nextval('public.kyselytyyppi_id_seq'::regclass);



ALTER TABLE ONLY public.kyselytyyppi_kentat ALTER COLUMN id SET DEFAULT nextval('public.kyselytyyppi_kentat_id_seq'::regclass);



ALTER TABLE ONLY public.kysymys ALTER COLUMN kysymysid SET DEFAULT nextval('public.kysymys_kysymysid_seq'::regclass);



ALTER TABLE ONLY public.kysymysryhma ALTER COLUMN kysymysryhmaid SET DEFAULT nextval('public.kysymysryhma_kysymysryhmaid_seq'::regclass);



ALTER TABLE ONLY public.monivalintavaihtoehto ALTER COLUMN monivalintavaihtoehtoid SET DEFAULT nextval('public.monivalintavaihtoehto_monivalintavaihtoehtoid_seq'::regclass);



ALTER TABLE ONLY public.organisaatiopalvelu_log ALTER COLUMN id SET DEFAULT nextval('public.organisaatiopalvelu_log_id_seq'::regclass);



ALTER TABLE ONLY public.rahoitusmuoto ALTER COLUMN rahoitusmuotoid SET DEFAULT nextval('public.rahoitusmuoto_rahoitusmuotoid_seq'::regclass);



ALTER TABLE ONLY public.rooli_organisaatio ALTER COLUMN rooli_organisaatio_id SET DEFAULT nextval('public.rooli_organisaatio_rooli_organisaatio_id_seq'::regclass);



ALTER TABLE ONLY public.vastaaja ALTER COLUMN vastaajaid SET DEFAULT nextval('public.vastaaja_vastaajaid_seq'::regclass);



ALTER TABLE ONLY public.vastaajatunnus ALTER COLUMN vastaajatunnusid SET DEFAULT nextval('public.vastaajatunnus_vastaajatunnusid_seq'::regclass);



ALTER TABLE ONLY public.vastaus ALTER COLUMN vastausid SET DEFAULT nextval('public.vastaus_vastausid_seq'::regclass);




INSERT INTO public.kayttaja VALUES ('JARJESTELMA', 'JARJESTELMA', 'Järjestelmä', '', true, '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.kayttaja VALUES ('KONVERSIO', 'KONVERSIO', 'Järjestelmä', '', true, '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.kayttaja VALUES ('INTEGRAATIO', 'INTEGRAATIO', 'Järjestelmä', '', true, '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.kayttaja VALUES ('VASTAAJA', 'VASTAAJA', 'Aipal-vastaus', '', true, '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');



INSERT INTO public.kayttajarooli VALUES ('YLLAPITAJA', 'Ylläpitäjäroolilla on kaikki oikeudet', '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.kayttajarooli VALUES ('OPH-KATSELIJA', 'Opetushallituksen katselija', '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.kayttajarooli VALUES ('OPL-VASTUUKAYTTAJA', 'Oppilaitoksen vastuukayttaja', '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.kayttajarooli VALUES ('OPL-KATSELIJA', 'Oppilaitoksen katselija', '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.kayttajarooli VALUES ('OPL-KAYTTAJA', 'Oppilaitoksen normaali käyttäjä (opettaja)', '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.kayttajarooli VALUES ('TTK-KATSELIJA', 'Toimikunnan raportointirooli', '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.kayttajarooli VALUES ('KATSELIJA', 'Yleinen katselijarooli erityistarpeita varten', '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.kayttajarooli VALUES ('AIPAL-VASTAAJA', 'Vastaajasovelluksen käyttäjän rooli', '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.kayttajarooli VALUES ('OPL-NTMVASTUUKAYTTAJA', 'Oppilaitoksen NTM-vastuukayttaja', '2021-02-09 17:03:52.027897+02', '2021-02-09 17:03:52.027897+02');



INSERT INTO public.kieli VALUES ('fi', 'JARJESTELMA', 'JARJESTELMA', '2021-02-09 17:03:51.266145+02', '2021-02-09 17:03:51.266145+02');
INSERT INTO public.kieli VALUES ('sv', 'JARJESTELMA', 'JARJESTELMA', '2021-02-09 17:03:51.266145+02', '2021-02-09 17:03:51.266145+02');
INSERT INTO public.kieli VALUES ('en', 'JARJESTELMA', 'JARJESTELMA', '2021-02-09 17:03:52.083111+02', '2021-02-09 17:03:52.083111+02');



INSERT INTO public.koulutustoimija VALUES ('0829731-2', 'Opetushallitus', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1.2.246.562.10.00000000001', '2021-02-09 17:03:50.431426+02', '2021-02-09 17:03:50.431426+02', true, NULL, NULL);



INSERT INTO public.kyselytyyppi VALUES (1, 'Palautekysely', NULL, NULL);
INSERT INTO public.kyselytyyppi VALUES (2, 'Rekrykysely', NULL, NULL);
INSERT INTO public.kyselytyyppi VALUES (3, 'Uraseuranta', NULL, NULL);
INSERT INTO public.kyselytyyppi VALUES (4, 'Itsearviointi', NULL, NULL);
INSERT INTO public.kyselytyyppi VALUES (5, 'AMIS-palaute', NULL, NULL);
INSERT INTO public.kyselytyyppi VALUES (6, 'Kandipalaute', NULL, NULL);



INSERT INTO public.kyselytyyppi_kentat VALUES (1, 1, 'kieli', 'Suorituskieli', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (2, 1, 'tutkinto', 'Tutkinto', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (3, 1, 'koulutusmuoto', 'Koulutusmuoto', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (4, 1, 'toimipaikka', 'Toimipaikka', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (5, 1, 'kunta', 'Kunta', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (6, 2, 'henkilonumero', 'Henkilönumero', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (7, 2, 'haun_numero', 'Haun numero', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (8, 3, 'oppilaitoskoodi', 'oppilaitoskoodi', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (9, 3, 'valmistumisvuosi', 'valmistumisvuosi', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (10, 3, 'sukupuoli', 'sukupuoli', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (11, 3, 'ika_valmistuessa', 'ika_valmistuessa', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (12, 3, 'kansalaisuus', 'kansalaisuus', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (13, 3, 'aidinkieli', 'aidinkieli', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (14, 3, 'koulutusalakoodi', 'koulutusalakoodi', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (15, 3, 'paaaine', 'paaaine', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (16, 3, 'tutkinnon_taso', 'tutkinnon_taso', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (17, 3, 'laajuus', 'laajuus', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (18, 3, 'valintavuosi', 'valintavuosi', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (19, 3, 'asuinkunta_koodi', 'asuinkunta_koodi', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (20, 3, 'valmistumisajankohta', 'valmistumisajankohta', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (21, 3, 'opiskelupaikkakunta_koodi', 'opiskelupaikkakunta_koodi', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (22, 3, 'kirjoilla_olo_kuukausia', 'kirjoilla_olo_kuukausia', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (23, 3, 'lasnaolo_lukukausia', 'lasnaolo_lukukausia', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (24, 3, 'arvosana', 'arvosana', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (25, 3, 'asteikko', 'asteikko', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (26, 3, 'koulutuskieli', 'koulutuskieli', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (27, 3, 'koulutustyyppi', 'koulutustyyppi', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (28, 5, 'hankintakoulutuksen_toteuttaja', 'Hankintakoulutuksen toteuttaja ', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (29, 5, 'tutkintomuoto', 'Tutkintomuoto', NULL, NULL);
INSERT INTO public.kyselytyyppi_kentat VALUES (30, 6, 'kieli', 'Suorituskieli', 'Examensspråk', 'Language of degree');
INSERT INTO public.kyselytyyppi_kentat VALUES (31, 6, 'tutkinto', 'Tutkinto', 'Examen', 'Degree');
INSERT INTO public.kyselytyyppi_kentat VALUES (32, 6, 'toimipaikka', 'Toimipaikka', 'Verksamhetsställe', 'Operational unit');
INSERT INTO public.kyselytyyppi_kentat VALUES (33, 6, 'kunta', 'Kunta', 'Kommun', 'Region');



INSERT INTO public.oppilaitostyyppi_tutkintotyyppi VALUES ('06', '41', 'JARJESTELMA', 'JARJESTELMA', '2021-02-09 17:03:52.50904+02', '2021-02-09 17:03:52.50904+02');
INSERT INTO public.oppilaitostyyppi_tutkintotyyppi VALUES ('07', '41', 'JARJESTELMA', 'JARJESTELMA', '2021-02-09 17:03:52.50904+02', '2021-02-09 17:03:52.50904+02');
INSERT INTO public.oppilaitostyyppi_tutkintotyyppi VALUES ('12', '41', 'JARJESTELMA', 'JARJESTELMA', '2021-02-09 17:03:52.50904+02', '2021-02-09 17:03:52.50904+02');
INSERT INTO public.oppilaitostyyppi_tutkintotyyppi VALUES ('13', '42', 'JARJESTELMA', 'JARJESTELMA', '2021-02-09 17:03:52.50904+02', '2021-02-09 17:03:52.50904+02');
INSERT INTO public.oppilaitostyyppi_tutkintotyyppi VALUES ('14', '42', 'JARJESTELMA', 'JARJESTELMA', '2021-02-09 17:03:52.50904+02', '2021-02-09 17:03:52.50904+02');
INSERT INTO public.oppilaitostyyppi_tutkintotyyppi VALUES ('15', '42', 'JARJESTELMA', 'JARJESTELMA', '2021-02-09 17:03:52.50904+02', '2021-02-09 17:03:52.50904+02');
INSERT INTO public.oppilaitostyyppi_tutkintotyyppi VALUES ('16', '42', 'JARJESTELMA', 'JARJESTELMA', '2021-02-09 17:03:52.50904+02', '2021-02-09 17:03:52.50904+02');



INSERT INTO public.rooli_organisaatio VALUES (1, NULL, 'YLLAPITAJA', 'JARJESTELMA', true, '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.rooli_organisaatio VALUES (2, NULL, 'YLLAPITAJA', 'KONVERSIO', true, '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.rooli_organisaatio VALUES (3, NULL, 'YLLAPITAJA', 'INTEGRAATIO', true, '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');
INSERT INTO public.rooli_organisaatio VALUES (4, NULL, 'AIPAL-VASTAAJA', 'VASTAAJA', true, '2021-02-09 17:03:49.954802+02', '2021-02-09 17:03:49.954802+02');



INSERT INTO public.tila_enum VALUES ('luonnos', '2021-02-09 17:03:50.663413', '2021-02-09 17:03:50.663413');
INSERT INTO public.tila_enum VALUES ('julkaistu', '2021-02-09 17:03:50.663413', '2021-02-09 17:03:50.663413');
INSERT INTO public.tila_enum VALUES ('suljettu', '2021-02-09 17:03:50.886251', '2021-02-09 17:03:50.886251');




INSERT INTO public.tutkintotyyppi VALUES ('erikoisammattitutkinto', '2021-02-09 17:03:52.370419+02', '2021-02-09 17:03:52.370419+02', NULL, NULL, NULL);
INSERT INTO public.tutkintotyyppi VALUES ('ammattitutkinto', '2021-02-09 17:03:52.370419+02', '2021-02-09 17:03:52.370419+02', NULL, NULL, NULL);
INSERT INTO public.tutkintotyyppi VALUES ('perustutkinto', '2021-02-09 17:03:52.370419+02', '2021-02-09 17:03:52.370419+02', NULL, NULL, NULL);


-- work around JDBC 'A result was returned when none was expected.'
DO $$
BEGIN
    PERFORM pg_catalog.setval('public.jatkokysymys_jatkokysymysid_seq', 1, false);
    PERFORM pg_catalog.setval('public.jatkovastaus_jatkovastausid_seq', 1, false);
    PERFORM pg_catalog.setval('public.kysely_kyselyid_seq', 1, false);
    PERFORM pg_catalog.setval('public.kyselykerta_kyselykertaid_seq', 1, false);
    PERFORM pg_catalog.setval('public.kyselypohja_kyselypohjaid_seq', 1, false);
    PERFORM pg_catalog.setval('public.kyselytyyppi_id_seq', 6, true);
    PERFORM pg_catalog.setval('public.kyselytyyppi_kentat_id_seq', 33, true);
    PERFORM pg_catalog.setval('public.kysymys_kysymysid_seq', 1, false);
    PERFORM pg_catalog.setval('public.kysymysryhma_kysymysryhmaid_seq', 1, false);
    PERFORM pg_catalog.setval('public.monivalintavaihtoehto_monivalintavaihtoehtoid_seq', 1, false);
    PERFORM pg_catalog.setval('public.organisaatiopalvelu_log_id_seq', 1, false);
    PERFORM pg_catalog.setval('public.rahoitusmuoto_rahoitusmuotoid_seq', 1, false);
    PERFORM pg_catalog.setval('public.rooli_organisaatio_rooli_organisaatio_id_seq', 4, true);
    PERFORM pg_catalog.setval('public.vastaaja_vastaajaid_seq', 1, false);
    PERFORM pg_catalog.setval('public.vastaajatunnus_vastaajatunnusid_seq', 1, false);
    PERFORM pg_catalog.setval('public.vastaus_vastausid_seq', 1, false);
END$$;


ALTER TABLE ONLY public.jatkokysymys
    ADD CONSTRAINT jatkokysymys_pk PRIMARY KEY (jatkokysymysid);



ALTER TABLE ONLY public.jatkovastaus
    ADD CONSTRAINT jatkovastaus_pk PRIMARY KEY (jatkovastausid);



ALTER TABLE ONLY public.kayttaja
    ADD CONSTRAINT kayttaja_pk PRIMARY KEY (oid);



ALTER TABLE ONLY public.kayttaja
    ADD CONSTRAINT kayttaja_uid_unique UNIQUE (uid);



ALTER TABLE ONLY public.kayttajarooli
    ADD CONSTRAINT kayttajarooli_pk PRIMARY KEY (roolitunnus);



ALTER TABLE ONLY public.kieli
    ADD CONSTRAINT kieli_pkey PRIMARY KEY (kieli);



ALTER TABLE ONLY public.koulutusala
    ADD CONSTRAINT koulutusala_pkey PRIMARY KEY (koulutusalatunnus);



ALTER TABLE ONLY public.koulutustoimija_ja_tutkinto
    ADD CONSTRAINT koulutustoimija_ja_tutkinto_pkey PRIMARY KEY (koulutustoimija, tutkinto);



ALTER TABLE ONLY public.koulutustoimija
    ADD CONSTRAINT koulutustoimija_pkey PRIMARY KEY (ytunnus);



ALTER TABLE ONLY public.kysely_kysymysryhma
    ADD CONSTRAINT kysely_kr_jarjestys_un UNIQUE (kyselyid, jarjestys) DEFERRABLE;



ALTER TABLE ONLY public.kysely_kysymys
    ADD CONSTRAINT kysely_kysymys_pk PRIMARY KEY (kysymysid, kyselyid);



ALTER TABLE ONLY public.kysely_kysymysryhma
    ADD CONSTRAINT kysely_kysymysryhma_pk PRIMARY KEY (kyselyid, kysymysryhmaid);



ALTER TABLE ONLY public.kysely
    ADD CONSTRAINT kysely_nimi_fi_uniq UNIQUE (koulutustoimija, nimi_fi);



ALTER TABLE ONLY public.kysely
    ADD CONSTRAINT kysely_nimi_sv_uniq UNIQUE (koulutustoimija, nimi_sv);



ALTER TABLE ONLY public.kysely
    ADD CONSTRAINT kysely_pk PRIMARY KEY (kyselyid);



ALTER TABLE ONLY public.kyselykerta
    ADD CONSTRAINT kyselykerta_pk PRIMARY KEY (kyselykertaid);



ALTER TABLE ONLY public.kyselypohja
    ADD CONSTRAINT kyselypohja_pk PRIMARY KEY (kyselypohjaid);



ALTER TABLE ONLY public.kyselytyyppi_kentat
    ADD CONSTRAINT kyselytyyppi_kentat_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.kyselytyyppi
    ADD CONSTRAINT kyselytyyppi_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.monivalintavaihtoehto
    ADD CONSTRAINT kysymys_lisatieto_pk PRIMARY KEY (monivalintavaihtoehtoid);



ALTER TABLE ONLY public.kysymys
    ADD CONSTRAINT kysymys_pk PRIMARY KEY (kysymysid);



ALTER TABLE ONLY public.kysymys
    ADD CONSTRAINT kysymys_ryhma_jarjestys_un UNIQUE (kysymysryhmaid, jarjestys) DEFERRABLE;



ALTER TABLE ONLY public.kysymysryhma_kyselypohja
    ADD CONSTRAINT kysymysryhma_kyselypohja_pk PRIMARY KEY (kysymysryhmaid, kyselypohjaid);



ALTER TABLE ONLY public.kysymysryhma
    ADD CONSTRAINT "kysymysryhmä_pk" PRIMARY KEY (kysymysryhmaid);



ALTER TABLE ONLY public.monivalintavaihtoehto
    ADD CONSTRAINT mv_kysymys_un UNIQUE (kysymysid, jarjestys);



ALTER TABLE ONLY public.ohje
    ADD CONSTRAINT ohje_pkey PRIMARY KEY (ohjetunniste);



ALTER TABLE ONLY public.opintoala
    ADD CONSTRAINT opintoala_pkey PRIMARY KEY (opintoalatunnus);



ALTER TABLE ONLY public.oppilaitos
    ADD CONSTRAINT oppilaitos_pkey PRIMARY KEY (oppilaitoskoodi);



ALTER TABLE ONLY public.oppilaitostyyppi_tutkintotyyppi
    ADD CONSTRAINT oppilaitostyyppi_tutkintotyyppi_pkey PRIMARY KEY (tutkintotyyppi, oppilaitostyyppi);



ALTER TABLE ONLY public.organisaatiopalvelu_log
    ADD CONSTRAINT organisaatiopalvelu_log_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.rahoitusmuoto
    ADD CONSTRAINT rahoitusmuoto_pk PRIMARY KEY (rahoitusmuotoid);



ALTER TABLE ONLY public.rooli_organisaatio
    ADD CONSTRAINT rooli_organisaatio_pkey PRIMARY KEY (rooli_organisaatio_id);



ALTER TABLE ONLY public.tiedote
    ADD CONSTRAINT tiedote_pkey PRIMARY KEY (tiedoteid);



ALTER TABLE ONLY public.tila_enum
    ADD CONSTRAINT tila_enum_pkey PRIMARY KEY (nimi);



ALTER TABLE ONLY public.toimipaikka
    ADD CONSTRAINT toimipaikka_pkey PRIMARY KEY (toimipaikkakoodi);



ALTER TABLE ONLY public.tutkinto
    ADD CONSTRAINT tutkinto_pkey PRIMARY KEY (tutkintotunnus);



ALTER TABLE ONLY public.tutkintotyyppi
    ADD CONSTRAINT tutkintotyyppi_pkey PRIMARY KEY (tutkintotyyppi);



ALTER TABLE ONLY public.vastaaja
    ADD CONSTRAINT vastaaja_pk PRIMARY KEY (vastaajaid);



ALTER TABLE ONLY public.vastaajatunnus
    ADD CONSTRAINT vastaajatunnus__un UNIQUE (tunnus);



ALTER TABLE ONLY public.vastaajatunnus
    ADD CONSTRAINT vastaajatunnus_pk PRIMARY KEY (vastaajatunnusid);



ALTER TABLE ONLY public.vastaus
    ADD CONSTRAINT vastaus_pk PRIMARY KEY (vastausid);



CREATE UNIQUE INDEX kayttaja_unique_uid_voimassa ON public.kayttaja USING btree (lower((uid)::text)) WHERE voimassa;



CREATE UNIQUE INDEX kysymys_vastaaja_uniq_idx ON public.kysymys_vastaaja_view USING btree (vastaajaid);



CREATE INDEX kysymys_vastaaja_uusi_idx ON public.kysymys_vastaaja_view USING btree (uusi, vastaajaid);



CREATE INDEX kysymysryhma_taustakysymysryhma_idx ON public.kysymysryhma_taustakysymysryhma_view USING btree (kysymysryhmaid, taustakysymysryhmaid);



CREATE INDEX kysymysryhma_valtakunnallinen_idx ON public.kysymysryhma USING btree (valtakunnallinen, kysymysryhmaid);












CREATE INDEX vastaaja_kyselykerta_idx ON public.vastaaja USING btree (kyselykertaid);



CREATE INDEX vastaaja_taustakysymysryhma_idx ON public.vastaaja_taustakysymysryhma_view USING btree (taustakysymysryhmaid, vastaajaid);



CREATE INDEX vastaaja_vastaajatunnus_idx ON public.vastaaja USING btree (vastaajatunnusid);



CREATE INDEX vastaajatunnus_kyselykertaid_idx ON public.vastaajatunnus USING btree (kyselykertaid);



CREATE INDEX vastaus_jatkovastaus_kysymys_valtakunnallinen_vastausaika_idx ON public.vastaus_jatkovastaus_valtakunnallinen_view USING btree (vastausaika);



CREATE UNIQUE INDEX vastaus_jatkovastaus_valtakunnallinen_view_uniq_idx ON public.vastaus_jatkovastaus_valtakunnallinen_view USING btree (vastausid);



CREATE INDEX vastaus_raportointi_idx ON public.vastaus USING btree (kysymysid, numerovalinta, vastaajaid);



CREATE INDEX vastaus_vastaaja_kysymys_idx ON public.vastaus USING btree (vastaajaid, kysymysid);



CREATE INDEX vipunen_vastausaika_idx ON public.vipunen_view USING btree (vastausaika);



CREATE TRIGGER jatkokysymys_update BEFORE UPDATE ON public.jatkokysymys FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER jatkokysymysl_insert BEFORE INSERT ON public.jatkokysymys FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER jatkokysymysm_insert BEFORE INSERT ON public.jatkokysymys FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER jatkovastaus_update BEFORE UPDATE ON public.jatkovastaus FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER jatkovastausl_insert BEFORE INSERT ON public.jatkovastaus FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER jatkovastausm_insert BEFORE INSERT ON public.jatkovastaus FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kayttaja_update BEFORE UPDATE ON public.kayttaja FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kayttajal_insert BEFORE INSERT ON public.kayttaja FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER kayttajam_insert BEFORE INSERT ON public.kayttaja FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kayttajarooli_update BEFORE UPDATE ON public.kayttajarooli FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kayttajaroolil_insert BEFORE INSERT ON public.kayttajarooli FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER kayttajaroolim_insert BEFORE INSERT ON public.kayttajarooli FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kieli_cu_insert BEFORE INSERT ON public.kieli FOR EACH ROW EXECUTE PROCEDURE public.update_creator();



CREATE TRIGGER kieli_mu_insert BEFORE INSERT ON public.kieli FOR EACH ROW EXECUTE PROCEDURE public.update_modifier();



CREATE TRIGGER kieli_mu_update BEFORE UPDATE ON public.kieli FOR EACH ROW EXECUTE PROCEDURE public.update_modifier();



CREATE TRIGGER kieli_update BEFORE UPDATE ON public.kieli FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kielil_insert BEFORE INSERT ON public.kieli FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER kielim_insert BEFORE INSERT ON public.kieli FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER koulutusala_update BEFORE UPDATE ON public.koulutusala FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER koulutusalal_insert BEFORE INSERT ON public.koulutusala FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER koulutusalam_insert BEFORE INSERT ON public.koulutusala FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER koulutustoimija_ja_tutkinto_update BEFORE UPDATE ON public.koulutustoimija_ja_tutkinto FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER koulutustoimija_ja_tutkintol_insert BEFORE INSERT ON public.koulutustoimija_ja_tutkinto FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER koulutustoimija_ja_tutkintom_insert BEFORE INSERT ON public.koulutustoimija_ja_tutkinto FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER koulutustoimija_update BEFORE UPDATE ON public.koulutustoimija FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER koulutustoimijal_insert BEFORE INSERT ON public.koulutustoimija FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER koulutustoimijam_insert BEFORE INSERT ON public.koulutustoimija FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kysely_kysymys_update BEFORE UPDATE ON public.kysely_kysymys FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kysely_kysymysl_insert BEFORE INSERT ON public.kysely_kysymys FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER kysely_kysymysm_insert BEFORE INSERT ON public.kysely_kysymys FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kysely_kysymysryhma_update BEFORE UPDATE ON public.kysely_kysymysryhma FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kysely_kysymysryhmal_insert BEFORE INSERT ON public.kysely_kysymysryhma FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER kysely_kysymysryhmam_insert BEFORE INSERT ON public.kysely_kysymysryhma FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kysely_update BEFORE UPDATE ON public.kysely FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kyselykerta_update BEFORE UPDATE ON public.kyselykerta FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kyselykertal_insert BEFORE INSERT ON public.kyselykerta FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER kyselykertam_insert BEFORE INSERT ON public.kyselykerta FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kyselyl_insert BEFORE INSERT ON public.kysely FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER kyselym_insert BEFORE INSERT ON public.kysely FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kyselypohja_update BEFORE UPDATE ON public.kyselypohja FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kyselypohjal_insert BEFORE INSERT ON public.kyselypohja FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER kyselypohjam_insert BEFORE INSERT ON public.kyselypohja FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kysymys_update BEFORE UPDATE ON public.kysymys FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kysymysl_insert BEFORE INSERT ON public.kysymys FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER kysymysm_insert BEFORE INSERT ON public.kysymys FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kysymysryhma_kyselypohja_update BEFORE UPDATE ON public.kysymysryhma_kyselypohja FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kysymysryhma_kyselypohjal_insert BEFORE INSERT ON public.kysymysryhma_kyselypohja FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER kysymysryhma_kyselypohjam_insert BEFORE INSERT ON public.kysymysryhma_kyselypohja FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kysymysryhma_update BEFORE UPDATE ON public.kysymysryhma FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER kysymysryhmal_insert BEFORE INSERT ON public.kysymysryhma FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER kysymysryhmam_insert BEFORE INSERT ON public.kysymysryhma FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER monivalintavaihtoehto_update BEFORE UPDATE ON public.monivalintavaihtoehto FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER monivalintavaihtoehtol_insert BEFORE INSERT ON public.monivalintavaihtoehto FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER monivalintavaihtoehtom_insert BEFORE INSERT ON public.monivalintavaihtoehto FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER ohje_update BEFORE UPDATE ON public.ohje FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER ohjel_insert BEFORE INSERT ON public.ohje FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER ohjem_insert BEFORE INSERT ON public.ohje FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER opintoala_update BEFORE UPDATE ON public.opintoala FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER opintoalal_insert BEFORE INSERT ON public.opintoala FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER opintoalam_insert BEFORE INSERT ON public.opintoala FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER oppilaitos_update BEFORE UPDATE ON public.oppilaitos FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER oppilaitosl_insert BEFORE INSERT ON public.oppilaitos FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER oppilaitosm_insert BEFORE INSERT ON public.oppilaitos FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER oppilaitostyyppi_tutkintotyyppi_cu_insert BEFORE INSERT ON public.oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE public.update_creator();



CREATE TRIGGER oppilaitostyyppi_tutkintotyyppi_mu_insert BEFORE INSERT ON public.oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE public.update_modifier();



CREATE TRIGGER oppilaitostyyppi_tutkintotyyppi_mu_update BEFORE UPDATE ON public.oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE public.update_modifier();



CREATE TRIGGER oppilaitostyyppi_tutkintotyyppi_update BEFORE UPDATE ON public.oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER oppilaitostyyppi_tutkintotyyppil_insert BEFORE INSERT ON public.oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER oppilaitostyyppi_tutkintotyyppim_insert BEFORE INSERT ON public.oppilaitostyyppi_tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER organisaatiopalvelu_log_update BEFORE UPDATE ON public.organisaatiopalvelu_log FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER organisaatiopalvelu_logl_insert BEFORE INSERT ON public.organisaatiopalvelu_log FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER organisaatiopalvelu_logm_insert BEFORE INSERT ON public.organisaatiopalvelu_log FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER rahoitusmuoto_update BEFORE UPDATE ON public.rahoitusmuoto FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER rahoitusmuotol_insert BEFORE INSERT ON public.rahoitusmuoto FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER rahoitusmuotom_insert BEFORE INSERT ON public.rahoitusmuoto FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER rooli_organisaatio_update BEFORE UPDATE ON public.rooli_organisaatio FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER rooli_organisaatiol_insert BEFORE INSERT ON public.rooli_organisaatio FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER rooli_organisaatiom_insert BEFORE INSERT ON public.rooli_organisaatio FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER tiedote_update BEFORE UPDATE ON public.tiedote FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER tiedotel_insert BEFORE INSERT ON public.tiedote FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER tiedotem_insert BEFORE INSERT ON public.tiedote FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER tila_enum_update BEFORE UPDATE ON public.tila_enum FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER tila_enuml_insert BEFORE INSERT ON public.tila_enum FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER tila_enumm_insert BEFORE INSERT ON public.tila_enum FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER toimipaikka_update BEFORE UPDATE ON public.toimipaikka FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER toimipaikkal_insert BEFORE INSERT ON public.toimipaikka FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER toimipaikkam_insert BEFORE INSERT ON public.toimipaikka FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER tutkinto_update BEFORE UPDATE ON public.tutkinto FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER tutkintol_insert BEFORE INSERT ON public.tutkinto FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER tutkintom_insert BEFORE INSERT ON public.tutkinto FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER tutkintotyyppi_update BEFORE UPDATE ON public.tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER tutkintotyyppil_insert BEFORE INSERT ON public.tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER tutkintotyyppim_insert BEFORE INSERT ON public.tutkintotyyppi FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER vastaaja_update BEFORE UPDATE ON public.vastaaja FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER vastaajal_insert BEFORE INSERT ON public.vastaaja FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER vastaajam_insert BEFORE INSERT ON public.vastaaja FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER vastaajatunnus_update BEFORE UPDATE ON public.vastaajatunnus FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER vastaajatunnusl_insert BEFORE INSERT ON public.vastaajatunnus FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER vastaajatunnusm_insert BEFORE INSERT ON public.vastaajatunnus FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER vastaus_update BEFORE UPDATE ON public.vastaus FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



CREATE TRIGGER vastausl_insert BEFORE INSERT ON public.vastaus FOR EACH ROW EXECUTE PROCEDURE public.update_created();



CREATE TRIGGER vastausm_insert BEFORE INSERT ON public.vastaus FOR EACH ROW EXECUTE PROCEDURE public.update_stamp();



ALTER TABLE ONLY public.asteikko
    ADD CONSTRAINT asteikko_koulutustoimija_fkey FOREIGN KEY (koulutustoimija) REFERENCES public.koulutustoimija(ytunnus);



ALTER TABLE ONLY public.jatkovastaus
    ADD CONSTRAINT jatkovastaus_jatkokysymys_fk FOREIGN KEY (jatkokysymysid) REFERENCES public.jatkokysymys(jatkokysymysid);



ALTER TABLE ONLY public.kieli
    ADD CONSTRAINT kieli_luotu_kayttaja_fkey FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kieli
    ADD CONSTRAINT kieli_muutettu_kayttaja_fkey FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.opintoala
    ADD CONSTRAINT koulutusala_fk FOREIGN KEY (koulutusala) REFERENCES public.koulutusala(koulutusalatunnus);



ALTER TABLE ONLY public.koulutustoimija_ja_tutkinto
    ADD CONSTRAINT koulutustoimija_ja_tutkinto_koulutustoimija_fkey FOREIGN KEY (koulutustoimija) REFERENCES public.koulutustoimija(ytunnus);



ALTER TABLE ONLY public.koulutustoimija_ja_tutkinto
    ADD CONSTRAINT koulutustoimija_ja_tutkinto_tutkinto_fkey FOREIGN KEY (tutkinto) REFERENCES public.tutkinto(tutkintotunnus);



ALTER TABLE ONLY public.kysymysryhma_kyselypohja
    ADD CONSTRAINT kr_kp_kayttaja_fk FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysymysryhma_kyselypohja
    ADD CONSTRAINT kr_kp_kayttaja_fkv1 FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysymysryhma_kyselypohja
    ADD CONSTRAINT kr_kp_kyselypohja_fk FOREIGN KEY (kyselypohjaid) REFERENCES public.kyselypohja(kyselypohjaid);



ALTER TABLE ONLY public.kysymysryhma_kyselypohja
    ADD CONSTRAINT kr_kp_kysymysryhma_fk FOREIGN KEY (kysymysryhmaid) REFERENCES public.kysymysryhma(kysymysryhmaid);



ALTER TABLE ONLY public.kysely
    ADD CONSTRAINT kysely_kayttaja_fk FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysely
    ADD CONSTRAINT kysely_kayttaja_fkv1 FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysely
    ADD CONSTRAINT kysely_koulutustoimija_fk FOREIGN KEY (koulutustoimija) REFERENCES public.koulutustoimija(ytunnus);



ALTER TABLE ONLY public.kysely_kysymysryhma
    ADD CONSTRAINT kysely_kr_kayttaja_fk FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysely_kysymysryhma
    ADD CONSTRAINT kysely_kr_kayttaja_fkv1 FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysely_kysymysryhma
    ADD CONSTRAINT kysely_kr_kysely_fk FOREIGN KEY (kyselyid) REFERENCES public.kysely(kyselyid);



ALTER TABLE ONLY public.kysely_kysymysryhma
    ADD CONSTRAINT kysely_kr_kysymysryhma_fk FOREIGN KEY (kysymysryhmaid) REFERENCES public.kysymysryhma(kysymysryhmaid);



ALTER TABLE ONLY public.kysely_kysymys
    ADD CONSTRAINT kysely_kysymys_kayttaja_fk FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysely_kysymys
    ADD CONSTRAINT kysely_kysymys_kayttaja_fkv1 FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysely_kysymys
    ADD CONSTRAINT kysely_kysymys_kysely_fk FOREIGN KEY (kyselyid) REFERENCES public.kysely(kyselyid);



ALTER TABLE ONLY public.kysely_kysymys
    ADD CONSTRAINT kysely_kysymys_kysymys_fk FOREIGN KEY (kysymysid) REFERENCES public.kysymys(kysymysid);



ALTER TABLE ONLY public.kysely
    ADD CONSTRAINT kysely_oppilaitos_fk FOREIGN KEY (oppilaitos) REFERENCES public.oppilaitos(oppilaitoskoodi);



ALTER TABLE ONLY public.kysely
    ADD CONSTRAINT kysely_tila_enum_fk FOREIGN KEY (tila) REFERENCES public.tila_enum(nimi);



ALTER TABLE ONLY public.kysely
    ADD CONSTRAINT kysely_toimipaikka_fk FOREIGN KEY (toimipaikka) REFERENCES public.toimipaikka(toimipaikkakoodi);



ALTER TABLE ONLY public.kysely
    ADD CONSTRAINT kysely_tyyppi_fkey FOREIGN KEY (tyyppi) REFERENCES public.kyselytyyppi(id);



ALTER TABLE ONLY public.kyselykerta
    ADD CONSTRAINT kyselykerta_kayttaja_fk FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kyselykerta
    ADD CONSTRAINT kyselykerta_kayttaja_fkv1 FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kyselykerta
    ADD CONSTRAINT kyselykerta_kysely_fk FOREIGN KEY (kyselyid) REFERENCES public.kysely(kyselyid);



ALTER TABLE ONLY public.kyselypohja
    ADD CONSTRAINT kyselypohja_kayttaja_fk FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kyselypohja
    ADD CONSTRAINT kyselypohja_kayttaja_fkv1 FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kyselypohja
    ADD CONSTRAINT kyselypohja_koulutustoimija_fk FOREIGN KEY (koulutustoimija) REFERENCES public.koulutustoimija(ytunnus);



ALTER TABLE ONLY public.kyselypohja
    ADD CONSTRAINT kyselypohja_oppilaitos_fk FOREIGN KEY (oppilaitos) REFERENCES public.oppilaitos(oppilaitoskoodi);



ALTER TABLE ONLY public.kyselypohja
    ADD CONSTRAINT kyselypohja_tila_enum_fk FOREIGN KEY (tila) REFERENCES public.tila_enum(nimi);



ALTER TABLE ONLY public.kyselypohja
    ADD CONSTRAINT kyselypohja_toimipaikka_fk FOREIGN KEY (toimipaikka) REFERENCES public.toimipaikka(toimipaikkakoodi);



ALTER TABLE ONLY public.kyselytyyppi_kentat
    ADD CONSTRAINT kyselytyyppi_kentat_kyselytyyppi_id_fkey FOREIGN KEY (kyselytyyppi_id) REFERENCES public.kyselytyyppi(id);



ALTER TABLE ONLY public.kysymys
    ADD CONSTRAINT kysymys_jatkokysymys_fk FOREIGN KEY (jatkokysymysid) REFERENCES public.jatkokysymys(jatkokysymysid);



ALTER TABLE ONLY public.kysymys_jatkokysymys
    ADD CONSTRAINT kysymys_jatkokysymys_jatkokysymysid_fkey FOREIGN KEY (jatkokysymysid) REFERENCES public.kysymys(kysymysid);



ALTER TABLE ONLY public.kysymys_jatkokysymys
    ADD CONSTRAINT kysymys_jatkokysymys_kysymysid_fkey FOREIGN KEY (kysymysid) REFERENCES public.kysymys(kysymysid);



ALTER TABLE ONLY public.kysymys
    ADD CONSTRAINT kysymys_kayttaja_fk FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysymys
    ADD CONSTRAINT kysymys_kayttaja_fkv1 FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysymys
    ADD CONSTRAINT "kysymys_kysymysryhmä_fk" FOREIGN KEY (kysymysryhmaid) REFERENCES public.kysymysryhma(kysymysryhmaid);



ALTER TABLE ONLY public.kysymysryhma
    ADD CONSTRAINT kysymysryhma_kayttaja_fk FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysymysryhma
    ADD CONSTRAINT kysymysryhma_kayttaja_fkv1 FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.kysymysryhma
    ADD CONSTRAINT kysymysryhma_koulutustoimija_fk FOREIGN KEY (koulutustoimija) REFERENCES public.koulutustoimija(ytunnus);



ALTER TABLE ONLY public.kysymysryhma
    ADD CONSTRAINT kysymysryhma_oppilaitos_fk FOREIGN KEY (oppilaitos) REFERENCES public.oppilaitos(oppilaitoskoodi);



ALTER TABLE ONLY public.kysymysryhma
    ADD CONSTRAINT kysymysryhma_tila_enum_fk FOREIGN KEY (tila) REFERENCES public.tila_enum(nimi);



ALTER TABLE ONLY public.kysymysryhma
    ADD CONSTRAINT kysymysryhma_toimipaikka_fk FOREIGN KEY (toimipaikka) REFERENCES public.toimipaikka(toimipaikkakoodi);



ALTER TABLE ONLY public.monivalintavaihtoehto
    ADD CONSTRAINT mv_kayttaja_fk FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.monivalintavaihtoehto
    ADD CONSTRAINT mv_kayttaja_fkv1 FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.monivalintavaihtoehto
    ADD CONSTRAINT mv_kysymys_fk FOREIGN KEY (kysymysid) REFERENCES public.kysymys(kysymysid);



ALTER TABLE ONLY public.oppilaitos
    ADD CONSTRAINT oppilaitos_koulutustoimija_fk FOREIGN KEY (koulutustoimija) REFERENCES public.koulutustoimija(ytunnus);



ALTER TABLE ONLY public.oppilaitostyyppi_tutkintotyyppi
    ADD CONSTRAINT oppilaitostyyppi_tutkintotyyppi_luotu_kayttaja_fkey FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.oppilaitostyyppi_tutkintotyyppi
    ADD CONSTRAINT oppilaitostyyppi_tutkintotyyppi_muutettu_kayttaja_fkey FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.rooli_organisaatio
    ADD CONSTRAINT rooli_organisaatio_kayttaja_fkey FOREIGN KEY (kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.rooli_organisaatio
    ADD CONSTRAINT rooli_organisaatio_organisaatio_fkey FOREIGN KEY (organisaatio) REFERENCES public.koulutustoimija(ytunnus);



ALTER TABLE ONLY public.rooli_organisaatio
    ADD CONSTRAINT rooli_organisaatio_rooli_fkey FOREIGN KEY (rooli) REFERENCES public.kayttajarooli(roolitunnus);



ALTER TABLE ONLY public.toimipaikka
    ADD CONSTRAINT toimipaikka_oppilaitos_fk FOREIGN KEY (oppilaitos) REFERENCES public.oppilaitos(oppilaitoskoodi);



ALTER TABLE ONLY public.tutkinto
    ADD CONSTRAINT tutkinto_opintoala_fk FOREIGN KEY (opintoala) REFERENCES public.opintoala(opintoalatunnus);



ALTER TABLE ONLY public.vastaaja
    ADD CONSTRAINT vastaaja_kyselykerta_fk FOREIGN KEY (kyselykertaid) REFERENCES public.kyselykerta(kyselykertaid);



ALTER TABLE ONLY public.vastaaja
    ADD CONSTRAINT vastaaja_vastaajatunnus_fk FOREIGN KEY (vastaajatunnusid) REFERENCES public.vastaajatunnus(vastaajatunnusid);



ALTER TABLE ONLY public.vastaajatunnus
    ADD CONSTRAINT vastaajatunnus_kayttaja_fk FOREIGN KEY (luotu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.vastaajatunnus
    ADD CONSTRAINT vastaajatunnus_kayttaja_fkv1 FOREIGN KEY (muutettu_kayttaja) REFERENCES public.kayttaja(oid);



ALTER TABLE ONLY public.vastaajatunnus
    ADD CONSTRAINT vastaajatunnus_kyselykerta_fk FOREIGN KEY (kyselykertaid) REFERENCES public.kyselykerta(kyselykertaid);



ALTER TABLE ONLY public.vastaajatunnus
    ADD CONSTRAINT vastaajatunnus_rahmuoto_fk FOREIGN KEY (rahoitusmuotoid) REFERENCES public.rahoitusmuoto(rahoitusmuotoid);



ALTER TABLE ONLY public.vastaajatunnus
    ADD CONSTRAINT vastaajatunnus_suorituskieli_fkey FOREIGN KEY (suorituskieli) REFERENCES public.kieli(kieli);



ALTER TABLE ONLY public.vastaajatunnus_tiedot
    ADD CONSTRAINT vastaajatunnus_tiedot_kentta_fkey FOREIGN KEY (kentta) REFERENCES public.kyselytyyppi_kentat(id);



ALTER TABLE ONLY public.vastaajatunnus_tiedot
    ADD CONSTRAINT vastaajatunnus_tiedot_vastaajatunnus_id_fkey FOREIGN KEY (vastaajatunnus_id) REFERENCES public.vastaajatunnus(vastaajatunnusid);



ALTER TABLE ONLY public.vastaajatunnus
    ADD CONSTRAINT vastaajatunnus_tutkinto_fk FOREIGN KEY (tutkintotunnus) REFERENCES public.tutkinto(tutkintotunnus);



ALTER TABLE ONLY public.vastaajatunnus
    ADD CONSTRAINT vastaajatunnus_valmistavan_koulutuksen_jarjestaja_fkey FOREIGN KEY (valmistavan_koulutuksen_jarjestaja) REFERENCES public.koulutustoimija(ytunnus);



ALTER TABLE ONLY public.vastaajatunnus
    ADD CONSTRAINT vastaajatunnus_valmistavan_koulutuksen_oppilaitos_fkey FOREIGN KEY (valmistavan_koulutuksen_oppilaitos) REFERENCES public.oppilaitos(oppilaitoskoodi);



ALTER TABLE ONLY public.vastaajatunnus
    ADD CONSTRAINT vastaajatunnus_valmistavan_koulutuksen_toimipaikka_fkey FOREIGN KEY (valmistavan_koulutuksen_toimipaikka) REFERENCES public.toimipaikka(toimipaikkakoodi);



ALTER TABLE ONLY public.vastaus
    ADD CONSTRAINT vastaus_jatkovastaus_fk FOREIGN KEY (jatkovastausid) REFERENCES public.jatkovastaus(jatkovastausid);



ALTER TABLE ONLY public.vastaus
    ADD CONSTRAINT vastaus_kysymys_fk FOREIGN KEY (kysymysid) REFERENCES public.kysymys(kysymysid);



ALTER TABLE ONLY public.vastaus
    ADD CONSTRAINT vastaus_vastaaja_fk FOREIGN KEY (vastaajaid) REFERENCES public.vastaaja(vastaajaid);



REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;



REVOKE ALL ON TABLE public.jatkokysymys FROM PUBLIC;
REVOKE ALL ON TABLE public.kysely FROM PUBLIC;
REVOKE ALL ON TABLE public.kyselykerta FROM PUBLIC;
REVOKE ALL ON TABLE public.kyselypohja FROM PUBLIC;
REVOKE ALL ON TABLE public.vastaajatunnus FROM PUBLIC;
REVOKE ALL ON TABLE public.kysymysryhma FROM PUBLIC;
REVOKE ALL ON TABLE public.kysymys FROM PUBLIC;
REVOKE ALL ON TABLE public.asteikko FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.jatkokysymys_jatkokysymysid_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.jatkovastaus FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.jatkovastaus_jatkovastausid_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.kayttaja FROM PUBLIC;
REVOKE ALL ON TABLE public.kayttajarooli FROM PUBLIC;
REVOKE ALL ON TABLE public.oppilaitos FROM PUBLIC;
REVOKE ALL ON TABLE public.toimipaikka FROM PUBLIC;
REVOKE ALL ON TABLE public.kysely_organisaatio_view FROM PUBLIC;
REVOKE ALL ON TABLE public.vastaaja FROM PUBLIC;
REVOKE ALL ON TABLE public.kayttotilasto_view FROM PUBLIC;
REVOKE ALL ON TABLE public.kieli FROM PUBLIC;
REVOKE ALL ON TABLE public.koulutusala FROM PUBLIC;
REVOKE ALL ON TABLE public.koulutustoimija FROM PUBLIC;
REVOKE ALL ON TABLE public.koulutustoimija_ja_tutkinto FROM PUBLIC;
REVOKE ALL ON TABLE public.kysely_kaytettavissa FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.kysely_kyselyid_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.kysely_kysymys FROM PUBLIC;
REVOKE ALL ON TABLE public.kysely_kysymysryhma FROM PUBLIC;
REVOKE ALL ON TABLE public.kyselykerta_kaytettavissa FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.kyselykerta_kyselykertaid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.kyselypohja_kyselypohjaid_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.kyselypohja_organisaatio_view FROM PUBLIC;
REVOKE ALL ON TABLE public.kyselytyyppi FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.kyselytyyppi_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.kyselytyyppi_kentat FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.kyselytyyppi_kentat_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.kysymys_jatkokysymys FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.kysymys_kysymysid_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.vastaus FROM PUBLIC;
REVOKE ALL ON TABLE public.kysymys_vastaaja_view FROM PUBLIC;
REVOKE ALL ON TABLE public.kysymysryhma_kyselypohja FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.kysymysryhma_kysymysryhmaid_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.kysymysryhma_organisaatio_view FROM PUBLIC;
REVOKE ALL ON TABLE public.kysymysryhma_taustakysymysryhma_view FROM PUBLIC;
REVOKE ALL ON TABLE public.monivalintavaihtoehto FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.monivalintavaihtoehto_monivalintavaihtoehtoid_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.ohje FROM PUBLIC;
REVOKE ALL ON TABLE public.opintoala FROM PUBLIC;
REVOKE ALL ON TABLE public.oppilaitostyyppi_tutkintotyyppi FROM PUBLIC;
REVOKE ALL ON TABLE public.organisaatiopalvelu_log FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.organisaatiopalvelu_log_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.rahoitusmuoto FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.rahoitusmuoto_rahoitusmuotoid_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.rooli_organisaatio FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.rooli_organisaatio_rooli_organisaatio_id_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.tiedote FROM PUBLIC;
REVOKE ALL ON TABLE public.tila_enum FROM PUBLIC;
REVOKE ALL ON TABLE public.tutkinto FROM PUBLIC;
REVOKE ALL ON TABLE public.tutkintotyyppi FROM PUBLIC;
REVOKE ALL ON TABLE public.vastaaja_taustakysymysryhma_view FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.vastaaja_vastaajaid_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.vastaajatunnus_kaytettavissa FROM PUBLIC;
REVOKE ALL ON TABLE public.vastaajatunnus_tiedot FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.vastaajatunnus_vastaajatunnusid_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.vastaus_jatkovastaus_valtakunnallinen_view FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.vastaus_vastausid_seq FROM PUBLIC;
REVOKE ALL ON TABLE public.vipunen_view FROM PUBLIC;
REVOKE ALL ON TABLE public.kysymysryhma_taustakysymysryhma_view FROM PUBLIC;

--
-- This allows granting table permissions dynamically.
--
CREATE OR REPLACE FUNCTION public.grant_all_table_access(_permission text, _user text)
  RETURNS void AS
$func$
BEGIN
EXECUTE format('GRANT %s ON ALL TABLES IN SCHEMA PUBLIC TO %I', _permission, _user);
END
$func$ LANGUAGE plpgsql;

DO $$
BEGIN
  PERFORM public.grant_all_table_access('SELECT,INSERT,UPDATE,DELETE', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvo_user', 'arvo_snap_user', 'arvo_test_user') LIMIT 1;
END
$$;

CREATE OR REPLACE FUNCTION public.grant_all_sequence_access(_permission text, _user text)
  RETURNS void AS
$func$
BEGIN
EXECUTE format('GRANT %s ON ALL SEQUENCES IN SCHEMA PUBLIC TO %I', _permission, _user);
END
$func$ LANGUAGE plpgsql;

DO $$
BEGIN
  PERFORM public.grant_all_sequence_access('SELECT,USAGE', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvo_user', 'arvo_snap_user', 'arvo_test_user') LIMIT 1;
END
$$;


REFRESH MATERIALIZED VIEW public.kysymys_vastaaja_view;
REFRESH MATERIALIZED VIEW public.kysymysryhma_taustakysymysryhma_view;
REFRESH MATERIALIZED VIEW public.vastaaja_taustakysymysryhma_view;
REFRESH MATERIALIZED VIEW public.vastaus_jatkovastaus_valtakunnallinen_view;
