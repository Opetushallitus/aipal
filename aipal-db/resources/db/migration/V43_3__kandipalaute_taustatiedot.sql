INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi, kentta_sv, kentta_en)
    VALUES ((SELECT id FROM kyselytyyppi WHERE nimi_fi = 'Kandipalaute'), 'kieli', 'Suorituskieli', 'Examensspråk', 'Language of degree');

INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi, kentta_sv, kentta_en)
    VALUES ((SELECT id FROM kyselytyyppi WHERE nimi_fi = 'Kandipalaute'), 'tutkinto', 'Tutkinto', 'Examen', 'Degree');

INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi, kentta_sv, kentta_en)
  VALUES ((SELECT id FROM kyselytyyppi WHERE nimi_fi = 'Kandipalaute'), 'toimipaikka', 'Toimipaikka', 'Verksamhetsställe', 'Operational unit');

INSERT INTO kyselytyyppi_kentat (kyselytyyppi_id, kentta_id, kentta_fi, kentta_sv, kentta_en)
  VALUES ((SELECT id FROM kyselytyyppi WHERE nimi_fi = 'Kandipalaute'), 'kunta', 'Kunta', 'Kommun', 'Region');