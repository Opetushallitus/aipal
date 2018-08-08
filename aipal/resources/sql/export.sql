-- :name export-kyselyt :? :*
SELECT k.koulutustoimija, k.tyyppi, k.kyselyid, k.nimi_fi AS kysely_fi, k.nimi_sv AS kysely_sv, k.nimi_en AS kysely_en,
  k.voimassa_alkupvm AS kysely_voimassa_alkupvm, k.voimassa_loppupvm AS kysely_voimassa_loppupvm, k.tila AS kysely_tila,
  kk.kyselykertaid, kk.nimi AS kyselykerta, kk.kategoria->>'vuosi' AS kyselykerta_vuosi,
  kp.kyselypohjaid, kp.nimi_fi AS kyselypohja_nimi, kp.kategoria->>'tarkenne' AS kyselypohja_tarkenne
FROM kyselykerta kk
JOIN kysely k ON kk.kyselyid = k.kyselyid
LEFT JOIN kyselypohja kp ON k.kyselypohjaid = kp.kyselypohjaid;

-- :name export-vastaukset :? :*
SELECT k.koulutustoimija, k.tyyppi, k.kyselyid, kk.kyselykertaid,
  vt.voimassa_alkupvm AS vastaajatunnus_alkupvm,
  v.vastaajaid, vt.tunnus AS vastaajatunnus, vt.vastaajatunnusid,  vs.vastaajaid,
  v.vastausid, v.kysymysid, v.vastausaika, v.numerovalinta, v.vapaateksti, v.vaihtoehto,
  monivalintavaihtoehto.teksti_fi AS monivalintavaihtoehto_fi,
  monivalintavaihtoehto.teksti_sv AS monivalintavaihtoehto_sv,
  monivalintavaihtoehto.teksti_en AS monivalintavaihtoehto_en

FROM vastaus v
JOIN vastaaja vs ON v.vastaajaid = vs.vastaajaid
JOIN kysymys kys ON v.kysymysid = kys.kysymysid
JOIN kysymysryhma kr ON kys.kysymysryhmaid = kr.kysymysryhmaid
LEFT JOIN monivalintavaihtoehto ON kys.vastaustyyppi = 'monivalinta'
                                   AND monivalintavaihtoehto.kysymysid = kys.kysymysid
                                   AND v.numerovalinta = monivalintavaihtoehto.jarjestys
JOIN vastaajatunnus vt ON vs.vastaajatunnusid = vt.vastaajatunnusid
JOIN kyselykerta kk ON vs.kyselykertaid = kk.kyselykertaid
JOIN kysely k ON kk.kyselyid = k.kyselyid;


-- :name export-kysymykset :? :*
SELECT k.kysymysid, k.vastaustyyppi, k.kysymys_fi, k.kysymys_sv, k.kysymys_en, k.kategoria, k.jatkokysymys,
  kjk.kysymysid AS jatkokysymys_kysymysid,
  kr.kysymysryhmaid, kr.nimi_fi AS kysymysryhma_fi, kr.nimi_sv AS kysymysryhma_sv, kr.nimi_en AS kysymysryhma_en,
  kr.valtakunnallinen
FROM kysymys k
LEFT JOIN kysymys_jatkokysymys kjk ON k.kysymysid = kjk.jatkokysymysid
JOIN kysymysryhma kr ON k.kysymysryhmaid = kr.kysymysryhmaid
-- TODO: Vapaatekstien ja omien kysymysten rajaus pois tarvittaessa (vipunen)
WHERE kr.tila = 'julkaistu';

-- :name export-taustatiedot :? :*
SELECT v.vastaajaid,vt.vastaajatunnusid,
  vt.valmistavan_koulutuksen_jarjestaja, vt.valmistavan_koulutuksen_oppilaitos, vt.suorituskieli,
  vt.taustatiedot
FROM vastaaja v
JOIN vastaajatunnus vt ON v.vastaajatunnusid = vt.vastaajatunnusid
JOIN kyselykerta kk ON vt.kyselykertaid = kk.kyselykertaid
JOIN kysely k on kk.kyselyid = k.kyselyid;
-- TODO: Taustatietojen raportointirajaukset

-- :name hae-api-kayttaja :? :1
SELECT * FROM api_kayttajat WHERE tunnus = :tunnus;