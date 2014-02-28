#!/bin/bash
set -eu

if [ $# -ne 6 ]
then
    echo "$0 <dump-tiedosto> <salasanatiedosto> <kanta> <käyttäjä> <osoite> <portti>"
    exit 1
fi

dumpfile=$1
pgpassfile=$2
db=$3
user=4
host=5
port=6

set -x

chmod u-x,go-rwx $pgpassfile
export PGPASSFILE=$pgpassfile

sudo -u postgres psql -d $db -c "drop schema public cascade; create schema public; alter user $user with superuser; grant all on schema public to public; grant all on schema public to postgres; "
pg_restore --no-acl --no-owner -U $user -h $host -d $db $dumpfile
sudo -u postgres psql -d $db -c "alter user $user with nosuperuser; "
