#!/bin/bash
set -eu

# poistetaan esteet hostin ja guestin valilta
iptables -F
service iptables save
