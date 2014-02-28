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
user=$4
host=$5
port=$6

set -x

chmod u-x,go-rwx $pgpassfile
export PGPASSFILE=$pgpassfile

pg_dump -Fc -U $user -h $host -p $port $db > $dumpfile
