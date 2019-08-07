-- :name hae-vipunen-vastaukset :? :*
SELECT * FROM vipunen_view
WHERE vastausaika BETWEEN :alkupvm::date AND :loppupvm::date
--~ (if (:since params) "AND vastausid > :since")
ORDER BY vastausid ASC LIMIT :pagelength;


-- :name hae-uraseuranta-vastaukset :? :*
SELECT k.kyselyid, k.koulutustoimija,
  k.nimi_fi AS kysely_fi, k.nimi_sv AS kysely_sv, k.nimi_en AS kysely_en,
  kk.kyselykertaid, kk.nimi AS kyselykerta,
  kr.kysymysryhmaid, kr.nimi_fi AS kysymysryhma_fi, kr.nimi_sv AS kysymysryhma_sv, kr.nimi_en AS kysymysryhma_en, kr.valtakunnallinen,
  (select jarjestys from kysely_kysymysryhma where kyselyid=k.kyselyid and kysymysryhmaid=kr.kysymysryhmaid) AS kysymysryhma_jarjestys,
  kys.kysymysid,

  COALESCE(kys.jarjestys, (SELECT jkk.jarjestys + 0.1 FROM kysymys jkk
    WHERE  kjk.kysymysid = jkk.kysymysid)) AS jarjestys,

  kys.vastaustyyppi, kys.kysymys_fi, kys.kysymys_sv, kys.kysymys_en, kys.jatkokysymys,

  kjk.kysymysid AS jatkokysymys_kysymysid,
  v.vastausid, v.numerovalinta, v.vapaateksti,
  monivalintavaihtoehto.teksti_fi AS monivalintavaihtoehto_fi,
  monivalintavaihtoehto.teksti_sv AS monivalintavaihtoehto_sv,
  monivalintavaihtoehto.teksti_en AS monivalintavaihtoehto_en,
  CASE v.vaihtoehto WHEN 'kylla' THEN TRUE WHEN 'ei' THEN FALSE ELSE NULL END AS vaihtoehto,
  vs.vastaajaid,
  vt.tunnus AS vastaajatunnus,  vt.valmistavan_koulutuksen_jarjestaja, vt.valmistavan_koulutuksen_oppilaitos,vt.taustatiedot,
  t.tutkintotunnus

FROM kysely k
JOIN kyselykerta kk ON k.kyselyid = kk.kyselyid
JOIN vastaajatunnus vt ON kk.kyselykertaid = vt.kyselykertaid
JOIN vastaaja vs ON vt.vastaajatunnusid = vs.vastaajatunnusid
JOIN vastaus v ON vs.vastaajaid = v.vastaajaid
JOIN kysymys kys ON v.kysymysid = kys.kysymysid
JOIN kysymysryhma kr ON kys.kysymysryhmaid = kr.kysymysryhmaid
LEFT JOIN tutkinto t ON vt.tutkintotunnus = t.tutkintotunnus
LEFT JOIN monivalintavaihtoehto ON kys.vastaustyyppi = 'monivalinta'
  AND monivalintavaihtoehto.kysymysid = kys.kysymysid
  AND v.numerovalinta = monivalintavaihtoehto.jarjestys
LEFT JOIN kysymys_jatkokysymys kjk ON kys.kysymysid = kjk.jatkokysymysid
WHERE k.tyyppi = 'yo-uraseuranta'
AND kys.raportoitava = TRUE
AND taustatiedot IS NOT NULL
--~(if (:since params) "AND vastausid > :since")
ORDER BY vastausid LIMIT :pagelength;
