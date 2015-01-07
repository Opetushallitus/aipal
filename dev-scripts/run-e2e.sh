#!/bin/bash

# Ajaa E2E testit alustettuun tietokantaan.
#
# Tietokannan nykyinen sisältö kopioidaan ja palautetaan testien jälkeen.
#
# Käyttö:
#
#     ./run-e2e.sh [-ei]
#
# Parametrit:
#     -e              Jos annettu, käyttää edellisen testiajon alustamaa tietokantaa.
#     -i              Ajaa testit IE:tä vasten (vaatii selenium serverin asentamisen windows-koneelle).

set -u

if ! echo -n |nc 192.168.50.1 8083; then
    echo Käynnistä vastaus-sovellus!
    exit 1
fi

use_existing_e2e_database=no
use_remote_ie=no
while getopts 'ei' o; do
    case $o in
        e)
            use_existing_e2e_database=yes
            ;;
        i)  use_remote_ie=yes
            ;;
    esac
done

repo_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

#export AIPAL_URL="http://192.168.50.62/aipal" # aipal in local vagrant
export AIPAL_URL="http://192.168.50.1:8082"
export AIPAL_DB_HOST=192.168.50.61
export AIPAL_DB_PORT=5432
export AIPAL_DB_USER=aipal_user
export AIPAL_DB_PASSWORD=aipal

if [ "$use_remote_ie" = "yes" ]; then
    export REMOTE_URL="http://127.0.0.1:5555/wd/hub" # for IE testing
    lein_options=":ie"
else
    lein_options="$LEIN_OPTIONS"
fi

cd $repo_path
dev-scripts/copy-db.sh aipal aipal_e2e_backup
if [ "$use_existing_e2e_database" = "no" ]; then
    dev-scripts/create-db-schema.sh
else
    dev-scripts/copy-db.sh aipal_e2e aipal
fi

cd $repo_path/e2e
lein test $lein_options

cd $repo_path
dev-scripts/copy-db.sh aipal aipal_e2e
dev-scripts/copy-db.sh aipal_e2e_backup aipal
