INSERT INTO kyselytyyppi (id, nimi_fi, nimi_sv, nimi_en) VALUES ('tyoelamapalaute', 'Työelämäpalaute', 'Arbetslivsrespons', 'Working life');
--;;
INSERT INTO kyselytyyppi_kentat (kentta_id, kentta_fi, kentta_sv, kentta_en, kyselytyyppi)
VALUES ('opiskelijan_nimi', 'Opiskelijan nimi', 'Studentens namn', 'Name of the student', 'tyoelamapalaute');
--;;
INSERT INTO kyselytyyppi_kentat (kentta_id, kentta_fi, kentta_sv, kentta_en, kyselytyyppi)
VALUES ('tyonantaja', 'Työnantaja', '', '', 'tyoelamapalaute');
--;;
INSERT INTO kyselytyyppi_kentat (kentta_id, kentta_fi, kentta_sv, kentta_en, kyselytyyppi)
VALUES ('tutkinto', 'Tutkinto', 'sv', 'en', 'tyoelamapalaute');
--;;
INSERT INTO kyselytyyppi_kentat (kentta_id, kentta_fi, kentta_sv, kentta_en, kyselytyyppi)
VALUES ('tutkinnon_osa', 'Tutkinnon osa', 'Examensdel', 'Qualification units', 'tyoelamapalaute');
--;;
INSERT INTO kyselytyyppi_kentat (kentta_id, kentta_fi, kentta_sv, kentta_en, kyselytyyppi)
VALUES ('tyopaikkajakson_alkupvm', 'Työelämässä oppimisen alkupvm', 'Startdatum för lärande i arbetslivet', 'Work-based learning start date', 'tyoelamapalaute');
--;;
INSERT INTO kyselytyyppi_kentat (kentta_id, kentta_fi, kentta_sv, kentta_en, kyselytyyppi)
VALUES ('tyopaikkajakson_loppupvm', 'Työelämässä oppimisen loppupvm', 'Slutdatum för lärande i arbetslivet', 'Work-based learning end date', 'tyoelamapalaute');
--;;
INSERT INTO kyselytyyppi_kentat (kentta_id, kentta_fi, kentta_sv, kentta_en, kyselytyyppi)
VALUES ('sopimustyyppi', 'Sopimustyyppi', '', '', 'tyoelamapalaute');
--;;
INSERT INTO kyselytyyppi_kentat (kentta_id, kentta_fi, kentta_sv, kentta_en, kyselytyyppi)
VALUES ('toimipiste', 'Toimipiste', '', '', 'tyoelamapalaute');
--;;
INSERT INTO kyselytyyppi_kentat (kentta_id, kentta_fi, kentta_sv, kentta_en, kyselytyyppi)
VALUES ('osaamisala', 'Osaamisala', '', '', 'tyoelamapalaute');
--;;
INSERT INTO kyselytyyppi_kentat (kentta_id, kentta_fi, kentta_sv, kentta_en, kyselytyyppi)
VALUES ('tutkintonimike', 'Tutkintonimike', '', '', 'tyoelamapalaute');
--;;
INSERT INTO kyselytyyppi_kentat (kentta_id, kentta_fi, kentta_sv, kentta_en, kyselytyyppi)
VALUES ('tyopaikka', 'Työpaikka', '', '', 'tyoelamapalaute');
--;;
CREATE TABLE nippu(
    tunniste TEXT NOT NULL PRIMARY KEY ,
    kyselyid INTEGER NOT NULL REFERENCES kysely(kyselyid),
    voimassa_alkupvm DATE NOT NULL,
    voimassa_loppupvm DATE,
    taustatiedot JSONB
);

--;;
do $$
    begin
        PERFORM grant_table_access('ALL PRIVILEGES', 'nippu', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvo_user', 'arvo_snap_user', 'arvo_test_user') LIMIT 1;
    end
$$;
--;;