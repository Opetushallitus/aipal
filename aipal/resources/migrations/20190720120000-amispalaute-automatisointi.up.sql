CREATE TABLE amispalaute_automatisointi(
    koulutustoimija TEXT REFERENCES koulutustoimija(ytunnus) PRIMARY KEY,
    voimassa_alkaen DATE NOT NULL,
    lahde TEXT NOT NULL
);
