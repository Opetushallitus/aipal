#!/bin/bash
set -eu

if [ $# -ne 2 ]
then
    echo "$0 <ympäristön-asetusten-polku> <sovelluspalvelimen-ip>"
    exit 1
fi

env_dir=$1
app_host=$2

software/postgresql.sh

# Sovelluspalvelin
iptables -I INPUT 1 -p tcp -s $app_host --dport 5432 -j ACCEPT

service iptables save

# alustetaan aipal tietokanta ilman tauluja
su postgres -c "psql --file=$env_dir/db-server/dev.sql"

# ympäristökohtaisesta hakemistosta ajetaan alustusskripti
$env_dir/db-server/setup.sh
