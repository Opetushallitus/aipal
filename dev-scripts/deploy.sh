#!/bin/bash
set -eu

repo_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

set -x

cd $repo_path/aipal
./build.sh

cd $repo_path/aipal-db
./build.sh

cd $repo_path/ansible
chmod 600 yhteiset/dev_id_rsa
ssh-add yhteiset/dev_id_rsa
ansible-playbook -v -i aipal_vagrant/hosts yhteiset/julkaise_paikallinen_versio.yml -e "sovellus_jar=\"$repo_path/aipal/target/aipal-standalone.jar\" migraatio_jar=\"$repo_path/aipal-db/target/aipal-db-standalone.jar\""
