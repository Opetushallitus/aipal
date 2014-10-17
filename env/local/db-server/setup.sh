#!/bin/bash
set -eu

# poistetaan esteet hostin ja guestin valilta
iptables -F
service iptables save

# postgres käyttäjien perussetup
script_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
sudo -u postgres psql < $script_path/dev.sql
