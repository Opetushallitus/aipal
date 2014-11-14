-- suomen tai ruotsinkielinen nimi pakolliseksi
ALTER TABLE kysely
  ADD CONSTRAINT nimi_fi_tai_sv_pakollinen CHECK (nimi_fi IS NOT NULL OR nimi_sv IS NOT NULL);

ALTER TABLE kysymys
  ALTER COLUMN kysymys_fi DROP NOT NULL,
  ADD CONSTRAINT kysymys_fi_tai_sv_pakollinen CHECK (kysymys_fi IS NOT NULL OR kysymys_sv IS NOT NULL);

ALTER TABLE kysymysryhma
  ALTER COLUMN nimi_fi DROP NOT NULL,
  ADD CONSTRAINT nimi_fi_tai_sv_pakollinen CHECK (nimi_fi IS NOT NULL OR nimi_sv IS NOT NULL);

ALTER TABLE jatkokysymys
  ADD CONSTRAINT jatkokysymys_tekstit_check CHECK (kylla_teksti_fi is not null OR kylla_teksti_sv is not null OR ei_teksti_fi is not null OR ei_teksti_sv is not null);

ALTER TABLE monivalintavaihtoehto
  ALTER COLUMN teksti_fi DROP NOT NULL,
  ALTER COLUMN teksti_sv DROP NOT NULL,
  ADD CONSTRAINT teksti_fi_tai_sv_pakollinen CHECK (teksti_fi IS NOT NULL OR teksti_sv IS NOT NULL);

ALTER TABLE kyselypohja
  ADD CONSTRAINT nimi_fi_tai_sv_pakollinen CHECK (nimi_fi IS NOT NULL OR nimi_sv IS NOT NULL);
