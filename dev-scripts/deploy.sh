#!/bin/bash
set -eu

if ! [[ $(ansible --version 2> /dev/null) == 'ansible 1.6' ]]
then
  echo 'Asenna Ansible 1.6: http://docs.ansible.com/intro_installation.html'
  exit 1
fi

repo_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

set -x

cd $repo_path/aipal
./build.sh

cd $repo_path/aipal-db
./build.sh

cd $repo_path/ansible
ansible-playbook -v -i aipal_vagrant/hosts yhteiset/julkaise_paikallinen_versio.yml -e "sovellus_jar=\"$repo_path/aipal/target/aipal-standalone.jar\" migraatio_jar=\"$repo_path/aipal-db/target/aipal-db-standalone.jar\""
