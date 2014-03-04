#!/bin/bash
set -eu

REPO_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

set -x

cd $REPO_PATH/vagrant
vagrant destroy -f aipal-db
vagrant up aipal-db

$REPO_PATH/dev-scripts/create-db-schema.sh
