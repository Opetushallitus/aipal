-- :name hae-vipunen-vastaukset :? :*
SELECT * FROM vipunen_view
WHERE vastausaika BETWEEN :alkupvm::date AND :loppupvm::date
--~ (if (:since params) "AND vastausid > :since")
ORDER BY vastausid ASC LIMIT :pagelength;


-- :name hae-uraseuranta-vastaukset :? :*
SELECT k.kyselyid, k.koulutustoimija,
  k.nimi_fi AS kysely_fi, k.nimi_sv AS kysely_sv, k.nimi_en AS kysely_en,
  kk.kyselykertaid, kk.nimi AS kyselykerta,
  kr.selite_fi, kr.nimi_fi, kr.nimi_sv, kr.nimi_en, kr.valtakunnallinen,
  (select jarjestys from kysely_kysymysryhma where kyselyid=k.kyselyid and kysymysryhmaid=kr.kysymysryhmaid) kysymysryhmajarjestys,
  kys.vastaustyyppi, kys.kysymys_fi, kys.kysymys_sv, kys.kysymys_en, kys.jarjestys,
  v.vastausid, v.numerovalinta, CASE v.vaihtoehto WHEN 'kylla' THEN 1 WHEN 'ei' THEN 0 ELSE NULL END AS vaihtoehto,
  vt.tunnus, vt.valmistavan_koulutuksen_jarjestaja, vt.valmistavan_koulutuksen_oppilaitos,vt.taustatiedot,
  vs.vastaajaid,
  t.tutkintotunnus, t.opintoala,
  COALESCE(monivalintavaihtoehto.teksti_fi,COALESCE(monivalintavaihtoehto.teksti_sv, monivalintavaihtoehto.teksti_en)) AS monivalintavaihtoehto
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
WHERE k.tyyppi = 3
--~(if (:since params) "AND vastausid > :since")
ORDER BY vastausid LIMIT :pagelength;
