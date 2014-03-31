create user aipal_adm with password 'aipal-adm';
CREATE DATABASE aipal;
GRANT ALL PRIVILEGES ON DATABASE aipal to aipal_adm;

create user aipal_user with password 'aipal';
GRANT CONNECT ON DATABASE aipal TO aipal_user;


