CREATE TABLE rahoitusmuoto(
  rahoitusmuotoid INTEGER PRIMARY KEY,
  rahoitusmuoto TEXT NOT NULL,
  luotuaika TIMESTAMP WITH TIME ZONE,
  muutettuaika TIMESTAMP WITH TIME ZONE
);
--;;
INSERT INTO rahoitusmuoto(rahoitusmuotoid, rahoitusmuoto, luotuaika, muutettuaika)
    VALUES (5, 'ei_rahoitusmuotoa', now(), now());
--;;
ALTER TABLE vastaajatunnus ADD COLUMN rahoitusmuotoid INTEGER REFERENCES rahoitusmuoto(rahoitusmuotoid);
--;;
UPDATE vastaajatunnus SET rahoitusmuotoid = 5;
