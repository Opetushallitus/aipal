#!/bin/bash
set -eu

repo_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

set -x

cd $repo_path/aipal-db
lein run 'postgresql://aipal_adm:aipal-adm@127.0.0.1:3456/aipal' -u aipal_user --clear -t $@
