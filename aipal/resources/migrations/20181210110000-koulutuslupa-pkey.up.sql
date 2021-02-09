ALTER TABLE koulutustoimija_ja_tutkinto DROP CONSTRAINT koulutustoimija_ja_tutkinto_pkey;
--;;
ALTER TABLE koulutustoimija_ja_tutkinto ADD CONSTRAINT koulutustoimija_ja_tutkinto_pkey PRIMARY KEY (koulutustoimija, tutkinto, voimassa_alkupvm);
