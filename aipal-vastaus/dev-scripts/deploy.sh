#!/bin/bash
set -eu

ansible_re='^ansible 1\.6\.[0-9]+$'

if ! [[ $(ansible --version 2> /dev/null) =~ $ansible_re ]]
then
  echo 'Asenna Ansible 1.6.x: http://docs.ansible.com/intro_installation.html'
  exit 1
fi

repo_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

set -x

cd $repo_path
./build.sh

cd $repo_path/ansible
chmod 600 yhteiset/dev_id_rsa
ssh-add yhteiset/dev_id_rsa
ansible-playbook -v -i aipalvastaus_vagrant/hosts yhteiset/julkaise_paikallinen_versio.yml -e "sovellus_jar=\"$repo_path/target/aipalvastaus-standalone.jar\""
