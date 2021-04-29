do $$
    begin
        PERFORM grant_table_access('SELECT', 'nippu', pg_user.usename) FROM pg_catalog.pg_user WHERE pg_user.usename IN ('arvovastaus_user', 'arvovastaus_snap_user', 'arvovastaus_test_user') LIMIT 1;
    end
$$;
