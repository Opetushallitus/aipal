#!/bin/bash
set -eu

if [ $# -lt 1 ]
then
    echo "$0 <tietokantapalvelimen-ip> [base-url] [id_rsa.pub]"
    exit 1
fi

system='aipal'

ip=`hostname -I | cut -f1 -d' '`
db_host=$1
base_url=${2:-"http://$ip/$system"}
id_rsa_pub=${3:-'ssh/ci_id_rsa.pub'}
install_dir=/data00/$system
install_jar=$system.jar
admin_user="${system}admin"

set -x

software/jre.sh
software/httpd.sh

mkdir -p $install_dir

useradd $admin_user

# Järjestelmää ajetaan tomcat-käyttäjänä
useradd -r -s /bin/false tomcat

# admin-käyttäjälle oikeudet ajaa rootina asennukseen tarvittavat komennot
# ilman salasanaa
echo "$admin_user ALL = NOPASSWD: /bin/cp * $install_dir, /bin/ln -sf * $install_jar, /bin/chown tomcat\:tomcat -R $install_dir, /sbin/service $system *" >> /etc/sudoers

#init.d-skripti
cp app-server/$system-init.d.sh /etc/init.d/$system
chmod 755 /etc/init.d/$system

# Palvelimen asetukset
cp app-server/$system.properties $install_dir
mkdir "$install_dir/resources"
chmod a+rx "$install_dir/resources"
cp app-server/logback.xml "$install_dir/resources"
mkdir "$install_dir/logs"
chmod a+rwx "$install_dir/logs"
sed -i -e "s|\\\$DB_HOST|$db_host|g" $install_dir/$system.properties
sed -i -e "s|\\\$BASE_URL|$base_url|g" $install_dir/$system.properties

# Migraatioiden asetukset
cp app-server/$system-db.properties $install_dir
sed -i -e "s|\\\$DB_HOST|$db_host|g" $install_dir/$system-db.properties

# Sallitaan asennusten pääsy ssh:lla
mkdir /home/$admin_user/.ssh
cat $id_rsa_pub >> /home/$admin_user/.ssh/authorized_keys

chown -R $admin_user:$admin_user /home/$admin_user/.ssh
chmod 700 /home/$admin_user/.ssh
chmod 644 /home/$admin_user/.ssh/authorized_keys

# Vagrant-isäntäkone
iptables -I INPUT 1 -p tcp -s 192.168.50.1 --dport 80 -j ACCEPT

service iptables save
