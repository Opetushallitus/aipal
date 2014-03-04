#!/bin/bash
set -eu

REPO_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

set -x

cd $REPO_PATH/vagrant
vagrant destroy -f aipal-app
vagrant up aipal-app

$REPO_PATH/dev-scripts/deploy.sh
