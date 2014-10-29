-- maksimipituus pakolliseksi vapaatekstikentille
ALTER TABLE kysymys ADD CHECK (max_vastaus is not null OR vastaustyyppi != 'vapaateksti') ;
-- maksimipituus pakollinen jos ei-jatkovastaus
ALTER TABLE jatkokysymys ADD CHECK (max_vastaus is not null OR (ei_teksti_fi is null and ei_teksti_sv is null)) ;
