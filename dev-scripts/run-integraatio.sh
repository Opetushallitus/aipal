#!/bin/bash

# Ajaa integraatiotestit alustettuun tietokantaan.
#
# Tietokannan nykyinen sisältö kopioidaan ja palautetaan testien jälkeen.
#
# Käyttö:
#
#     ./run-integraatio.sh [-e]
#
# Parametrit:
#     -e              Jos annettu, käyttää edellisen testiajon alustamaa tietokantaa.

set -u

use_existing_integraatio_database=no
while getopts 'e' o; do
    case $o in
        e)
            use_existing_integraatio_database=yes
            ;;
    esac
done

repo_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

lein_options="${LEIN_OPTIONS:-}"

cd $repo_path
dev-scripts/copy-db.sh aipal aipal_integraatio_backup
if [ "$use_existing_integraatio_database" = "no" ]; then
    dev-scripts/create-db-schema.sh
else
    dev-scripts/copy-db.sh aipal_integraatio aipal
fi

cd $repo_path/aipal
lein test :integraatio $lein_options

cd $repo_path
dev-scripts/copy-db.sh aipal aipal_integraatio
dev-scripts/copy-db.sh aipal_integraatio_backup aipal
