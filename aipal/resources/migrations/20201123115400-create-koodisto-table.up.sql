CREATE SEQUENCE IF NOT EXISTS koodi_seq;

CREATE TABLE koodi (
    id integer primary key default nextval('koodi_seq'),
    koodisto_uri character varying(256) NOT NULL,
    nimi_fi character varying(256),
    nimi_sv character varying(256),
    nimi_en character varying(256),
    koodi_arvo character varying(256) NOT NULL,
    voimassa_alkupvm date,
    voimassa_loppupvm date
);

CREATE INDEX IF NOT EXISTS koodi_koodisto_uri_idx ON koodi (koodisto_uri);


-- This allows granting table permissions dynamically.
CREATE OR REPLACE FUNCTION grant_table_access(_permission text, _table text, _user text)
  RETURNS void AS
$func$
BEGIN
   EXECUTE format('GRANT %s ON %I TO %I', _permission, _table, _user);
END
$func$ LANGUAGE plpgsql;

do $$
begin
  PERFORM grant_table_access('ALL PRIVILEGES', 'koodi', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvo_user', 'arvo_snap_user', 'arvo_test_user') LIMIT 1;
end
$$;

do $$
begin
  PERFORM grant_table_access('ALL PRIVILEGES', 'koodi_seq', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvo_user', 'arvo_snap_user', 'arvo_test_user') LIMIT 1;
end
$$;

do $$
begin
  PERFORM grant_table_access('SELECT', 'koodi', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
end
$$;
