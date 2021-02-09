ALTER TABLE kysymys ADD COLUMN kategoria JSONB;
--;;
ALTER TABLE kyselypohja ADD COLUMN kategoria JSONB;
--;;
ALTER TABLE kysely ADD COLUMN kyselypohjaid INTEGER REFERENCES kyselypohja(kyselypohjaid);
--;;
CREATE TABLE api_kayttajat(
  tunnus TEXT NOT NULL PRIMARY KEY,
  salasana TEXT NOT NULL,
  organisaatio TEXT NOT NULL REFERENCES koulutustoimija(ytunnus)
);
