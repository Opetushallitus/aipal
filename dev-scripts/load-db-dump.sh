#!/bin/bash
set -eu

REPO_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

set -x

cd $REPO_PATH/vagrant
vagrant ssh aipal-db -c 'cd /env && ./pgload.sh /dumps/aipal-dump.db dev-db.pgpass aipal aipal_adm 127.0.0.1 5432'

cd $REPO_PATH/aipal-db
lein run 'postgresql://aipal_adm:aipal-adm@127.0.0.1:3456/aipal' -u aipal_user -t
