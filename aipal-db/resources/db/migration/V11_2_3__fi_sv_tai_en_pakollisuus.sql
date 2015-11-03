-- suomen, ruotsin tai englanninkieliset kentät pakolliseksi
ALTER TABLE kysely
  DROP CONSTRAINT nimi_fi_tai_sv_pakollinen,
  ADD CONSTRAINT nimi_fi_sv_tai_en_pakollinen CHECK (COALESCE(nimi_fi, nimi_sv, nimi_en) IS NOT NULL);

ALTER TABLE kysymys
  DROP CONSTRAINT kysymys_fi_tai_sv_pakollinen,
  ADD CONSTRAINT kysymys_fi_sv_tai_en_pakollinen CHECK (COALESCE(kysymys_fi, kysymys_sv, kysymys_en) IS NOT NULL);

ALTER TABLE kysymysryhma
  DROP CONSTRAINT nimi_fi_tai_sv_pakollinen,
  ADD CONSTRAINT nimi_fi_sv_tai_en_pakollinen CHECK (COALESCE(nimi_fi, nimi_sv, nimi_en) IS NOT NULL);

ALTER TABLE jatkokysymys
  DROP CONSTRAINT jatkokysymys_tekstit_check,
  ADD CONSTRAINT jatkokysymys_tekstit_check CHECK (COALESCE(kylla_teksti_fi, kylla_teksti_sv, kylla_teksti_en, ei_teksti_fi, ei_teksti_sv, ei_teksti_en) IS NOT NULL);

ALTER TABLE monivalintavaihtoehto
  DROP CONSTRAINT teksti_fi_tai_sv_pakollinen,
  ADD CONSTRAINT teksti_fi_sv_tai_en_pakollinen CHECK (COALESCE(teksti_fi, teksti_sv, teksti_en) IS NOT NULL);

ALTER TABLE kyselypohja
  DROP CONSTRAINT nimi_fi_tai_sv_pakollinen,
  ADD CONSTRAINT nimi_fi_sv_tai_en_pakollinen CHECK (COALESCE(nimi_fi, nimi_sv, nimi_en) IS NOT NULL);
