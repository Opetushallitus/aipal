#!/bin/bash

# Julkaisee annetun AIPAL-version etäpalvelimelle.
#
# Käyttö:
#
#     ./deploy.sh [-c] [-t] <aipal.jar> <aipal-db.jar> <user@host>
#
# Parametrit:
#     -c              Jos annettu, tyhjentää tietokannan.
#
#     -t              Jos annettu, luo testikäyttäjät T-1001 ja T-800.
#
#     <aipal.jar>     Polku aipal.jar:iin.
#
#     <aipal-db.jar>  Polku aipal-db.jar:iin.
#
#     <user@host>     Käyttäjätunnus palvelimella ja palvelimen nimi.
#                     Käyttäjällä tulee olla sudo-oikeudet.

set -eu

db_extra_args=''
while getopts 'ct' o; do
    case $o in
        c)
            db_extra_args+=' --clear'
            ;;
        t)
            db_extra_args+=' -t'
            ;;
    esac
    shift
    ((OPTIND-=1))
done

if [ $# -lt 3 ]
then
    echo "$0 [-t] <aipal.jar> <aipal-db.jar> <user@host>"
    exit 1
fi

db_user=${AIPAL_DB_USER:-aipal}
service=${AIPAL_SERVICE:-aipal}

version_jarfile=$1
version_dbjarfile=$2
user_host=$3
app_home=${AIPAL_HOME:-/data00/aipal}
ssh_key=${AIPAL_SSH_KEY:-~/.ssh/id_rsa}

set -x

# Ei tarkisteta isäntäavaimia, koska testiajoihin käytettävien
# virtuaalipalvelinten IP:t vaihtuvat, kun ne tuhotaan ja luodaan uudelleen
echo "kopioidaan uusi versio etäpalvelimelle $user_host"
scp -i $ssh_key -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -p start-aipal.sh stop-aipal.sh $version_jarfile $version_dbjarfile $user_host:~

echo "päivitetään tietokanta ja sovellus"

ssh -t -t -i $ssh_key -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $user_host "sudo cp start-aipal.sh stop-aipal.sh `basename $version_dbjarfile` `basename $version_jarfile` $app_home && cd $app_home && sudo chown tomcat:tomcat -R $app_home && sudo /sbin/service $service stop && java -jar `basename $version_dbjarfile` -u $db_user $db_extra_args && sudo ln -sf `basename $version_jarfile` aipal.jar; sudo /sbin/service $service start && sleep 2"
