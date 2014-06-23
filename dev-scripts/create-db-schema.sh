#!/bin/bash
set -eu

if ! [[ $(ansible --version 2> /dev/null) == 'ansible 1.6.'* ]]
then
  echo 'Asenna Ansible 1.6.0+: http://docs.ansible.com/intro_installation.html'
  exit 1
fi

repo_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

cd $repo_path/ansible
chmod 600 yhteiset/dev_id_rsa
ssh-add yhteiset/dev_id_rsa
ansible-playbook -i aipal_vagrant/hosts yhteiset/konfiguroi_tietokanta.yml

set -x

cd $repo_path/aipal-db
lein run 'postgresql://aipal_adm:aipal-adm@127.0.0.1:3456/aipal' -u aipal_user --clear -t $@
