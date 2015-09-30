#!/bin/bash
set -eu

if [ $# -ne 3 ]
then
    echo "$0 <ympäristön-asetusten-polku> <sovelluspalvelimen-ip> <id_rsa.pub>"
    exit 1
fi

system='aipal'

env_dir=$1
app_host=$2
id_rsa_pub=$3
admin_user="${system}admin"

sed -ri 's/(debuglevel)=[0-9]*/\1=10/' /etc/yum.conf
export URLGRABBER_DEBUG=1

software/postgresql.sh

useradd $admin_user

# admin-käyttäjälle oikeudet ajaa rootina asennukseen tarvittavat komennot
# ilman salasanaa
echo "$admin_user ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

# Sallitaan asennusten pääsy ssh:lla
mkdir /home/$admin_user/.ssh
cat $id_rsa_pub >> /home/$admin_user/.ssh/authorized_keys

chown -R $admin_user:$admin_user /home/$admin_user/.ssh
chmod 700 /home/$admin_user/.ssh
chmod 644 /home/$admin_user/.ssh/authorized_keys

# Sovelluspalvelin
iptables -I INPUT 1 -p tcp -s $app_host --dport 5432 -j ACCEPT

service iptables save

# alustetaan aipal tietokanta ilman tauluja

until service postgresql-9.4 status > /dev/null; do
  echo "Waiting for postgresql...\n"
  sleep 1
done

# ympäristökohtaisesta hakemistosta ajetaan alustusskripti
$env_dir/db-server/setup.sh
