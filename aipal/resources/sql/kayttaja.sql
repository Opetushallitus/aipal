--:name hae-voimassaoleva-kayttaja :? :1
SELECT * FROM kayttaja
WHERE uid = :uid
      AND voimassa = TRUE
      AND (muutettuaika + :voimassaolo::interval >= now()
           OR uid IN ('JARJESTELMA', 'KONVERSIO', 'INTEGRAATIO', 'VASTAAJA'));