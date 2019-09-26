CREATE TABLE automaattikysely(
    tunniste TEXT NOT NULL PRIMARY KEY,
    kyselytyyppi TEXT NOT NULL REFERENCES kyselytyyppi(id),
    kyselypohjaid INTEGER NOT NULL REFERENCES kyselypohja(kyselypohjaid),
    voimassa_alkupvm DATE NOT NULL,
    nimi_fi TEXT,
    nimi_sv TEXT,
    nimi_en TEXT,
    kyselykerta_nimi TEXT NOT NULL,
    kyselykerta_kategoria JSONB,
    selite_fi TEXT,
    selite_sv TEXT,
    selite_en TEXT,
    automatisointi_voimassa_loppupvm DATE
);

ALTER TABLE automaattikysely ADD CONSTRAINT automaattikysely_nimi CHECK
    (COALESCE(nimi_fi, nimi_sv, nimi_en) IS NOT NULL);

ALTER TABLE kysely ADD COLUMN kategoria JSONB;

