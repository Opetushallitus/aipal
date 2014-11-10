#!/bin/bash

# Ajaa E2E testit alustettuun tietokantaan.
#
# Tietokannan nykyinen sisältö kopioidaan ja palautetaan testien jälkeen.
#
# Käyttö:
#
#     ./run-e2e.sh [-e]
#
# Parametrit:
#     -e              Jos annettu, käyttää edellisen testiajon alustamaa tietokantaa.

set -eu

use_existing_e2e_database=no
while getopts 'e' o; do
    case $o in
        e)
            use_existing_e2e_database=yes
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
#export REMOTE_URL="http://127.0.0.1:5555/wd/hub" # for IE testing

cd $repo_path
dev-scripts/copy-db.sh aipal aipal_e2e_backup
if [ "$use_existing_e2e_database" = "no" ]; then
    dev-scripts/create-db-schema.sh
else
    dev-scripts/copy-db.sh aipal_e2e aipal
fi

cd $repo_path/e2e
lein test

cd $repo_path
dev-scripts/copy-db.sh aipal aipal_e2e
dev-scripts/copy-db.sh aipal_e2e_backup aipal
