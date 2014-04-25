#!/bin/bash
set -eu

repo_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

set -x

cd $repo_path/vagrant
vagrant ssh aipal-db -c 'cd /env && ./pgdump.sh /dumps/aipal-dump.db dev-db.pgpass aipal aipal_adm 127.0.0.1 5432'
