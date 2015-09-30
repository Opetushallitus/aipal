#!/bin/bash
set -eu

if [ $# -ne 2 ]
then
    echo "$0 <ympäristön-asetusten-polku> <deploy_id_rsa.pub>"
    exit 1
fi

system='aipal'

env_dir=$1
id_rsa_pub=$2
admin_user="${system}admin"

set -x

sed -ri 's/(debuglevel)=[0-9]*/\1=10/' /etc/yum.conf
export URLGRABBER_DEBUG=1

software/jre.sh
software/httpd.sh

useradd $admin_user

# admin-käyttäjälle oikeudet ajaa rootina asennukseen tarvittavat komennot
# ilman salasanaa
echo "$admin_user ALL = NOPASSWD: ALL" >> /etc/sudoers

# Sallitaan asennusten pääsy ssh:lla
mkdir /home/$admin_user/.ssh
cat $id_rsa_pub >> /home/$admin_user/.ssh/authorized_keys

chown -R $admin_user:$admin_user /home/$admin_user/.ssh
chmod 700 /home/$admin_user/.ssh
chmod 644 /home/$admin_user/.ssh/authorized_keys

$env_dir/app-server/setup.sh
