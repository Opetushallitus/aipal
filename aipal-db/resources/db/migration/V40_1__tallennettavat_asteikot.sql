CREATE TABLE asteikko(
  koulutustoimija VARCHAR(10) REFERENCES koulutustoimija(ytunnus),
  nimi TEXT NOT NULL,
  asteikko JSON NOT NULL
);