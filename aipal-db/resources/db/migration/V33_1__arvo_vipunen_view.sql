-- Materialized View: vipunen_view

DROP MATERIALIZED VIEW vipunen_view;

CREATE MATERIALIZED VIEW vipunen_view AS
 SELECT vastaus.vastausid,
    vastaus.vastausaika,
    vastaus.numerovalinta,
    kysymysryhma.valtakunnallinen,
        CASE vastaus.vaihtoehto
            WHEN 'kylla'::text THEN 1
            WHEN 'ei'::text THEN 0
            ELSE NULL::integer
        END AS vaihtoehto,
    regexp_replace(COALESCE(monivalintavaihtoehto.teksti_fi, monivalintavaihtoehto.teksti_sv)::text, '\n'::text, ' '::text, 'g'::text) AS monivalintavaihtoehto,
    kysymys.kysymysid,
    regexp_replace(kysymys.kysymys_fi::text, '\n'::text, ' '::text, 'g'::text) AS kysymys_fi,
    regexp_replace(kysymys.kysymys_sv::text, '\n'::text, ' '::text, 'g'::text) AS kysymys_sv,
    kysymys.kysymysryhmaid,
    regexp_replace(kysymysryhma.nimi_fi::text, '\n'::text, ' '::text, 'g'::text) AS kysymysryhma_fi,
    regexp_replace(kysymysryhma.nimi_sv::text, '\n'::text, ' '::text, 'g'::text) AS kysymysryhma_sv,
    kysymys.vastaustyyppi,
    vastaaja.vastaajaid,
    rahoitusmuoto.rahoitusmuoto,
    tutkinto.tutkintotunnus,
    regexp_replace(tutkinto.nimi_fi::text, '\n'::text, ' '::text, 'g'::text) AS tutkinto_fi,
    regexp_replace(tutkinto.nimi_sv::text, '\n'::text, ' '::text, 'g'::text) AS tutkinto_sv,
    opintoala.opintoalatunnus,
    regexp_replace(opintoala.nimi_fi::text, '\n'::text, ' '::text, 'g'::text) AS opintoala_fi,
    regexp_replace(opintoala.nimi_sv::text, '\n'::text, ' '::text, 'g'::text) AS opintoala_sv,
    vastaajatunnus.suorituskieli,
    vastaajatunnus.valmistavan_koulutuksen_jarjestaja,
    regexp_replace(valmistava_koulutustoimija.nimi_fi::text, '\n'::text, ' '::text, 'g'::text) AS valmistavan_koulutuksen_jarjestaja_fi,
    regexp_replace(valmistava_koulutustoimija.nimi_sv::text, '\n'::text, ' '::text, 'g'::text) AS valmistavan_koulutuksen_jarjestaja_sv,
    vastaajatunnus.valmistavan_koulutuksen_oppilaitos,
    regexp_replace(valmistava_oppilaitos.nimi_fi::text, '\n'::text, ' '::text, 'g'::text) AS valmistavan_koulutuksen_oppilaitos_fi,
    regexp_replace(valmistava_oppilaitos.nimi_sv::text, '\n'::text, ' '::text, 'g'::text) AS valmistavan_koulutuksen_oppilaitos_sv,
    kyselykerta.kyselykertaid,
    regexp_replace(kyselykerta.nimi::text, '\n'::text, ' '::text, 'g'::text) AS kyselykerta,
    kysely.kyselyid,
    regexp_replace(kysely.nimi_fi::text, '\n'::text, ' '::text, 'g'::text) AS kysely_fi,
    regexp_replace(kysely.nimi_fi::text, '\n'::text, ' '::text, 'g'::text) AS kysely_sv,
    kysely.koulutustoimija,
    regexp_replace(koulutustoimija.nimi_fi::text, '\n'::text, ' '::text, 'g'::text) AS koulutustoimija_fi,
    regexp_replace(koulutustoimija.nimi_sv::text, '\n'::text, ' '::text, 'g'::text) AS koulutustoimija_sv,
    regexp_replace(monivalintasukupuoli.teksti_fi::text, '\n'::text, ' '::text, 'g'::text) AS taustakysymys_sukupuoli,
    regexp_replace(monivalintaika.teksti_fi::text, '\n'::text, ' '::text, 'g'::text) AS taustakysymys_ika,
    regexp_replace(monivalintapohjakoulutus.teksti_fi::text, '\n'::text, ' '::text, 'g'::text) AS taustakysymys_pohjakoulutus
   FROM vastaus
     JOIN kysymys ON vastaus.kysymysid = kysymys.kysymysid
     JOIN kysymysryhma ON kysymys.kysymysryhmaid = kysymysryhma.kysymysryhmaid
     LEFT JOIN monivalintavaihtoehto ON kysymys.vastaustyyppi::text = 'monivalinta'::text AND monivalintavaihtoehto.kysymysid = kysymys.kysymysid AND vastaus.numerovalinta = monivalintavaihtoehto.jarjestys
     JOIN vastaaja ON vastaus.vastaajaid = vastaaja.vastaajaid
     JOIN vastaajatunnus ON vastaaja.vastaajatunnusid = vastaajatunnus.vastaajatunnusid
     LEFT JOIN tutkinto ON vastaajatunnus.tutkintotunnus::text = tutkinto.tutkintotunnus::text
     LEFT JOIN opintoala ON tutkinto.opintoala::text = opintoala.opintoalatunnus::text
     LEFT JOIN koulutustoimija valmistava_koulutustoimija ON vastaajatunnus.valmistavan_koulutuksen_jarjestaja::text = valmistava_koulutustoimija.ytunnus::text
     LEFT JOIN oppilaitos valmistava_oppilaitos ON vastaajatunnus.valmistavan_koulutuksen_oppilaitos::text = valmistava_oppilaitos.oppilaitoskoodi::text
     JOIN rahoitusmuoto ON vastaajatunnus.rahoitusmuotoid = rahoitusmuoto.rahoitusmuotoid
     JOIN kyselykerta ON vastaajatunnus.kyselykertaid = kyselykerta.kyselykertaid
     JOIN kysely ON kyselykerta.kyselyid = kysely.kyselyid
     LEFT JOIN koulutustoimija ON kysely.koulutustoimija::text = koulutustoimija.ytunnus::text
     LEFT JOIN vastaus vastaussukupuoli ON vastaussukupuoli.vastaajaid = vastaaja.vastaajaid AND vastaussukupuoli.kysymysid = (select kysymysid from kysymys where kysymysryhmaid in (select kysymysryhmaid from kysymysryhma where valtakunnallinen=true and taustakysymykset=true and tila='julkaistu' and selite_fi = 'AVOP Taustatiedot') and kysymys_fi='Sukupuoli')
     LEFT JOIN monivalintavaihtoehto monivalintasukupuoli ON monivalintasukupuoli.kysymysid = vastaussukupuoli.kysymysid AND vastaussukupuoli.numerovalinta = monivalintasukupuoli.jarjestys
     LEFT JOIN vastaus vastausika ON vastausika.vastaajaid = vastaaja.vastaajaid AND vastausika.kysymysid = (select kysymysid from kysymys where kysymysryhmaid in (select kysymysryhmaid from kysymysryhma where valtakunnallinen=true and taustakysymykset=true and tila='julkaistu' and selite_fi = 'AVOP Taustatiedot') and kysymys_fi='Ikä')
     LEFT JOIN monivalintavaihtoehto monivalintaika ON monivalintaika.kysymysid = vastausika.kysymysid AND vastausika.numerovalinta = monivalintaika.jarjestys
     LEFT JOIN vastaus vastauspohjakoulutus ON vastauspohjakoulutus.vastaajaid = vastaaja.vastaajaid AND vastauspohjakoulutus.kysymysid = (select kysymysid from kysymys where kysymysryhmaid in (select kysymysryhmaid from kysymysryhma where valtakunnallinen=true and taustakysymykset=true and tila='julkaistu' and selite_fi = 'AVOP Taustatiedot') and kysymys_fi='Pohjakoulutus')
     LEFT JOIN monivalintavaihtoehto monivalintapohjakoulutus ON monivalintapohjakoulutus.kysymysid = vastauspohjakoulutus.kysymysid AND vastauspohjakoulutus.numerovalinta = monivalintapohjakoulutus.jarjestys
  WHERE kysymys.vastaustyyppi::text <> 'vapaateksti'::text
  AND vastaus.kysymysid NOT IN (
	select kysymysid from kysymys where kysymysryhmaid in (
		select kysymysryhmaid from kysymysryhma
		where valtakunnallinen=true and taustakysymykset=true and tila='julkaistu' and selite_fi = 'AVOP Taustatiedot'
	)
  )
  AND (monivalintasukupuoli.teksti_fi IS NOT NULL OR monivalintaika.teksti_fi IS NOT NULL OR monivalintapohjakoulutus.teksti_fi IS NOT NULL)
WITH DATA;
